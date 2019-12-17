package org.tudelft.plugins.npm.util

import org.apache.logging.log4j.scala.Logging
import org.codefeedr.stages.utilities.HttpRequester
import org.tudelft.plugins.npm.protocol.Protocol.NpmProject
import scalaj.http.{Http, HttpRequest}
import org.json4s.{DefaultFormats, Formats}
import org.json4s.ext.JavaTimeSerializers
import org.json4s.jackson.JsonMethods.parseOpt
import org.json4s.Extraction._
import org.json4s.JsonAST._
import org.apache.commons.lang3.StringEscapeUtils
import org.json4s
import org.jsoup.Jsoup

/** Services to retrieve a project from the NPM API. */
object NpmService extends Logging with Serializable {


  /** Extraction formats. */
  lazy implicit val formats: Formats = new DefaultFormats {} ++ JavaTimeSerializers.all

  /** the API url to retrieve projects from. */
  private val url = "https://source.runkitcdn.com/npm/"

  /** Retrieves a Npm project.
    *
    * @param projectName the name of the project.
    * @return an optional NpmProject.
    */
  def getProject(projectName: String): Option[NpmProject] = {
    val rawProject = getProjectRaw(projectName) // retrieve the project, returns complex HTML string

    if (rawProject.isEmpty) {
      logger.error(s"Couldn't retrieve npm project with name $projectName.")
      return None
    }                                                                                            // cleaning up:
    val doc = Jsoup.parse(rawProject.get)
    val escapedString = doc.body().text()                                                   // 1. unescape HTML vals
    var jsonString = escapedString.slice(escapedString.indexOf('{'), escapedString.lastIndexOf('}') + 1)        // 3. get actual json content


    val json: Option[json4s.JValue] = parseOpt(jsonString)

    if (json.isEmpty){
      logger.error(s"Couldn't retrieve npm project with name $projectName and json string $jsonString and rawproject= $rawProject")
      println("jsonString= " + jsonString)
      return None
    }

    val jsonproject = extractOpt[NpmProject](transformProject(json.get))                         // 5. turn JSON String into JSON4S Object

    if (jsonproject.isEmpty) {
      logger.error(s"Couldn't retrieve npm project with name $projectName and json $json.")
      return None
    }
    jsonproject // return the project
  }

  /** Returns a project as a raw string.
    *
    * @param endpoint the end_point to do the request.
    * @return an optional String.
    */
  def getProjectRaw(endpoint: String): Option[String] = {
    val response = try {
      val request = Http(url + endpoint).headers(getHeaders)
      new HttpRequester().retrieveResponse(request)
    } catch {
      case _: Throwable => return None
    }

    Some(response.body)
  }

  def transformProject(json: JValue): JValue =
    json transformField {
      case JField("releases", JObject(x)) => {
        val newList = x.map { y =>
          new JObject(
            List(JField("version", JString(y._1)), JField("releases", y._2)))
        }

        JField("releases", JArray(newList))
      }
    }

  /** Add a user-agent with contact details. */
  def getHeaders: List[(String, String)] =
    ("User-Agent", "CodeFeedr-Npm/1.0 Contact: zonneveld.noordwijk@gmail.com") :: Nil

}