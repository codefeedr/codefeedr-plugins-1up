package org.tudelft.plugins.maven.protocol

import java.util.Date


object Protocol {

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
                        dType: Option[String], //called type, but type is reserved word
                        scope: Option[String],
                        optional: Option[Boolean])

}
