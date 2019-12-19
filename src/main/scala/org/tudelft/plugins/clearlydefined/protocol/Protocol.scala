package org.tudelft.plugins.clearlydefined.protocol

object Protocol {
  case class ClearlyDefinedRelease(described: CDDescribed,
                                   licensed: CDLicensed,
                                   coordinates: CDCoordinates,
                                   _meta: CD_meta,
                                   scores: CDScores)

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

  case class CDDescribedUrls(registry: String,
                             version: String,
                             download: String)

  case class CDDescribedHashes(gitSha: Option[String],
                               sha1: Option[String],
                               sha256: Option[String])

  case class CDDescribedToolScore(total: Int,
                                  date: Int,
                                  source: Int)

  case class CDDescribedSourceLocation(locationType: String,
                              provider: String,
                              namespace: String,
                              name: String,
                              revision: String,
                              url: String)

  case class CDDescribedScore(total: Int,
                              date: Int,
                              source: Int)

  case class CDLicensed(declared: Option[String],
                        toolScore: CDLicensedToolScore,
                        facets: CDLicensedFacets,
                        score: CDLicensedScore)

  case class CDLicensedToolScore(total: Int,
                                 declared: Int,
                                 discovered: Int,
                                 consistency: Int,
                                 spdx: Int,
                                 texts: Int)

  case class CDLicensedFacets(core: CDLFCore)

  case class CDLFCore(attribution: CDLFCoreAttribution,
                      discovered: CDLFCoreDiscovered,
                      files: Int)

  case class CDLFCoreAttribution(unknown: Int,
                                 parties: List[String])

  case class CDLFCoreDiscovered(unknown: Int,
                                expressions: List[String])

  case class CDLicensedScore(total: Int,
                             declared: Int,
                             discovered: Int,
                             consistency: Int,
                             spdx: Int,
                             texts: Int)

  case class CDCoordinates(`type`: String,
                           provider: String,
                           name: String,
                           namespace: Option[String],
                           revision: String)

  case class CD_meta(schemaVersion: String,
                     updated: String)

  case class CDScores(effective: Int,
                      tool: Int)

}
