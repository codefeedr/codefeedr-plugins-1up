package org.tudelft.plugins.npm.util

import java.io.File

import org.json4s.JsonAST.JNothing
import org.json4s.ext.JavaTimeSerializers
import org.json4s.jackson.JsonMethods.parse
import org.json4s.jackson.Serialization.read
import org.json4s.{DefaultFormats, Formats}
import org.scalatest.FunSuite
import org.tudelft.plugins.npm.protocol.Protocol
import org.tudelft.plugins.npm.protocol.Protocol.PersonObject

import scala.io.Source

/**
 * Tests the <object>NpmService</object>
 *
 * @author Roald van der Heijden
 * Date: 2019-12-08 (YYYY-MM-DD)
 */
class NpmServiceTest extends FunSuite {

  // variables to test conditions for getProjectRaw & convertProjectFrom
  val correct_base_url = "http://registry.npmjs.com/"
  val incorrect_base_url = "http://somenonexistentdomain.com"
  val nonExistingProject = "roaldroaldroaldroaldroald8D"
  val unPublishedProject = "/@lizheng11/t1"
  val existingProject = "ts2php"

  // variables simulating certain conditions in JSON for testing dependencies extraction method
  val jsonStringWithNoDistTags = """{"_id": "@bamblehorse/tiny","_rev": "2-9526bb8fdedb67c1fe82abbad9de9a4f","name": "@bamblehorse/tiny" }"""

  // variables simulating incorrect JSON (used e.g. in convertProjectFrom
  val incorrectJsonString = """{"_id": "@bamblehorse/tiny","_rev": }"""

  // variables simulating certain conditions in JSON for testing time extraction method
  val jsonNoTime = """{ "_id": "@roald/test" } """
  val jsonIncorrectTimeType = """{"name":"luca", "id": "1q2w3e4r5t", "time": 26, "url":"http://www.nosqlnocry.wordpress.com"}"""
  val jsonEmptyTime = """{"_id": "@lizheng11/t1","_rev": "19-a7bf0b8b42b6b157b2b0f490a7222227","name": "@lizheng11/t1","time": {}}"""
  val jsonTimeOk = """{"time": {"created": "2019-07-22T11:41:29.226Z","1.0.0": "2019-07-22T11:41:29.631Z","modified": "2019-07-24T12:04:56.497Z","2.0.0": "2019-07-24T12:04:52.956Z"}}"""
  val jsonTimeNoCreated = """{"time": {"1.0.0": "2011-07-22T11:41:29.631Z","modified": "2011-07-24T12:04:56.497Z","2.0.0": "2011-07-24T12:04:52.956Z"}}"""
  val jsonTimeNoModified = """{"time": {"created": "2001-07-22T11:41:29.226Z","1.0.0": "2001-07-22T11:41:29.631Z","2.0.0": "2001-07-24T12:04:52.956Z"}}"""
  val jsonTimeBothFieldsMissing = """{"time": {"1.0.0": "1980-07-22T11:41:29.631Z","2.0.0": "1980-07-24T12:04:52.956Z"}}"""

  // variable simulating conditions in JSON for testing author extraction method
  val jsonAuthorString = """{
	"author": "Barney Rubble < b @rubble.com > (http: //barnyrubble.tumblr.com/)"
}"""

  // test for withConfiguredHeaders


  test("Configuration of headers was done correct") {
    val headersList = NpmService.withConfiguredHeaders
    // Assert
    assert(headersList.size == 1)
    assert(headersList.head._1 == "User-Agent")
    assert(headersList.head._2 == "CodeFeedr-Npm/1.0 Contact: zonneveld.noordwijk@gmail.com")
  }

  // tests for getProjectRaw

  test("getProjectRAW - fetching a NONEXISTING Npm package returns a JSON ERROR string") {
    val result = NpmService.getProjectRaw(correct_base_url, nonExistingProject)
    assert(result.get=="""{"error":"Not found"}""")
  }

  test("getProjectRaw - fetching an UNPUBLISHED Npm package returns some JSON string") {
    // Act
    val result = NpmService.getProjectRaw(correct_base_url, unPublishedProject)
    val json = parse(result.get)
    val unpublishedTimeField =  (json \ "time") \ "unpublished"

    // Assert
      // maybe too broad of an assumption?
      // could also check for \ "time" and then check it's a JString ... but then would the string
      // be a date format? Check again?
    assert(unpublishedTimeField != JNothing)
  }

  test("getProjectRAW - fetching a EXISTING Npm package returns a good JSON string") {
    val optionString = NpmService.getProjectRaw(correct_base_url, existingProject)
    assert(optionString.isInstanceOf[Option[String]])
  }


  // tests for getProject

  test("getProject - fetching an UNPUBLISHED Npm package yields None") {
    val result = NpmService.getProject(unPublishedProject)
    assert(result.isEmpty)
  }

  test("getProject - fetching an EXISTING project works correctly") {
    // Act
    val result = NpmService.getProject(existingProject)
    // Assert
    assert(result.isDefined)
    val pid = result.get.project._id
    assert(result.get.project._id == "ts2php")
    assert(result.get.project.license.get == "MIT")
    assert(result.get.project.bugs.get.url == Some("https://github.com/searchfe/ts2php/issues"))
  }


  // tests for createJsonString


  test("createjsonString - working ok") {
    // Act 1
    val jsonString = NpmService.createJsonStringFor(correct_base_url , existingProject)
    println(jsonString)
    // Assert 1
    assert(jsonString.isInstanceOf[Option[String]])
    // Act 2
    implicit val formats: Formats = new DefaultFormats {} ++ JavaTimeSerializers.all
    val result = read[Protocol.NpmProject](jsonString.get)
    // Assert 2
    assert(result._id == "ts2php")
    assert(result.dependencies.isEmpty) // upon first reading this is Empty, we need to extract this from somehwere in the time field
    assert(result.contributors.get.size==1)
  }

  test("createjsonString - unpublished npm package results in Some(...)") {
    // Act 1
    val jsonString = NpmService.createJsonStringFor(correct_base_url, unPublishedProject)
    // Assert 1
    assert(jsonString.isInstanceOf[Option[String]])
    assert(jsonString.get != "")
    // Act 2
    val id = parse(jsonString.get) \ "_id"
    // Assert 2
    assert(id.values == "@lizheng11/t1")
  }

  test("createJsonString - incorrectly specified npm package url results in None") {
    val jsonString = NpmService.createJsonStringFor(correct_base_url, nonExistingProject)
    assert(jsonString == None)
  }


  // Time extraction tests


  test("Extracing time - with incorrectly typed subfields should return unknown/None") {
    // Arrange
    val json = parse(jsonIncorrectTimeType)
    // Act
    val result = NpmService.extractTimeFrom(json)
    // Assert
    assert(result.created == "unknown")
    assert(result.modified == None)
  }

  test("Extracting time - from a json string with NO time field returns unknown/None") {
    // Arrange
    val json = parse(jsonNoTime)
    // Act
    val result = NpmService.extractTimeFrom(json)
    // Assert
    assert(result.created == "unknown")
    assert(result.modified == None)
  }

  test("Extracting time - from a json string with no created and no modified field returns unknown/None") {
    // Arrange
    val json = parse(jsonTimeBothFieldsMissing)
    // Act
    val result = NpmService.extractTimeFrom(json)
    // Assert
    assert(result.created == "unknown")
    assert(result.modified == None)
  }

  test("Extracting time - from a json string with no created field returns unknown/Some(...)") {
    // Arrange
    val json = parse(jsonTimeNoCreated)
    // Act
    val result = NpmService.extractTimeFrom(json)
    // Assert
    assert(result.created == "unknown")
    assert(result.modified == Some("2011-07-24T12:04:56.497Z"))
  }

  test("Extracting time - from a json string with no modified field returns value/None") {
    // Arrange
    val json = parse(jsonTimeNoModified)
    // Act
    val result = NpmService.extractTimeFrom(json)
    // Assert
    assert(result.created == "2001-07-22T11:41:29.226Z")
    assert(result.modified == None)
  }

  test("Extracting time - from a json string with both created/modified present returns value/Some(...)") {
    // Arrange
    val json  = parse(jsonTimeOk)
    // Act
    val result = NpmService.extractTimeFrom(json)
    // Assert
    assert(result.created == "2019-07-22T11:41:29.226Z")
    assert(result.modified == Some("2019-07-24T12:04:56.497Z"))
  }


  // Dependencies extraction tests


  test("Extracting dependencies - Homophone: Fire the Nun") {
    // Fires the special case resulting in None in the method, just before flatMapping and returning the result
    // Arrange
    val file = new File("src/test/resources/npm/test-data/firetheNun.json")
    // Act
    val json = parse(file)
    val result = NpmService.extractDependenciesFrom(json)
    // Assert
    assert(result.isEmpty)
  }

  test("Extracting dependencies - failing due to the absence of dist - tags from a JSON String fails and results in Nil") {
    // Arrange
    val json = parse(jsonStringWithNoDistTags)
    // Act
    val projectWithoutDistTags = NpmService.extractDependenciesFrom(json)
    // Assert
    assert(projectWithoutDistTags == Nil)
  }

  test("Extracting dependencies - failing on nonexistent dependencies returns Nil") {
    // Arrange
    val file = new File("src/test/resources/npm/test-data/tiny.json")
    val json = parse(file)
    // Act
    val projectWithoutDependencies = NpmService.extractDependenciesFrom(json)
    // Assert
    assert(projectWithoutDependencies==Nil)
  }

  test("Extracting dependencies - failing on existing but empty dependencies field returns Nil") {
    // Arrange
    val file = new File("src/test/resources/npm/test-data/bslet.json")
    val json  = parse(file)
    // Act
    val projectWithoutDependencies = NpmService.extractDependenciesFrom(json)
    // Assert
    assert(projectWithoutDependencies==Nil)
  }

  test("Extracting existing dependencies works ok") {
    // Arrange
    val file = new File("src/test/resources/npm/test-data/ts2php.json")
    val json = parse(file)
    // Act
    val projectWithoutDependencies: List[Protocol.Dependency] = NpmService.extractDependenciesFrom(json)
    // Assert
    assert(projectWithoutDependencies.size == 5)
    assert(projectWithoutDependencies.head.packageName=="fs-extra")
    assert(projectWithoutDependencies.last.packageName=="yargs")
  }


  // tests for author

  // what happens goes from root level to children to grab author, not one lower in versions..
  // Steps to determine author
  // what happens if you have a string and parse it as author CO?

  // algorithm...
  // 1. check if root child has author field, try to parse CO or else String
  // 2. check latest for author object, try to parse CO else String
  // 3. else unknown

  // bslet: no author field at all => unknown
  // tiny: co-only author => only name, None, None or name, null, null in case of pojo
  // mocked String coAuthor only name, string mentions name, email => then coAuthor is leading, only CO with name
  // mocked String with no author in root child, but author as co with name, email, and string contains, name, email, url, => co name, email

  // react : only previous versions mention author, Jeff Barczewski, jeff.barczewski@gmail.com => in this situation author will stay UNKNOWN

  test("debug test author") {
    val file = new File("src/test/resources/npm/test-data/nrk-authortest.json")
    val json  = parse(file)
    // Act
    val author = NpmService.extractAuthorFrom(json)
    // Assert
    assert(author.get.name == "NRK")
    assert(author.get.email.get == "opensource@nrk.no")
    assert(author.get.url.get == "https://www.nrk.no/")

  }

  // test for buildrelextusing

  test("buildNpmReleaseExt - simple test works") {
    // Arrange
    val jsonString = Source.fromFile("src/test/resources/npm/test-data/bslet.json").getLines().mkString
    // Act
    val npmProject = NpmService.convertProjectFrom(jsonString).get
    val result = NpmService.buildNpmReleaseExtUsing("bslet", jsonString, npmProject)
    // Assert
    assert(result.name == "bslet")
    assert(result.project.maintainers.head.name == "mrmurphy")
    assert(result.project.license.get == "MIT")
  }

  // Test for convertProjectFrom


    test("convertProjectFrom - an UNPUBLISHED npmproject json string will return None") {
    // Arrange
    val jsonString = Source.fromFile("src/test/resources/npm/test-data/unpublished.json").getLines().mkString
    // Act
    val npmProject = NpmService.convertProjectFrom(jsonString)
    // Assert
    assert(npmProject.isEmpty)
  }

    test("convertProjectFrom - Incorrect json String will fail and result in None") {
      // Act
      val json = NpmService.convertProjectFrom(incorrectJsonString)
      // Assert
      assert(json.isEmpty)
    }

    test("convertProjectFrom - converting a correct json String will result in an NpmProject") {
      // Arrange
      val jsonString = Source.fromFile("src/test/resources/npm/test-data/bslet.json").getLines().mkString
      // Act
      val npmProject = NpmService.convertProjectFrom(jsonString)
      // Assert
      assert(npmProject.isDefined)
      assert(npmProject.get.isInstanceOf[Protocol.NpmProject])
      assert(npmProject.get.maintainers.head.name == "mrmurphy")
    }

    test("boilerplate test - testing toString of NpmService Companion Object") {
      // Assert
      assert(NpmService.toString == "NpmService Companion Object")
    }


}

/* Notes
TODO
 - fix the Author == None bug
 - fix code duplication
 - possibly deploy Futures in testing to test bogus domains
 */