package org.tudelft.plugins

import javassist.bytecode.stackmap.TypeTag
import org.apache.flink.api.common.accumulators.LongCounter
import org.apache.flink.api.common.state.ListState
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.source.RichSourceFunction
import org.tudelft.plugins.clearlydefined.protocol.Protocol.ClearlyDefinedRelease

abstract class PluginSourceConfig{
  def pollingInterval: Int
  def maxNumberOfRuns: Int
}

abstract class PluginReleasesSource[T <: AnyRef : TypeTag](config: PluginSourceConfig)
  extends RichSourceFunction[T] {

  // Some track variables of this source
  protected var isRunning: Boolean = false
  protected var runsLeft: Int = 0

  /** Accumulator for the amount of processed releases. */
  protected val releasesProcessed = new LongCounter()

  def getIsRunning : Boolean = isRunning

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
   * Wait a certain amount of times the polling interval
   *
   * @param times Times the polling interval should be waited
   */
  def waitPollingInterval(times: Int, config: PluginSourceConfig): Unit = {
    Thread.sleep(times * config.pollingInterval)
  }

  /**
   * Reduces runsLeft by 1
   */
  def decreaseRunsLeft(): Unit = {
    if (runsLeft > 0) {
      runsLeft -= 1
    }
  }

}
