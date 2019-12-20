package org.tudelft.plugins.npm.util

import org.json4s.{DefaultFormats, Formats}
import org.json4s.ext.JavaTimeSerializers
import org.json4s.jackson.Serialization.read
import org.tudelft.plugins.npm.protocol.Protocol.NpmProject
import org.scalatest.FunSuite

/**
 * Tests the <object>NpmService</object>
 *
 * @author Roald van der Heijden
 * Date: 2019-12-08 (YYYY-MM-DD)
 */
class NpmServiceTest extends FunSuite {

  test("Configuration of headers was done correct") {
    val headersList = NpmService.withConfiguredHeaders
    // Assert
    assert(headersList.size == 1)
    assert(headersList.head._1 == "User-Agent")
    assert(headersList.head._2 == "CodeFeedr-Npm/1.0 Contact: zonneveld.noordwijk@gmail.com")
  }


  test("getting a RAW of a NONEXISTING package returns a JSON ERROR string") {
    // Arrange
    val ofProject = "roaldroaldroaldroaldroald8D"
    // Act
    val result = NpmService.getProjectRaw(ofProject)
    // Assert
    assert(result.get=="""{"error":"Not found"}""")
  }

  test("getting a RAW of an EXISTING package works ok") {
    // Arrange
    val ofProject = "ts2php"
    lazy implicit val formats: Formats = new DefaultFormats {} ++ JavaTimeSerializers.all
    // Act
    val jsonString = NpmService.getProjectRaw(ofProject).get
    val myNpmProject = read[NpmProject](jsonString)
    // Assert
    assert(myNpmProject._id == "ts2php")
    assert(myNpmProject.license.get == "MIT")
    assert(myNpmProject.bugs.get.url == Some("https://github.com/searchfe/ts2php/issues"))
  }

  test("get existing project works correctly") {
    // Arrange
    val projectName = "ts2php"
    // Act
    val result = NpmService.getProject(projectName)
    // Assert
    assert(result.isDefined)
    val pid = result.get.project._id
    assert(result.get.project._id == "ts2php")
    assert(result.get.project.license.get == "MIT")
    assert(result.get.project.bugs.get.url == Some("https://github.com/searchfe/ts2php/issues"))
  }

  test (" get NON existing project will result in NONE") {
    // Arrange
    val projectName = "roaldisfinallyfinishingup_thisprojectnamedoesnotexist"
    // Act
    val result = NpmService.getProject(projectName)
    // Assert
    assert(result.isEmpty)
  }
}