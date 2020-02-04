package org.tudelft

import java.util.concurrent.TimeUnit

import org.apache.flink.api.common.restartstrategy.RestartStrategies
import org.apache.flink.api.common.time.Time
import org.codefeedr.buffer._
import org.codefeedr.pipeline.PipelineBuilder


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

    val query4: String =
      """
        | SELECT updated_at
        | FROM CargoCrate
    """.stripMargin

    val cdQuery =
      """
        | SELECT updated, name, schemaVersion
        | FROM ClearlyDefinedMeta
        | GROUP BY HOP(updated, INTERVAL '1' HOUR, INTERVAL '1' DAY), updated, name, schemaVersion
        |""".stripMargin

    val mavenQuery =
      """
        | SELECT title, pubDate
        | FROM Maven
        | GROUP BY HOP(pubDate, INTERVAL '1' HOUR, INTERVAL '1' DAY), pubDate, title
        |""".stripMargin

    val npmQuery =
      """
        |SELECT name, retrieveDate
        |FROM Npm
        |GROUP BY HOP(retrieveDate, INTERVAL '30' SECOND, INTERVAL '1' MINUTE), name, retrieveDate
        |""".stripMargin

    val releaseSource = new MavenReleasesStage()
//    val jsonStage = new JsonExitStage[MavenRelease]
    val enrichReleases = new MavenReleasesExtStage()
    val sqlStage = new SQLStage[MavenReleaseExt](mavenQuery)

    val cdSource = new ClearlyDefinedReleasesStage()
    val cdSQLStage = new SQLStage[ClearlyDefinedRelease](cdQuery)

    val cargoSource = new CargoReleasesStage()
    val cargoSqlStage = new SQLStage[CrateRelease](query4)

    val npmSource = new NpmReleasesStage()
    val npmEnrich = new NpmReleasesExtStage()
    val npmSQL = new SQLStage[NpmReleaseExt](npmQuery)

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
      .edge(npmSource, npmEnrich)
      .edge(npmEnrich, npmSQL)
      .build()
      .startMock
  }
}

