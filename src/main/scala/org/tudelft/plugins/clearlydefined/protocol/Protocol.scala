package org.tudelft.plugins.clearlydefined.protocol

object Protocol {

  case class ClearlyDefinedRelease(described: CDDescribed,
                                   licensed: CDLicensed,
                                   coordinates: CDCoordinates,
                                   _meta: CD_meta,
                                   scores: CDScores)

  class ClearlyDefinedReleasePojo extends Serializable {
    var described: CDDescribedPojo = _
    var licensed: CDLicensedPojo = _
    var coordinates: CDCoordinatesPojo = _
    var _meta: CD_metaPojo = _
    var scores: CDScoresPojo = _
  }

  object ClearlyDefinedReleasePojo {
    def fromClearlyDefinedRelease(clearlyDefinedRelease: ClearlyDefinedRelease): ClearlyDefinedReleasePojo = {
      val pojo = new ClearlyDefinedReleasePojo
      pojo.described = CDDescribedPojo.fromCDDescribed(clearlyDefinedRelease.described)
      pojo.licensed = CDLicensedPojo.fromCDLicensed(clearlyDefinedRelease.licensed)
      pojo.coordinates = CDCoordinatesPojo.fromCDCoordinates(clearlyDefinedRelease.coordinates)
      pojo._meta = CD_metaPojo.fromCD_meta(clearlyDefinedRelease._meta)
      pojo.scores = CDScoresPojo.fromCDScores(clearlyDefinedRelease.scores)
      pojo
    }
  }

  case class CDDescribed(releaseDate: String,
                         urls: CDDescribedUrls,
                         projectWebsite: Option[String],
                         issueTracker: Option[String],
                         hashes: CDDescribedHashes,
                         files: Int,
                         tools: List[String],
                         toolScore: CDDescribedToolScore,
                         sourceLocation: Option[CDDescribedSourceLocation],
                         score: CDDescribedScore)

  class CDDescribedPojo extends Serializable {
    var releaseDate: String = _
    var urls: CDDescribedUrlsPojo = _
    var projectWebsite: String = _
    var issueTracker: String = _
    var hashes: CDDescribedHashesPojo = _
    var files: Int = _
    var tools: List[String] = _
    var toolScore: CDDescribedToolScorePojo = _
    var sourceLocation: CDDescribedSourceLocationPojo = _
    var score: CDDescribedScorePojo = _
  }

  object CDDescribedPojo {
    def fromCDDescribed(cdDescribed: CDDescribed): CDDescribedPojo = {
      val pojo = new CDDescribedPojo
      pojo.releaseDate = cdDescribed.releaseDate
      pojo.urls = CDDescribedUrlsPojo.fromCDDescribedUrls(cdDescribed.urls)
      pojo.projectWebsite = cdDescribed.projectWebsite.orNull
      pojo.issueTracker = cdDescribed.issueTracker.orNull
      pojo.hashes = CDDescribedHashesPojo.fromCDDescribedHashes(cdDescribed.hashes)
      pojo.files = cdDescribed.files
      pojo.tools = cdDescribed.tools
      pojo.toolScore = CDDescribedToolScorePojo.fromCDDescribedToolScore(cdDescribed.toolScore)

      // Set the source location
      if (cdDescribed.sourceLocation.isDefined) {
        pojo.sourceLocation = CDDescribedSourceLocationPojo.fromCDDescribedSourceLocation(cdDescribed.sourceLocation.get)
      }

      pojo.score = CDDescribedScorePojo.fromCDDescribedScore(cdDescribed.score)
      pojo
    }
  }

  case class CDDescribedUrls(registry: String,
                             version: String,
                             download: String)

  class CDDescribedUrlsPojo extends Serializable {
    var registry: String = _
    var version: String = _
    var download: String = _
  }

  object CDDescribedUrlsPojo {
    def fromCDDescribedUrls(cdDescribedUrls: CDDescribedUrls): CDDescribedUrlsPojo = {
      val pojo = new CDDescribedUrlsPojo
      pojo.registry = cdDescribedUrls.registry
      pojo.version = cdDescribedUrls.version
      pojo.download = cdDescribedUrls.download
      pojo
    }
  }

  case class CDDescribedHashes(gitSha: Option[String],
                               sha1: Option[String],
                               sha256: Option[String])

  class CDDescribedHashesPojo extends Serializable {
    var gitSha: String = _
    var sha1: String = _
    var sha256: String = _
  }

  object CDDescribedHashesPojo {
    def fromCDDescribedHashes(cdDescribedHashes: CDDescribedHashes): CDDescribedHashesPojo = {
      val pojo = new CDDescribedHashesPojo
      pojo.gitSha = cdDescribedHashes.gitSha.orNull
      pojo.sha1 = cdDescribedHashes.sha1.orNull
      pojo.sha256 = cdDescribedHashes.sha256.orNull
      pojo
    }
  }

  case class CDDescribedToolScore(total: Int,
                                  date: Int,
                                  source: Int)

  class CDDescribedToolScorePojo extends Serializable {
    var total: Int = _
    var date: Int = _
    var source: Int = _
  }

  object CDDescribedToolScorePojo {
    def fromCDDescribedToolScore(cdDescribedToolScore: CDDescribedToolScore): CDDescribedToolScorePojo = {
      val pojo = new CDDescribedToolScorePojo
      pojo.total = cdDescribedToolScore.total
      pojo.date = cdDescribedToolScore.date
      pojo.source = cdDescribedToolScore.source
      pojo
    }
  }

  case class CDDescribedSourceLocation(locationType: String,
                                       provider: String,
                                       namespace: String,
                                       name: String,
                                       revision: String,
                                       url: String)

  class CDDescribedSourceLocationPojo extends Serializable {
    var locationType: String = _
    var provider: String = _
    var namespace: String = _
    var name: String = _
    var revision: String = _
    var url: String = _
  }

  object CDDescribedSourceLocationPojo {
    def fromCDDescribedSourceLocation(cdDescribedSourceLocation: CDDescribedSourceLocation): CDDescribedSourceLocationPojo = {
      val pojo = new CDDescribedSourceLocationPojo
      pojo.locationType = cdDescribedSourceLocation.locationType
      pojo.provider = cdDescribedSourceLocation.provider
      pojo.namespace = cdDescribedSourceLocation.namespace
      pojo.name = cdDescribedSourceLocation.name
      pojo.revision = cdDescribedSourceLocation.revision
      pojo.url = cdDescribedSourceLocation.url
      pojo
    }
  }

  case class CDDescribedScore(total: Int,
                              date: Int,
                              source: Int)

  class CDDescribedScorePojo extends Serializable {
    var total: Int = _
    var date: Int = _
    var source: Int = _
  }

  object CDDescribedScorePojo {
    def fromCDDescribedScore(cdDescribedScore: CDDescribedScore): CDDescribedScorePojo = {
      val pojo = new CDDescribedScorePojo
      pojo.total = cdDescribedScore.total
      pojo.date = cdDescribedScore.date
      pojo.source = cdDescribedScore.source
      pojo
    }
  }

  case class CDLicensed(declared: Option[String],
                        toolScore: CDLicensedToolScore,
                        facets: CDLicensedFacets,
                        score: CDLicensedScore)

  class CDLicensedPojo extends Serializable {
    var declared: String = _
    var toolScore: CDLicensedToolScorePojo = _
    var facets: CDLicensedFacetsPojo = _
    var score: CDLicensedScorePojo = _
  }

  object CDLicensedPojo {
    def fromCDLicensed(cdLicensed: CDLicensed): CDLicensedPojo = {
      val pojo = new CDLicensedPojo
      if(cdLicensed.declared.isDefined) {
        pojo.declared = cdLicensed.declared.get
      }
      pojo.toolScore = CDLicensedToolScorePojo.fromCDLicensedToolScore(cdLicensed.toolScore)
      pojo.facets = CDLicensedFacetsPojo.fromCDLicensedFacets(cdLicensed.facets)
      pojo.score = CDLicensedScorePojo.fromCDLicensedScore(cdLicensed.score)
      pojo
    }
  }

  case class CDLicensedToolScore(total: Int,
                                 declared: Int,
                                 discovered: Int,
                                 consistency: Int,
                                 spdx: Int,
                                 texts: Int)

  class CDLicensedToolScorePojo extends Serializable {
    var total: Int = _
    var declared: Int = _
    var discovered: Int = _
    var consistency: Int = _
    var spdx: Int = _
    var texts: Int = _
  }

  object CDLicensedToolScorePojo {
    def fromCDLicensedToolScore(cdLicensedToolScore: CDLicensedToolScore): CDLicensedToolScorePojo = {
      val pojo = new CDLicensedToolScorePojo
      pojo.total = cdLicensedToolScore.total
      pojo.declared = cdLicensedToolScore.declared
      pojo.discovered = cdLicensedToolScore.discovered
      pojo.consistency = cdLicensedToolScore.consistency
      pojo.spdx = cdLicensedToolScore.spdx
      pojo.texts = cdLicensedToolScore.texts
      pojo
    }
  }

  case class CDLicensedFacets(core: CDLFCore)

  class CDLicensedFacetsPojo extends Serializable {
    var core: CDLFCorePojo = _
  }

  object CDLicensedFacetsPojo {
    def fromCDLicensedFacets(cdLicensedFacets: CDLicensedFacets): CDLicensedFacetsPojo = {
      val pojo = new CDLicensedFacetsPojo
      pojo.core = CDLFCorePojo.fromCDLFCore(cdLicensedFacets.core)
      pojo
    }
  }

  case class CDLFCore(attribution: CDLFCoreAttribution,
                      discovered: CDLFCoreDiscovered,
                      files: Int)

  class CDLFCorePojo extends Serializable {
    var attribution: CDLFCoreAttributionPojo = _
    var discovered: CDLFCoreDiscoveredPojo = _
    var files: Int = _
  }

  object CDLFCorePojo {
    def fromCDLFCore(cdlfCore: CDLFCore): CDLFCorePojo = {
      val pojo = new CDLFCorePojo
      pojo.attribution = CDLFCoreAttributionPojo.fromCDLFCoreAttribution(cdlfCore.attribution)
      pojo.discovered = CDLFCoreDiscoveredPojo.fromCDLFCoreDiscovered(cdlfCore.discovered)
      pojo.files = cdlfCore.files
      pojo
    }
  }

  case class CDLFCoreAttribution(unknown: Int,
                                 parties: Option[List[String]])

  class CDLFCoreAttributionPojo extends Serializable {
    var unknown: Int = _
    var parties: List[String] = _
  }

  class CDLFCoreAttributionPojoExt extends Serializable {
    var id: String = _
    var unknown: Int = _
    var party: String = _
  }

  object CDLFCoreAttributionPojo {
    def fromCDLFCoreAttribution(cdlfCoreAttribution: CDLFCoreAttribution): CDLFCoreAttributionPojo = {
      val pojo = new CDLFCoreAttributionPojo
      pojo.unknown = cdlfCoreAttribution.unknown
      if(cdlfCoreAttribution.parties.isDefined) {
        pojo.parties = cdlfCoreAttribution.parties.get
      }
      pojo
    }
  }

  case class CDLFCoreDiscovered(unknown: Int,
                                expressions: List[String])

  class CDLFCoreDiscoveredPojo extends Serializable {
    var unknown: Int = _
    var expressions: List[String] = _
  }

  class CDLFCoreDiscoveredPojoExt extends Serializable {
    var id: String = _
    var unknown: Int = _
    var expression: String = _
  }

  object CDLFCoreDiscoveredPojo {
    def fromCDLFCoreDiscovered(cdlfCoreDiscovered: CDLFCoreDiscovered): CDLFCoreDiscoveredPojo = {
      val pojo = new CDLFCoreDiscoveredPojo
      pojo.unknown = cdlfCoreDiscovered.unknown
      pojo.expressions = cdlfCoreDiscovered.expressions
      pojo
    }
  }

  case class CDLicensedScore(total: Int,
                             declared: Int,
                             discovered: Int,
                             consistency: Int,
                             spdx: Int,
                             texts: Int)

  class CDLicensedScorePojo extends Serializable {
    var total: Int = _
    var declared: Int = _
    var discovered: Int = _
    var consistency: Int = _
    var spdx: Int = _
    var texts: Int = _
  }

  object CDLicensedScorePojo {
    def fromCDLicensedScore(cdLicensedScore: CDLicensedScore): CDLicensedScorePojo = {
      val pojo = new CDLicensedScorePojo
      pojo.total = cdLicensedScore.total
      pojo.declared = cdLicensedScore.declared
      pojo.discovered = cdLicensedScore.discovered
      pojo.consistency = cdLicensedScore.consistency
      pojo.spdx = cdLicensedScore.spdx
      pojo.texts = cdLicensedScore.texts
      pojo
    }
  }

  case class CDCoordinates(`type`: String,
                           provider: String,
                           name: String,
                           namespace: Option[String],
                           revision: String)

  class CDCoordinatesPojo extends Serializable {
    var `type`: String = _
    var provider: String = _
    var name: String = _
    var namespace: String = _
    var revision: String = _
  }

  object CDCoordinatesPojo {
    def fromCDCoordinates(cdCoordinates: CDCoordinates): CDCoordinatesPojo = {
      val pojo = new CDCoordinatesPojo
      pojo.`type` = cdCoordinates.`type`
      pojo.provider = cdCoordinates.provider
      pojo.name = cdCoordinates.name
      pojo.namespace = cdCoordinates.namespace.orNull
      pojo.revision = cdCoordinates.revision
      pojo
    }
  }

  case class CD_meta(schemaVersion: String,
                     updated: String)

  class CD_metaPojo extends Serializable {
    var schemaVersion: String = _
    var updated: String = _
  }

  object CD_metaPojo {
    def fromCD_meta(cd_meta: CD_meta): CD_metaPojo = {
      val pojo = new CD_metaPojo
      pojo.schemaVersion = cd_meta.schemaVersion
      pojo.updated = cd_meta.updated
      pojo
    }
  }

  case class CDScores(effective: Int,
                      tool: Int)

  class CDScoresPojo extends Serializable {
    var effective: Int = _
    var tool: Int = _
  }

  object CDScoresPojo {
    def fromCDScores(cdScores: CDScores): CDScoresPojo = {
      val pojo = new CDScoresPojo
      pojo.effective = cdScores.effective
      pojo.tool = cdScores.tool
      pojo
    }
  }
}
