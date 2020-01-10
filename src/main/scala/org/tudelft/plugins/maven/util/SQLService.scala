package org.tudelft.plugins.maven.util

import org.apache.flink.api.common.typeinfo.{TypeInformation, Types}
import org.apache.flink.api.java.typeutils.{PojoField, PojoTypeInfo}
import org.apache.flink.streaming.api.scala.{DataStream, OutputTag, StreamExecutionEnvironment}
import org.apache.flink.table.api.Table
import org.tudelft.plugins.maven.protocol.Protocol._
import org.apache.flink.api.scala._
import org.apache.flink.table.api.EnvironmentSettings
import org.apache.flink.table.api.scala._
import org.apache.flink.table.sources.CsvTableSource
import org.apache.flink.types.Row

import scala.reflect.runtime.universe._


object SQLService {
  val rootTableName: String = "Maven"
  val projectTableName: String = "MavenProject"
  val projectParentTableName: String = "MavenProjectParent"
  val projectDependenciesTableName: String = "MavenProjectDependencies"
  val projectLicensesTableName: String = "MavenProjectLicenses"
  val projectRepositoriesTableName: String = "MavenProjectRepositories"
  val projectOrganizationTableName: String = "MavenProjectOrganization"
  val projectIssueManagementTableName: String = "MavenProjectIssueManagement"
  val projectSCMTableName: String = "MavenProjectSCM"

  def performQuery[T: TypeTag](in: DataStream[T]): Unit = {

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

    val query = "SELECT optional FROM " + projectDependenciesTableName
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
        val pojos: DataStream[MavenReleaseExtPojo] = in.map(x => {
          MavenReleaseExtPojo.fromMavenReleaseExt(x)
        })

        registerTables(pojos, tEnv)
      }

      // Other plugins

      //TODO add all other types here
      case _ => throw new IllegalArgumentException("stream of unsupported type")
    }
  }

  def registerTables[T: TypeTag](field: DataStream[T], tEnv: StreamTableEnvironment): Unit = field match {
    case _ if typeOf[T] <:< typeOf[MavenReleaseExtPojo] => {
      val releasesStream = field.asInstanceOf[DataStream[MavenReleaseExtPojo]]
      tEnv.registerDataStream(rootTableName, releasesStream)

      val projectPojoStream: DataStream[MavenProjectPojo] = releasesStream.map(x => x.project)
      tEnv.registerDataStream(projectTableName, projectPojoStream)

      val organizationPojoStream: DataStream[OrganizationPojo] = releasesStream
        .filter(x => x.project.organization != null)
        .map(x => x.project.organization)
      tEnv.registerDataStream(projectOrganizationTableName, organizationPojoStream)

      val issueManagementPojoStream: DataStream[IssueManagementPojo] = releasesStream
        .filter(x => x.project.issueManagement != null)
        .map(x => x.project.issueManagement)
      tEnv.registerDataStream(projectIssueManagementTableName, issueManagementPojoStream)

      val scmPojoStream: DataStream[SCMPojo] = releasesStream
        .filter(x => x.project.scm != null)
        .map(x => x.project.scm)
      tEnv.registerDataStream(projectSCMTableName, scmPojoStream)

      val dependenciesPojoStream: DataStream[DependencyPojo] = releasesStream
        .filter(x => x.project.dependencies != null)
        .flatMap(x => x.project.dependencies)
      tEnv.registerDataStream(projectDependenciesTableName, dependenciesPojoStream)
    }

    case _: T => print("Have not implemented registering a table for object of type " + typeOf[T].toString)
  }

}
