package org.tudelft.plugins.maven.util

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.scala.DataStream
import org.apache.flink.table.api.scala.StreamTableEnvironment
import org.tudelft.plugins.maven.protocol.Protocol._

import scala.reflect.runtime.universe._

object MavenSQLService {
  val rootTableName: String = "Maven"
  val projectTableName: String = "MavenProject"
  val projectParentTableName: String = "MavenProjectParent"
  val projectDependenciesTableName: String = "MavenProjectDependencies"
  val projectLicensesTableName: String = "MavenProjectLicenses"
  val projectRepositoriesTableName: String = "MavenProjectRepositories"
  val projectOrganizationTableName: String = "MavenProjectOrganization"
  val projectIssueManagementTableName: String = "MavenProjectIssueManagement"
  val projectSCMTableName: String = "MavenProjectSCM"

  def registerTables[T: TypeTag](stream: DataStream[T], tEnv: StreamTableEnvironment): Unit = stream match {
    case _ if typeOf[T] <:< typeOf[MavenReleaseExtPojo] => {
      val releasesStream = stream.asInstanceOf[DataStream[MavenReleaseExtPojo]]
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
    implicit val typeInfo = TypeInformation.of(classOf[MavenProjectPojo])
    val projectStream: DataStream[MavenProjectPojo] = stream.map(x => x.project)
    tEnv.registerDataStream(projectTableName, projectStream)
  }

  def registerOrganizationTable(stream: DataStream[MavenReleaseExtPojo], tEnv: StreamTableEnvironment): Unit = {
    implicit val typeInfo = TypeInformation.of(classOf[OrganizationPojoExt])
    val organizationPojoStream: DataStream[OrganizationPojo] = stream
      .filter(x => x.project.organization != null)
      .map(x => new OrganizationPojoExt(x.project.artifactId, x.project.organization.name, x.project.organization.url))
    tEnv.registerDataStream(projectOrganizationTableName, organizationPojoStream)
  }

  def registerIssueManagementTable(stream: DataStream[MavenReleaseExtPojo], tEnv: StreamTableEnvironment): Unit = {
    implicit val typeInfo = TypeInformation.of(classOf[IssueManagementPojoExt])
    val issueManagementPojoStream: DataStream[IssueManagementPojo] = stream
      .filter(x => x.project.issueManagement != null)
      .map(x => new IssueManagementPojoExt(x.project.artifactId, x.project.issueManagement.system, x.project.issueManagement.url))
    tEnv.registerDataStream(projectIssueManagementTableName, issueManagementPojoStream)
  }

  def registerSCMTable(stream: DataStream[MavenReleaseExtPojo], tEnv: StreamTableEnvironment): Unit = {
    implicit val typeInfo = TypeInformation.of(classOf[SCMPojoExt])
    val scmPojoStream: DataStream[SCMPojo] = stream
      .filter(x => x.project.scm != null)
      .map(x => new SCMPojoExt(x.project.artifactId, x.project.scm.connection, x.project.scm.developerConnection, x.project.scm.tag, x.project.scm.url))
    tEnv.registerDataStream(projectSCMTableName, scmPojoStream)
  }

  def registerDependenciesTable(stream: DataStream[MavenReleaseExtPojo], tEnv: StreamTableEnvironment): Unit = {
    implicit val typeInfo = TypeInformation.of(classOf[List[DependencyPojoExt]])
    implicit val typeInfo2 = TypeInformation.of(classOf[DependencyPojoExt])

    val dependenciesPojoStream: DataStream[DependencyPojoExt] = stream
      .filter(x => x.project.dependencies != null)
      .flatMap(x => {
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
    tEnv.registerDataStream(projectDependenciesTableName, dependenciesPojoStream)
  }

  def registerLicensesTable(stream: DataStream[MavenReleaseExtPojo], tEnv: StreamTableEnvironment): Unit = {
    implicit val typeInfo = TypeInformation.of(classOf[List[LicensePojoExt]])
    implicit val typeInfo2 = TypeInformation.of(classOf[LicensePojoExt])

    val licensesPojoStream: DataStream[LicensePojoExt] = stream
      .filter(x => x.project.licenses != null)
      .flatMap(x => {
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
    tEnv.registerDataStream(projectLicensesTableName, licensesPojoStream)
  }

  def registerRepositoriesTable(stream: DataStream[MavenReleaseExtPojo], tEnv: StreamTableEnvironment): Unit = {
    implicit val typeInfo = TypeInformation.of(classOf[List[RepositoryPojoExt]])
    implicit val typeInfo2 = TypeInformation.of(classOf[RepositoryPojoExt])

    val repositoryPojoStream: DataStream[RepositoryPojoExt] = stream
      .filter(x => x.project.repositories != null)
      .flatMap(x => {
        x.project.repositories.map(y => {
          new RepositoryPojoExt() {
            projectId = x.project.artifactId
            id = y.id
            name = y.name
            url = y.url
          }
        })
      })
    tEnv.registerDataStream(projectRepositoriesTableName, repositoryPojoStream)
  }
}
