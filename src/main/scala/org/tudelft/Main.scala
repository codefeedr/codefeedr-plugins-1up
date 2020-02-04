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
        | SELECT
        |  description,
        |  TUMBLE_START(updated_at, INTERVAL '1' DAY) as wStart
        | FROM CargoCrate
        | GROUP BY TUMBLE(updated_at, INTERVAL '1' DAY), description
        |""".stripMargin

    val query2: String =
      """
        |SELECT dl_path
        |FROM CargoCrateVersions
        |""".stripMargin

    val query3: String =
      """
        | SELECT description, updated_at
        | FROM CargoCrate
        | GROUP BY HOP(updated_at, INTERVAL '1' HOUR, INTERVAL '1' DAY), description, updated_at
    """.stripMargin

    val cdQuery =
      """
        | SELECT updated
        | FROM ClearlyDefinedMeta
        |""".stripMargin

//    val releaseSource = new MavenReleasesStage()
//    val jsonStage = new JsonExitStage[MavenRelease]
//    val enrichReleases = new MavenReleasesExtStage()
//    val sqlStage = SQLStage.createSQLStage[MavenReleaseExt](query)

    val cdSource = new ClearlyDefinedReleasesStage()
    val cdSQLStage = new SQLStage[ClearlyDefinedRelease](cdQuery)

    //val cargoSource = new CargoReleasesStage()
    //val cargoSqlStage = new SQLStage[CrateRelease](query2)

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
      .setBufferProperty("message.max.bytes", "41943040") // max message size is 40mb
      .setBufferProperty("max.request.size", "41943040") // max message size is 40 mb
      .edge(cdSource, cdSQLStage)
      .build()
      .startMock
  }
}

