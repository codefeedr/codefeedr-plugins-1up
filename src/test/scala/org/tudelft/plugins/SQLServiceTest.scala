package org.tudelft.plugins

import java.util.Date

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.scala.StreamTableEnvironment
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funsuite.AnyFunSuite
import org.tudelft.plugins.cargo.protocol.Protocol.CrateRelease
import org.tudelft.plugins.cargo.protocol.ProtocolTests
import org.tudelft.plugins.cargo.util.CargoSQLService
import org.tudelft.plugins.clearlydefined.protocol.Protocol.ClearlyDefinedRelease
import org.tudelft.plugins.clearlydefined.util.ClearlyDefinedSQLService
import org.tudelft.plugins.maven.protocol.Protocol.{Guid, MavenRelease, MavenReleaseExt, OrganizationPojo}
import org.tudelft.plugins.maven.util.MavenSQLService
import org.tudelft.plugins.npm.protocol.Protocol.NpmReleaseExt
import org.tudelft.plugins.npm.util.NpmSQLService

class SQLServiceTest extends AnyFunSuite with BeforeAndAfterEach {

  //Get the required environments
  val env = StreamExecutionEnvironment.getExecutionEnvironment
  env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
  var tEnv: StreamTableEnvironment = _

  /**
    * Before each test, create a new (empty) table environment
    */
  override def beforeEach() {
    tEnv = StreamTableEnvironment.create(env)
  }

  test("registerTableFromStreamMavenRelease") {
    val element = MavenRelease("title", "link", "desc", new Date(), Guid("tag"))
    implicit val typeInfo = TypeInformation.of(classOf[MavenRelease])
    val stream = env.fromElements(element)
    SQLService.registerTableFromStream(stream, tEnv)
    assert(tEnv.listTables().contains("Maven"))
    assert(tEnv.listTables().length == 1)
  }

  test("registerTableFromStreamMavenReleaseExt") {
    val element = MavenReleaseExt("title", "link", "desc", new Date(), Guid("tag"),
      new org.tudelft.plugins.maven.protocol.ProtocolTest().projectFull)

    implicit val typeInfo = TypeInformation.of(classOf[MavenReleaseExt])
    val stream = env.fromElements(element)
    SQLService.registerTableFromStream(stream, tEnv)
    assert(tEnv.listTables().contains(MavenSQLService.rootTableName))
    assert(tEnv.listTables().length == 9)
  }

  test("registerTableFromStreamCreateRelease") {
    val element = new ProtocolTests().crateRelease
    implicit val typeInformation = TypeInformation.of(classOf[CrateRelease])
    val stream = env.fromElements(element)
    SQLService.registerTableFromStream(stream, tEnv)
    assert(tEnv.listTables().contains(CargoSQLService.rootTableName))
    assert(tEnv.listTables().length == 9)
  }

  test("registerTableFromStreamNpm") {
    val element = new org.tudelft.plugins.npm.protocol.ProtocolTest().npmrele
    implicit val typeInfo = TypeInformation.of(classOf[NpmReleaseExt])
    val stream = env.fromElements(element)
    SQLService.registerTableFromStream(stream, tEnv)
    assert(tEnv.listTables().contains(NpmSQLService.npm_rootTableName))
    assert(tEnv.listTables().length == 10)
  }

  test("registerTableFromStreamClearlyDefined") {
    val element = new org.tudelft.plugins.clearlydefined.protocol.ProtocolTests().cdRelease
    implicit val typeInfo = TypeInformation.of(classOf[ClearlyDefinedRelease])
    val stream = env.fromElements(element)
    SQLService.registerTableFromStream(stream, tEnv)
    assert(tEnv.listTables().contains(ClearlyDefinedSQLService.rootTableName))
    assert(tEnv.listTables().length == 18)
  }

  test("registerTableFromStreamFailTest"){
    val element = new OrganizationPojo()
    implicit val typeInfo = TypeInformation.of(classOf[OrganizationPojo])
    val stream = env.fromElements(element)
    assertThrows[IllegalArgumentException]{
      SQLService.registerTableFromStream(stream, tEnv)
    }
  }

  test("setupEnvTest"){
    val tEnv = SQLService.setupEnv()
    assert(tEnv.isInstanceOf[StreamTableEnvironment])
  }



}
