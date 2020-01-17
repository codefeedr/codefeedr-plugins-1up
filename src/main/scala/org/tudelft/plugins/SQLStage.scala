package org.tudelft.plugins

import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.stages.{OutputStage, OutputStage2, OutputStage3, OutputStage4}

import scala.reflect.runtime.universe._
import scala.reflect.ClassTag

/**
  * SQL stages with 1-4 input streams
  *
  */
object SQLStage {

  /**
    * This stage takes in a stream and performs a query on it
    *
    * @tparam T Type of the input stream
    */
  class SQLStage[T <: Serializable with AnyRef : ClassTag : TypeTag](query: String)
    extends OutputStage[T](stageId = Some(System.currentTimeMillis().toString)) {

    override def main(source: DataStream[T]): Unit = {
      val tEnv = SQLService.setupEnv()
      SQLService.registerTableFromStream[T](source, tEnv)
      SQLService.performQuery(query, tEnv)
    }
  }

  /**
    * This stage takes in two streams and performs a query on it
    *
    * @tparam T1 The input type of the first datastream
    * @tparam T2 The input type of the second datastream
    */
  class SQLStage2[T1 <: Serializable with AnyRef : ClassTag : TypeTag,
  T2 <: Serializable with AnyRef : ClassTag : TypeTag](query: String) extends OutputStage2[T1, T2] {

    override def main(source1: DataStream[T1], source2: DataStream[T2]): Unit = {
      val tEnv = SQLService.setupEnv()
      SQLService.registerTableFromStream[T1](source1, tEnv)
      SQLService.registerTableFromStream[T2](source2, tEnv)
      SQLService.performQuery(query, tEnv)
    }
  }


  /**
    * This stage takes in two streams and performs a query on it
    *
    * @tparam T1 The input type of the first datastream
    * @tparam T2 The input type of the second datastream
    * @tparam T3 The input type of the third datastream
    */
  class SQLStage3[T1 <: Serializable with AnyRef : ClassTag : TypeTag,
  T2 <: Serializable with AnyRef : ClassTag : TypeTag,
  T3 <: Serializable with AnyRef : ClassTag : TypeTag](query: String) extends OutputStage3[T1, T2, T3] {

    override def main(source1: DataStream[T1], source2: DataStream[T2], source3: DataStream[T3]): Unit = {
      val tEnv = SQLService.setupEnv()
      SQLService.registerTableFromStream[T1](source1, tEnv)
      SQLService.registerTableFromStream[T2](source2, tEnv)
      SQLService.registerTableFromStream[T3](source3, tEnv)
      SQLService.performQuery(query, tEnv)
    }
  }


  /**
    * This stage takes in two streams and performs a query on it
    *
    * @tparam T1 The input type of the first datastream
    * @tparam T2 The input type of the second datastream
    * @tparam T3 The input type of the third datastream
    * @tparam T4 The input type of the fourth datastream
    */
  class SQLStage4[T1 <: Serializable with AnyRef : ClassTag : TypeTag,
  T2 <: Serializable with AnyRef : ClassTag : TypeTag,
  T3 <: Serializable with AnyRef : ClassTag : TypeTag,
  T4 <: Serializable with AnyRef : ClassTag : TypeTag](query: String) extends OutputStage4[T1, T2, T3, T4] {

    override def main(source1: DataStream[T1], source2: DataStream[T2], source3: DataStream[T3], source4: DataStream[T4]): Unit = {
      val tEnv = SQLService.setupEnv()
      SQLService.registerTableFromStream[T1](source1, tEnv)
      SQLService.registerTableFromStream[T2](source2, tEnv)
      SQLService.registerTableFromStream[T3](source3, tEnv)
      SQLService.registerTableFromStream[T4](source4, tEnv)
      SQLService.performQuery(query, tEnv)
    }
  }

}
