package org.tudelft.plugins

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.{EnvironmentSettings, Table}
import org.tudelft.plugins.maven.protocol.Protocol._
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.scala.DataStream
import org.apache.flink.table.api.scala._
import org.apache.flink.types.Row
import org.tudelft.plugins.clearlydefined.protocol.Protocol.{ClearlyDefinedRelease, ClearlyDefinedReleasePojo}
import org.tudelft.plugins.clearlydefined.util.ClearlyDefinedSQLService
import org.tudelft.plugins.cargo.protocol.Protocol.{CrateRelease, CrateReleasePojo}
import org.tudelft.plugins.cargo.util.CargoSQLService
import org.tudelft.plugins.maven.util.MavenSQLService
import org.tudelft.plugins.npm.protocol.Protocol.{NpmReleaseExt, NpmReleaseExtPojo}
import org.tudelft.plugins.npm.util.NpmSQLService

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

    //Perform query
    val queryTable: Table = tEnv.sqlQuery(query)
    tEnv.explain(queryTable)

    // Just for printing purposes, in reality you would need something other than Row
    implicit val typeInfo = TypeInformation.of(classOf[Row])

    tEnv.toAppendStream(queryTable)(typeInfo).print()

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

      // Maven cases
      case x if typeOf[T] <:< typeOf[MavenRelease] => {
        val in = x.asInstanceOf[DataStream[MavenRelease]]
        val pojos = in.map(x => {
          MavenReleasePojo.fromMavenRelease(x)
        })

        tEnv.registerDataStream("Maven", pojos)
      }

      case x if typeOf[T] <:< typeOf[MavenReleaseExt] => {
        val in = x.asInstanceOf[DataStream[MavenReleaseExt]]
        val pojos: DataStream[MavenReleaseExtPojo] = in.map(x => {
          MavenReleaseExtPojo.fromMavenReleaseExt(x)
        })

        MavenSQLService.registerTables(pojos, tEnv)
      }

      // Cargo cases
      case x if typeOf[T] <:< typeOf[CrateRelease] => {
        implicit val typeInfo = TypeInformation.of(classOf[CrateReleasePojo])
        val in = x.asInstanceOf[DataStream[CrateRelease]]
        val pojos: DataStream[CrateReleasePojo] = in.map(x => {
          CrateReleasePojo.fromCrateRelease(x)
        })

        CargoSQLService.registerTables(pojos, tEnv)
      }

      // Npm cases
      case x if typeOf[T] <:< typeOf[NpmReleaseExt] => {
        val in = x.asInstanceOf[DataStream[NpmReleaseExt]]
        val pojos: DataStream[NpmReleaseExtPojo] = in.map(x => {
          NpmReleaseExtPojo.fromNpmReleaseExt(x)
        })
        NpmSQLService.registerTables(pojos, tEnv)
      }

      // ClearlyDefined
      case x if typeOf[T] <:< typeOf[ClearlyDefinedRelease] => {
        val in = x.asInstanceOf[DataStream[ClearlyDefinedRelease]]
        val pojos: DataStream[ClearlyDefinedReleasePojo] = in.map(x => {
          ClearlyDefinedReleasePojo.fromClearlyDefinedRelease(x)
        })

        ClearlyDefinedSQLService.registerTables(pojos, tEnv)

      }

      // TODO add all other types here
      case _ => throw new IllegalArgumentException("stream of unsupported type")
    }
  }


}
