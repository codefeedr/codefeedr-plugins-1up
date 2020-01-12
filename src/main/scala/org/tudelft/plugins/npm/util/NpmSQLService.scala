package org.tudelft.plugins.npm.util

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.scala.DataStream
import org.apache.flink.table.api.scala.StreamTableEnvironment
import org.tudelft.plugins.maven.protocol.Protocol.{MavenReleaseExtPojo, SCMPojo}
import org.tudelft.plugins.maven.util.MavenSQLService.projectSCMTableName
import org.tudelft.plugins.npm.protocol.Protocol._

import scala.reflect.runtime.universe._

/**
 * Services for registering POJO types as datastreams within a Flink Table Environment so
 * SQL queries can be run on them
 *
 * @author Roald van der Heijden
 * Date: 2020-01-12 (YYYY-MM-DD)
 */
object NpmSQLService {
  // for every case class object from org.tudelft.plugins.npm.protocol.Protocol we need to have a String as tableName
  // and a def register[%CASE CLASS NAME%] which makes sure the Flink Table environment knows of the type/struct of this table
  val npm_rootTableName = "Npm"
  val npm_projectTableName = "NpmProject" //
  val npm_dependencyTableName = "NpmDependency"
  val npm_personTableName = "NpmPerson"
  val npm_repositoryTableName = "NpmRepository"
  val npm_bugTableName = "NpmBug"
  val npm_timeTableName = "NpmTime"

  def registerTables[T: TypeTag](stream: DataStream[T], tEnv: StreamTableEnvironment): Unit = stream match {
    case _ if typeOf[T] <:< typeOf[NpmReleaseExtPojo] => {
      val releasesStream = stream.asInstanceOf[DataStream[NpmReleaseExtPojo]]
      tEnv.registerDataStream(npm_rootTableName, releasesStream)

      this.registerNpmProjectTable(releasesStream, tEnv)
      this.registerNpmDependencyTable(releasesStream, tEnv)
      //this.registerNpmPersonTable(releasesStream, tEnv)
      this.registerNpmRepositoryTable(releasesStream, tEnv)
      this.registerNpmBugTable(releasesStream, tEnv)
      this.registerNpmTimeTable(releasesStream, tEnv)
    }
   case _: T => print("Have not implemented registering a table for object of type " + typeOf[T].toString)
  }

  def registerNpmProjectTable(stream: DataStream[NpmReleaseExtPojo], tEnv: StreamTableEnvironment): Unit = {
    implicit val typeInfo = TypeInformation.of(classOf[NpmProjectPojo])
    val projectStream: DataStream[NpmProjectPojo] = stream.map(x => x.project)
    tEnv.registerDataStream(npm_projectTableName, projectStream)
  }

  def registerNpmDependencyTable(stream: DataStream[NpmReleaseExtPojo], tEnv: StreamTableEnvironment): Unit = {
    implicit val typeInfo = TypeInformation.of(classOf[DependencyPojoExt])
    val dependencyPojoStream : DataStream[DependencyPojoExt] = stream
      .filter(x => x.project.dependencies != null)
      .flatMap(x => {                         // x is a NpmProject with a possible list of dependencies
        x.project.dependencies.map(y => { // map the dependencies into a separate DependencyTable with FK to project.id
          new DependencyPojoExt(x.project._id, y.packageName, y.version)
        })
      })
    tEnv.registerDataStream(npm_dependencyTableName, dependencyPojoStream)
  }

  // TODO -> create separate datastreams for author/contributors...? since type is different

//  def registerNpmPersonTable(stream: DataStream[NpmReleaseExtPojo], tEnv: StreamTableEnvironment): Unit = {
//    implicit val typeInfo = TypeInformation.of(classOf[PersonObjectPojo])
//    val personPojoExtStream: DataStream[PersonObjectPojoExt] = stream
//      .filter(x => x.project.contributors != null)
//      .flatMap(x => {                         // x is a NpmProject with a possible list of contributors
//        x.project.contributors.map(y => { // map the dependencies into a separate DependencyTable with FK to project.id
//          new PersonObjectPojoExt(x.project._id, y.name, y.email, y.url)
//        })
//      })
//    tEnv.registerDataStream(npm_dependencyTableName, personPojoExtStream)
//  }

  def registerNpmRepositoryTable(stream: DataStream[NpmReleaseExtPojo], tEnv: StreamTableEnvironment): Unit = {
    implicit val typeInfo = TypeInformation.of(classOf[RepositoryPojoExt])
    val repositoryPojoStream : DataStream[RepositoryPojoExt] = stream
      .filter(x => x.project.repository != null)
      .map(x => new RepositoryPojoExt(x.project._id, x.project.repository.`type`, x.project.repository.url, x.project.repository.directory))
  }

  def registerNpmBugTable(stream: DataStream[NpmReleaseExtPojo], tEnv: StreamTableEnvironment): Unit = {
    implicit val typeInfo = TypeInformation.of(classOf[BugPojoExt])
    val bugPojoStream : DataStream[BugPojoExt] = stream
      .filter(x => x.project.bugs != null)
      .map(x => new BugPojoExt(x.project._id, x.project.bugs.email, x.project.bugs.url))
  }

  def registerNpmTimeTable(stream: DataStream[NpmReleaseExtPojo], tEnv: StreamTableEnvironment): Unit = {
    implicit val typeInfo = TypeInformation.of(classOf[TimePojoExt])
    val timePojoStream : DataStream[TimePojoExt] = stream
      .map(x=> new TimePojoExt(x.project._id, x.project.time.created, x.project.time.modified))
  }

}
