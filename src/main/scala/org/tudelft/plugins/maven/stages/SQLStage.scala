package org.tudelft.plugins.maven.stages

import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.stages.OutputStage
import org.tudelft.plugins.maven.protocol.Protocol._
import org.tudelft.plugins.maven.util.SQLService

import scala.reflect.runtime.universe._
import scala.reflect.ClassTag

/**
  * SQL object to enable the passing of a string
  * This string originally is the stageID, however in our case it will be used to pass the SQLQuery
  */
object SQLStage{
  var id: Option[String] = None
  def createSQLStage[T <: Serializable with AnyRef: ClassTag: TypeTag](in: String) : SQLStage[T] = {
    this.id = Some(in)
    new SQLStage[T]()
  }

  class SQLStage[T <: Serializable with AnyRef: ClassTag: TypeTag] extends OutputStage[T](stageId = id){

    override def main(source: DataStream[T]): Unit = {
      //Perform the query
      SQLService.performQuery(source, this.stageId.get)
    }

  }

}
