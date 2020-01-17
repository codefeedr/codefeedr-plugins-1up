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

    val query: String = "Select tool, title from ClearlyDefinedDescribedScore, Maven"

    val releaseSource = new MavenReleasesStage()
    val enrichReleases = new MavenReleasesExtStage()
    val sqlStage2 = SQLStage.createSQLStage2[ClearlyDefinedRelease, MavenReleaseExt](query)
    val sqlStage = SQLStage.createSQLStage[MavenReleaseExt]("Select title from Maven")

    //val cdSource = new ClearlyDefinedReleasesStage()
    //val cdSQLStage = SQLStage.createSQLStage[ClearlyDefinedRelease](query)

    val cargoSource = new CargoReleasesStage()
    val cargoSqlStage = SQLStage.createSQLStage[CrateRelease](query)

//    val npmReleaseSource = new NpmReleasesStage()
//    val npmExtendedReleases = new NpmReleasesExtStage()
//    val npmSQlstage0 = SQLStage.createSQLStage[NpmReleaseExt]("SELECT * FROM Npm")
//    val npmSQlstage1 = SQLStage.createSQLStage[NpmReleaseExt]("SELECT * FROM NpmProject")
//    val npmSQlstage2 = SQLStage.createSQLStage[NpmReleaseExt]("SELECT * FROM NpmDependency")
//    val npmSQlstage3 = SQLStage.createSQLStage[NpmReleaseExt]("SELECT * FROM NpmAuthor")
//    val npmSQlstage4 = SQLStage.createSQLStage[NpmReleaseExt]("SELECT * FROM NpmContributors")
//    val npmSQlstage5 = SQLStage.createSQLStage[NpmReleaseExt]("SELECT * FROM NpmMaintainers")
//    val npmSQlstage6 = SQLStage.createSQLStage[NpmReleaseExt]("SELECT * FROM NpmRepository")
//    val npmSQlstage7 = SQLStage.createSQLStage[NpmReleaseExt]("SELECT * FROM NpmBug")
//    val npmSQlstage8 = SQLStage.createSQLStage[NpmReleaseExt]("SELECT * FROM NpmTime")
//    val npmSQlstage9 = SQLStage.createSQLStage[NpmReleaseExt]("SELECT * FROM NpmKeywords")

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
      .setBufferProperty("message.max.bytes", "5000000") // max message size is 5mb
      .setBufferProperty("max.request.size", "5000000") // max message size is 5 mb
      //      .edge(cdSource, cdSQLStage)
      .edge(releaseSource, enrichReleases)
      .edge(enrichReleases, sqlStage)
      //      .edge(releaseSource, sqlStage)
      .build()
      .start(args)
  }
}

