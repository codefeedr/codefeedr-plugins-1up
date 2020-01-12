package org.tudelft

import java.lang.reflect.Constructor
import java.util.Date
import java.util.concurrent.TimeUnit

import org.apache.flink.api.common.restartstrategy.RestartStrategies
import org.apache.flink.api.common.time.Time
import org.apache.flink.streaming.api.scala._
import org.codefeedr.buffer.KafkaBuffer
import org.codefeedr.pipeline.PipelineBuilder
import org.codefeedr.stages.OutputStage
import org.tudelft.plugins.{SQLService, SQLStage}
import org.tudelft.plugins.json.{JsonExitStage, JsonTransformStage, StringWrapper}
import org.tudelft.plugins.maven.protocol.Protocol.{Guid, MavenProject, MavenRelease, MavenReleaseExt}
import org.tudelft.plugins.SQLStage.SQLStage
import org.tudelft.plugins.maven.stages.{MavenReleasesExtStage, MavenReleasesStage}

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
      .append(new MavenReleasesStage())
      .append(new MavenReleasesExtStage())
//      .append(SQLStage.createSQLStage[MavenReleaseExt]("Select * from Maven"))
      .append(new JsonExitStage[MavenReleaseExt]())
//      .append (new CrateDownloadsOutput)
      .build()
      .startMock()
  }
}

class CrateDownloadsOutput extends OutputStage[StringWrapper] {
  override def main(source: DataStream[StringWrapper]): Unit = {
    source.map(x => println(x.s))
  }
}
*/

// Maven
object Main {
  def main(args: Array[String]): Unit = {

    val releaseSource = new MavenReleasesStage()
    val jsonStage = new JsonExitStage[MavenRelease]
    val enrichReleases = new MavenReleasesExtStage()
    val sqlStage = SQLStage.createSQLStage[MavenReleaseExt]("SELECT * FROM MavenProjectDependencies")

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
      .append(releaseSource)
      .append(jsonStage)
//      .edge(releaseSource, enrichReleases)
//      .edge(enrichReleases, sqlStage)
      //      .edge(releaseSource, sqlStage)
      .build()
      .startMock()
  }
}

