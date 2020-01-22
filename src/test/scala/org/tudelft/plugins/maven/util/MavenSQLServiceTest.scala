package org.tudelft.plugins.maven.util

import java.util.Date

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.scala.StreamTableEnvironment
import org.apache.flink.types.Row
import org.scalatest.funsuite.AnyFunSuite
import org.tudelft.plugins.maven.protocol.Protocol.{Guid, MavenReleaseExt, MavenReleaseExtPojo, OrganizationPojo}
import org.tudelft.plugins.maven.protocol.ProtocolTest

class MavenSQLServiceTest extends AnyFunSuite {
  //Get the required environments
  val env = StreamExecutionEnvironment.getExecutionEnvironment

  val tEnv = StreamTableEnvironment.create(env)

  implicit val typeInfo = TypeInformation.of(classOf[MavenReleaseExtPojo])

  val mavenReleaseExt = MavenReleaseExt("title", "link", "description", new Date(0), Guid("tag"), new ProtocolTest().projectFull)
  val pojo =  MavenReleaseExtPojo.fromMavenReleaseExt(mavenReleaseExt)

  val stream = env.fromElements(pojo)

  test("registerProjectTableTest"){
    MavenSQLService.registerProjectTable(stream, tEnv)
    assert(tEnv.listTables().contains(MavenSQLService.projectTableName))
  }

  test("registerParentTableTest"){
    MavenSQLService.registerParentTable(stream, tEnv)
    assert(tEnv.listTables().contains(MavenSQLService.projectParentTableName))
  }

  test("registerOrganizationTableTest"){
    MavenSQLService.registerOrganizationTable(stream, tEnv)
    assert(tEnv.listTables().contains(MavenSQLService.projectOrganizationTableName))
  }

  test("registerIssueManagementTableTest"){
    MavenSQLService.registerIssueManagementTable(stream, tEnv)
    assert(tEnv.listTables().contains(MavenSQLService.projectIssueManagementTableName))
  }

  test("registerSCMTableTest"){
    MavenSQLService.registerSCMTable(stream, tEnv)
    assert(tEnv.listTables().contains(MavenSQLService.projectSCMTableName))
  }

  test("registerDependenciesTableTest"){
    MavenSQLService.registerDependenciesTable(stream, tEnv)
    assert(tEnv.listTables().contains(MavenSQLService.projectDependenciesTableName))
  }

  test("registerLicensesTableTest"){
    MavenSQLService.registerLicensesTable(stream, tEnv)
    assert(tEnv.listTables().contains(MavenSQLService.projectLicensesTableName))
  }

  test("registerRepositoriesTableTest"){
    MavenSQLService.registerRepositoriesTable(stream, tEnv)
    assert(tEnv.listTables().contains(MavenSQLService.projectRepositoriesTableName))
  }

  test("registerTablesTest"){
    val tEnv = StreamTableEnvironment.create(env)
    assert(tEnv.listTables().length == 0)
    MavenSQLService.registerTables(stream, tEnv)
    assert(tEnv.listTables().length == 9)

    //The only way to test the inner map functions (which are lazy) is to execute a query
    val queryTable = tEnv.sqlQuery("Select * from " + MavenSQLService.rootTableName)
    implicit val typeInfo = TypeInformation.of(classOf[Row])

    tEnv.toAppendStream(queryTable)(typeInfo).print()
    env.execute()
  }

  test("registerTablesNonRegisteredTest"){
    val tEnv = StreamTableEnvironment.create(env)
    assert(tEnv.listTables().length == 0)
    implicit val typeInfo = TypeInformation.of(classOf[OrganizationPojo])
    MavenSQLService.registerTables(env.fromElements(new OrganizationPojo()), tEnv)
    assert(tEnv.listTables().length == 0)
  }
}
