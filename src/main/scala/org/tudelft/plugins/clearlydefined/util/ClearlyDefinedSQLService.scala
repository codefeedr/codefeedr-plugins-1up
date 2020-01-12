package org.tudelft.plugins.clearlydefined.util
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.scala.DataStream
import org.apache.flink.table.api.scala.StreamTableEnvironment
import org.tudelft.plugins.clearlydefined.protocol.Protocol._

import scala.reflect.runtime.universe._

object ClearlyDefinedSQLService {

  val rootTableName: String = "ClearlyDefined"

  val describedTableName: String = "ClearlyDefinedDescribed"
  val describedUrlsTableName: String = "ClearlyDefinedDescribedUrls"
  val describedHashesTableName: String = "ClearlyDefinedDescribedHashes"
  val describedToolScoreTableName: String = "ClearlyDefinedDescribedToolScore"
  val describedSourceLocationTableName: String = "ClearlyDefinedDescribedSourceLocation"
  val describedScoreTableName: String = "ClearlyDefinedDescribedScore"

  val licensedTableName: String = "ClearlyDefinedLicensed"
  val licensedToolScoreTableName: String = "ClearlyDefinedLicensedToolScore"
  val licensedFacetsTableName: String = "ClearlyDefinedLicensedFacets"
  val licensedFacetsCDLFCoreTableName: String = "ClearlyDefinedFacetsCDLFCore"
  val licensedFacetsCDLFCoreAttributionTableName: String = "ClearlyDefinedFacetsCDLFCoreAttribution"
  val licensedFacetsCDLFCoreDiscoveredTableName: String = "ClearlyDefinedFacetsCDLFCoreDiscovered"
  val licensedScoreTableName: String = "ClearlyDefinedLicensedScore"

  val coordinatesTableName: String = "ClearlyDefinedCoordinates"
  val metaTableName: String = "ClearlyDefinedMeta"
  val scoresTableName: String = "ClearlyDefinedScores"

  def registerTables[T: TypeTag](stream: DataStream[T], tEnv: StreamTableEnvironment): Unit = stream match {
    case _ if typeOf[T] <:< typeOf[ClearlyDefinedReleasePojo] => {
      val releasesStream = stream.asInstanceOf[DataStream[ClearlyDefinedReleasePojo]]
      tEnv.registerDataStream(rootTableName, releasesStream)

      //Register all other tables
      registerDescribedTable(releasesStream, tEnv)
      registerDescribedUrlsTable(releasesStream, tEnv)
      registerDescribedHashesTable(releasesStream, tEnv)
      registerDescribedToolScoreTable(releasesStream, tEnv)
      registerDescribedSourceLocationTable(releasesStream, tEnv)
      registerDescribedScoreTable(releasesStream, tEnv)
      registerLicensedTable(releasesStream, tEnv)
      registerLicensedToolScoreTable(releasesStream, tEnv)
      registerLicensedFacetsTable(releasesStream, tEnv)
      registerLicensedFacetsCDLFCoreTable(releasesStream, tEnv)
      registerLicensedCDLFCoreAttributionTable(releasesStream, tEnv)
      registerLicensedCDLFCoreDiscoveredTable(releasesStream, tEnv)
      registerLicensedScoreTable(releasesStream, tEnv)
      registerCoordinatesTable(releasesStream, tEnv)
      registerMetaTable(releasesStream, tEnv)
      registerScoresTable(releasesStream, tEnv)
    }
    case _: T => print("Have not implemented registering a table for object of type " + typeOf[T].toString)
  }

  def registerDescribedTable(stream: DataStream[ClearlyDefinedReleasePojo], tEnv: StreamTableEnvironment): Unit = {
    implicit val typeInfo = TypeInformation.of(classOf[CDDescribedPojo])
    val describedStream = stream.map(x => x.described)
    tEnv.registerDataStream(describedTableName, describedStream)
  }

  def registerDescribedUrlsTable(stream: DataStream[ClearlyDefinedReleasePojo], tEnv: StreamTableEnvironment): Unit = {
    implicit val typeInfo = TypeInformation.of(classOf[CDDescribedUrlsPojo])
    val describedUrlsStream = stream.map(x => x.described.urls)
    tEnv.registerDataStream(describedUrlsTableName, describedUrlsStream)
  }

  def registerDescribedHashesTable(stream: DataStream[ClearlyDefinedReleasePojo], tEnv: StreamTableEnvironment): Unit = {
    implicit val typeInfo = TypeInformation.of(classOf[CDDescribedHashesPojo])
    val describedHashesStream = stream.map(x => x.described.hashes)
    tEnv.registerDataStream(describedHashesTableName, describedHashesStream)
  }

  def registerDescribedToolScoreTable(stream: DataStream[ClearlyDefinedReleasePojo], tEnv: StreamTableEnvironment): Unit = {
    implicit val typeInfo = TypeInformation.of(classOf[CDDescribedToolScorePojo])
    val describedToolScoreStream = stream.map(x => x.described.toolScore)
    tEnv.registerDataStream(describedToolScoreTableName, describedToolScoreStream)
  }

  def registerDescribedSourceLocationTable(stream: DataStream[ClearlyDefinedReleasePojo], tEnv: StreamTableEnvironment): Unit = {
    implicit val typeInfo = TypeInformation.of(classOf[CDDescribedSourceLocationPojo])
    val describedSourceLocationStream = stream.map(x => x.described.sourceLocation)
    tEnv.registerDataStream(describedSourceLocationTableName, describedSourceLocationStream)
  }

  def registerDescribedScoreTable(stream: DataStream[ClearlyDefinedReleasePojo], tEnv: StreamTableEnvironment): Unit = {
    implicit val typeInfo = TypeInformation.of(classOf[CDDescribedScorePojo])
    val describedScoreStream = stream.map(x => x.described.score)
    tEnv.registerDataStream(describedScoreTableName, describedScoreStream)
  }

  def registerLicensedTable(stream: DataStream[ClearlyDefinedReleasePojo], tEnv: StreamTableEnvironment): Unit = {
    implicit val typeInfo = TypeInformation.of(classOf[CDLicensedPojo])
    val licensedStream = stream.map(x => x.licensed)
    tEnv.registerDataStream(licensedTableName, licensedStream)
  }

  def registerLicensedToolScoreTable(stream: DataStream[ClearlyDefinedReleasePojo], tEnv: StreamTableEnvironment): Unit = {
    implicit val typeInfo = TypeInformation.of(classOf[CDLicensedToolScorePojo])
    val licensedToolScoreStream = stream.map(x => x.licensed.toolScore)
    tEnv.registerDataStream(licensedToolScoreTableName, licensedToolScoreStream)
  }

  def registerLicensedFacetsTable(stream: DataStream[ClearlyDefinedReleasePojo], tEnv: StreamTableEnvironment): Unit = {
    implicit val typeInfo = TypeInformation.of(classOf[CDLicensedFacetsPojo])
    val licensedFacetsStream = stream.map(x => x.licensed.facets)
    tEnv.registerDataStream(licensedFacetsTableName, licensedFacetsStream)
  }

  def registerLicensedFacetsCDLFCoreTable(stream: DataStream[ClearlyDefinedReleasePojo], tEnv: StreamTableEnvironment): Unit = {
    implicit val typeInfo = TypeInformation.of(classOf[CDLFCorePojo])
    val licensedFacetsCDLFCoreStream = stream.map(x => x.licensed.facets.core)
    tEnv.registerDataStream(licensedFacetsCDLFCoreTableName, licensedFacetsCDLFCoreStream)
  }

  def registerLicensedCDLFCoreAttributionTable(stream: DataStream[ClearlyDefinedReleasePojo], tEnv: StreamTableEnvironment): Unit = {
    implicit val typeInfo = TypeInformation.of(classOf[CDLFCoreAttributionPojo])
    val licensedCDLFCoreAttributionStream = stream.map(x => x.licensed.facets.core.attribution)
    tEnv.registerDataStream(licensedFacetsCDLFCoreAttributionTableName, licensedCDLFCoreAttributionStream)
  }

  def registerLicensedCDLFCoreDiscoveredTable(stream: DataStream[ClearlyDefinedReleasePojo], tEnv: StreamTableEnvironment): Unit = {
    implicit val typeInfo = TypeInformation.of(classOf[CDLFCoreDiscoveredPojo])
    val licensedCDLFCoreDiscoveredStream = stream.map(x => x.licensed.facets.core.discovered)
    tEnv.registerDataStream(licensedFacetsCDLFCoreDiscoveredTableName, licensedCDLFCoreDiscoveredStream)
  }

  def registerLicensedScoreTable(stream: DataStream[ClearlyDefinedReleasePojo], tEnv: StreamTableEnvironment): Unit = {
    implicit val typeInfo = TypeInformation.of(classOf[CDLicensedScorePojo])
    val licensedScoreStream = stream.map(x => x.licensed.score)
    tEnv.registerDataStream(licensedScoreTableName, licensedScoreStream)
  }

  def registerCoordinatesTable(stream: DataStream[ClearlyDefinedReleasePojo], tEnv: StreamTableEnvironment): Unit = {
    implicit val typeInfo = TypeInformation.of(classOf[CDCoordinatesPojo])
    val coordinatesStream = stream.map(x => x.coordinates)
    tEnv.registerDataStream(coordinatesTableName, coordinatesStream)
  }

  def registerMetaTable(stream: DataStream[ClearlyDefinedReleasePojo], tEnv: StreamTableEnvironment): Unit = {
    implicit val typeInfo = TypeInformation.of(classOf[CD_metaPojo])
    val metaStream = stream.map(x => x._meta)
    tEnv.registerDataStream(metaTableName, metaStream)
  }

  def registerScoresTable(stream: DataStream[ClearlyDefinedReleasePojo], tEnv: StreamTableEnvironment): Unit = {
    implicit val typeInfo = TypeInformation.of(classOf[CDScoresPojo])
    val scoresStream = stream.map(x => x.scores)
    tEnv.registerDataStream(scoresTableName, scoresStream)
  }

}