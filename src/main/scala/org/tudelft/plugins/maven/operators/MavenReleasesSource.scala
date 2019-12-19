package org.tudelft.plugins.maven.operators

import java.text.SimpleDateFormat

import org.apache.flink.api.common.accumulators.LongCounter
import org.apache.flink.api.common.state.{ListState, ListStateDescriptor}
import org.apache.flink.configuration.Configuration
import org.apache.flink.runtime.state.{FunctionInitializationContext, FunctionSnapshotContext}
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction
import org.apache.flink.streaming.api.functions.source.{RichSourceFunction, SourceFunction}
import org.codefeedr.stages.utilities.{HttpRequester, RequestException}
import org.tudelft.plugins.maven.protocol.Protocol.{Guid, MavenRelease}
import scalaj.http.Http

import scala.collection.JavaConverters._
import scala.xml.XML

case class MavenSourceConfig(pollingInterval: Int = 1000,
                             maxNumberOfRuns: Int = -1)


class MavenReleasesSource(config: MavenSourceConfig = MavenSourceConfig())
  extends RichSourceFunction[MavenRelease]
    with CheckpointedFunction {
  // Date formats + URL
  val pubDateFormat = "EEE, dd MMM yyyy HH:mm:ss ZZ"
  val url = "https://mvnrepository.com/feeds/rss2.0.xml"

  /** Some track variables of this source. */
  private var isRunning = false
  private var runsLeft = 0
  private var lastItem: Option[MavenRelease] = None
  @transient
  private var checkpointedState: ListState[MavenRelease] = _

  def getIsRunning: Boolean = isRunning

  /** Accumulator for the amount of processed releases. */
  val releasesProcessed = new LongCounter()

  /** Opens this source. */
  override def open(parameters: Configuration): Unit = {
    isRunning = true
    runsLeft = config.maxNumberOfRuns
  }

  /** Close the source. */
  override def cancel(): Unit = {
    isRunning = false
  }

  /** Runs the source.
    *
    * @param ctx the source the context.
    */
  override def run(ctx: SourceFunction.SourceContext[MavenRelease]): Unit = {
    val lock = ctx.getCheckpointLock

    /** While is running or #runs left. */
    while (isRunning && runsLeft != 0) {
      lock.synchronized { // Synchronize to the checkpoint lock.
        try {
          // Polls the RSS feed
          val rssAsString = getRSSAsString
          // Parses the received rss items
          val items: Seq[MavenRelease] = parseRSSString(rssAsString)

          // Decrease the amount of runs left.
          decreaseRunsLeft()

          // Collect right items and update last item
          val validSortedItems = sortAndDropDuplicates(items)
          validSortedItems.foreach(x =>
            ctx.collectWithTimestamp(x, x.pubDate.getTime))
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
    * Requests the RSS feed and returns its body as a string.
    * Will keep trying with increasing intervals if it doesn't succeed
    *
    * @return Body of requested RSS feed
    */
  @throws[RequestException]
  def getRSSAsString: String = {
    new HttpRequester().retrieveResponse(Http(url)).body
  }

  /**
    * Parses a string that contains xml with RSS items
    *
    * @param rssString XML string with RSS items
    * @return Sequence of RSS items
    */
  def parseRSSString(rssString: String): Seq[MavenRelease] = {
    try {
      val xml = XML.loadString(rssString)
      val nodes = xml \\ "item"
      for (t <- nodes) yield xmlToMavenRelease(t)
    } catch {
      // If the string cannot be parsed return an empty list
      case _: Throwable => Nil
    }
  }

  /**
    * Parses a xml node to a RSS item
    *
    * @param node XML node
    * @return RSS item
    */
  def xmlToMavenRelease(node: scala.xml.Node): MavenRelease = {
    val title = (node \ "title").text
    val link = (node \ "link").text
    val description = (node \ "description").text
    val formatterPub = new SimpleDateFormat(pubDateFormat)
    val pubDate = formatterPub.parse((node \ "pubDate").text)

    val tag = (node \ "guid").text
    MavenRelease(title, description, link, pubDate, Guid(tag))
  }

  def decreaseRunsLeft(): Unit = {
    if (runsLeft > 0) {
      runsLeft -= 1
    }
  }

  /**
    * Drops items that already have been collected and sorts them based on times
    *
    * @param items Potential items to be collected
    * @return Valid sorted items
    */
  def sortAndDropDuplicates(items: Seq[MavenRelease]): Seq[MavenRelease] = {
    items
      .filter((x: MavenRelease) => {
        if (lastItem.isDefined)
          lastItem.get.pubDate.before(x.pubDate) && lastItem.get.link != x.link
        else
          true
      })
      .sortWith((x: MavenRelease, y: MavenRelease) => x.pubDate.before(y.pubDate))
  }

  /**
    * Wait a certain amount of times the polling interval
    *
    * @param times Times the polling interval should be waited
    */
  def waitPollingInterval(times: Int = 1): Unit = {
    Thread.sleep(times * config.pollingInterval)
  }

  /** Make a snapshot of the current state. */
  override def snapshotState(context: FunctionSnapshotContext): Unit = {
    if (lastItem.isDefined) {
      checkpointedState.clear()
      checkpointedState.add(lastItem.get)
    }
  }

  /** Initializes state by reading from a checkpoint or creating an empty one. */
  override def initializeState(context: FunctionInitializationContext): Unit = {
    val descriptor =
      new ListStateDescriptor[MavenRelease]("last_element", classOf[MavenRelease])

    checkpointedState = context.getOperatorStateStore.getListState(descriptor)

    if (context.isRestored) {
      checkpointedState.get().asScala.foreach { x =>
        lastItem = Some(x)
      }
    }
  }

}
