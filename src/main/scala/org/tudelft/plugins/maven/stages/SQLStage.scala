package org.tudelft.plugins.maven.stages

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.stages.OutputStage
import org.tudelft.plugins.maven.protocol.Protocol._
import org.tudelft.plugins.maven.util.SQLService

class SQLStage extends OutputStage[MavenReleaseExt](stageId = Some("maven_SQL")){

  override def main(source: DataStream[MavenReleaseExt]): Unit = {
    //Perform the query
    SQLService.performQuery(source)
  }

}
