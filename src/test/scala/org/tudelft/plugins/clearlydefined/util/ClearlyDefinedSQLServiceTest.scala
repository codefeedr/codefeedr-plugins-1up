package org.tudelft.plugins.clearlydefined.util

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.EnvironmentSettings
import org.apache.flink.table.api.scala.StreamTableEnvironment
import org.apache.flink.types.Row
import org.scalatest.FunSuite
import org.tudelft.plugins.clearlydefined.protocol.Protocol.ClearlyDefinedReleasePojo
import org.tudelft.plugins.clearlydefined.protocol.ProtocolTests
import org.tudelft.plugins.maven.protocol.Protocol.OrganizationPojo

class ClearlyDefinedSQLServiceTest extends FunSuite {

  //Get the required environments
  val env = StreamExecutionEnvironment.getExecutionEnvironment
  val tEnv = StreamTableEnvironment.create(env)

  val release = new ProtocolTests().cdRelease
  val pojo = ClearlyDefinedReleasePojo.fromClearlyDefinedRelease(release)

  implicit val typeInfo = TypeInformation.of(classOf[ClearlyDefinedReleasePojo])
  val stream = env.fromElements(pojo)

  //  added to fix testing
  val streamExt = ClearlyDefinedSQLService.getExtendedStream(stream)

  test("registerDescribedTableTest"){
    ClearlyDefinedSQLService.registerDescribedTable(streamExt, tEnv)
    assert(tEnv.listTables().contains(ClearlyDefinedSQLService.describedTableName))
  }

  test("registerDescribedUrlsTableTest"){
    ClearlyDefinedSQLService.registerDescribedUrlsTable(streamExt, tEnv)
    assert(tEnv.listTables().contains(ClearlyDefinedSQLService.describedUrlsTableName))
  }

  test("registerDescribedHashesTableTest"){
    ClearlyDefinedSQLService.registerDescribedHashesTable(streamExt, tEnv)
    assert(tEnv.listTables().contains(ClearlyDefinedSQLService.describedHashesTableName))
  }

  test("registerDescribedToolScoreTableTest"){
    ClearlyDefinedSQLService.registerDescribedToolScoreTable(streamExt, tEnv)
    assert(tEnv.listTables().contains(ClearlyDefinedSQLService.describedToolScoreTableName))
  }

  test("registerDescribedSourceLocationTableTest"){
    ClearlyDefinedSQLService.registerDescribedSourceLocationTable(streamExt, tEnv)
    assert(tEnv.listTables().contains(ClearlyDefinedSQLService.describedSourceLocationTableName))
  }

  test("registerDescribedScoreTableTest"){
    ClearlyDefinedSQLService.registerDescribedScoreTable(streamExt, tEnv)
    assert(tEnv.listTables().contains(ClearlyDefinedSQLService.describedScoreTableName))
  }

  test("registerLicensedTableTest"){
    ClearlyDefinedSQLService.registerLicensedTable(streamExt, tEnv)
    assert(tEnv.listTables().contains(ClearlyDefinedSQLService.licensedTableName))
  }

  test("registerLicensedToolScoreTableTest"){
    ClearlyDefinedSQLService.registerLicensedToolScoreTable(streamExt, tEnv)
    assert(tEnv.listTables().contains(ClearlyDefinedSQLService.licensedToolScoreTableName))
  }

  test("registerLicensedFacetsTableTest"){
    ClearlyDefinedSQLService.registerLicensedFacetsTable(streamExt, tEnv)
    assert(tEnv.listTables().contains(ClearlyDefinedSQLService.licensedFacetsTableName))
  }

  test("registerLicensedFacetsCDLFCoreTableTest"){
    ClearlyDefinedSQLService.registerLicensedFacetsCDLFCoreTable(streamExt, tEnv)
    assert(tEnv.listTables().contains(ClearlyDefinedSQLService.licensedFacetsCoreTableName))
  }

  test("registerLicensedCDLFCoreAttributionTableTest"){
    ClearlyDefinedSQLService.registerLicensedCDLFCoreAttributionTable(streamExt, tEnv)
    assert(tEnv.listTables().contains(ClearlyDefinedSQLService.licensedFacetsCoreAttributionTableName))
  }

  test("registerLicensedCDLFCoreDiscoveredTableTest"){
    ClearlyDefinedSQLService.registerLicensedCDLFCoreDiscoveredTable(streamExt, tEnv)
    assert(tEnv.listTables().contains(ClearlyDefinedSQLService.licensedFacetsCoreDiscoveredTableName))
  }

  test("registerLicensedScoreTableTest"){
    ClearlyDefinedSQLService.registerLicensedScoreTable(streamExt, tEnv)
    assert(tEnv.listTables().contains(ClearlyDefinedSQLService.licensedScoreTableName))
  }

  test("registerCoordinatesTableTest"){
    ClearlyDefinedSQLService.registerCoordinatesTable(streamExt, tEnv)
    assert(tEnv.listTables().contains(ClearlyDefinedSQLService.coordinatesTableName))
  }

  test("registerMetaTableTest"){
    ClearlyDefinedSQLService.registerMetaTable(streamExt, tEnv)
    assert(tEnv.listTables().contains(ClearlyDefinedSQLService.metaTableName))
  }

  test("registerScoresTableTest"){
    ClearlyDefinedSQLService.registerScoresTable(streamExt, tEnv)
    assert(tEnv.listTables().contains(ClearlyDefinedSQLService.scoresTableName))
  }

  test("registerTablesTest") {
    val tEnv = StreamTableEnvironment.create(env)
    assert(tEnv.listTables().length == 0)
    ClearlyDefinedSQLService.registerTables(stream, tEnv)
    assert(tEnv.listTables().size == 17)

    //The only way to test the inner map functions (which are lazy) is to execute a query
    val queryTable = tEnv.sqlQuery("Select * from " + ClearlyDefinedSQLService.rootTableName)
    implicit val typeInfo = TypeInformation.of(classOf[Row])

    tEnv.toAppendStream(queryTable)(typeInfo).print()
    env.execute()
  }

  test("registerTablesNonRegisteredTest"){
    val tEnv = StreamTableEnvironment.create(env)
    assert(tEnv.listTables().length == 0)
    implicit val typeInfo = TypeInformation.of(classOf[OrganizationPojo])
    ClearlyDefinedSQLService.registerTables(env.fromElements(new OrganizationPojo()), tEnv)
    assert(tEnv.listTables().length == 0)
  }
}
