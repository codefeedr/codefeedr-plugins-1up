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
                        author: Option[PersonSimple],
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

  case class PersonSimple(nameAndOptEmailOptURL: String)

  case class Repository(`type`: String,
                        url: String,
                        directory: Option[String])

  case class Bug(url: Option[String],
                 email: Option[String])

  case class TimeObject(created: String,
                        modified: Option[String])

  // underneath is a POJO representation of all case classes mentioned above (in a functional style ^_^ )

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
                        val _rev: Option[String],
                        val name: String,
                        val author: Option[PersonSimplePojo],
                        val authorObject: Option[PersonObjectPojo],
                        val contributors: Option[List[PersonObjectPojo]],
                        val description: Option[String],
                        val homepage: Option[String],
                        val keywords: Option[List[String]],
                        val license: Option[String],
                        val dependencies: Option[List[DependencyPojo]],
                        val maintainers: List[PersonObjectPojo],
                        val readme: String,
                        val readmeFilename: String,
                        val bugs: Option[BugPojo],
                        val bugString: Option[String],
                        val repository: Option[RepositoryPojo],
                        val time: TimePojo)
    extends Serializable {}

  object NpmProjectPojo {
    def fromNpmProject(project: NpmProject): NpmProjectPojo = {
      // transform author
      val author =
        if (project.author.isEmpty) None
        else Some(PersonSimplePojo.fromPersonSimple(project.author.get))

      // transform the authorObject
      val authorObject =
        if (project.authorObject.isEmpty) None
        else Some(PersonObjectPojo.fromPersonObject(project.authorObject.get))

      // map contributors
      val contributors =
        if (project.contributors.isEmpty) None
        else Some(project.contributors.get.map(person => PersonObjectPojo.fromPersonObject(person)))

      // map the keywords
      val keywords =
        if (project.keywords.isEmpty) None
        else Some(project.keywords.get)

      // map the dependencies
      val dependencies =
        if (project.dependencies.isEmpty) None
        else Some(project.dependencies.get.map(x => DependencyPojo.fromDependency(x)))

      // transform the bug
      val bugs =
        if (project.bugs.isEmpty) None
        else Some(BugPojo.fromBug(project.bugs.get))

      // transform the repo
      val repository =
        if (project.repository.isEmpty) None
        else Some(RepositoryPojo.fromRepository(project.repository.get))
      val time = TimePojo.fromTime(project.time)
      // now create a new POJO with these vals
      new NpmProjectPojo(project._id, project._rev, project.name, author, authorObject, contributors,
        project.description, project.homepage, keywords, project.license, dependencies,
        project.maintainers.map(arg => PersonObjectPojo.fromPersonObject(arg)), project.readme, project.readmeFilename,
        bugs, project.bugString, repository, time)
    }
  }

  class DependencyPojo(val packageName: String,
                       val version: String) extends Serializable {}

  object DependencyPojo {
    def fromDependency(dep: Dependency): DependencyPojo = new DependencyPojo(dep.packageName, dep.version)
  }

  class PersonObjectPojo(val name: String,
                         val email: Option[String],
                         val url: Option[String]) extends Serializable {}

  object PersonObjectPojo {
    def fromPersonObject(person: PersonObject): PersonObjectPojo =
      new PersonObjectPojo(person.name, person.email, person.url)
  }

  class PersonSimplePojo(val nameAndOptEmailOptUrl: String) extends Serializable {}

  object PersonSimplePojo {
    def fromPersonSimple(person: PersonSimple): PersonSimplePojo = new PersonSimplePojo(person.nameAndOptEmailOptURL)
  }

  class RepositoryPojo(val `type`: String,
                       val url: String,
                       val directory: Option[String]) extends Serializable {}

  object RepositoryPojo {
    def fromRepository(r: Repository): RepositoryPojo = new RepositoryPojo(r.`type`, r.url, r.directory)
  }

  class BugPojo(val url: Option[String],
                val email: Option[String]) extends Serializable {}

  object BugPojo {
    def fromBug(b: Bug): BugPojo = new BugPojo(b.url, b.email)
  }

  class TimePojo(val created: String,
                 val modified: Option[String]) extends Serializable {}

  object TimePojo {
    def fromTime(obj: TimeObject): TimePojo = new TimePojo(obj.created, obj.modified)
  }

  override def toString : String = "Protocol companion object"
}