package org.tudelft.plugins.maven.stages

import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.stages.OutputStage
import org.tudelft.plugins.maven.util.SQLService

import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

/**
  * SQL object to enable the passing of a string
  * This string originally is the stageID, however in our case it will be used to pass the SQLQuery
  */
object SQLStage {
  var id: Option[String] = None
  def createSQLStage[T <: Serializable with AnyRef: ClassTag: TypeTag](in: String) : SQLStage[T] = {
    this.id = Some(in)
    new SQLStage[T]()
  }

  /**
    * This stage takes in a stream and performs a query on it
    * For constructing an SQLStage use SQLStage.createSQLStage or a None.get exception is thrown
    *
    * @param classTag$T The classTag of the input stream
    * @param typeTag$T The typeTag of the input stream
    * @tparam T Type of the input stream
    */
  class SQLStage[T <: Serializable with AnyRef: ClassTag: TypeTag] extends OutputStage[T](stageId = id) {
    override def main(source: DataStream[T]): Unit = {
      SQLService.performQuery(source, this.stageId.get)
    }
  }

}
