package org.tudelft.plugins.npm.protocol

import java.util.Date

/**
 * Contains all the case classes and POJO equivalent classes to represent a NPM package release
 *
 * @author Roald van der Heijden
 * Date: 2019-12-19 (YYYY-MM-DD)
 */
object Protocol {

  case class NpmRelease(name: String,
                        retrieveDate: Date) // using ingestion time

  case class NpmReleaseExt(name: String,
                           retrieveDate: Date,
                           project: NpmProject)

  case class NpmProject(_id: String,
                        _rev: Option[String],
                        name: String,
                        author:Option[String],
                        authorObject: Option[PersonObject],
                        contributors: Option[List[PersonObject]],
                        description: Option[String],
                        homepage: Option[String],
                        keywords: Option[List[String]],
                        license: Option[String],
                        dependencies: Option[List[Dependency]],
                        maintainers: List[PersonObject],
                        readme: String,
                        readmeFilename: String,
                        bugs: Option[Bug],
                        bugString: Option[String],
                        repository: Option[Repository],
                        time: TimeObject
                       )

  case class Dependency(packageName: String,
                        version: String)

  case class PersonObject(name: String,
                          email: Option[String],
                          url: Option[String])

  case class Repository(`type`: String,
                        url: String,
                        directory: Option[String])

  case class Bug(url: Option[String],
                 email: Option[String])

  case class TimeObject(created: String,
                        modified: Option[String])

  // underneath is a POJO representation of all case classes mentioned above

  class NpmReleasePojo(val name: String,
                       val retrieveDate: Long) extends Serializable {}

  object NpmReleasePojo {
    def fromNpmRelease(release: NpmRelease): NpmReleasePojo =
      new NpmReleasePojo(release.name, release.retrieveDate.getTime)
  }

  class NpmReleaseExtPojo(val name: String,
                          val retrieveDate: Long,
                          val project: NpmProjectPojo) extends Serializable {}

  object NpmReleaseExtPojo {
    def fromNpmReleaseExt(release: NpmReleaseExt): NpmReleaseExtPojo =
      new NpmReleaseExtPojo(release.name, release.retrieveDate.getTime, NpmProjectPojo.fromNpmProject(release.project))
  }

  class NpmProjectPojo(
                        val _id: String,
                        val _rev: String,
                        val name: String,
                        val author : String,
                        val authorObject: PersonObjectPojo,
                        val contributors: List[PersonObjectPojo],
                        val description: String,
                        val homepage: String,
                        val keywords: List[String],
                        val license: String,
                        val dependencies: List[DependencyPojo],
                        val maintainers: List[PersonObjectPojo],
                        val readme: String,
                        val readmeFilename: String,
                        val bugs: BugPojo,
                        val bugString: String,
                        val repository: RepositoryPojo,
                        val time: TimePojo)
    extends Serializable {}

  object NpmProjectPojo {
    def fromNpmProject(project: NpmProject): NpmProjectPojo = {

      // transform the authorObject
      val authorObject = if (project.authorObject.isDefined) {
        PersonObjectPojo.fromPersonObject(project.authorObject.get)
      } else null

      // map contributors
      val contributors =  if (project.contributors.isDefined) {
        project.contributors.get.map(person => PersonObjectPojo.fromPersonObject(person))
      } else null

      // map the keywords
      val keywords = if (project.keywords.isDefined) {
        project.keywords.get
      } else null

      // map the dependencies
      val dependencies =
        if (project.dependencies.isDefined) {
          project.dependencies.get.map(x => DependencyPojo.fromDependency(x))
        } else null

      // transform the bug
      val bugs = if (project.bugs.isDefined) {
        BugPojo.fromBug(project.bugs.get)
      } else null

      // transform the repo
      val repository = RepositoryPojo.fromRepository(project.repository.getOrElse(null))
      // could do if(isDefined) and .get and nothing otherwise, but compiler was nagging type of repository to be Any
      // so either had to add "else null" or use this form

      val time = TimePojo.fromTime(project.time)
      // now create a new POJO with these vals
      new NpmProjectPojo(project._id, project._rev.orNull, project.name, project.author.orNull, authorObject, contributors,
        project.description.orNull, project.homepage.orNull, keywords, project.license.orNull, dependencies,
        project.maintainers.map(arg => PersonObjectPojo.fromPersonObject(arg)), project.readme, project.readmeFilename,
        bugs, project.bugString.orNull, repository, time)
    }
  }

  class DependencyPojo(val packageName: String,
                       val version: String) extends Serializable {}

  object DependencyPojo {
    def fromDependency(dep: Dependency): DependencyPojo = new DependencyPojo(dep.packageName, dep.version)
  }

  class PersonObjectPojo(val name: String,
                         val email: String,
                         val url: String) extends Serializable {}

  object PersonObjectPojo {
    def fromPersonObject(person: PersonObject): PersonObjectPojo =
      new PersonObjectPojo(person.name, person.email.orNull, person.url.orNull)
  }

  class RepositoryPojo(val `type`: String,
                       val url: String,
                       val directory: String) extends Serializable {}

  object RepositoryPojo {
    def fromRepository(r: Repository): RepositoryPojo = new RepositoryPojo(r.`type`, r.url, r.directory.orNull)
  }

  class BugPojo(val url: String,
                val email: String) extends Serializable {}

  object BugPojo {
    def fromBug(b: Bug): BugPojo = new BugPojo(b.url.orNull, b.email.orNull)
  }

  class TimePojo(val created: String,
                 val modified: String) extends Serializable {}

  object TimePojo {
    def fromTime(obj: TimeObject): TimePojo = new TimePojo(obj.created, obj.modified.orNull)
  }

  override def toString : String = "Protocol companion object"
}