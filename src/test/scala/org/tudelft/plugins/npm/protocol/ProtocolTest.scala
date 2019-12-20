package org.tudelft.plugins.npm.protocol

import java.util.{Calendar, Date}
import org.scalatest.FunSuite
import org.tudelft.plugins.npm.protocol.Protocol.{Bug, BugPojo, DependencyObject, DependencyPojo, NpmProject, NpmProjectPojo, NpmRelease, NpmReleaseExt, NpmReleaseExtPojo, NpmReleasePojo, PersonObject, PersonObjectPojo, PersonSimple, PersonSimplePojo, Repository, RepositoryPojo, TimeObject, TimePojo}

/**
 * Class to test the creation of POJO for our SQL Service (since the Datastream[NPM Case Class] will not work
 * with field referencing.
 *
 * Some initial variables are declared and then each conversion method to convert a NPM Case Class into its relevant
 * NPM POJO is tested
 *
 * @author Roald van der Heijden
 * Date: 2019 - 12 - 19 (YYYY-MM-DD)
 */
class ProtocolTest extends FunSuite {

  // variables to test the creation of POJOs from their relevant case class counterparts
  val timeobj = TimeObject("2019-02-19T06:00:04.974Z",Some("2019-12-13T07:51:00.925Z"))
  val timeEmptyobj = TimeObject("2019-02-19T06:00:04.974Z",None)
  val bugobj = Bug(Some("https://github.com/searchfe/ts2php/issues"),None)
  val emptybugobj = Bug(None, None)
  val repoobj = Repository("git","git+https://github.com/searchfe/ts2php.git",None)
  val emptyrepoobj = Repository("", "", None)
  val simplepersonobj = PersonSimple("Barney Rubble <b@rubble.com> (http://barnyrubble.tumblr.com/)")
  val personobj = PersonObject("cxtom",Some("cxtom2010@gmail.com"),None)
  val emptypersonobj = PersonObject("", None, None)
  val dependencyobj = DependencyObject("semver","^6.2.0")

  val bigProject = NpmProject(
    "ts2php",
    Some("82-79c18b748261d1370bd45e0efa753721"),
    "ts2php",
    None,
    None,
    Some(List(PersonObject("cxtom",Some("cxtom2008@gmail.com"),None))),
    Some("TypeScript to PHP Transpiler"),
    Some("https://github.com/searchfe/ts2php#readme"),
    None,
    Some("MIT"),
    Some(List(DependencyObject("fs-extra","^7.0.1"), DependencyObject("lodash","^4.17.14"), DependencyObject("semver","^6.2.0"))),
    List(PersonObject("cxtom",Some("cxtom2010@gmail.com"),None), PersonObject("meixg",Some("meixg@foxmail.com"),None)),
    "some story on how this project came to be",
    "indication where to find the above line",
    Some(Bug(Some("https://github.com/searchfe/ts2php/issues"),None)),
    None,
    Some(Repository("git","git+https://github.com/searchfe/ts2php.git",None)),
    TimeObject("2019-02-19T06:00:04.974Z",Some("2019-12-13T07:51:00.925Z"))
  )
  val now = new Date(0)
  val npmrel = NpmRelease("ts2php", now)
  val npmrele = NpmReleaseExt("ts2php", now, bigProject)

  test( "NpmRelease POJO creation") {
    val pojo = NpmReleasePojo.fromNpmRelease(npmrel)
    // Assert
    assert(pojo.name=="ts2php")
    assert(pojo.retrieveDate == 0)
  }

  test( "NpmReleaseExt POJO creation") {
    val result = NpmReleaseExtPojo.fromNpmReleaseExt(npmrele)
    // Assert
    assert(result.project.name=="ts2php")
    assert(result.retrieveDate == 0)
    assert(result.project.name=="ts2php")
    assert(result.project._id=="ts2php")
    assert(result.project._rev == Some("82-79c18b748261d1370bd45e0efa753721"))
    assert(result.project.name=="ts2php")
    assert(result.project.author.isEmpty)
    assert(result.project.authorObject.isEmpty)
    assert(result.project.bugString.isEmpty)
    assert(result.project.readme == "some story on how this project came to be")
    assert(result.project.readmeFilename == "indication where to find the above line")
    assert(result.project.contributors.get.head.email == Some("cxtom2008@gmail.com"))
    assert(result.project.dependencies.get.head.packageName=="fs-extra")
    assert(result.project.dependencies.get.last.packageName=="semver")
    assert(result.project.license == Some("MIT"))
    assert(result.project.maintainers.head.name=="cxtom")
    assert(result.project.maintainers.last.name=="meixg")
    assert(result.project.description == Some("TypeScript to PHP Transpiler"))
    assert(result.project.homepage == Some("https://github.com/searchfe/ts2php#readme"))
    assert(result.project.keywords == None)
    assert(result.project.bugs.get.url == Some("https://github.com/searchfe/ts2php/issues"))
    assert(result.project.bugString == None)
    assert(result.project.repository.get.url == "git+https://github.com/searchfe/ts2php.git")
    assert(result.project.time.modified == Some("2019-12-13T07:51:00.925Z"))
  }


  test("filled TimeObject POJO creation") {
    val result = TimePojo.fromTime(timeobj)
    // Assert
    assert(result.created == "2019-02-19T06:00:04.974Z" )
    assert(result.modified == Some("2019-12-13T07:51:00.925Z"))
  }

  test("empty TimeObject POJO creation") {
    val result = TimePojo.fromTime(timeEmptyobj)
    // Assert
    assert(result.created == "2019-02-19T06:00:04.974Z" )
    assert(result.modified == None)
  }

  test("partially filled BugObject POJO creation") {
    val result = BugPojo.fromBug(bugobj)
    // Assert
    assert(result.url == Some("https://github.com/searchfe/ts2php/issues"))
    assert(result.email == None)
  }

  test("empty BugObject POJO creation") {
    val result = BugPojo.fromBug(emptybugobj)
    // Assert
    assert(result.url == None)
    assert(result.email == None)
  }

  test("partially filled repository POJO creation") {
    val result = RepositoryPojo.fromRepository(repoobj)
    // Assert
    assert(result.`type` == "git")
    assert(result.url == "git+https://github.com/searchfe/ts2php.git")
    assert(result.directory == None)
  }

  test("empty filled repository POJO creation") {
    val result = RepositoryPojo.fromRepository(emptyrepoobj)
    // Assert
    assert(result.`type` == "")
    assert(result.url == "")
    assert(result.directory == None)
  }

  test("simple person POJO creation") {
    val result = PersonSimplePojo.fromPersonSimple(simplepersonobj)
    // Assert
    assert(result.nameAndOptEmailOptUrl == "Barney Rubble <b@rubble.com> (http://barnyrubble.tumblr.com/)")
  }

  test("partially filled Person POJO creation") {
    val result = PersonObjectPojo.fromPersonObject(personobj)
    // Assert
    assert(result.name == "cxtom")
    assert(result.email == Some("cxtom2010@gmail.com"))
    assert(result.url == None)
  }

  test("empty Person POJO creation") {
    val result = PersonObjectPojo.fromPersonObject(emptypersonobj)
    // Assert
    assert(result.name == "")
    assert(result.email == None)
    assert(result.url == None)
  }

  test("dependencies POJO creation") {
    val result = DependencyPojo.fromDependency(dependencyobj)
    // Assert
    assert(result.packageName == "semver")
    assert(result.version == "^6.2.0")
  }

  test("NPM Project POJO creation") {
    val result = NpmProjectPojo.fromNpmProject(bigProject)
    // Assert
    assert(result._id=="ts2php")
    assert(result._rev == Some("82-79c18b748261d1370bd45e0efa753721"))
    assert(result.name=="ts2php")
    assert(result.author.isEmpty)
    assert(result.authorObject.isEmpty)
    assert(result.bugString.isEmpty)
    assert(result.readme == "some story on how this project came to be")
    assert(result.readmeFilename == "indication where to find the above line")
    assert(result.contributors.get.head.email == Some("cxtom2008@gmail.com"))
    assert(result.dependencies.get.head.packageName=="fs-extra")
    assert(result.dependencies.get.last.packageName=="semver")
    assert(result.license == Some("MIT"))
    assert(result.maintainers.head.name=="cxtom")
    assert(result.maintainers.last.name=="meixg")
    assert(result.description == Some("TypeScript to PHP Transpiler"))
    assert(result.homepage == Some("https://github.com/searchfe/ts2php#readme"))
    assert(result.keywords == None)
    assert(result.bugs.get.url == Some("https://github.com/searchfe/ts2php/issues"))
    assert(result.bugString == None)
    assert(result.repository.get.url == "git+https://github.com/searchfe/ts2php.git")
    assert(result.time.modified == Some("2019-12-13T07:51:00.925Z"))
  }
}