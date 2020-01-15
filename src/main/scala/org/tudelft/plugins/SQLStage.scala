package org.tudelft.plugins

import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.stages.{OutputStage, OutputStage2, OutputStage3, OutputStage4}

import scala.reflect.runtime.universe._
import scala.reflect.ClassTag

/**
  * SQL object to enable the passing of a string
  * This string originally is the stageID, however in our case it will be used to pass the SQLQuery
  */
object SQLStage {
  var id: Option[String] = None

  /**
    * Method used for created an SQLStage
    *
    * @param query The query to be executed by the SQL stage
    * @tparam T The input type of the datastream
    * @return a new SQLStage of type T
    */
  def createSQLStage[T <: Serializable with AnyRef : ClassTag : TypeTag](query: String): SQLStage[T] = {
    this.id = Some(query)
    new SQLStage[T]()
  }

  /**
    * This stage takes in a stream and performs a query on it
    * For constructing an SQLStage use SQLStage.createSQLStage
    *
    * @tparam T Type of the input stream
    */
  class SQLStage[T <: Serializable with AnyRef : ClassTag : TypeTag] extends OutputStage[T](stageId = id) {

    override def main(source: DataStream[T]): Unit = {
      val tEnv = SQLService.setupEnv()
      SQLService.registerTableFromStream[T](source, tEnv)
      SQLService.performQuery(this.stageId.get, tEnv)
    }
  }

  /**
    * Method used for created an SQLStage2
    *
    * @param query The query to be executed by the SQL stage
    * @tparam T1 The input type of the first datastream
    * @tparam T2 The input type of the second datastream
    * @return A new SQLStage2 of type [T1, T2]
    */
  def createSQLStage2[T1 <: Serializable with AnyRef : ClassTag : TypeTag,
  T2 <: Serializable with AnyRef : ClassTag : TypeTag](query: String): SQLStage2[T1, T2] = {
    this.id = Some(query)
    new SQLStage2[T1, T2]()
  }

  /**
    * This stage takes in two streams and performs a query on it
    * For constructing an SQLStage2 use SQLStage.createSQLStage2
    *
    * @tparam T1 The input type of the first datastream
    * @tparam T2 The input type of the second datastream
    */
  class SQLStage2[T1 <: Serializable with AnyRef : ClassTag : TypeTag,
  T2 <: Serializable with AnyRef : ClassTag : TypeTag] extends OutputStage2[T1, T2](stageId = id) {

    override def main(source1: DataStream[T1], source2: DataStream[T2]): Unit = {
      val tEnv = SQLService.setupEnv()
      SQLService.registerTableFromStream[T1](source1, tEnv)
      SQLService.registerTableFromStream[T2](source2, tEnv)
      SQLService.performQuery(this.stageId.get, tEnv)
    }
  }

  /**
    * Method used for created an SQLStage3
    *
    * @param query The query to be executed by the SQL stage
    * @tparam T1 The input type of the first datastream
    * @tparam T2 The input type of the second datastream
    * @tparam T3 The input type of the third datastream
    * @return A new SQLStage3 of type [T1, T2, T3]
    */
  def createSQLStage3[T1 <: Serializable with AnyRef : ClassTag : TypeTag,
  T2 <: Serializable with AnyRef : ClassTag : TypeTag,
  T3 <: Serializable with AnyRef : ClassTag : TypeTag](query: String): SQLStage3[T1, T2, T3] = {
    this.id = Some(query)
    new SQLStage3[T1, T2, T3]()
  }

  /**
    * This stage takes in two streams and performs a query on it
    * For constructing an SQLStage3 use SQLStage.createSQLStage3
    *
    * @tparam T1 The input type of the first datastream
    * @tparam T2 The input type of the second datastream
    * @tparam T3 The input type of the third datastream
    */
  class SQLStage3[T1 <: Serializable with AnyRef : ClassTag : TypeTag,
  T2 <: Serializable with AnyRef : ClassTag : TypeTag,
  T3 <: Serializable with AnyRef : ClassTag : TypeTag] extends OutputStage3[T1, T2, T3](stageId = id) {

    override def main(source1: DataStream[T1], source2: DataStream[T2], source3: DataStream[T3]): Unit = {
      val tEnv = SQLService.setupEnv()
      SQLService.registerTableFromStream[T1](source1, tEnv)
      SQLService.registerTableFromStream[T2](source2, tEnv)
      SQLService.registerTableFromStream[T3](source3, tEnv)
      SQLService.performQuery(this.stageId.get, tEnv)
    }
  }

  /**
    * Method used for creating an SQLStage4
    *
    * @param query The query to be executed by the SQL stage
    * @tparam T1 The input type of the first datastream
    * @tparam T2 The input type of the second datastream
    * @tparam T3 The input type of the third datastream
    * @tparam T4 The input type of the fourth datastream
    * @return A new SQLStage4 of type [T1, T2, T3, T4]
    */
  def createSQLStage4[T1 <: Serializable with AnyRef : ClassTag : TypeTag,
  T2 <: Serializable with AnyRef : ClassTag : TypeTag,
  T3 <: Serializable with AnyRef : ClassTag : TypeTag,
  T4 <: Serializable with AnyRef : ClassTag : TypeTag](query: String): SQLStage4[T1, T2, T3, T4] = {
    this.id = Some(query)
    new SQLStage4[T1, T2, T3, T4]()
  }

  /**
    * This stage takes in two streams and performs a query on it
    * For constructing an SQLStage4 use SQLStage.createSQLStage4
    *
    * @tparam T1 The input type of the first datastream
    * @tparam T2 The input type of the second datastream
    * @tparam T3 The input type of the third datastream
    * @tparam T4 The input type of the fourth datastream
    */
  class SQLStage4[T1 <: Serializable with AnyRef : ClassTag : TypeTag,
  T2 <: Serializable with AnyRef : ClassTag : TypeTag,
  T3 <: Serializable with AnyRef : ClassTag : TypeTag,
  T4 <: Serializable with AnyRef : ClassTag : TypeTag] extends OutputStage4[T1, T2, T3, T4](stageId = id) {

    override def main(source1: DataStream[T1], source2: DataStream[T2], source3: DataStream[T3], source4: DataStream[T4]): Unit = {
      val tEnv = SQLService.setupEnv()
      SQLService.registerTableFromStream[T1](source1, tEnv)
      SQLService.registerTableFromStream[T2](source2, tEnv)
      SQLService.registerTableFromStream[T3](source3, tEnv)
      SQLService.registerTableFromStream[T4](source4, tEnv)
      SQLService.performQuery(this.stageId.get, tEnv)
    }
  }

}
