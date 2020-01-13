package org.tudelft.plugins.cargo.util

import java.util.Date

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.EnvironmentSettings
import org.apache.flink.table.api.scala.StreamTableEnvironment
import org.apache.flink.types.Row
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.tudelft.plugins.cargo.protocol.Protocol._

class CargoSQLServiceTest extends FunSuite with BeforeAndAfter {

  //Get the required environments
  val env = StreamExecutionEnvironment.getExecutionEnvironment
  val settings = EnvironmentSettings.newInstance()
    .useOldPlanner()
    .inStreamingMode()
    .build()

  val tEnv = StreamTableEnvironment.create(env)

  implicit val typeInfo = TypeInformation.of(classOf[CrateReleasePojo])
  //TODO copy pasta from ProtocolTest, needs refactoring
  val crateVersionFeatures = CrateVersionFeatures()
  val crateVersionLinks = CrateVersionLinks("dependencies", "version_downloads", "authors")
  val crateVersionPublishedBy = CrateVersionPublishedBy(1, "login", Some("name"), "avatar", "url")
  val crateVersion = CrateVersion(1, "crate", "num", "dl_path", "readme_path",
    new Date(), new Date(), 100, crateVersionFeatures, true, "license", crateVersionLinks,
    Some(20), Some(crateVersionPublishedBy))
  val crateKeyword = CrateKeyword("id", "keyword", "created_at", 1)
  val crateCategory = CrateCategory("id", "category", "slug", "description", "created_at", 1)
  val crateLinks = CrateLinks("version_downloads", Some("versions"), "owners",
    "owner_team", "owner_user", "reverse_dependencies")
  val crate = Crate("id", "name", new Date(), List(1, 2), List("keyword1", "keyword2"),
    List("category1", "category2"), new Date(), 100, Some(4), "max_version",
    "description", Some("homepage"), Some("documentation"), Some("repository"), crateLinks, true)

  val crateRelease = CrateRelease(crate, List(crateVersion, crateVersion), List(crateKeyword, crateKeyword),
    List(crateCategory, crateCategory))
  val pojo = CrateReleasePojo.fromCrateRelease(crateRelease)

  val stream = env.fromElements(pojo)


  test("registerCrateTableTest") {
    CargoSQLService.registerCrateTable(stream, tEnv)
    assert(tEnv.listTables().contains(CargoSQLService.crateTableName))
  }

  test("registerCrateLinksTableTest") {
    CargoSQLService.registerCrateLinksTable(stream, tEnv)
    assert(tEnv.listTables().contains(CargoSQLService.crateLinksTableName))
    //    val queryTable = tEnv.sqlQuery("Select * from " + CargoSQLService.crateLinksTableName)
    //    implicit val typeInfo = TypeInformation.of(classOf[Row])
    //
    //    tEnv.toAppendStream(queryTable)(typeInfo).print()
    //    env.execute()
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
    implicit val typeInfo = TypeInformation.of(classOf[CrateVersion])
    CargoSQLService.registerTables(env.fromElements(crateVersion), tEnv)
    assert(tEnv.listTables().size == 0)
  }
}
