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
import org.tudelft.plugins.cargo.operators.CargoReleasesSource
import org.tudelft.plugins.cargo.protocol.Protocol.CrateRelease
import org.tudelft.plugins.cargo.stages.CargoReleasesStage
import org.tudelft.plugins.cargo.util.CargoSQLService
import org.tudelft.plugins.maven.stages.{MavenReleasesExtStage, MavenReleasesStage}

object Main {
  def main(args: Array[String]): Unit = {

    val mavenReleaseSource = new MavenReleasesStage()
    val mavenEnrichReleases = new MavenReleasesExtStage()
    val mavenSqlStage = SQLStage.createSQLStage[MavenReleaseExt]("SELECT * FROM MavenProjectDependencies")

    val cargoSource = new CargoReleasesStage()
    val cargoSqlStage = SQLStage.createSQLStage[CrateRelease]("SELECT * FROM CargoCrateCategories")

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
      .edge(cargoSource, cargoSqlStage)
      //      .edge(releaseSource, sqlStage)
      .build()
      .startMock()
  }
}

