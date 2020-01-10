package org.tudelft.plugins.maven.util

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.{EnvironmentSettings, Table}
import org.tudelft.plugins.maven.protocol.Protocol._
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.scala.DataStream
import org.apache.flink.table.api.scala._
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

      this.registerProjectTable(releasesStream, tEnv)
      this.registerOrganizationTable(releasesStream, tEnv)
      this.registerIssueManagementTable(releasesStream, tEnv)
      this.registerSCMTable(releasesStream, tEnv)
      this.registerDependenciesTable(releasesStream, tEnv)
      this.registerLicensesTable(releasesStream, tEnv)
      this.registerRepositoriesTable(releasesStream, tEnv)
    }

    case _: T => print("Have not implemented registering a table for object of type " + typeOf[T].toString)
  }

  def registerProjectTable(stream: DataStream[MavenReleaseExtPojo], tEnv: StreamTableEnvironment): Unit = {
    val projectStream: DataStream[MavenProjectPojo] = stream.map(x => x.project)
    tEnv.registerDataStream(projectTableName, projectStream)
  }

  def registerOrganizationTable(stream: DataStream[MavenReleaseExtPojo], tEnv: StreamTableEnvironment): Unit = {
    val organizationPojoStream: DataStream[OrganizationPojo] = stream
      .filter(x => x.project.organization != null)
      .map(x => x.project.organization)
    tEnv.registerDataStream(projectOrganizationTableName, organizationPojoStream)
  }

  def registerIssueManagementTable(stream: DataStream[MavenReleaseExtPojo], tEnv: StreamTableEnvironment): Unit = {
    val issueManagementPojoStream: DataStream[IssueManagementPojo] = stream
      .filter(x => x.project.issueManagement != null)
      .map(x => x.project.issueManagement)
    tEnv.registerDataStream(projectIssueManagementTableName, issueManagementPojoStream)
  }

  def registerSCMTable(stream: DataStream[MavenReleaseExtPojo], tEnv: StreamTableEnvironment): Unit = {
    val scmPojoStream: DataStream[SCMPojo] = stream
      .filter(x => x.project.scm != null)
      .map(x => x.project.scm)
    tEnv.registerDataStream(projectSCMTableName, scmPojoStream)
  }

  def registerDependenciesTable(stream: DataStream[MavenReleaseExtPojo], tEnv: StreamTableEnvironment): Unit = {
    val dependenciesPojoStream: DataStream[DependencyPojoExt] = stream
      .filter(x => x.project.dependencies != null)
      .map(x => {
        x.project.dependencies.map(y => {
          new DependencyPojoExt() {
            projectId = x.project.artifactId
            groupId = y.groupId
            artifactId = y.artifactId
            version = y.version
            `type` = y.`type`
            scope = y.scope
            optional = y.optional
          }
        })
      })
      .flatMap(x => x)
    tEnv.registerDataStream(projectDependenciesTableName, dependenciesPojoStream)
  }

  def registerLicensesTable(stream: DataStream[MavenReleaseExtPojo], tEnv: StreamTableEnvironment): Unit = {
    val licensesPojoStream: DataStream[LicensePojoExt] = stream
      .filter(x => x.project.licenses != null)
      .map(x => {
        x.project.licenses.map(y => {
          new LicensePojoExt() {
            projectId = x.project.artifactId
            name = y.name
            url = y.url
            distribution = y.distribution
            comments = y.comments
          }
        })
      })
      .flatMap(x => x)
    tEnv.registerDataStream(projectLicensesTableName, licensesPojoStream)
  }

  def registerRepositoriesTable(stream: DataStream[MavenReleaseExtPojo], tEnv: StreamTableEnvironment): Unit = {
    val repositoryPojoStream: DataStream[RepositoryPojoExt] = stream
      .filter(x => x.project.repositories != null)
      .map(x => {
        x.project.repositories.map(y => {
          new RepositoryPojoExt() {
            projectId = x.project.artifactId
            id = y.id
            name = y.name
            url = y.url
          }
        })
      })
      .flatMap(x => x)
    tEnv.registerDataStream(projectRepositoriesTableName, repositoryPojoStream)
  }
}
