package org.tudelft.plugins.clearlydefined.util
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.scala.DataStream
import org.apache.flink.table.api.scala.StreamTableEnvironment
import org.tudelft.plugins.clearlydefined.protocol.Protocol._

import scala.reflect.runtime.universe._

object ClearlyDefinedSQLService {

  def getExtendedStream(releaseStream : DataStream[ClearlyDefinedReleasePojo]) = {
    implicit val typeInfo = TypeInformation.of(classOf[ClearlyDefinedReleasePojoExt])
    val extendedReleasesStream = releaseStream.map(x => new ClearlyDefinedReleasePojoExt() {
    described = x.described
    licensed = x.licensed
    coordinates = x.coordinates
    _meta = x._meta
    scores = x.scores
    name = x.coordinates.name
    })
    extendedReleasesStream
  }


  val rootTableName: String = "ClearlyDefined"

  val describedTableName: String = "ClearlyDefinedDescribed"
  val describedUrlsTableName: String = "ClearlyDefinedDescribedUrls"
  val describedHashesTableName: String = "ClearlyDefinedDescribedHashes"
  val describedToolsTableName: String = "ClearlyDefinedDescribedTools"
  val describedToolScoreTableName: String = "ClearlyDefinedDescribedToolScore"
  val describedSourceLocationTableName: String = "ClearlyDefinedDescribedSourceLocation"
  val describedScoreTableName: String = "ClearlyDefinedDescribedScore"

  val licensedTableName: String = "ClearlyDefinedLicensed"
  val licensedToolScoreTableName: String = "ClearlyDefinedLicensedToolScore"
  val licensedFacetsTableName: String = "ClearlyDefinedLicensedFacets"
  val licensedFacetsCoreTableName: String = "CDLFCore"
  val licensedFacetsCoreAttributionTableName: String = "CDLFCoreAttribution"
  val licensedFacetsCoreDiscoveredTableName: String = "CDLFCoreDiscovered"
  val licensedScoreTableName: String = "ClearlyDefinedLicensedScore"

  val coordinatesTableName: String = "ClearlyDefinedCoordinates"
  val metaTableName: String = "ClearlyDefinedMeta"
  val scoresTableName: String = "ClearlyDefinedScores"

  def registerTables[T: TypeTag](stream: DataStream[T], tEnv: StreamTableEnvironment): Unit = stream match {
    case _ if typeOf[T] <:< typeOf[ClearlyDefinedReleasePojo] => {
      implicit val typeInfo = TypeInformation.of(classOf[ClearlyDefinedReleasePojoExt])
      val releasesStream = stream.asInstanceOf[DataStream[ClearlyDefinedReleasePojo]]
      val releasesStreamExt = getExtendedStream(releasesStream)
      tEnv.registerDataStream(rootTableName, releasesStreamExt)

      //Register all other tables
      registerDescribedTable(releasesStreamExt, tEnv)
      registerDescribedUrlsTable(releasesStreamExt, tEnv)
      registerDescribedHashesTable(releasesStreamExt, tEnv)
      registerDescribedToolsTable(releasesStreamExt, tEnv)
      registerDescribedToolScoreTable(releasesStreamExt, tEnv)
      registerDescribedSourceLocationTable(releasesStreamExt, tEnv)
      registerDescribedScoreTable(releasesStreamExt, tEnv)
      registerLicensedTable(releasesStreamExt, tEnv)
      registerLicensedToolScoreTable(releasesStreamExt, tEnv)
      registerLicensedFacetsTable(releasesStreamExt, tEnv)
      registerLicensedFacetsCDLFCoreTable(releasesStreamExt, tEnv)
      registerLicensedCDLFCoreAttributionTable(releasesStreamExt, tEnv)
      registerLicensedCDLFCoreDiscoveredTable(releasesStreamExt, tEnv)
      registerLicensedScoreTable(releasesStreamExt, tEnv)
      registerCoordinatesTable(releasesStreamExt, tEnv)
      registerMetaTable(releasesStreamExt, tEnv)
      registerScoresTable(releasesStreamExt, tEnv)

    }
    case _: T => print("Have not implemented registering a table for object of type " + typeOf[T].toString)
  }

  def registerDescribedTable(stream: DataStream[ClearlyDefinedReleasePojoExt], tEnv: StreamTableEnvironment): Unit = {
    implicit val typeInfo = TypeInformation.of(classOf[CDDescribedPojoExt])
    val describedStream = stream
      .map(x => new CDDescribedPojoExt() {
        name = x.name
        releaseDate = x.described.releaseDate
        urls = x.described.urls
        projectWebsite = x.described.projectWebsite
        issueTracker = x.described.issueTracker
        hashes = x.described.hashes
        files = x.described.files
        tools = x.described.tools
        toolScore = x.described.toolScore
        sourceLocation = x.described.sourceLocation
        score = x.described.score
      })
    tEnv.registerDataStream(describedTableName, describedStream)
  }

  def registerDescribedUrlsTable(stream: DataStream[ClearlyDefinedReleasePojoExt], tEnv: StreamTableEnvironment): Unit = {
    implicit val typeInfo = TypeInformation.of(classOf[CDDescribedUrlsPojoExt])
    val describedUrlsStream = stream
      .map(x => new CDDescribedUrlsPojoExt() {
        name = x.name
        registry = x.described.urls.registry
        version = x.described.urls.version
        download = x.described.urls.download
      })
    tEnv.registerDataStream(describedUrlsTableName, describedUrlsStream)
  }

  def registerDescribedHashesTable(stream: DataStream[ClearlyDefinedReleasePojoExt], tEnv: StreamTableEnvironment): Unit = {
    implicit val typeInfo = TypeInformation.of(classOf[CDDescribedHashesPojoExt])
    val describedHashesStream = stream
      .map(x => new CDDescribedHashesPojoExt() {
        name = x.name
        gitSha = x.described.hashes.gitSha
        sha1 = x.described.hashes.sha1
        sha256 = x.described.hashes.sha256
      })
    tEnv.registerDataStream(describedHashesTableName, describedHashesStream)
  }

  /**
   * Register the tools list of a Clearly Described object as a streaming table in Flink
   * @param releasesStreamExt the stream of ClearlyDefinedReleasePojoExt used as base to glean the tools from
   * @param tEnv the Flink table environment used for registration
   */
  def registerDescribedToolsTable(releasesStreamExt: DataStream[ClearlyDefinedReleasePojoExt], tEnv: StreamTableEnvironment) = {
    implicit val typeInfo = TypeInformation.of(classOf[ToolPojoExt])
    val toolName_pojostream : DataStream[ToolPojoExt] = releasesStreamExt
      .flatMap(x => {
        x.described.tools.map(y => {
          val pojo_ext = new ToolPojoExt()
          pojo_ext.toolname = y.toolname
          pojo_ext.id = x.name
          pojo_ext
        })
      })
    tEnv.registerDataStream(describedToolsTableName,toolName_pojostream)
    
  }


  def registerDescribedToolScoreTable(stream: DataStream[ClearlyDefinedReleasePojoExt], tEnv: StreamTableEnvironment): Unit = {
    implicit val typeInfo = TypeInformation.of(classOf[CDDescribedToolScorePojoExt])
    val describedToolScoreStream = stream
      .map(x => new CDDescribedToolScorePojoExt() {
        name = x.name
        total = x.described.toolScore.total
        date = x.described.toolScore.date
        source = x.described.toolScore.source
      })
    tEnv.registerDataStream(describedToolScoreTableName, describedToolScoreStream)
  }

  def registerDescribedSourceLocationTable(stream: DataStream[ClearlyDefinedReleasePojoExt], tEnv: StreamTableEnvironment): Unit = {
    implicit val typeInfo = TypeInformation.of(classOf[CDDescribedSourceLocationPojoExt])
    val describedSourceLocationStream = stream
      .filter(x => x.described.sourceLocation != null)
      .map(x => new CDDescribedSourceLocationPojoExt() {
        packageName = x.name
        locationType = x.described.sourceLocation.locationType
        provider = x.described.sourceLocation.provider
        namespace = x.described.sourceLocation.namespace
        name = x.described.sourceLocation.name
        revision = x.described.sourceLocation.revision
        url = x.described.sourceLocation.url
      })
    tEnv.registerDataStream(describedSourceLocationTableName, describedSourceLocationStream)
  }

  def registerDescribedScoreTable(stream: DataStream[ClearlyDefinedReleasePojoExt], tEnv: StreamTableEnvironment): Unit = {
    implicit val typeInfo = TypeInformation.of(classOf[CDDescribedScorePojoExt])
    val describedScoreStream = stream
      .map(x => new CDDescribedScorePojoExt() {
        name = x.name
        total = x.described.score.total
        date = x.described.score.date
        source = x.described.score.source
      })
    tEnv.registerDataStream(describedScoreTableName, describedScoreStream)
  }

  def registerLicensedTable(stream: DataStream[ClearlyDefinedReleasePojoExt], tEnv: StreamTableEnvironment): Unit = {
    implicit val typeInfo = TypeInformation.of(classOf[CDLicensedPojoExt])
    val licensedStream = stream
      .map(x => new CDLicensedPojoExt() {
        name = x.name
        declared = x.licensed.declared
        toolScore = x.licensed.toolScore
        facets = x.licensed.facets
        score = x.licensed.score
    })
    tEnv.registerDataStream(licensedTableName, licensedStream)
  }

  def registerLicensedToolScoreTable(stream: DataStream[ClearlyDefinedReleasePojoExt], tEnv: StreamTableEnvironment): Unit = {
    implicit val typeInfo = TypeInformation.of(classOf[CDLicensedToolScorePojoExt])
    val licensedToolScoreStream = stream
      .map(x => new CDLicensedToolScorePojoExt() {
        name = x.name
        total = x.licensed.toolScore.total
        declared = x.licensed.toolScore.declared
        discovered = x.licensed.toolScore.discovered
        consistency = x.licensed.toolScore.consistency
        spdx = x.licensed.toolScore.spdx
        texts = x.licensed.toolScore.texts
      })
    tEnv.registerDataStream(licensedToolScoreTableName, licensedToolScoreStream)
  }

  def registerLicensedFacetsTable(stream: DataStream[ClearlyDefinedReleasePojoExt], tEnv: StreamTableEnvironment): Unit = {
    implicit val typeInfo = TypeInformation.of(classOf[CDLicensedFacetsPojoExt])
    val licensedFacetsStream = stream
      .map(x => new CDLicensedFacetsPojoExt() {
        name = x.name
        core = x.licensed.facets.core
    })
    tEnv.registerDataStream(licensedFacetsTableName, licensedFacetsStream)
  }

  def registerLicensedFacetsCDLFCoreTable(stream: DataStream[ClearlyDefinedReleasePojoExt], tEnv: StreamTableEnvironment): Unit = {
    implicit val typeInfo = TypeInformation.of(classOf[CDLFCorePojoExt])
    val licensedFacetsCDLFCoreStream = stream
      .map(x => new CDLFCorePojoExt() {
        name = x.name
        attribution = x.licensed.facets.core.attribution
        discovered = x.licensed.facets.core.discovered
        files = x.licensed.facets.core.files
      })
    tEnv.registerDataStream(licensedFacetsCoreTableName, licensedFacetsCDLFCoreStream)
  }

  def registerLicensedCDLFCoreAttributionTable(stream: DataStream[ClearlyDefinedReleasePojoExt], tEnv: StreamTableEnvironment): Unit = {
    implicit val typeInfo = TypeInformation.of(classOf[CDLFCoreAttributionPojoExt])
    implicit val typeInfoList = TypeInformation.of(classOf[List[CDLFCoreAttributionPojoExt]])
    val licensedCDLFCoreAttributionStream = stream
      .flatMap(x => x.licensed.facets.core.attribution.parties.map(y => {
        new CDLFCoreAttributionPojoExt() {
          id = x.name
          unknown = x.licensed.facets.core.attribution.unknown
          party = y
        }
      }))
    tEnv.registerDataStream(licensedFacetsCoreAttributionTableName, licensedCDLFCoreAttributionStream)
  }

  def registerLicensedCDLFCoreDiscoveredTable(stream: DataStream[ClearlyDefinedReleasePojoExt], tEnv: StreamTableEnvironment): Unit = {
    implicit val typeInfo = TypeInformation.of(classOf[CDLFCoreDiscoveredPojoExt])
    implicit val typeInfoList = TypeInformation.of(classOf[List[CDLFCoreDiscoveredPojoExt]])
    val licensedCDLFCoreDiscoveredStream = stream
      .flatMap(x => x.licensed.facets.core.discovered.expressions.map(y => {
        new CDLFCoreDiscoveredPojoExt() {
          id = x.coordinates.name
          unknown = x.licensed.facets.core.discovered.unknown
          expression = y
        }
      }))
    tEnv.registerDataStream(licensedFacetsCoreDiscoveredTableName, licensedCDLFCoreDiscoveredStream)
  }

  def registerLicensedScoreTable(stream: DataStream[ClearlyDefinedReleasePojoExt], tEnv: StreamTableEnvironment): Unit = {
    implicit val typeInfo = TypeInformation.of(classOf[CDLicensedScorePojoExt])
    val licensedScoreStream = stream
      .map(x => new CDLicensedScorePojoExt() {
        name = x.name
        total = x.licensed.score.total
        declared = x.licensed.score.declared
        discovered = x.licensed.score.discovered
        consistency = x.licensed.score.consistency
        spdx = x.licensed.score.spdx
        texts = x.licensed.score.texts
      })
    tEnv.registerDataStream(licensedScoreTableName, licensedScoreStream)
  }

  def registerCoordinatesTable(stream: DataStream[ClearlyDefinedReleasePojoExt], tEnv: StreamTableEnvironment): Unit = {
    implicit val typeInfo = TypeInformation.of(classOf[CDCoordinatesPojo])
    val coordinatesStream = stream.map(x => x.coordinates)
    tEnv.registerDataStream(coordinatesTableName, coordinatesStream)
  }

  def registerMetaTable(stream: DataStream[ClearlyDefinedReleasePojoExt], tEnv: StreamTableEnvironment): Unit = {
    implicit val typeInfo = TypeInformation.of(classOf[CD_metaPojoExt])
    val metaStream = stream
      .map(x => new CD_metaPojoExt() {
        name = x.name
        schemaVersion = x._meta.schemaVersion
        updated = x._meta.updated
      })
    tEnv.registerDataStream(metaTableName, metaStream)
  }

  def registerScoresTable(stream: DataStream[ClearlyDefinedReleasePojoExt], tEnv: StreamTableEnvironment): Unit = {
    implicit val typeInfo = TypeInformation.of(classOf[CDScoresPojoExt])
    val scoresStream = stream
      .map(x => new CDScoresPojoExt() {
        name = x.name
        effective = x.scores.effective
        tool = x.scores.tool
      })
    tEnv.registerDataStream(scoresTableName, scoresStream)
  }
}