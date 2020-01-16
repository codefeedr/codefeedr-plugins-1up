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
case class NpmSourceConfig(pollingInterval: Int = 10000, maxNumberOfRuns: Int = -1) // 10 sec polling interval

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
    * The latest poll
    */
  private var lastPoll: Option[Seq[NpmRelease]] = None

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

          var newItems: Seq[NpmRelease] = null
          // Collect right items and update last item
          if (lastPoll.isDefined){
            newItems = items.map(x => x.name).diff(lastPoll.get.map(x => x.name)).map(x => NpmRelease(x, now))
          }
          else newItems = items

          newItems.foreach(x => ctx.collectWithTimestamp(x, x.retrieveDate.getTime))
          releasesProcessed.add(newItems.size)

          if (items.nonEmpty) lastPoll = Some(items)

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
    if (lastPoll.isDefined) {
      checkpointedState.clear()
      checkpointedState.add(lastPoll.get.head)
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
      lastPoll = Some(checkpointedState.get().asScala.to[collection.immutable.Seq])
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