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
                           dependencies: Option[List[Dependency]],
                           licenses: Option[List[License]],
                           repositories: Option[List[Repository]],
                           xml: String)

  case class License(name: String,
                     url: String,
                     distribution: String,
                     comments: Option[String])

  case class Repository(id: String,
                        name: String,
                        url: String)

  case class Dependency(groupId: String,
                        artifactId: String,
                        version: Option[String])

}
