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

  test("cdReleasePojo convert success") {
    val pojo = ClearlyDefinedReleasePojo.fromClearlyDefinedRelease(cdRelease)

    // Assert various normal members and None members
    assert(pojo.described.releaseDate.equals("releaseDate"))
    assert(pojo.described.issueTracker.get.equals("issueTracker"))
    assert(pojo.licensed.score.spdx == 1)
    assert(pojo.coordinates.namespace.get.equals("namespace"))
    assert(pojo._meta.updated.equals("updated"))
    assert(pojo.scores.effective == 1)
  }

  test("cdReleasePojo convert with None fields success") {
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

  test("cdDescribed convert success") {
    val pojo = CDDescribedPojo.fromCDDescribed(cdDescribed)

    // Assert fields
    assert(pojo.releaseDate.equals("releaseDate"))
    assert(pojo.projectWebsite.get.equals("projectWebsite"))
    assert(pojo.issueTracker.get.equals("issueTracker"))
    assert(pojo.files == 1)
    assert(pojo.tools.head.equals("tools1"))
  }

}
