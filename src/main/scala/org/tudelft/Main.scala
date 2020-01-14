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
import org.tudelft.plugins.clearlydefined.operators.ClearlyDefinedReleasesSource
import org.tudelft.plugins.clearlydefined.protocol.Protocol.ClearlyDefinedRelease
import org.tudelft.plugins.clearlydefined.stages.ClearlyDefinedReleasesStage
import org.tudelft.plugins.maven.stages.{MavenReleasesExtStage, MavenReleasesStage}
import org.tudelft.plugins.npm.protocol.Protocol.NpmReleaseExt
import org.tudelft.plugins.npm.stages.{NpmReleasesExtStage, NpmReleasesStage}

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

    val query: String =
      """
        | SELECT *
        | FROM CDLFCoreDiscovered
        | WHERE expression LIKE '%%'
        |""".stripMargin

    val releaseSource = new MavenReleasesStage()
    val jsonStage = new JsonExitStage[MavenRelease]
    val enrichReleases = new MavenReleasesExtStage()
    val sqlStage = SQLStage.createSQLStage[MavenReleaseExt](query)

    val cdSource = new ClearlyDefinedReleasesStage()
    val cdSQLStage = SQLStage.createSQLStage[ClearlyDefinedRelease](query)

    val npmReleaseSource = new NpmReleasesStage()
    val npmExtendedReleases = new NpmReleasesExtStage()
    val npmSQlstage0 = SQLStage.createSQLStage[NpmReleaseExt]("SELECT * FROM Npm")
    val npmSQlstage1 = SQLStage.createSQLStage[NpmReleaseExt]("SELECT * FROM NpmProject")
    val npmSQlstage2 = SQLStage.createSQLStage[NpmReleaseExt]("SELECT * FROM NpmDependency")
    val npmSQlstage3 = SQLStage.createSQLStage[NpmReleaseExt]("SELECT * FROM NpmAuthor")
    val npmSQlstage4 = SQLStage.createSQLStage[NpmReleaseExt]("SELECT * FROM NpmContributors")
    val npmSQlstage5 = SQLStage.createSQLStage[NpmReleaseExt]("SELECT * FROM NpmMaintainers")
    val npmSQlstage6 = SQLStage.createSQLStage[NpmReleaseExt]("SELECT * FROM NpmRepository")
    val npmSQlstage7 = SQLStage.createSQLStage[NpmReleaseExt]("SELECT * FROM NpmBug")
    val npmSQlstage8 = SQLStage.createSQLStage[NpmReleaseExt]("SELECT * FROM NpmTime")
    val npmSQlstage9 = SQLStage.createSQLStage[NpmReleaseExt]("SELECT * FROM NpmKeywords")

    new PipelineBuilder()
      .setPipelineName("Npm plugin")
      .setRestartStrategy(RestartStrategies.fixedDelayRestart(
        3,
        Time.of(10, TimeUnit.SECONDS))) // try restarting 3 times
      .enableCheckpointing(1000) // checkpointing every 1000ms
      .setBufferProperty(KafkaBuffer.COMPRESSION_TYPE, "gzip")
      .setBufferProperty(KafkaBuffer.BROKER, "localhost:9092")
      .setBufferProperty(KafkaBuffer.ZOOKEEPER, "localhost:2181")
      .setBufferProperty("message.max.bytes", "5000000") // max message size is 5mb
      .setBufferProperty("max.request.size", "5000000") // max message size is 5 mb
      
      .edge(npmReleaseSource, npmExtendedReleases)
      //.edge(npmExtendedReleases, npmSQlstage0)
      //.edge(npmExtendedReleases, npmSQlstage1)
      //.edge(npmExtendedReleases, npmSQlstage2)
      //.edge(npmExtendedReleases, npmSQlstage3)
      //.edge(npmExtendedReleases, npmSQlstage4)
      //.edge(npmExtendedReleases, npmSQlstage5)
      //.edge(npmExtendedReleases, npmSQlstage6)
      //.edge(npmExtendedReleases, npmSQlstage7)
      //.edge(npmExtendedReleases, npmSQlstage8)
      //.edge(npmExtendedReleases, npmSQlstage9) // deze werkte 1x en had daarna moeite, troubles met Kafka maybe?
      .build()
      .startLocal()
  }
}

