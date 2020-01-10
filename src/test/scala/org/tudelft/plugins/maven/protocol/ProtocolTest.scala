package org.tudelft.plugins.maven.protocol

import java.util.Date

import org.scalatest.FunSuite
import org.tudelft.plugins.maven.protocol.Protocol._

class ProtocolTest extends FunSuite{

  val parent = Some(Parent("groupPId", "artifactPId", "pVersion", Some("path")))
  val organization = Some(Organization("name", "url"))
  val issueManagement = Some(IssueManagement("system", "url"))
  val scm = Some(SCM("connection", Some("devConnection"), Some("tag"), "url"))
  val deps = Some(List(Dependency("groupId2", "artifactId2", Some("version"), Some("type"), Some("scope"), Some(true))))
  val depsEmptyVersion = Some(List(Dependency("groupId2", "artifactId2", None, None, None, None)))
  val license = Some(List(License("name", "url", "distribution", Some("comments"))))
  val licenseEmpty = Some(List(License("name", "url", "distribution", None)))
  val repository = Some(List(Repository("id", "name","url")))

  val projectFull = MavenProject("modelVersion", "groupId", "artifactId", "version", parent, deps, license, repository, organization, Some("packaging"), issueManagement, scm, "xml")
  val projectEmpty = MavenProject("modelVersion", "groupId", "artifactId", "version", None, None, None, None, None, None, None, None, "xml")

  test("mavenReleasePojoTest"){
    val release = MavenRelease("title", "link", "description", new Date(0), Guid("tag"))
    val pojo = MavenReleasePojo.fromMavenRelease(release)

    assert(pojo.title == "title")
    assert(pojo.link == "link")
    assert(pojo.description == "description")
    assert(pojo.pubDate == 0)
    assert(pojo.guid_tag == "tag")
  }

  test("mavenReleaseExtPojoTest"){
    val releaseExt = MavenReleaseExt("title", "link", "description", new Date(0), Guid("tag"), projectFull)
    val pojo = MavenReleaseExtPojo.fromMavenReleaseExt(releaseExt)

    assert(pojo.title == "title")
    assert(pojo.link == "link")
    assert(pojo.description == "description")
    assert(pojo.pubDate == 0)
    assert(pojo.guid_tag == "tag")
    assert(pojo.project != null)
  }

  test("parentPojoTest"){
    val pojo = ParentPojo.fromParent(parent.get)

    assert(pojo.groupId == "groupPId")
    assert(pojo.artifactId == "artifactPId")
    assert(pojo.version == "pVersion")
    assert(pojo.relativePath.contains("path"))
  }

  test("dependencyPojoFullTest"){
    val pojo = DependencyPojo.fromDependency(deps.get.head)

    assert(pojo.groupId == "groupId2")
    assert(pojo.artifactId == "artifactId2")
    assert(pojo.version.contains("version"))
    assert(pojo.`type`.contains("type"))
    assert(pojo.scope.contains("scope"))
  }

  test("dependencyPojoEmptyVersionTest"){
    val pojo = DependencyPojo.fromDependency(depsEmptyVersion.get.head)

    assert(pojo.groupId == "groupId2")
    assert(pojo.artifactId == "artifactId2")
    assert(pojo.version.isEmpty)
    assert(pojo.`type`.isEmpty)
    assert(pojo.scope.isEmpty)
  }

  test("licensePojoFullTest"){
    val pojo = LicensePojo.fromLicense(license.get.head)

    assert(pojo.name == "name")
    assert(pojo.url == "url")
    assert(pojo.distribution == "distribution")
    assert(pojo.comments.contains("comments"))
  }

  test("licensePojoEmptyTest"){
    val pojo = LicensePojo.fromLicense(licenseEmpty.get.head)

    assert(pojo.name == "name")
    assert(pojo.url == "url")
    assert(pojo.distribution == "distribution")
    assert(pojo.comments.isEmpty)
  }

  test("repositoryPojoTest"){
    val pojo = RepositoryPojo.fromRepository(repository.get.head)

    assert(pojo.id == "id")
    assert(pojo.name == "name")
    assert(pojo.url == "url")
  }

  test("organizationPojoTest"){
    val pojo = OrganizationPojo.fromOrganization(organization.get)

    assert(pojo.name == "name")
    assert(pojo.url == "url")
  }

  test("issueManagementPojoTest"){
    val pojo = IssueManagementPojo.fromIssueManagement(issueManagement.get)

    assert(pojo.system == "system")
    assert(pojo.url == "url")
  }

  test("scmPojoTest"){
    val pojo = SCMPojo.fromSCM(scm.get)

    assert(pojo.connection == "connection")
    assert(pojo.developerConnection.contains("devConnection"))
    assert(pojo.tag.contains("tag"))
    assert(pojo.url == "url")
  }

  test("mavenProjectPojoFullTest"){
    val pojo = MavenProjectPojo.fromMavenProject(projectFull)

    assert(pojo.modelVersion == "modelVersion")
    assert(pojo.groupId == "groupId")
    assert(pojo.artifactId == "artifactId")
    assert(pojo.version == "version")
    assert(pojo.packaging.contains("packaging"))
    assert(pojo.xml == "xml")
  }

  test("mavenProjectPojoEmptyTest"){
    val pojo = MavenProjectPojo.fromMavenProject(projectEmpty)

    assert(pojo.modelVersion == "modelVersion")
    assert(pojo.groupId == "groupId")
    assert(pojo.artifactId == "artifactId")
    assert(pojo.version == "version")
    assert(pojo.dependencies == null)
    assert(pojo.licenses == null)
    assert(pojo.repositories == null)
    assert(pojo.xml == "xml")
  }
}
