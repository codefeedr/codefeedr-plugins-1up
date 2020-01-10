package org.tudelft

import java.lang.reflect.Constructor
import java.util.Date

import org.apache.flink.streaming.api.scala._
import org.codefeedr.pipeline.PipelineBuilder
import org.codefeedr.stages.OutputStage
import org.tudelft.plugins.json.{JsonExitStage, JsonTransformStage, StringWrapper}
import org.tudelft.plugins.maven.protocol.Protocol.{Guid, MavenProject, MavenRelease, MavenReleaseExt}
import org.tudelft.plugins.maven.stages.SQLStage.SQLStage
import org.tudelft.plugins.maven.stages.{MavenReleasesExtStage, MavenReleasesStage, SQLStage}

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
