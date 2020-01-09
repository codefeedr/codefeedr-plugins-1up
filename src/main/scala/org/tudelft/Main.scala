package org.tudelft

import java.util.concurrent.TimeUnit

import org.apache.flink.api.common.restartstrategy.RestartStrategies
import org.apache.flink.streaming.api.scala._
import org.codefeedr.buffer.KafkaBuffer
import org.codefeedr.pipeline.PipelineBuilder
import org.apache.flink.api.common.time.Time
import org.codefeedr.stages.OutputStage
import org.tudelft.plugins.cargo.protocol.Protocol.CrateRelease
import org.tudelft.plugins.cargo.stages.CargoReleasesStage
import org.tudelft.plugins.maven.stages.{MavenReleasesExtStage, MavenReleasesStage, SQLStage}

// Cargo
/*
object Main {
  def main(args: Array[String]): Unit = {
    new PipelineBuilder()
      .append(new CargoReleasesStage())
      .append (new CrateDownloadsOutput)
      .build()
      .startMock()
  }
}

class CrateDownloadsOutput extends OutputStage[CrateRelease] {
  override def main(source: DataStream[CrateRelease]): Unit = {
    source
      .map { item => (item.crate.name,
        " " + item.crate.downloads,
        " Nr. of versions: " + item.crate.versions.size) }
      .print()
  }
}
*/


// ClearlyDefined
/*
object Main {
  def main(args: Array[String]): Unit = {
    new PipelineBuilder()
      .append(new ClearlyDefinedReleasesStage())
      .append (new WordCountOutput)
      .build()
      .startMock()
  }
}

class WordCountOutput extends OutputStage[ClearlyDefinedRelease] {
  override def main(source: DataStream[ClearlyDefinedRelease]): Unit = {
    source
      .map { item => (item.coordinates.name, 1) }
      .keyBy(0)
      .sum(1)
      .print()
  }
}
 */

// Maven

object Main {
  def main(args: Array[String]): Unit = {

    val releaseSource = new MavenReleasesStage()
    val enrichReleases = new MavenReleasesExtStage()
    val sqlStage = new SQLStage()

    new PipelineBuilder()
      .setPipelineName("Maven plugin")
      .setRestartStrategy(RestartStrategies.fixedDelayRestart(
        3,
        Time.of(10, TimeUnit.SECONDS))) // try restarting 3 times
      .enableCheckpointing(1000) // checkpointing every 1000ms
      .setBufferProperty(KafkaBuffer.COMPRESSION_TYPE, "gzip")
      .setBufferProperty(KafkaBuffer.BROKER, "localhost:9092")
      .setBufferProperty(KafkaBuffer.ZOOKEEPER, "localhost:2181")
      .setBufferProperty("message.max.bytes", "5000000") // max message size is 5mb
      .setBufferProperty("max.request.size", "5000000") // max message size is 5 mb
      .edge(releaseSource, enrichReleases)
      .edge(enrichReleases, sqlStage)
      //      .edge(releaseSource, sqlStage)
      .build()
      .startMock()
  }
}

