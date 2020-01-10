package org.tudelft.plugins.clearlydefined.operators

import java.text.SimpleDateFormat
import org.apache.flink.api.common.accumulators.LongCounter
import org.apache.flink.api.common.state.{ListState, ListStateDescriptor}
import org.apache.flink.configuration.Configuration
import org.apache.flink.runtime.state.{FunctionInitializationContext, FunctionSnapshotContext}
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction
import org.apache.flink.streaming.api.functions.source.{RichSourceFunction, SourceFunction}
import org.codefeedr.stages.utilities.{HttpRequester, RequestException}
import org.tudelft.plugins.clearlydefined.protocol.Protocol.ClearlyDefinedRelease
import scalaj.http.Http
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization.read
import scala.collection.JavaConverters._

/**
 * The configuration class for the ClearlyDefinedReleasesSource class
 * @param pollingInterval Amount of milliseconds to wait for next poll. Put to 30 seconds due to typically slow feed
 * @param maxNumberOfRuns if positive, runs definitely up till x. If negative, runs indefinitely.
 */
case class ClearlyDefinedSourceConfig(pollingInterval: Int = 30000,
                                      maxNumberOfRuns: Int = -1)

/**
 * Important to note in retrieving data from the stream of projects in ClearlyDefined is the following:
 *  - The stream URL is https://api.clearlydefined.io/definitions?matchCasing=false&sort=releaseDate&sortDesc=true
 *  - 100 most recently changed packages can be found there, which is way too much to process with each poll
 *  - Therefore only the first {packageAmount} number of packages are processed
 * @param config the ClearlyDefined source configuration, has pollingInterval and maxNumberOfRuns fields
 */
class ClearlyDefinedReleasesSource(config: ClearlyDefinedSourceConfig = ClearlyDefinedSourceConfig())
  extends RichSourceFunction[ClearlyDefinedRelease]
    with CheckpointedFunction {

  /** url for the stream of new CD projects */
  val url = "https://api.clearlydefined.io/definitions?matchCasing=false&sort=releaseDate&sortDesc=true"

  /** The first x number of packages to process with each poll */
  val packageAmount = 10

  val dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS'Z'")

  /** Accumulator for the amount of processed releases. */
  val releasesProcessed = new LongCounter()

  // Some track variables of this source
  private var isRunning = false
  private var runsLeft = 0
  private var lastItem: Option[ClearlyDefinedRelease] = None
  @transient
  private var checkpointedState: ListState[ClearlyDefinedRelease] = _

  def getCheckpointedstate: ListState[ClearlyDefinedRelease] = checkpointedState
  def getIsRunning        : Boolean                          = isRunning

  /** Opens this source. */
  override def open(parameters: Configuration): Unit = {
    isRunning = true
    runsLeft = config.maxNumberOfRuns
  }

  /** Closes this source. */
  override def cancel(): Unit = {
    isRunning = false
  }

  /**
   * Main fetcher of new items in the ClearlyDefined package source
   * @param ctx
   */
  override def run(ctx: SourceFunction.SourceContext[ClearlyDefinedRelease]): Unit = {
    val lock = ctx.getCheckpointLock

    /** While is running or #runs left. */
    while (isRunning && runsLeft != 0) {
      lock.synchronized { // Synchronize to the checkpoint lock.
        try {
          // Polls the RSS feed
          val rssAsString = getRSSAsString.get
          // Parses the received rss items
          val items: Seq[ClearlyDefinedRelease] = parseRSSString(rssAsString)

          // Decrease the amount of runs left.
          decreaseRunsLeft()

          // Collect right items and update last item
          val validSortedItems = sortAndDropDuplicates(items)
          validSortedItems.foreach(x =>
            ctx.collectWithTimestamp(x, dateFormat.parse(x._meta.updated).getTime))
          releasesProcessed.add(validSortedItems.size)
          if (validSortedItems.nonEmpty) {
            lastItem = Some(validSortedItems.last)
          }

          // Wait until the next poll
          waitPollingInterval()
        } catch {
          case _: Throwable =>
        }
      }
    }
  }

  /**
   * Reduces runsLeft by 1
   */
  def decreaseRunsLeft(): Unit = {
    if (runsLeft > 0) {
      runsLeft -= 1
    }
  }

  /**
   * Drops items that already have been collected and sorts them based on times
   * TODO: x._meta.updated is not chronological ~5% of the time, which means 1 in 20 packages are SKIPPED
   *
   * @param items Potential items to be collected
   * @return Valid sorted items
   */
  def sortAndDropDuplicates(items: Seq[ClearlyDefinedRelease]): Seq[ClearlyDefinedRelease] = {
    items
      .filter((x: ClearlyDefinedRelease) => {
        if (lastItem.isDefined)
          dateFormat.parse(lastItem.get._meta.updated).before(dateFormat.parse(x._meta.updated))
        else
          true
      })
      .sortWith((x: ClearlyDefinedRelease, y: ClearlyDefinedRelease) => dateFormat.parse(x._meta.updated).before(dateFormat.parse(y._meta.updated)))
  }

  /**
   * Wait a certain amount of times the polling interval
   *
   * @param times Times the polling interval should be waited
   */
  def waitPollingInterval(times: Int = 1): Unit = {
    Thread.sleep(times * config.pollingInterval)
  }

  /**
   * Requests the RSS feed and returns its body as a string.
   * Will keep trying with increasing intervals if it doesn't succeed
   *
   * @return Body of requested RSS feed
   */
  @throws[RequestException]
  def getRSSAsString: Option[String] = {
    try {
      Some(new HttpRequester().retrieveResponse(Http(url)).body)
    }
    catch {
      case _: Throwable => None
    }
  }

  /**
   * Parses a string that contains JSON with RSS items into a list of ClearlyDefinedRelease's
   * @param rssString
   * @return
   */
  def parseRSSString(rssString: String): Seq[ClearlyDefinedRelease] = {
    try {
      // Parse the big release string as a Json object
      val json: JValue = parse(rssString)

      // Retrieve the first {packageAmount} packages
      val packages: List[JValue] = (json\"data").children.take(this.packageAmount)

      // Render back to string to prepare for the 'read' call (requires string input)
      val packagesString: List[String] = packages.map(x => compact(render(x)))

      // Convert from string to ClearlyDefinedRelease
      implicit val formats = DefaultFormats
      for (packageString <- packagesString) yield {
        read[ClearlyDefinedRelease](packageString)
      }
    }
    catch {
      // If the string cannot be parsed return an empty list
      case _: Throwable =>
        printf("Failed parsing the RSSString in the ClearlyDefinedReleasesSource.scala file")
        Nil
    }
  }

  override def snapshotState(context: FunctionSnapshotContext): Unit = {
    if (lastItem.isDefined) {
      checkpointedState.clear()
      checkpointedState.add(lastItem.get)
    }
  }

  override def initializeState(context: FunctionInitializationContext): Unit = {
    val descriptor = new ListStateDescriptor[ClearlyDefinedRelease]("last_element", classOf[ClearlyDefinedRelease])

    checkpointedState = context.getOperatorStateStore.getListState(descriptor)

    if(context.isRestored) {
      checkpointedState.get().asScala.foreach { x => lastItem = Some(x)}
    }
  }
}