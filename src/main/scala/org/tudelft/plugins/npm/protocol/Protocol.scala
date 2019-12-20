package org.tudelft.plugins.npm.protocol

import java.util.Date

/**
 * Contains all the case classes and POJO equivalent classes to represent a NPM package release
 *
 * @author Roald van der Heijden
 * Date: 2019-12-19 (YYYY-MM-DD)
 */
object Protocol {

  case class NpmRelease( name         : String,
                         retrieveDate : Date) // using ingestion time

  case class NpmReleaseExt(name         : String,
                           retrieveDate : Date,
                           project      : NpmProject)

  case class NpmProject(_id             : String,
                        _rev            : Option[String],
                        name            : String,
                        author          : Option[PersonSimple],
                        authorObject    : Option[PersonObject],
                        contributors    : Option[List[PersonObject]],
                        description     : Option[String],
                        homepage        : Option[String],
                        keywords        : Option[List[String]],
                        license         : Option[String],
                        dependencies    : Option[List[DependencyObject]],
                        maintainers     : List[PersonObject],
                        readme          : String,
                        readmeFilename  : String,
                        bugs            : Option[Bug],
                        bugString       : Option[String],
                        repository      : Option[Repository],
                        time            : TimeObject
                       )

  case class DependencyObject( packageName : String,
                               version     : String)
  case class PersonObject( name   : String,
                           email  : Option[String],
                           url    : Option[String])

  case class PersonSimple( nameAndOptEmailOptURL : String)

  case class Repository(`type`     : String,
                        url        : String,
                        directory  : Option[String])

  case class Bug( url   : Option[String],
                  email : Option[String])

  case class TimeObject( created  : String,
                         modified : Option[String])

  // underneath is a POJO representation of all case classes mentioned above

  class NpmReleasePojo extends Serializable {
    var name: String = _
    var retrieveDate: Long = _
  }

  object NpmReleasePojo {
    def fromNpmRelease(release : NpmRelease) : NpmReleasePojo = {
      val pojo = new NpmReleasePojo()
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
    def fromNpmReleaseExt(release : NpmReleaseExt) : NpmReleaseExtPojo = {
      val pojo = new NpmReleaseExtPojo()
      pojo.name = release.name
      pojo.retrieveDate = release.retrieveDate.getTime
      pojo.project = NpmProjectPojo.fromNpmProject(release.project)
      pojo
    }
  }

  class NpmProjectPojo extends Serializable {
    var _id: String = _
    var _rev: Option[String] = _
    var name: String = _
    var author: Option[PersonSimplePojo] = _
    var authorObject: Option[PersonObjectPojo] = _
    var contributors: Option[List[PersonObjectPojo]] = _
    var description: Option[String] = _
    var homepage: Option[String] = _
    var keywords: Option[List[String]] = _
    var license: Option[String] = _
    var dependencies: Option[List[DependencyPojo]] = _
    var maintainers: List[PersonObjectPojo] = _
    var readme: String = _
    var readmeFilename: String = _
    var bugs: Option[BugPojo] = _
    var bugString: Option[String] = _
    var repository: Option[RepositoryPojo] = _
    var time: TimePojo = _
  }

  object NpmProjectPojo {
    def fromNpmProject(project: NpmProject): NpmProjectPojo = {
      val pojo = new NpmProjectPojo()
      pojo._id = project._id
      pojo._rev = project._rev
      pojo.name = project.name

      // Transform author
      pojo.author =
        if (project.author.isEmpty) None
        else Some(PersonSimplePojo.fromPersonSimple(project.author.get))

      // Transform the authorObject
      pojo.authorObject =
        if (project.author.isEmpty) None
        else Some(PersonObjectPojo.fromPersonObject(project.authorObject.get))

      // Map contributors
      pojo.contributors =
        if (project.contributors.isEmpty) None
        else Some(project.contributors.get.map(person => PersonObjectPojo.fromPersonObject(person)))

      pojo.description = project.description
      pojo.homepage = project.homepage

      // map the keywords
      pojo.keywords =
        if (project.keywords.isEmpty) None
        else Some(project.keywords.get)

      pojo.license = project.license

      // map the dependencies
      pojo.dependencies =
        if (project.dependencies.isEmpty) None
        else Some(project.dependencies.get.map(x => DependencyPojo.fromDependency(x)))

      pojo.maintainers = project.maintainers.map(arg => PersonObjectPojo.fromPersonObject(arg))
      pojo.readme = project.readme
      pojo.readmeFilename = project.readmeFilename

      // transform the bug
      pojo.bugs =
        if (project.bugs.isEmpty) None
        else Some(BugPojo.fromBug(project.bugs.get))

      pojo.bugString = project.bugString

      // transform the repo
      pojo.repository =
        if (project.repository.isEmpty) None
        else Some(RepositoryPojo.fromRepository(project.repository.get))
      pojo.time = TimePojo.fromTime(project.time)
      pojo
    }
  }

  class DependencyPojo extends Serializable {
    var packageName: String = _
    var version: String = _
  }

  object DependencyPojo {
    def fromDependency(dep : DependencyObject) : DependencyPojo = {
      val pojo = new DependencyPojo()
      pojo.packageName = dep.packageName
      pojo.version = dep.version
      pojo
    }
  }

  class PersonObjectPojo extends Serializable {
    var name: String = _
    var email: Option[String] = _
    var url: Option[String] = _
  }

  object PersonObjectPojo {
    def fromPersonObject(person : PersonObject) : PersonObjectPojo = {
      val pojo = new PersonObjectPojo
      pojo.name = person.name
      pojo.email = person.email
      pojo.url = person.url
      pojo
    }
  }

  class PersonSimplePojo extends Serializable {
    var nameAndOptEmailOptUrl: String = _
  }

  object PersonSimplePojo {
    def fromPersonSimple(person : PersonSimple): PersonSimplePojo = {
      val pojo = new PersonSimplePojo()
      pojo.nameAndOptEmailOptUrl = person.nameAndOptEmailOptURL
      pojo
    }
  }

  class RepositoryPojo extends Serializable {
    var `type`: String = _
    var url: String = _
    var directory: Option[String] = _
  }

  object RepositoryPojo {
    def fromRepository(r: Repository): RepositoryPojo = {
      val pojo = new RepositoryPojo()
      pojo.`type` = r.`type`
      pojo.url = r.url
      pojo.directory = r.directory
      pojo
    }
  }

  class BugPojo extends Serializable {
    var url: Option[String] = _
    var email: Option[String] = _
  }

  object BugPojo {
    def fromBug(b : Bug) : BugPojo = {
      val pojo = new BugPojo()
      pojo.url = b.url
      pojo.email = b.email
      pojo
    }
  }

  class TimePojo extends Serializable {
    var created: String = _
    var modified: Option[String] = _
  }

  object TimePojo {
    def fromTime(obj : TimeObject ) : TimePojo = {
      val pojo = new TimePojo()
      pojo.created = obj.created
      pojo.modified = obj.modified
      pojo
    }
  }
}