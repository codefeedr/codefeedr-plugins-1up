package org.tudelft.plugins.maven.stages

import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.stages.OutputStage
import org.tudelft.plugins.maven.protocol.Protocol._
import org.tudelft.plugins.maven.util.SQLService

/**
  * SQL object to enable the passing of a string
  * This string originally is the stageID, however in our case it will be used to pass the SQLQuery
  */
object SQLStage{
  var id: Option[String] = None
  def createSQLStage(in: String) : SQLStage = {
    this.id = Some(in)
    new SQLStage()
  }

  class SQLStage extends OutputStage[MavenRelease](stageId = id){

    override def main(source: DataStream[MavenRelease]): Unit = {
      //Perform the query
      SQLService.performQuery(source, this.stageId.get)
    }

  }

}
