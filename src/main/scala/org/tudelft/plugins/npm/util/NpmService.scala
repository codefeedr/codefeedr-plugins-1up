package org.tudelft.plugins.npm.util

import org.apache.logging.log4j.scala.Logging
import org.codefeedr.stages.utilities.HttpRequester
import org.tudelft.plugins.npm.protocol.Protocol.{DependencyObject, NpmProject, TimeObject}
import org.tudelft.plugins.npm.protocol.Protocol.NpmReleaseExt
import scalaj.http.Http
import org.json4s.{DefaultFormats, Formats}
import org.json4s.ext.JavaTimeSerializers
import org.json4s.JsonAST._
import org.jsoup.Jsoup
import org.json4s.jackson.Serialization.read
import org.json4s.jackson.JsonMethods._
import java.util.Date

/**
 * Services to retrieve a project from the NPM API registry.
 *
 * @author Roald van der Heijden
 * Date: 2019-12-01 (YYYY-MM-DD)
 */
object NpmService extends Logging with Serializable {

  /**
   * Extraction formats.
   */
  lazy implicit val formats: Formats = new DefaultFormats {} ++ JavaTimeSerializers.all

  /**
   * the API url to retrieve projects from.
   */
  val url_packageInfo = "http://registry.npmjs.com/"

  /**
   * Retrieves a Npm project, resulting in the Protocol case class with information filled in where possible.
   *
   * @param projectName the name of the project.
   * @return an optional NpmProject.
   */
  def getProject(projectName: String): Option[NpmReleaseExt] = {
    // retrieve the project, returns complex HTML string
    val rawProject = getProjectRaw(projectName)

    // preliminary checks if the project request got through ok
    if (rawProject.isEmpty || rawProject.get == """{"error":"Not found"}""") {
      logger.error(s"Couldn't retrieve npm project with name $projectName.")
      return None
    }

    // some intermediate setup values culminating in the parsing of the project into a case class
    val doc = Jsoup.parse(rawProject.get)

    //If the parser fails in the future, its because of this line
    val escapedString = doc.body().text() + doc.body().data()

    val jsonString = escapedString.slice(escapedString.indexOf('{'), escapedString.lastIndexOf('}') + 1)

    var myJsonProject: NpmProject = null
    try{
      myJsonProject = read[NpmProject](jsonString: String)
    }
    catch{
      case _: Throwable =>
        None
    }


    // STEP 1: now set the time right (find the created / modified field and update the time)

    val json = parse(jsonString)

    // TODO separate method!

    val timelog = (json \ "time")

    // first get the field with id "created", if it's a JNothing, the extract[String] will throw an exception!
    // so case match to convert JNothing to JString, or otherwise leave it be
    // since this is not an Option[String], we don't need to match twice
    val createdField = timelog \ "created" match {
      case JNothing         => JString("unknown")
      case otherStuff       => otherStuff
    }
    val createdValue = createdField match {
      case JString("unknown")   => "unknown" // design choice
      case JString(s : String)  => createdField.extract[String]
      //case _                    => "unknown"
    }

    // with the modified field it's different, if it's not in it, we need to accumulate a None, otherwise a Some(s...)
    // the intermediate step is because extract[String] can't handle JNothing -> None / JString(s) -> Some(s)
    val modifiedField = (timelog \ "modified" ) match {
      case JNothing       => JString("unknown")
      case otherStuff       => otherStuff
    }
    val modifiedValue = modifiedField match {
      case JString("unknown")   => None
      case JString(s : String)  => Some(s)
      //case _                    => Some("unknown")
    }

    val myTimeObject = TimeObject(createdField.extract[String], modifiedValue)


    // STEP 2 : Now lookup the dependencies

    val firstLookupLatest = (json \ "dist-tags") \ "latest"

    val versionNroflatestVersion = firstLookupLatest match {
      case JString(x) => x
      case _ => "-1"
    }

    // then get me that version object with all info
    val dependenciesObject = (( json \ "versions" ) \ versionNroflatestVersion ) \ "dependencies"
    val dependenciesList = dependenciesObject match {
          case JObject(lijstje) => lijstje
          case _ => Nil
    }

    val finalListOfTuplesWithDeps = dependenciesList.map { case (x, JString(y)) => DependencyObject(x,y) }


    // STEP 3: Update the Case Class with the results of time & dependencies

    val myNpmProject = myJsonProject.copy(time = myTimeObject, dependencies = Some(finalListOfTuplesWithDeps))
    return Some(NpmReleaseExt(projectName, new Date(), myNpmProject))
  }

  /**
   * Returns a project as a raw string.
   *
   * @param endpoint the end_point to do the request.
   * @return an optional String.
   */
  def getProjectRaw(endpoint : String): Option[String] = {
    val response = try {
      val request = Http(url_packageInfo + endpoint).headers(withConfiguredHeaders)
      new HttpRequester().retrieveResponse(request)
    } catch {
      case _: Throwable => return None
    }
    Some(response.body)
  }

  /**
   * Add a user-agent with contact details.
   */
  def withConfiguredHeaders: List[(String, String)] =
    ("User-Agent", "CodeFeedr-Npm/1.0 Contact: zonneveld.noordwijk@gmail.com") :: Nil
}