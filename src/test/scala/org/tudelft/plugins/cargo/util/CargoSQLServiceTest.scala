package org.tudelft.plugins.cargo.util

import java.util.Date

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.EnvironmentSettings
import org.apache.flink.table.api.scala.StreamTableEnvironment
import org.apache.flink.types.Row
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.tudelft.plugins.cargo.protocol.Protocol._
import org.tudelft.plugins.cargo.protocol.ProtocolTests

class CargoSQLServiceTest extends FunSuite with BeforeAndAfter {

  //Get the required environments
  val env = StreamExecutionEnvironment.getExecutionEnvironment

  val tEnv = StreamTableEnvironment.create(env)

  implicit val typeInfo = TypeInformation.of(classOf[CrateReleasePojo])

  val pojo = CrateReleasePojo.fromCrateRelease(new ProtocolTests().crateRelease)

  val stream = env.fromElements(pojo)


  test("registerCrateTableTest") {
    CargoSQLService.registerCrateTable(stream, tEnv)
    assert(tEnv.listTables().contains(CargoSQLService.crateTableName))
  }

  test("registerCrateLinksTableTest") {
    CargoSQLService.registerCrateLinksTable(stream, tEnv)
    assert(tEnv.listTables().contains(CargoSQLService.crateLinksTableName))
  }

  test("registerCrateVersionTableTest") {
    CargoSQLService.registerCrateVersionTable(stream, tEnv)
    assert(tEnv.listTables().contains(CargoSQLService.crateVersionsTableName))
  }

  test("registerCrateVersionFeaturesTableTest") {
    CargoSQLService.registerCrateVersionFeaturesTable(stream, tEnv)
    assert(tEnv.listTables().contains(CargoSQLService.crateVersionFeaturesTableName))
  }

  test("registerCrateVersionLinksTableTest") {
    CargoSQLService.registerCrateVersionLinksTable(stream, tEnv)
    assert(tEnv.listTables().contains(CargoSQLService.crateVersionLinksTableName))
  }

  test("registerCrateVersionPublishedByTableTest") {
    CargoSQLService.registerCrateVersionPublishedByTable(stream, tEnv)
    assert(tEnv.listTables().contains(CargoSQLService.crateVersionPublishedByTableName))
  }

  test("registerCrateKeywordsTableTest") {
    CargoSQLService.registerCrateKeywordsTable(stream, tEnv)
    assert(tEnv.listTables().contains(CargoSQLService.crateKeywordsTableName))
  }

  test("registerCrateCategoriesTable") {
    CargoSQLService.registerCrateCategoriesTable(stream, tEnv)
    assert(tEnv.listTables().contains(CargoSQLService.crateCategoriesTableName))
  }

  test("registerTablesTest") {
    val tEnv = StreamTableEnvironment.create(env)
    assert(tEnv.listTables().size == 0)
    CargoSQLService.registerTables(stream, tEnv)
    assert(tEnv.listTables().size == 9)

    //The only way to test the inner map functions (which are lazy) is to execute a query
    val queryTable = tEnv.sqlQuery("Select * from " + CargoSQLService.rootTableName)
    implicit val typeInfo = TypeInformation.of(classOf[Row])

    tEnv.toAppendStream(queryTable)(typeInfo).print()
    env.execute()
  }

  test("registerTablesNonRegisteredTest"){
    val tEnv = StreamTableEnvironment.create(env)
    assert(tEnv.listTables().size == 0)
    implicit val typeInfo = TypeInformation.of(classOf[CrateKeywordPojo])
    CargoSQLService.registerTables(env.fromElements(new CrateKeywordPojo), tEnv)
    assert(tEnv.listTables().size == 0)
  }
}
