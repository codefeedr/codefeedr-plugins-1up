package org.tudelft.plugins.npm.operators

import java.text.SimpleDateFormat
import java.util.Date

import org.apache.flink.api.common.accumulators.LongCounter
import org.apache.flink.api.common.state.{ListState, ListStateDescriptor}
import org.apache.flink.configuration.Configuration
import org.apache.flink.runtime.state.{FunctionInitializationContext, FunctionSnapshotContext}
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction
import org.apache.flink.streaming.api.functions.source.{RichSourceFunction, SourceFunction}
import org.codefeedr.stages.utilities.{HttpRequester, RequestException}
import org.tudelft.plugins.npm.protocol.Protocol.NpmRelease
import scalaj.http.Http

import scala.collection.JavaConverters._

case class NpmSourceConfig(pollingInterval: Int = 1000,
                             maxNumberOfRuns: Int = -1)


class NpmReleasesSource(config: NpmSourceConfig = NpmSourceConfig())
  extends RichSourceFunction[NpmRelease]
    with CheckpointedFunction {

  // URL to get update stream from
  val url = "https://npm-update-stream.libraries.io/"

  /* Some track variables of this source. */
  private var isRunning = false
  private var runsLeft = 0
  private var lastItem: Option[NpmRelease] = None
  @transient
  private var checkpointedState: ListState[NpmRelease] = _

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
  override def run(ctx: SourceFunction.SourceContext[NpmRelease]): Unit = {
    val lock = ctx.getCheckpointLock

    /* While is running or #runs left. */
    while (isRunning && runsLeft != 0) {
      lock.synchronized { // Synchronize to the checkpoint lock.
        try {
          // Polls the RSS feed
          val rssAsString = getRSSAsString
          // Parses the received rss items
          val items: Seq[NpmRelease] = stringToNpmReleases(rssAsString)

          // Decrease the amount of runs left.
          decreaseRunsLeft()

          // Collect right items and update last item
          val validSortedItems = sortAndDropDuplicates(items)
          validSortedItems.foreach(x =>
            ctx.collectWithTimestamp(x, x.retrieveDate.getTime))
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
    * Turns a string into npm releases
    * @param input the input string containing the names of the releases
    * @return a sequence of npm releases
    */
  def stringToNpmReleases(input: String) : Seq[NpmRelease] = {
    val splitted = input.replace("\"", "").replace("[", "").replace("]", "").split(",")
    // for every npm package, there is no event time, so use ingestion time as event time for now,
    // to at least have a timestamp to process
    val res = splitted.map(x => NpmRelease(x, new Date(System.currentTimeMillis())))
    res
  }

  def decreaseRunsLeft(): Unit = {
    if (runsLeft > 0) {
      runsLeft -= 1
    }
  }

  /**
    * Drops items that already have been collected and sorts them based on times
    * @param items Potential items to be collected
    * @return Valid sorted items
    */
  def sortAndDropDuplicates(items: Seq[NpmRelease]): Seq[NpmRelease] = {
    items
      .filter((x: NpmRelease) => {
        if (lastItem.isDefined)
          lastItem.get.retrieveDate.before(x.retrieveDate) && lastItem.get.name != x.name
        else
          true
      })
      .sortWith((x: NpmRelease, y: NpmRelease) => x.retrieveDate.before(y.retrieveDate))
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
      new ListStateDescriptor[NpmRelease]("last_element", classOf[NpmRelease])

    checkpointedState = context.getOperatorStateStore.getListState(descriptor)

    if (context.isRestored) {
      checkpointedState.get().asScala.foreach { x =>
        lastItem = Some(x)
      }
    }
  }

}