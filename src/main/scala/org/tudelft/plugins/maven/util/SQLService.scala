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

    //      tEnv.registerDataStream("MavenTest", in)

    println(tEnv.explain(tEnv.fromDataStream(in)))

    val tables: Array[String] = tEnv.listTables()

    //    tEnv.fromDataStream(in)

    println(tEnv.fromDataStream(in).getSchema())

    val query = "SELECT * FROM MavenProjectPojo"
    //Perform query
    val res: Table = tEnv.sqlQuery(query)
    println(tEnv.explain(res))

    // Just for printing purposes, in reality you would need something other than Row
    implicit val typeInfo = TypeInformation.of(classOf[Row])

    tEnv.toAppendStream(res)(typeInfo).print()

    env.execute()
  }

  var projectPojoTable: Table = _

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

        tEnv.registerDataStream("Maven", dataStream = pojos)

        registerSubtables(pojos, tEnv)

        println("he")
      }

      // Other plugins

      //TODO add all other types here
      case _ => throw new IllegalArgumentException("stream of unsupported type")
    }
  }

  def registerSubtables[T: TypeTag](field: DataStream[T], tEnv: StreamTableEnvironment): Unit = field match {
    case _ if typeOf[T] <:< typeOf[MavenReleaseExtPojo] => {
      val projectTableName = "MavenProjectPojo"
      val projectPojoStream: DataStream[MavenProjectPojo] = field
        .getSideOutput(OutputTag[MavenProjectPojo](projectTableName))

      tEnv.registerDataStream(projectTableName, field.map(x => x.asInstanceOf[MavenReleaseExtPojo].project))
//      projectPojoTable = tEnv.fromDataStream(projectPojoStream)
//      println(tEnv.explain(projectPojoTable))
//      println(projectPojoTable.getSchema)

      val organizationTableName = "MavenProjectOrganizationPojo"
      val organizationPojoStream: DataStream[OrganizationPojo] = field
        .getSideOutput(OutputTag[OrganizationPojo](organizationTableName))
      tEnv.registerDataStream(organizationTableName, organizationPojoStream)

      // Activate table
//      val table: Table = tEnv.fromDataStream(projectPojoStream)
//      println(tEnv.explain(table))
//      println(table.getSchema())
//      implicit val typeInfo = TypeInformation.of(classOf[Row])
//      tEnv.toAppendStream(table)(typeInfo).print()


    }
//    case _ if typeOf[T] <:< typeOf[MavenProjectPojo] => {
//      val organizationTableName = "MavenProjectOrganization"
//      val organizationPojoStream: DataStream[OrganizationPojo] = field
//        .getSideOutput(OutputTag[OrganizationPojo](organizationTableName))
//      tEnv.registerDataStream(organizationTableName, organizationPojoStream)

//      val issuemanagementPojo: DataStream[IssueManagementPojo] = mavenProjectPojoStream.map(x => x.issueManagement)
//      registerSubtables(issuemanagementPojo, tEnv)
//
//      val scmPojo: DataStream[SCMPojo] = mavenProjectPojoStream.map(x => x.scm)
//      registerSubtables(scmPojo, tEnv)
//
//      val dependenciesPojo: DataStream[List[DependencyPojo]] = mavenProjectPojoStream.map(x => x.dependencies)
//      registerSubtables(dependenciesPojo, tEnv)
    case _ if typeOf[T] <:< typeOf[OrganizationPojo] => {
      tEnv.registerDataStream("MavenProjectOrganization", field)
    }
    case _ if typeOf[T] <:< typeOf[IssueManagementPojo] => {
      tEnv.registerDataStream("MavenProjectIssueManagement", field)
    }
    case _ if typeOf[T] <:< typeOf[SCMPojo] => {
      tEnv.registerDataStream("MavenProjectSCM", field)
    }
    case _ if typeOf[T] <:< typeOf[List[DependencyPojo]] => {
      tEnv.registerDataStream("MavenProjectDependencies", field)
    }

    case _: T => print("Have not implemented registering a table for object of type " + typeOf[T].toString)
  }

//  def fixPojos[T: TypeTag](pojos: DataStream[T]): DataStream[T] = {
//    val pojoTypeInfo = pojos.javaStream.getTransformation.getOutputType.asInstanceOf[PojoTypeInfo[T]]
//    val fieldAmount = pojoTypeInfo.getTotalFields
//    for(w <- 0 to fieldAmount) {
//      var pojoField = pojoTypeInfo.getPojoFieldAt(w).getField
//      pojoField match {
//        case x if typeOf[x.type] <:< typeOf[Option[String]] => {
//          //pojoTypeInfo.getPojoFieldAt(w).getField = x.asInstanceOf[Option[String]].get
//        }
//      }
//    }
//    pojos
//  }

//  val dependencies: DataStream[List[DependencyPojo]] = pojos.map(x => {
//    x.project.dependencies
//  })
//
//  val dependencyTable = CsvTableSource.builder()
//    .fieldDelimiter("|")
//    .field("groupId", Types.STRING)
//    .field("artifactId", Types.STRING)
//    .field("version", Types.STRING)
//    .field("type", Types.STRING)
//    .field("scope", Types.STRING)
//    .field("optional", Types.BOOLEAN)
//    .build()
//
//  val table: Table = tEnv
//    .scan("Maven")
//    .filter('project.->('dependencies.isNotNull))

}
