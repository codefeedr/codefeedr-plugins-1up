package org.tudelft.plugins.clearlydefined.protocol

import org.scalatest.FunSuite
import org.tudelft.plugins.clearlydefined.protocol.Protocol._

class ProtocolTests extends FunSuite {
  /** all full example classes */
  val cdDescribedUrls = CDDescribedUrls("registry", "version", "download")
  val cdDescribedHashes = CDDescribedHashes(Some("gitSha"), Some("sha1"), Some("sha256"))
  val cdDescribedToolScore = CDDescribedToolScore(1, 1, 1)
  val cdDescribedSourceLocation = CDDescribedSourceLocation("locationType", "provider", "namespace", "name", "revision",
    "url")
  val cdDescribedScore = CDDescribedScore(1, 1, 1)
  val cdLicensedToolScore = CDLicensedToolScore(1, 1, 1, 1, 1, 1)
  val cdlfCoreAttribution = CDLFCoreAttribution(1, List("parties1", "parties2"))
  val cdlfCoreDiscovered = CDLFCoreDiscovered(1, List("expressions1", "expressions2"))
  val cdlfCore = CDLFCore(cdlfCoreAttribution, cdlfCoreDiscovered, 1)
  val cdLicensedFacets = CDLicensedFacets(cdlfCore)
  val cdLicensedScore = CDLicensedScore(1, 1, 1, 1, 1, 1)
  val cdLicensed = CDLicensed("declared", cdLicensedToolScore, cdLicensedFacets, cdLicensedScore)
  val cdCoordinates = CDCoordinates("type", "provider", "name", Some("namespace"), "revision")
  val cd_meta = CD_meta("schemaVersion", "updated")
  val cdScores = CDScores(1, 1)
  val cdDescribed = CDDescribed("releaseDate", cdDescribedUrls, Some("projectWebsite"), Some("issueTracker"),
    cdDescribedHashes, 1, List("tools1", "tools2"), cdDescribedToolScore, Some(cdDescribedSourceLocation),
    cdDescribedScore)

  val cdRelease = ClearlyDefinedRelease(cdDescribed, cdLicensed, cdCoordinates, cd_meta, cdScores)

  /** all classes with potential None fields */
  val cdDescribedHashesEmpty = CDDescribedHashes(None, None, None)
  val cdDescribedEmpty = CDDescribed("releaseDate", cdDescribedUrls, None, None, cdDescribedHashesEmpty, 1,
    List("tools1", "tools2"), cdDescribedToolScore, None, cdDescribedScore)
  val cdCoordinatesEmpty = CDCoordinates("type", "provider", "name", None, "revision")

  val cdReleaseEmpty = ClearlyDefinedRelease(cdDescribedEmpty, cdLicensed, cdCoordinatesEmpty, cd_meta, cdScores)

  test("ClearlyDefinedReleasePojo convert success") {
    val pojo = ClearlyDefinedReleasePojo.fromClearlyDefinedRelease(cdRelease)

    // Assert various normal members and None members
    assert(pojo.described.releaseDate.equals("releaseDate"))
    assert(pojo.described.issueTracker.get.equals("issueTracker"))
    assert(pojo.licensed.score.spdx == 1)
    assert(pojo.coordinates.namespace.get.equals("namespace"))
    assert(pojo._meta.updated.equals("updated"))
    assert(pojo.scores.effective == 1)
  }

  test("ClearlyDefinedReleasePojo convert with None fields success") {
    val pojo = ClearlyDefinedReleasePojo.fromClearlyDefinedRelease(cdReleaseEmpty)

    // Assert none fields
    assert(pojo.described.issueTracker.isEmpty)
    assert(pojo.described.projectWebsite.isEmpty)
    assert(pojo.described.sourceLocation.isEmpty)
    assert(pojo.described.hashes.gitSha.isEmpty)
    assert(pojo.described.hashes.sha1.isEmpty)
    assert(pojo.described.hashes.sha256.isEmpty)
    assert(pojo.coordinates.namespace.isEmpty)
  }

  test("CDDescribed convert success") {
    val pojo = CDDescribedPojo.fromCDDescribed(cdDescribed)

    // Assert fields
    assert(pojo.releaseDate.equals("releaseDate"))
    assert(pojo.projectWebsite.get.equals("projectWebsite"))
    assert(pojo.issueTracker.get.equals("issueTracker"))
    assert(pojo.files == 1)
    assert(pojo.tools.head.equals("tools1"))

    // Assert complex fields
    assert(pojo.urls.isInstanceOf[CDDescribedUrlsPojo])
    assert(pojo.hashes.isInstanceOf[CDDescribedHashesPojo])
    assert(pojo.toolScore.isInstanceOf[CDDescribedToolScorePojo])
    assert(pojo.sourceLocation.get.isInstanceOf[CDDescribedSourceLocationPojo])
    assert(pojo.score.isInstanceOf[CDDescribedScorePojo])
  }

  test("CDDescribed convert with none fields success") {
    val pojo = CDDescribedPojo.fromCDDescribed(cdDescribedEmpty)

    // Assert fields + None's
    assert(pojo.releaseDate.equals("releaseDate"))
    assert(pojo.projectWebsite.isEmpty)
    assert(pojo.issueTracker.isEmpty)
    assert(pojo.files == 1)
    assert(pojo.tools.head.equals("tools1"))
    assert(pojo.sourceLocation.isEmpty)
  }

  test("CDDescribedUrls convert success") {
    val pojo = CDDescribedUrlsPojo.fromCDDescribedUrls(cdDescribedUrls)

    // Assert fields
    assert(pojo.registry.equals("registry"))
    assert(pojo.version.equals("version"))
    assert(pojo.download.equals("download"))
  }

  test("CDDescribedHashes convert success") {
    val pojo = CDDescribedHashesPojo.fromCDDescribedHashes(cdDescribedHashes)

    // Assert fields
    assert(pojo.gitSha.get.equals("gitSha"))
    assert(pojo.sha1.get.equals("sha1"))
    assert(pojo.sha256.get.equals("sha256"))
  }

  test("CDDescribedHashes convert with None fields success") {
    val pojo = CDDescribedHashesPojo.fromCDDescribedHashes(cdDescribedHashesEmpty)

    // Assert fields
    assert(pojo.gitSha.isEmpty)
    assert(pojo.sha1.isEmpty)
    assert(pojo.sha256.isEmpty)
  }

  test("CDDescribedToolScore convert success") {
    val pojo = CDDescribedToolScorePojo.fromCDDescribedToolScore(cdDescribedToolScore)

    // Assert fields
    assert(pojo.total == 1)
    assert(pojo.date == 1)
    assert(pojo.source == 1)
  }

  test("CDDescribedSourceLocation convert success") {
    val pojo = CDDescribedSourceLocationPojo.fromCDDescribedSourceLocation(cdDescribedSourceLocation)

    // Assert fields
    assert(pojo.locationType.equals("locationType"))
    assert(pojo.provider.equals("provider"))
    assert(pojo.namespace.equals("namespace"))
    assert(pojo.name.equals("name"))
    assert(pojo.revision.equals("revision"))
    assert(pojo.url.equals("url"))
  }

  test("CDDescribedScore convert success") {
    val pojo = CDDescribedScorePojo.fromCDDescribedScore(cdDescribedScore)

    // Assert fields
    assert(pojo.total == 1)
    assert(pojo.date == 1)
    assert(pojo.source == 1)
  }

  test("CDLicensed convert success") {
    val pojo = CDLicensedPojo.fromCDLicensed(cdLicensed)

    // Assert fields
    assert(pojo.declared.equals("declared"))
    assert(pojo.toolScore.isInstanceOf[CDLicensedToolScorePojo])
    assert(pojo.facets.isInstanceOf[CDLicensedFacetsPojo])
    assert(pojo.score.isInstanceOf[CDLicensedScorePojo])
  }

  test("CDLicensedToolScore convert success") {
    val pojo = CDLicensedToolScorePojo.fromCDLicensedToolScore(cdLicensedToolScore)

    // Assert fields
    assert(pojo.total == 1)
    assert(pojo.declared == 1)
    assert(pojo.discovered == 1)
    assert(pojo.consistency == 1)
    assert(pojo.spdx == 1)
    assert(pojo.texts == 1)
  }

  test("CDLicensedFacets convert success") {
    val pojo = CDLicensedFacetsPojo.fromCDLicensedFacets(cdLicensedFacets)

    // Assert fields
    assert(pojo.core.isInstanceOf[CDLFCorePojo])
  }

  test("CDLFCore convert success") {
    val pojo = CDLFCorePojo.fromCDLFCore(cdlfCore)

    // Assert fields
    assert(pojo.attribution.isInstanceOf[CDLFCoreAttributionPojo])
    assert(pojo.discovered.isInstanceOf[CDLFCoreDiscoveredPojo])
    assert(pojo.files == 1)
  }

  test("CDLFCoreAttribution convert success") {
    val pojo = CDLFCoreAttributionPojo.fromCDLFCoreAttribution(cdlfCoreAttribution)

    // Assert fields
    assert(pojo.unknown == 1)
    assert(pojo.parties.head.equals("parties1"))
  }

  test("CDLFCoreDiscovered convert success") {
    val pojo = CDLFCoreDiscoveredPojo.fromCDLFCoreDiscovered(cdlfCoreDiscovered)

    // Assert fields
    assert(pojo.unknown == 1)
    assert(pojo.expressions.head.equals("expressions1"))
  }

  test("CDLicensedScore convert success") {
    val pojo = CDLicensedScorePojo.fromCDLicensedScore(cdLicensedScore)

    // Assert fields
    assert(pojo.total == 1)
    assert(pojo.declared == 1)
    assert(pojo.discovered == 1)
    assert(pojo.consistency == 1)
    assert(pojo.spdx == 1)
    assert(pojo.texts == 1)
  }

  test("CDCoordinates convert success") {
    val pojo = CDCoordinatesPojo.fromCDCoordinates(cdCoordinates)

    // Assert fields
    assert(pojo.`type`.equals("type"))
    assert(pojo.provider.equals("provider"))
    assert(pojo.name.equals("name"))
    assert(pojo.namespace.get.equals("namespace"))
    assert(pojo.revision.equals("revision"))
  }

  test("CDCoordinates convert with None fields success") {
    val pojo = CDCoordinatesPojo.fromCDCoordinates(cdCoordinatesEmpty)

    // Assert fields
    assert(pojo.`type`.equals("type"))
    assert(pojo.provider.equals("provider"))
    assert(pojo.name.equals("name"))
    assert(pojo.namespace.isEmpty)
    assert(pojo.revision.equals("revision"))
  }

  test("CD_meta convert success") {
    val pojo = CD_metaPojo.fromCD_meta(cd_meta)

    // Assert fields
    assert(pojo.schemaVersion.equals("schemaVersion"))
    assert(pojo.updated.equals("updated"))
  }

  test("CDScores convert success") {
    val pojo = CDScoresPojo.fromCDScores(cdScores)

    // Assert fields
    assert(pojo.effective == 1)
    assert(pojo.tool == 1)
  }

}
