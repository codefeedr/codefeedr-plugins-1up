package org.tudelft

import java.util.Date

import org.apache.flink.streaming.api.scala._
import org.codefeedr.pipeline.PipelineBuilder
import org.codefeedr.stages.OutputStage
import org.tudelft.plugins.json.{JsonTransformStage, StringWrapper}
import org.tudelft.plugins.maven.protocol.Protocol.{Guid, MavenRelease, MavenReleaseExt}
import org.tudelft.plugins.maven.stages.{MavenReleasesExtStage, MavenReleasesStage}

object Main {
  def main(args: Array[String]): Unit = {
    new PipelineBuilder()
      .append(new MavenReleasesStage())
      .append(new MavenReleasesExtStage())
      .append(new JsonTransformStage[MavenReleaseExt]())
      .append (new CrateDownloadsOutput)
      .build()
      .startMock()
  }
}

class CrateDownloadsOutput extends OutputStage[StringWrapper] {
  override def main(source: DataStream[StringWrapper]): Unit = {
    source.map(x => println(x.s))
  }
}
