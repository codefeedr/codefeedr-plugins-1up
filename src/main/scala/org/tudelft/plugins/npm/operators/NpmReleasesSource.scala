package org.tudelft.plugins.npm.operators

import java.util.{Calendar, Date}
import org.apache.flink.api.common.accumulators.LongCounter
import org.apache.flink.api.common.state.{ListState, ListStateDescriptor}
import org.apache.flink.configuration.Configuration
import org.apache.flink.runtime.state.{FunctionInitializationContext, FunctionSnapshotContext}
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction
import org.apache.flink.streaming.api.functions.source.{RichSourceFunction, SourceFunction}
import org.codefeedr.stages.utilities.HttpRequester
import org.tudelft.plugins.npm.protocol.Protocol.NpmRelease
import scalaj.http.Http
import scala.collection.JavaConverters._

/**
 * Configuration parameters for connecting to the NPM Packages data source
 *
 * @param pollingInterval the milliseconds between consecutive polls
 * @param maxNumberOfRuns the maximum number of polls executed
 *
 * @author Roald van der Heijden
 * Date: 2019 - 12 - 03
 */
case class NpmSourceConfig(pollingInterval: Int = 1000, maxNumberOfRuns: Int = -1)

/**
 * Class to represent a source in CodeFeedr to query NPM package releases
 *
 * This files gets information on NPM packages from "https://npm-update-stream.libraries.io/"
 * Then queries "http://registry.npmjs.com/%packagenameWithPossibleScope%
 * to acquire the information for each specific package
 *
 * @param config the configuration paramaters object for this specific data source
 *
 * @author Roald van der Heijden
 * Date: 2019 - 12 - 03
 */
class NpmReleasesSource(config: NpmSourceConfig = NpmSourceConfig())
  extends RichSourceFunction[NpmRelease]
    with CheckpointedFunction {

  /**
   * URL to get update stream from
   */
  val url_updatestream = "https://npm-update-stream.libraries.io/"

  /**
   * Indication of operation status, i.e. running or not
   */
  private var isRunning = false

  /**
   * Counter to indicate the number of polls left to poll the update stream
   */
  private var runsLeft = 0

  /**
   * The last item that got processed from the update stream
   */
  private var lastItem: Option[NpmRelease] = None

  /**
   * Keeps track of a checkpointed state of NpmReleases
   */
  @transient
  private var checkpointedState: ListState[NpmRelease] = _

  /**
   * @return whether this source is running or not
   */
  def getIsRunning: Boolean = isRunning

  /**
   * Accumulator for the amount of processed releases.
   */
  val releasesProcessed = new LongCounter()

  /**
   * Opens this source.
   * @param parameters configuration data
   */
  override def open(parameters: Configuration): Unit = {
    isRunning = true
    runsLeft = config.maxNumberOfRuns
  }

  /**
   * Close the source.
   */

  override def cancel(): Unit = {
    isRunning = false
  }

  /**
   * Runs the source.
   * @param ctx the source the context.
   */
  override def run(ctx: SourceFunction.SourceContext[NpmRelease]): Unit = {
    val lock = ctx.getCheckpointLock

    /* While is running or #runs left. */
    while (isRunning && runsLeft != 0) {
      lock.synchronized { // Synchronize to the checkpoint lock.
        try {
          // Polls the update stream
          val resultString = retrieveUpdateStringFrom(url_updatestream)

          val now = Calendar.getInstance().getTime()
          // convert string with updated packages into list of updated packages
          val items: Seq[NpmRelease] = createListOfUpdatedNpmIdsFrom(resultString.get, now)

          // Decrease the amount of runs left.
          decreaseRunsLeft()

          // Collect right items and update last item
          val validSortedItems = sortAndDropDuplicates(items)
          validSortedItems.foreach(x => ctx.collectWithTimestamp(x, x.retrieveDate.getTime))
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
   * Decreases the number of times the source will be polled by 1
   */
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
          lastItem.get.retrieveDate.before(x.retrieveDate)
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

  /**
   * Make a snapshot of the current state.
   */
  override def snapshotState(context: FunctionSnapshotContext): Unit = {
    if (lastItem.isDefined) {
      checkpointedState.clear()
      checkpointedState.add(lastItem.get)
    }
  }

  /**
   * Initializes state by reading from a checkpoint or creating an empty one.
   */
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

  /**
   * Requests the update stream and returns its body as a string.
   * Will keep trying with increasing intervals if it doesn't succeed
   *
   * @return Body of requested update stream
   */
  def retrieveUpdateStringFrom(urlEndpoint : String) : Option[String] = {
    val response = try {
      val request = Http(urlEndpoint)
      return Some(new HttpRequester().retrieveResponse(request).body)
    } catch {
      case _ : Throwable => None
    }
    response
  }

  /**
   * Turns a given string (following request response from Librarios.io's update stream) into npm ids
   * @param input the string containing the names of the releases
   * @return a sequence of NPMRelease case classes
   */
  def createListOfUpdatedNpmIdsFrom(input: String, time : Date) : List[NpmRelease] = {
    val packageList = input
      .replace("\"", "")
      .replace("[", "")
      .replace("]", "")
      .split(",")
      .toList
    packageList match {
      case List("") => Nil
      case _ =>     packageList.map(arg => NpmRelease(arg, time))
    }
  }

}

/* NOTES

Todo Check if the next really is a bug, if so CodeFeedr-Team needs to update their plugins as well!

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
 */