package org.tudelft

import java.util.concurrent.TimeUnit

import org.apache.flink.api.common.restartstrategy.RestartStrategies
import org.apache.flink.api.common.time.Time
import org.codefeedr.buffer._
import org.codefeedr.pipeline.PipelineBuilder
import org.tudelft.plugins.SQLStage
import org.tudelft.plugins.cargo.protocol.Protocol.CrateRelease
import org.tudelft.plugins.cargo.stages.CargoReleasesStage
import org.tudelft.plugins.clearlydefined.protocol.Protocol.ClearlyDefinedRelease
import org.tudelft.plugins.clearlydefined.stages.ClearlyDefinedReleasesStage
import org.tudelft.plugins.json.JsonExitStage
import org.tudelft.plugins.maven.protocol.Protocol.{MavenRelease, MavenReleaseExt}
import org.tudelft.plugins.maven.stages.{MavenReleasesExtStage, MavenReleasesStage}
import org.tudelft.plugins.npm.protocol.Protocol.NpmReleaseExt
import org.tudelft.plugins.npm.stages.{NpmReleasesExtStage, NpmReleasesStage}


// Maven
object Main {
  def main(args: Array[String]): Unit = {

    val query: String =
      """
        | SELECT *
        | FROM CDLFCore
        |""".stripMargin

//    val releaseSource = new MavenReleasesStage()
//    val jsonStage = new JsonExitStage[MavenRelease]
//    val enrichReleases = new MavenReleasesExtStage()
//    val sqlStage = SQLStage.createSQLStage[MavenReleaseExt](query)

    val cdSource = new ClearlyDefinedReleasesStage()
    val cdSQLStage = new SQLStage[ClearlyDefinedRelease](query)

    //val cargoSource = new CargoReleasesStage()
   // val cargoSqlStage = new SQLStage[CrateRelease](query)

    new PipelineBuilder()
      .setPipelineName("Cargo plugin")
      .setRestartStrategy(RestartStrategies.fixedDelayRestart(
        3,
        Time.of(10, TimeUnit.SECONDS))) // try restarting 3 times
      .enableCheckpointing(1000) // checkpointing every 1000ms
      .setBufferProperty(KafkaBuffer.AMOUNT_OF_PARTITIONS, "8")
      .setBufferProperty(KafkaBuffer.AMOUNT_OF_REPLICAS, "2")
      .setBufferProperty(KafkaBuffer.COMPRESSION_TYPE, "gzip")
      .setBufferProperty(KafkaBuffer.BROKER, "localhost:9092")
      .setBufferProperty(KafkaBuffer.ZOOKEEPER, "localhost:2181")
      .setBufferProperty("message.max.bytes", "10485760") // max message size is 10mb
      .setBufferProperty("max.request.size", "10485760") // max message size is 10 mb
      .edge(cdSource, cdSQLStage)
      .build()
      .startMock
  }
}

