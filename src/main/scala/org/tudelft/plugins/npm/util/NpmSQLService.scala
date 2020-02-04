package org.tudelft.plugins.npm.util

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor
import org.apache.flink.streaming.api.scala.DataStream
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.table.api.scala._
import org.tudelft.plugins.npm.protocol.Protocol._

import scala.reflect.runtime.universe._ // for TypeInformation

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
  val npm_projectTableName = "NpmProject"
  val npm_dependencyTableName = "NpmDependency"
  // author and contributors need separate tables
  val npm_person_authorTableName = "NpmAuthor"
  val npm_person_contributorsTableName = "NpmContributors"
  val npm_person_maintainersTableName = "NpmMaintainers"
  val npm_repositoryTableName = "NpmRepository"
  val npm_bugTableName = "NpmBug"
  val npm_timeTableName = "NpmTime"
  val npm_keywordsTableName = "NpmKeywords"

  /**
   * Registers the npm case classes as datastream with the Flink Table Environment
   * @param stream the DataStream of type T you want to register
   * @param tEnv the table environent from Flink where you register your datastream
   * @tparam T the type of the stream you want to register
   */
  def registerTables[T: TypeTag](stream: DataStream[T], tEnv: StreamTableEnvironment): Unit = stream match {
    case _ if typeOf[T] <:< typeOf[NpmReleaseExtPojo] => {
      val releasesStream = stream.asInstanceOf[DataStream[NpmReleaseExtPojo]]

      val timestampedStream = releasesStream.assignTimestampsAndWatermarks(
        new BoundedOutOfOrdernessTimestampExtractor[NpmReleaseExtPojo](Time.seconds(10)) {
          override def extractTimestamp(element: NpmReleaseExtPojo): Long = element.retrieveDate
        })

      tEnv.registerDataStream(npm_rootTableName, timestampedStream,
        'name,
        'retrieveDate.rowtime,
        'project)

      this.registerNpmProjectTable(timestampedStream, tEnv)
      this.registerNpmDependencyTable(timestampedStream, tEnv)
      // author & contributors thus twice!
      this.registerNpmPerson_AuthorTable(timestampedStream, tEnv)
      this.registerNpmPerson_ContributorsTable(timestampedStream, tEnv)
      this.registerNpmRepositoryTable(timestampedStream, tEnv)
      this.registerNpmBugTable(timestampedStream, tEnv)
      this.registerNpmTimeTable(timestampedStream, tEnv)
      // maintainers & keywords have to be registered as table as well!!
      this.registerNpmMaintainersTable(timestampedStream, tEnv)
      this.registerNpmKeywordsTable(timestampedStream, tEnv)
    }
   case _: T => print("Have not implemented registering a table for object of type " + typeOf[T].toString)
  }

  /**
   * Register the NpmProject case class as a streaming table in Flink
   * @param stream the stream of NpmReleaseExt used as base to glean the NpmProject from
   * @param tEnv the Flink table environment used for registration
   */
  def registerNpmProjectTable(stream: DataStream[NpmReleaseExtPojo], tEnv: StreamTableEnvironment): Unit = {
    implicit val typeInfo = TypeInformation.of(classOf[NpmProjectPojo])
    val projectStream: DataStream[NpmProjectPojo] = stream.map(x => x.project)
    tEnv.registerDataStream(npm_projectTableName, projectStream)
  }

  /**
   * Register the Npm Dependency case class as a streaming table in Flink
   * @param stream the stream of NpmReleaseExt used as base to glean the Dependency from
   * @param tEnv the Flink table environment used for registration
   */
  def registerNpmDependencyTable(stream: DataStream[NpmReleaseExtPojo], tEnv: StreamTableEnvironment): Unit = {
    implicit val typeInfo = TypeInformation.of(classOf[DependencyPojoExt])
    val dependencyPojoStream : DataStream[DependencyPojoExt] = stream
      .filter(x => x.project.dependencies != null) // x is a NpmProject with a possible list of dependencies
      .flatMap(x => {
        x.project.dependencies.map(y => { // map the dependencies into a separate DependencyTable with FK to project.id
          val pojo_ext = new DependencyPojoExt()
          pojo_ext.id = x.project._id
          pojo_ext.packageName = y.packageName
          pojo_ext.version = y.version
          pojo_ext
        })
      })
    tEnv.registerDataStream(npm_dependencyTableName, dependencyPojoStream)
  }

  /**
   * Register the Npm Person case class from the NpmProject .authorObject as a streaming table in Flink
   * @param stream the stream of NpmReleaseExt used as base to glean the Author from
   * @param tEnv the Flink table environment used for registration
   */
  // PersonObjectPojos are a bit more difficult, since we are using them in both author : PersonObject
  // and contributors : List[PersonObject], so we can't really do away with only one table format for PersonObject
  def registerNpmPerson_AuthorTable(stream : DataStream[NpmReleaseExtPojo], tEnv: StreamTableEnvironment) = {
    implicit val typeInfo = TypeInformation.of(classOf[PersonObjectPojoExt])
    val person_authorPojoStream : DataStream[PersonObjectPojoExt] = stream
      .filter(x => x.project.author != null)
      .map(x => {
        val pojo_ext = new PersonObjectPojoExt()
        pojo_ext.id = x.project._id
        pojo_ext.name = x.project.author.name
        pojo_ext.email = x.project.author.email
        pojo_ext.url = x.project.author.url
        pojo_ext
      })
    tEnv.registerDataStream(npm_person_authorTableName, person_authorPojoStream)
  }

  /**
   * Register the Npm Person case class from the NpmProject .contributors as a streaming table in Flink
   * @param stream the stream of NpmReleaseExt used as base to glean the contributors from
   * @param tEnv the Flink table environment used for registration
   */
  def registerNpmPerson_ContributorsTable(stream : DataStream[NpmReleaseExtPojo], tEnv: StreamTableEnvironment) = {
    implicit val typeInfo = TypeInformation.of(classOf[PersonObjectPojoExt])
    val person_contributorsPojoStream : DataStream[PersonObjectPojoExt] = stream
      .filter(x => x.project.contributors != null)
      .flatMap(x => {
        x.project.contributors.map(y => {
          val pojo_ext = new PersonObjectPojoExt()
          pojo_ext.id = x.project._id
          pojo_ext.name = y.name
          pojo_ext.email = y.email
          pojo_ext.url = y.url
          pojo_ext
        })
      })
    tEnv.registerDataStream(npm_person_contributorsTableName, person_contributorsPojoStream)
  }

    /**
     * Register the Npm keywords of a project as a streaming table in Flink
     * @param stream the stream of NpmReleaseExt used as base to glean the keywords from
     * @param tEnv the Flink table environment used for registration
     */
    def registerNpmKeywordsTable(stream: DataStream[NpmReleaseExtPojo], tEnv: StreamTableEnvironment) = {
      implicit val typeInfo = TypeInformation.of(classOf[NpmKeyWordPojoExt])
      val keywords_pojostream : DataStream[NpmKeyWordPojoExt] = stream
        .filter(x => x.project.keywords != null)
        .flatMap(x => {
          x.project.keywords.map(y => {
          val pojo_ext = new NpmKeyWordPojoExt()
          pojo_ext.id = x.project._id
          pojo_ext.keyword = y.keyword
          pojo_ext
          })
        })
        tEnv.registerDataStream(npm_keywordsTableName, keywords_pojostream)
    }

  /**
   * Register the Npm Repository case class as a streaming table in Flink
   * @param stream the stream of NpmReleaseExt used as base to glean the Repository from
   * @param tEnv the Flink table environment used for registration
   */
  def registerNpmRepositoryTable(stream: DataStream[NpmReleaseExtPojo], tEnv: StreamTableEnvironment): Unit = {
    implicit val typeInfo = TypeInformation.of(classOf[RepositoryPojoExt])
    val repositoryPojoStream : DataStream[RepositoryPojoExt] = stream
      .filter(x => x.project.repository != null)
      .map(x => {
        val pojo_ext = new RepositoryPojoExt()
        pojo_ext.id = x.project._id
        pojo_ext.`type` = x.project.repository.`type`
        pojo_ext.url = x.project.repository.url
        pojo_ext.directory = x.project.repository.directory
        pojo_ext
      })
    tEnv.registerDataStream(npm_repositoryTableName, repositoryPojoStream)
  }

  /**
   * Register the Npm Bug case class as a streaming table in Flink
   * @param stream the stream of NpmReleaseExt used as base to glean the Bug from
   * @param tEnv the Flink table environment used for registration
   */
  def registerNpmBugTable(stream: DataStream[NpmReleaseExtPojo], tEnv: StreamTableEnvironment): Unit = {
    implicit val typeInfo = TypeInformation.of(classOf[BugPojoExt])
    val bugPojoStream : DataStream[BugPojoExt] = stream
      .filter(x => x.project.bugs != null)
      .map(x => {
        val pojo_ext = new BugPojoExt()
        pojo_ext.id = x.project._id
        pojo_ext.email = x.project.bugs.email
        pojo_ext.url = x.project.bugs.url
        pojo_ext
      })
    tEnv.registerDataStream(npm_bugTableName, bugPojoStream)
  }

  /**
   * Register the Npm Time Object case class as a streaming table in Flink
   * @param stream the stream of NpmReleaseExt used as base to glean the Time Object from
   * @param tEnv the Flink table environment used for registration
   */
  def registerNpmTimeTable(stream: DataStream[NpmReleaseExtPojo], tEnv: StreamTableEnvironment): Unit = {
    implicit val typeInfo = TypeInformation.of(classOf[TimePojoExt])
    val timePojoStream : DataStream[TimePojoExt] = stream
      .map(x => {
        val pojo_ext = new TimePojoExt()
        pojo_ext.id =  x.project._id
        pojo_ext.created = x.project.time.created
        pojo_ext.modified = x.project.time.modified
        pojo_ext
      })
    tEnv.registerDataStream(npm_timeTableName, timePojoStream)
  }

  /**
   * Register the Npm Maintainers Person Object case class as a streaming table in Flink
   * @param stream the stream of NpmReleaseExt used as base to glean the maintainers as Person Object from
   * @param tEnv the Flink table environment used for registration
   */
  def registerNpmMaintainersTable(stream: DataStream[NpmReleaseExtPojo], tEnv: StreamTableEnvironment) = {
    implicit val typeInfo = TypeInformation.of(classOf[PersonObjectPojoExt])
    val person_maintainersPojoStream : DataStream[PersonObjectPojoExt] = stream
      .flatMap(x => {
        x.project.contributors.map(y => {
          val pojo_ext = new PersonObjectPojoExt()
          pojo_ext.id = x.project._id
          pojo_ext.name = y.name
          pojo_ext.email = y.email
          pojo_ext.url = y.url
          pojo_ext
        })
      })
    tEnv.registerDataStream(npm_person_maintainersTableName, person_maintainersPojoStream)
  }

}
