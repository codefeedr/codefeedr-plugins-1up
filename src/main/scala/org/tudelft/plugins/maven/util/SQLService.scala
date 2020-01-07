package org.tudelft.plugins.maven.util

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.Table
import org.tudelft.plugins.maven.protocol.Protocol._
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.scala.DataStream
import org.apache.flink.table.api.EnvironmentSettings
import org.apache.flink.table.api.scala._
import org.apache.flink.types.Row

import scala.reflect.runtime.universe._


object SQLService {
  def performQuery[T: TypeTag](in: DataStream[T], query: String): Unit = {

    //Get the required environments
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val settings = EnvironmentSettings.newInstance()
      .useOldPlanner()
      .inStreamingMode()
      .build()

    val tEnv = StreamTableEnvironment.create(env)


    //Maybe needed later
    //    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

    registerTableFromStream[T](in, tEnv)

    //      tEnv.registerDataStream("MavenTest", in)

//    println(tEnv.explain(tEnv.fromDataStream(in)))



    //    tEnv.fromDataStream(in)

//    println(tEnv.fromDataStream(in).getSchema())

    //Perform query
    val res: Table = tEnv.sqlQuery(query)

    // Just for printing purposes, in reality you would need something other than Row
    implicit val typeInfo = TypeInformation.of(classOf[Row])

    tEnv.toAppendStream(res)(typeInfo).print()

    env.execute()
  }

  /**
   * Registers a table from a DataStream
   *
   * @param stream the incoming stream
   * @param tEnv   the current table environment
   * @tparam T the type of the incoming datastream
   */
  def registerTableFromStream[T: TypeTag](stream: DataStream[T], tEnv: StreamTableEnvironment): Unit = {
    stream match {
      // For testing
      case x if typeOf[T] <:< typeOf[MavenReleasePojo] => tEnv.registerDataStream("Maven", stream)
      case x if typeOf[T] <:< typeOf[MavenReleaseExtPojo] => tEnv.registerDataStream("Maven", stream)

      // Actual cases
      case x if typeOf[T] <:< typeOf[MavenRelease] => {
        val in = x.asInstanceOf[DataStream[MavenRelease]]
        val pojos = in.map(x => {
          MavenReleasePojo.fromMavenRelease(x)
        })
        tEnv.registerDataStream("Maven", pojos)
      }

      case x if typeOf[T] <:< typeOf[MavenReleaseExt] => {
        val in = x.asInstanceOf[DataStream[MavenReleaseExt]]
        val pojos = in.map(x => {
          MavenReleaseExtPojo.fromMavenReleaseExt(x)
        })
        tEnv.registerDataStream("Maven", pojos)
      }

      // Other plugins


      //TODO add all other types here
      case _ => throw new IllegalArgumentException("stream of unsupported type")
    }
  }

}
