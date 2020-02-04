package org.tudelft

import java.util.concurrent.TimeUnit

import org.apache.flink.api.common.restartstrategy.RestartStrategies
import org.apache.flink.api.common.time.Time
import org.codefeedr.buffer._
import org.codefeedr.pipeline.PipelineBuilder


object Main {
  def main(args: Array[String]): Unit = {

    val query: String = args.mkString(" ")

    val builder = new PipelineBuilder()
      .setPipelineName("Demo")
      .setRestartStrategy(RestartStrategies.fixedDelayRestart(
        3,
        Time.of(10, TimeUnit.SECONDS))) // try restarting 3 times
      .enableCheckpointing(1000) // checkpointing every 1000ms
      .setBufferProperty(KafkaBuffer.AMOUNT_OF_PARTITIONS, "8")
      .setBufferProperty(KafkaBuffer.AMOUNT_OF_REPLICAS, "2")
      .setBufferProperty(KafkaBuffer.COMPRESSION_TYPE, "gzip")
      .setBufferProperty(KafkaBuffer.BROKER, "localhost:9092")
      .setBufferProperty(KafkaBuffer.ZOOKEEPER, "localhost:2181")
      .setBufferProperty("message.max.bytes", "41943040") // max message size is 40mb
      .setBufferProperty("max.request.size", "41943040") // max message size is 40 mb

    val pipeline = InputParser.createCorrespondingStages(query, builder)

      pipeline.build().startMock
  }
}

