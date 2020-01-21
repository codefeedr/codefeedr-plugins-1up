package org.tudelft.plugins.npm.util

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.EnvironmentSettings
import org.apache.flink.table.api.scala.StreamTableEnvironment
import org.apache.flink.types.Row
import org.scalatest.funsuite.AnyFunSuite
import org.tudelft.plugins.npm.protocol.Protocol.{BugPojo, NpmReleaseExtPojo}
import org.tudelft.plugins.npm.protocol.ProtocolTest

class NpmSQLServiceTest extends AnyFunSuite {

  val env = StreamExecutionEnvironment.getExecutionEnvironment
  val settings = EnvironmentSettings.newInstance()
    .useOldPlanner()
    .inStreamingMode()
    .build()

  val tEnv = StreamTableEnvironment.create(env)

  implicit val typeInfo = TypeInformation.of(classOf[NpmReleaseExtPojo])

  val npmReleaseExt = new ProtocolTest().npmrele
  val pojo = NpmReleaseExtPojo.fromNpmReleaseExt(npmReleaseExt)

  val stream = env.fromElements(pojo)

  test("registerNpmProjectTableTest"){
    NpmSQLService.registerNpmProjectTable(stream, tEnv)
    assert(tEnv.listTables().contains(NpmSQLService.npm_projectTableName))
  }

  test("registerNpmDependencyTableTest"){
    NpmSQLService.registerNpmDependencyTable(stream, tEnv)
    assert(tEnv.listTables().contains(NpmSQLService.npm_dependencyTableName))
  }

  test("registerNpmPerson_AuthorTable"){
    NpmSQLService.registerNpmPerson_AuthorTable(stream, tEnv)
    assert(tEnv.listTables().contains(NpmSQLService.npm_person_authorTableName))
  }

  test("registerNpmPerson_ContributorsTable"){
    NpmSQLService.registerNpmPerson_ContributorsTable(stream, tEnv)
    assert(tEnv.listTables().contains(NpmSQLService.npm_person_contributorsTableName))
  }

  test("registerNpmRepositoryTable"){
    NpmSQLService.registerNpmRepositoryTable(stream, tEnv)
    assert(tEnv.listTables().contains(NpmSQLService.npm_repositoryTableName))
  }

  test("registerNpmBugTableTest"){
    NpmSQLService.registerNpmBugTable(stream, tEnv)
    assert(tEnv.listTables().contains(NpmSQLService.npm_bugTableName))
  }

  test("registerNpmTimeTableTest"){
    NpmSQLService.registerNpmTimeTable(stream, tEnv)
    assert(tEnv.listTables().contains(NpmSQLService.npm_timeTableName))
  }

  test("registerNpmMaintainersTableTest"){
    NpmSQLService.registerNpmMaintainersTable(stream, tEnv)
    assert(tEnv.listTables().contains(NpmSQLService.npm_person_maintainersTableName))
  }

  test("registerNpmKeywordsTableTest"){
    NpmSQLService.registerNpmKeywordsTable(stream, tEnv)
    assert(tEnv.listTables().contains(NpmSQLService.npm_keywordsTableName))
  }

  test("registerTablesTest"){
    val tEnv = StreamTableEnvironment.create(env)
    assert(tEnv.listTables().length == 0)
    NpmSQLService.registerTables(stream, tEnv)
    assert(tEnv.listTables().length == 10)


    //The only way to test the inner map functions (which are lazy) is to execute a query
    val queryTable = tEnv.sqlQuery("Select * from " + NpmSQLService.npm_rootTableName)
    implicit val typeInfo = TypeInformation.of(classOf[Row])

    tEnv.toAppendStream(queryTable)(typeInfo).print()
    env.execute()
  }

  test("registerTablesNonRegisteredTest"){
    val tEnv = StreamTableEnvironment.create(env)
    assert(tEnv.listTables().length == 0)
    implicit val typeInfo = TypeInformation.of(classOf[BugPojo])
    NpmSQLService.registerTables(env.fromElements(new BugPojo()), tEnv)
    assert(tEnv.listTables().length == 0)
  }

}
