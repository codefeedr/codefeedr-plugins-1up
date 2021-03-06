package org.tudelft.plugins.maven.protocol

import java.io.Serializable
import java.util.Date


object Protocol {

  /**
   * A POJO style representation of the MavenRelease case class
   */
  class MavenReleasePojo extends Serializable {
    var title: String = _
    var link: String = _
    var description: String = _
    var pubDate: Long = _
    var guid_tag: String = _
  }

  object MavenReleasePojo {
    def fromMavenRelease(release: MavenRelease): MavenReleasePojo = {
      val pojo = new MavenReleasePojo
      pojo.title = release.title
      pojo.link = release.link
      pojo.description = release.description
      pojo.pubDate = release.pubDate.getTime
      pojo.guid_tag = release.guid.tag
      pojo
    }
  }

  class MavenReleaseExtPojo extends Serializable {
    var title: String = _
    var link: String = _
    var description: String = _
    var pubDate: Long = _
    var guid_tag: String = _
    var project: MavenProjectPojo = _
  }

  object MavenReleaseExtPojo {
    def fromMavenReleaseExt(releaseExt: MavenReleaseExt): MavenReleaseExtPojo = {
      val pojo = new MavenReleaseExtPojo
      pojo.title = releaseExt.title
      pojo.link = releaseExt.link
      pojo.description = releaseExt.description
      pojo.pubDate = releaseExt.pubDate.getTime
      pojo.guid_tag = releaseExt.guid.tag
      pojo.project = MavenProjectPojo.fromMavenProject(releaseExt.project)
      pojo
    }
  }


  class ParentPojo extends Serializable {
    var groupId: String = _
    var artifactId: String = _
    var version: String = _
    var relativePath: Option[String] = _
  }

  object ParentPojo {
    def fromParent(parent: Parent): ParentPojo = {
      val pojo = new ParentPojo
      pojo.groupId = parent.groupId
      pojo.artifactId = parent.artifactId
      pojo.version = parent.version
      pojo.relativePath = parent.relativePath
      pojo
    }
  }

  class MavenProjectPojo extends Serializable {
    var modelVersion: String = _
    var groupId: String = _
    var artifactId: String = _
    var version: String = _
    var parent: Option[ParentPojo] = _
    var dependencies: Option[List[DependencyPojo]] = _
    var licenses: Option[List[LicensePojo]] = _
    var repositories: Option[List[RepositoryPojo]] = _
    var organization: Option[OrganizationPojo] = _
    var packaging: Option[String] = _
    var issueManagement: Option[IssueManagementPojo] = _
    var scm: Option[SCMPojo] = _
    var xml: String = _
  }

  object MavenProjectPojo {
    def fromMavenProject(mavenProject: MavenProject): MavenProjectPojo = {
      val pojo = new MavenProjectPojo
      pojo.modelVersion = mavenProject.modelVersion
      pojo.groupId = mavenProject.groupId
      pojo.artifactId = mavenProject.artifactId
      pojo.version = mavenProject.version

      // Set the parent
      if (mavenProject.parent.isEmpty) {
        pojo.parent = None
      } else {
        pojo.parent = Some(ParentPojo.fromParent(mavenProject.parent.get))
      }

      // Map the dependencies
      if (mavenProject.dependencies.isEmpty) {
        pojo.dependencies = None
      } else {
        pojo.dependencies = Some(mavenProject.dependencies.get.map(x => {
          DependencyPojo.fromDependency(x)
        }))
      }

      // Map the licenses
      if (mavenProject.licenses.isEmpty) {
        pojo.licenses = None
      } else {
        pojo.licenses = Some(mavenProject.licenses.get.map(x => {
          LicensePojo.fromLicense(x)
        }))
      }

      // Map the repositories
      if (mavenProject.repositories.isEmpty) {
        pojo.repositories = None
      } else {
        pojo.repositories = Some(mavenProject.repositories.get.map(x => {
          RepositoryPojo.fromRepository(x)
        }))
      }

      // Set the organization
      if (mavenProject.organization.isEmpty) {
        pojo.organization = None
      } else {
        pojo.organization = Some(OrganizationPojo.fromOrganization(mavenProject.organization.get))
      }

      pojo.packaging = mavenProject.packaging

      // Set the issueManagement
      if (mavenProject.issueManagement.isEmpty) {
        pojo.issueManagement = None
      } else {
        pojo.issueManagement = Some(IssueManagementPojo.fromIssueManagement(mavenProject.issueManagement.get))
      }

      // Set the SCM
      if (mavenProject.scm.isEmpty) {
        pojo.scm = None
      } else {
        pojo.scm = Some(SCMPojo.fromSCM(mavenProject.scm.get))
      }

      pojo.xml = mavenProject.xml
      pojo
    }
  }

  class DependencyPojo extends Serializable {
    var groupId: String = _
    var artifactId: String = _
    var version: Option[String] = _
    var `type`: Option[String] = _
    var scope: Option[String] = _
    var optional: Option[Boolean] = _
  }

  object DependencyPojo {
    def fromDependency(dependency: Dependency): DependencyPojo = {
      val pojo = new DependencyPojo
      pojo.groupId = dependency.groupId
      pojo.artifactId = dependency.artifactId
      pojo.version = dependency.version
      pojo.`type` = dependency.`type`
      pojo.scope = dependency.scope
      pojo.optional = dependency.optional
      pojo
    }
  }

  class LicensePojo extends Serializable {
    var name: String = _
    var url: String = _
    var distribution: String = _
    var comments: Option[String] = _
  }

  object LicensePojo {
    def fromLicense(license: License): LicensePojo = {
      val pojo = new LicensePojo
      pojo.name = license.name
      pojo.url = license.url
      pojo.distribution = license.distribution
      pojo.comments = license.comments
      pojo
    }
  }

  class RepositoryPojo extends Serializable {
    var id: String = _
    var name: String = _
    var url: String = _
  }

  object RepositoryPojo {
    def fromRepository(repository: Repository): RepositoryPojo = {
      val pojo = new RepositoryPojo
      pojo.id = repository.id
      pojo.name = repository.name
      pojo.url = repository.url
      pojo
    }
  }

  class OrganizationPojo extends Serializable {
    var name: String = _
    var url: String = _
  }

  object OrganizationPojo {
    def fromOrganization(organization: Organization): OrganizationPojo = {
      val pojo = new OrganizationPojo
      pojo.name = organization.name
      pojo.url = organization.url
      pojo
    }
  }

  class IssueManagementPojo extends Serializable {
    var system: String = _
    var url: String = _
  }

  object IssueManagementPojo {
    def fromIssueManagement(issueManagement: IssueManagement): IssueManagementPojo = {
      val pojo = new IssueManagementPojo
      pojo.system = issueManagement.system
      pojo.url = issueManagement.url
      pojo
    }
  }

  class SCMPojo extends Serializable {
    var connection: String = _
    var developerConnection: Option[String] = _
    var tag: Option[String] = _
    var url: String = _
  }

  object SCMPojo {
    def fromSCM(scm: SCM): SCMPojo = {
      val pojo = new SCMPojo
      pojo.connection = scm.connection
      pojo.developerConnection = scm.developerConnection
      pojo.tag = scm.tag
      pojo.url = scm.url
      pojo
    }
  }


  case class MavenRelease(title: String,
                          link: String,
                          description: String,
                          pubDate: Date,
                          guid: Guid)

  case class Guid(tag: String)

  case class MavenReleaseExt(title: String,
                             link: String,
                             description: String,
                             pubDate: Date,
                             guid: Guid,
                             project: MavenProject)

  case class MavenProject(
                           modelVersion: String,
                           groupId: String,
                           artifactId: String,
                           version: String,
                           parent: Option[Parent],
                           dependencies: Option[List[Dependency]],
                           licenses: Option[List[License]],
                           repositories: Option[List[Repository]],
                           organization: Option[Organization],
                           packaging: Option[String],
                           issueManagement: Option[IssueManagement],
                           scm: Option[SCM],
                           xml: String)

  case class SCM(connection: String,
                 developerConnection: Option[String],
                 tag: Option[String],
                 url: String)

  case class Organization(name: String,
                          url: String)

  case class IssueManagement(system: String,
                             url: String)

  case class Parent(groupId: String,
                    artifactId: String,
                    version: String,
                    relativePath: Option[String])

  case class License(name: String,
                     url: String,
                     distribution: String,
                     comments: Option[String])


  case class Repository(id: String,
                        name: String,
                        url: String)

  case class Dependency(groupId: String,
                        artifactId: String,
                        version: Option[String],
                        `type`: Option[String],
                        scope: Option[String],
                        optional: Option[Boolean])

}
