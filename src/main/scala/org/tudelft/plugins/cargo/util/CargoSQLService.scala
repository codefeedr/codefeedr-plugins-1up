package org.tudelft.plugins.cargo.util

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.scala.DataStream
import org.apache.flink.table.api.scala.StreamTableEnvironment
import org.tudelft.plugins.cargo.protocol.Protocol._

import scala.reflect.runtime.universe._

object CargoSQLService {
  val rootTableName: String = "Cargo"
  val crateTableName: String = "CargoCrate"
  val crateLinksTableName: String = "CargoCrateLinks"
  val crateVersionsTableName: String = "CargoCrateVersions"
  val crateVersionFeaturesTableName: String = "CargoCrateVersionFeatures"
  val crateVersionLinksTableName: String = "CargoCrateVersionLinks"
  val crateVersionPublishedByTableName: String = "CargoCrateVersionPublishedBy"
  val crateKeywordsTableName: String = "CargoCrateKeywords"
  val crateCategoriesTableName: String = "CargoCrateCategories"

  implicit val crateVersionListTypeInfo: TypeInformation[List[CrateVersionPojo]] =
    TypeInformation.of(classOf[List[CrateVersionPojo]])
  implicit val crateVersionTypeInfo: TypeInformation[CrateVersionPojo] =
    TypeInformation.of(classOf[CrateVersionPojo])
  implicit val crateTypeInfo: TypeInformation[CratePojo] = TypeInformation.of(classOf[CratePojo])

  def registerTables[T: TypeTag](stream: DataStream[T], tEnv: StreamTableEnvironment): Unit = stream match {
    case _ if typeOf[T] <:< typeOf[CrateReleasePojo] =>
      val releasesStream: DataStream[CrateReleasePojo] = stream.asInstanceOf[DataStream[CrateReleasePojo]]
      tEnv.registerDataStream(rootTableName, releasesStream)

      this.registerCrateTable(releasesStream, tEnv)
      this.registerCrateLinksTable(releasesStream, tEnv)
      this.registerCrateVersionTable(releasesStream, tEnv)
      this.registerCrateVersionFeaturesTable(releasesStream, tEnv)
      this.registerCrateVersionLinksTable(releasesStream, tEnv)
      this.registerCrateVersionPublishedByTable(releasesStream, tEnv)
      this.registerCrateKeywordsTable(releasesStream, tEnv)
      this.registerCrateCategoriesTable(releasesStream, tEnv)

    case _: T => print("Have not implemented registering a table for object of type " + typeOf[T].toString)
  }

  def registerCrateTable(stream: DataStream[CrateReleasePojo], tEnv: StreamTableEnvironment): Unit = {
    val crateStream: DataStream[CratePojo] = stream.map(x => x.crate)
    tEnv.registerDataStream(crateTableName, crateStream)
  }

  def registerCrateLinksTable(stream: DataStream[CrateReleasePojo], tEnv: StreamTableEnvironment): Unit = {
    implicit val typeInfo: TypeInformation[CrateLinksPojoExt] = TypeInformation.of(classOf[CrateLinksPojoExt])
    val crateLinksStream: DataStream[CrateLinksPojoExt] = stream
      .map(x => x.crate)
      .map(x => new CrateLinksPojoExt() {
        crateId = x.id
        version_downloads = x.links.version_downloads
        versions = x.links.versions
        owners = x.links.owners
        owner_team = x.links.owner_team
        owner_user = x.links.owner_user
        reverse_dependencies = x.links.reverse_dependencies
      })
    tEnv.registerDataStream(crateLinksTableName, crateLinksStream)
  }

  def registerCrateVersionTable(stream: DataStream[CrateReleasePojo], tEnv: StreamTableEnvironment): Unit = {
    // Because the CrateVersion object already saves the Crate name as a given field, no need for an Ext version
    val crateVersionsStream: DataStream[CrateVersionPojo] = stream
      .filter(x => x.versions != null)
      .flatMap(x => x.versions)

    tEnv.registerDataStream(crateVersionsTableName, crateVersionsStream)
  }

  def registerCrateVersionFeaturesTable(stream: DataStream[CrateReleasePojo], tEnv: StreamTableEnvironment): Unit = {
    implicit val typeInfo: TypeInformation[CrateVersionFeaturesPojoExt] =
      TypeInformation.of(classOf[CrateVersionFeaturesPojoExt])

    val crateVersionFeaturesStream: DataStream[CrateVersionFeaturesPojoExt] = stream
      .filter(x => x.versions != null)
      .flatMap(x => x.versions)
      .map(x => new CrateVersionFeaturesPojoExt() {
        versionId = x.id
        crate = x.crate
      })

    tEnv.registerDataStream(crateVersionFeaturesTableName, crateVersionFeaturesStream)
  }

  def registerCrateVersionLinksTable(stream: DataStream[CrateReleasePojo], tEnv: StreamTableEnvironment): Unit = {
    implicit val typeInfo: TypeInformation[CrateVersionLinksPojoExt] =
      TypeInformation.of(classOf[CrateVersionLinksPojoExt])

    val crateVersionLinksStream: DataStream[CrateVersionLinksPojoExt] = stream
      .filter(x => x.versions != null)
      .flatMap(x => x.versions)
      .map(x => new CrateVersionLinksPojoExt() {
        versionId = x.id
        crate = x.crate
        dependencies = x.links.dependencies
        version_downloads = x.links.version_downloads
        authors = x.links.authors
      })

    tEnv.registerDataStream(crateVersionLinksTableName, crateVersionLinksStream)
  }

  def registerCrateVersionPublishedByTable(stream: DataStream[CrateReleasePojo], tEnv: StreamTableEnvironment): Unit = {
    implicit val typeInfo: TypeInformation[CrateVersionPublishedByPojoExt] =
      TypeInformation.of(classOf[CrateVersionPublishedByPojoExt])

    val crateVersionPublishedByStream: DataStream[CrateVersionPublishedByPojoExt] = stream
      .filter(x => x.versions != null)
      .flatMap(x => x.versions)
      .filter(x => x.published_by != null)
      .map(x => new CrateVersionPublishedByPojoExt() {
        versionId = x.id
        crate = x.crate
        id = x.published_by.id
        login = x.published_by.login
        name = x.published_by.name
        avatar = x.published_by.avatar
        url = x.published_by.url
      })

    tEnv.registerDataStream(crateVersionPublishedByTableName, crateVersionPublishedByStream)
  }

  def registerCrateKeywordsTable(stream: DataStream[CrateReleasePojo], tEnv: StreamTableEnvironment): Unit = {
    implicit val typeInfo: TypeInformation[CrateKeywordPojoExt] =
      TypeInformation.of(classOf[CrateKeywordPojoExt])
    implicit val typeInfoList: TypeInformation[List[CrateKeywordPojoExt]] =
      TypeInformation.of(classOf[List[CrateKeywordPojoExt]])

    val crateKeywordsStream: DataStream[CrateKeywordPojoExt] = stream
      .flatMap(x => x.keywords.map(y => {
        new CrateKeywordPojoExt() {
          crate = x.crate.id
          id = y.id
          keyword = y.keyword
          created_at = y.created_at
          crates_cnt = y.crates_cnt
        }
      }))

    tEnv.registerDataStream(crateKeywordsTableName, crateKeywordsStream)
  }

  def registerCrateCategoriesTable(stream: DataStream[CrateReleasePojo], tEnv: StreamTableEnvironment): Unit = {
    implicit val typeInfo: TypeInformation[CrateCategoryPojoExt] =
      TypeInformation.of(classOf[CrateCategoryPojoExt])
    implicit val typeInfoList: TypeInformation[List[CrateCategoryPojoExt]] =
      TypeInformation.of(classOf[List[CrateCategoryPojoExt]])

    val crateCategoriesStream: DataStream[CrateCategoryPojoExt] = stream
      .flatMap(x => x.categories.map(y => {
        new CrateCategoryPojoExt() {
          crate = x.crate.id
          id = y.id
          category = y.category
          slug = y.slug
          description = y.description
          created_at = y.created_at
          crates_cnt = y.crates_cnt
        }
      }))

    tEnv.registerDataStream(crateCategoriesTableName, crateCategoriesStream)
  }
}
