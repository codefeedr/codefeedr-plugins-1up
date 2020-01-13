package org.tudelft.plugins.npm.protocol

import java.util.Date

/**
 * Contains all the case classes and POJO equivalent classes to represent a NPM package release
 *
 * @author Roald van der Heijden
 * Date: 2019-12-19 (YYYY-MM-DD)
 */
object Protocol {

  case class NpmRelease(name          : String,
                        retrieveDate  : Date) // using ingestion time

  case class NpmReleaseExt(name         : String,
                           retrieveDate : Date,
                           project      : NpmProject)

  case class NpmProject(_id             : String,
                        _rev            : Option[String],
                        name            : String,
                        author          : Option[String],
                        authorObject    : Option[PersonObject],
                        contributors    : Option[List[PersonObject]],
                        description     : Option[String],
                        homepage        : Option[String],
                        keywords        : Option[List[String]],
                        license         : Option[String],
                        dependencies    : Option[List[Dependency]],
                        maintainers     : List[PersonObject],
                        readme          : String,
                        readmeFilename  : String,
                        bugs            : Option[Bug],
                        bugString       : Option[String],
                        repository      : Option[Repository],
                        time            : TimeObject)

  case class Dependency(packageName : String,
                        version     : String)

  case class PersonObject(name  : String,
                          email : Option[String],
                          url   : Option[String])

  case class Repository(`type`    : String,
                        url       : String,
                        directory : Option[String])

  case class Bug(url   : Option[String],
                 email : Option[String])

  case class TimeObject(created  : String,
                        modified : Option[String])

  // underneath is a POJO representation of all case classes mentioned above

  class NpmReleasePojo extends Serializable {
    var name: String = _
    var retrieveDate: Long = _
  }

  object NpmReleasePojo {
    def fromNpmRelease(release: NpmRelease): NpmReleasePojo = {
      val pojo = new NpmReleasePojo
      pojo.name = release.name
      pojo.retrieveDate = release.retrieveDate.getTime
      pojo
    }
  }

  class NpmReleaseExtPojo extends Serializable {
    var name: String = _
    var retrieveDate: Long = _
    var project: NpmProjectPojo = _
  }

  object NpmReleaseExtPojo {
    def fromNpmReleaseExt(release: NpmReleaseExt): NpmReleaseExtPojo = {
      val pojo = new NpmReleaseExtPojo
      pojo.name = release.name
      pojo.retrieveDate = release.retrieveDate.getTime
      pojo.project = NpmProjectPojo.fromNpmProject(release.project)
      pojo
    }
  }

  class NpmProjectPojo extends Serializable {
    var _id: String = _
    var _rev: String = _
    var name: String = _
    var author : String = _
    var authorObject: PersonObjectPojo = _
    var contributors: List[PersonObjectPojo] = _
    var description: String = _
    var homepage: String = _
    var keywords: List[String] = _
    var license: String = _
    var dependencies: List[DependencyPojo] = _
    var maintainers: List[PersonObjectPojo] = _
    var readme: String = _
    var readmeFilename: String = _
    var bugs: BugPojo = _
    var bugString: String = _
    var repository: RepositoryPojo = _
    var time: TimePojo = _
  }

  object NpmProjectPojo {
    def fromNpmProject(project: NpmProject): NpmProjectPojo = {
      val pojo = new NpmProjectPojo
      /*
         new NpmProjectPojo(project._id, project._rev.orNull, project.name, project.author.orNull, authorObject, contributors,
        project.description.orNull, project.homepage.orNull, keywords, project.license.orNull, dependencies,
        project.maintainers.map(arg => PersonObjectPojo.fromPersonObject(arg)), project.readme, project.readmeFilename,
        bugs, project.bugString.orNull, repository, time)
       */
      pojo._id = project._id
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
      val repository = if (project.repository.isDefined) RepositoryPojo.fromRepository(project.repository.get) else null

      val time = TimePojo.fromTime(project.time)
      // now create a new POJO with these vals
      pojo
    }
  }

  class DependencyPojo(val packageName: String,
                       val version: String) extends Serializable {}

  object DependencyPojo {
    def fromDependency(dep: Dependency): DependencyPojo = new DependencyPojo(dep.packageName, dep.version)
  }
  // added for SQL queries
  class DependencyPojoExt(val id : String, val packageName : String, val version : String)

  class PersonObjectPojo(val name: String,
                         val email: String,
                         val url: String) extends Serializable {}
  // added for SQL queries
  class PersonObjectPojoExt(val id : String, val name: String, val email : String, val url : String)

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

  // added for SQL queries
  class RepositoryPojoExt(val id: String, val `type` : String, val url : String, val directory : String)

  class BugPojo(val url: String,
                val email: String) extends Serializable {}

  object BugPojo {
    def fromBug(b: Bug): BugPojo = new BugPojo(b.url.orNull, b.email.orNull)
  }

  // added for SQL queries
  class BugPojoExt(val id : String, val url : String, val email : String)

  class TimePojo(val created: String,
                 val modified: String) extends Serializable {}

  object TimePojo {
    def fromTime(obj: TimeObject): TimePojo = new TimePojo(obj.created, obj.modified.orNull)
  }

  // added for SQL queries
  class TimePojoExt(val id : String, val created : String, val modified : String)

  override def toString : String = "Protocol companion object"
}