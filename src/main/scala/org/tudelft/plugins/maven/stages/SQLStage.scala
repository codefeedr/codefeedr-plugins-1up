package org.tudelft.plugins.maven.stages

import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.stages.OutputStage
import org.tudelft.plugins.maven.protocol.Protocol._
import org.tudelft.plugins.maven.util.SQLService

class SQLStage extends OutputStage[MavenRelease](stageId = Some("maven_SQL")){

  override def main(source: DataStream[MavenRelease]): Unit = {
    //Perform the query
    SQLService.performQuery(source)
  }

}
