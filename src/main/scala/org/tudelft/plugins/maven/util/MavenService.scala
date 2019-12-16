package org.tudelft.plugins.maven.util

import org.apache.logging.log4j.scala.Logging
import org.codefeedr.stages.utilities.HttpRequester
import org.tudelft.plugins.maven.protocol.Protocol.{Dependency, License, MavenProject, Repository}
import scalaj.http.Http

import scala.util.control.Breaks._
import scala.xml.XML


/** Services to retrieve a project from the Maven APi. */
object MavenService extends Logging with Serializable {

  /** Retrieve the API url. */
  private val url = "https://repo1.maven.org/maven2/"

  /** Retrieves a Maven project.
    *
    * @param projectName the name of the project.
    * @return an optional MavenProject.
    */
  def getProject(projectName: String): Option[MavenProject] = {
    /** Retrieve the project. */

    val rawProject = getProjectRaw(projectName).get
    if (rawProject.isEmpty) {
      logger.error(
        s"Couldn't retrieve Maven project with name $projectName.")

      return None
    }


    var xml: scala.xml.Node = null
    try {
      xml = XML.loadString(rawProject)
    }
    catch {
      case _: Exception => {
        logger.error(s"Couldn't convert string to xml with name $projectName.")
        return None
      }
    }

    /** Extract into an optional if it can't be parsed. */
    val project = xmlToMavenProject(xml)



    if (project.isEmpty) {
      logger.error(
        s"Couldn't retrieve Maven project with name $projectName and xml $xml.")

      return None
    }

    /** Forward the project */
    project
  }

  def xmlToMavenProject(node: scala.xml.Node): Option[MavenProject] = {
    // Set initial values
    val modelVersion = (node \ "modelVersion").text
    var groupId = ""
    var artifactId = ""
    var version = ""
    var dependencies: Option[List[Dependency]] = None
    var licenses: Option[List[License]] = None
    var repositories: Option[List[Repository]] = None

    //Sometimes groupId, artifactId and version are inside parent, sometimes not
    val parentNode = node.child.filter(item => item.label == "parent")

    //If parentnode is empty, get the fields directly from $node
    //TODO sometimes parent is different, so make different parent node
    if (parentNode.isEmpty) {
      groupId = (node \ "groupId").text
      artifactId = (node \ "artifactId").text
      version = (node \ "version").text
    }
    //Else get the fields from the parent field
    else {
      groupId = (parentNode \ "groupId").text
      artifactId = (parentNode \ "artifactId").text
      version = (parentNode \ "version").text
    }



    //Get dependencies
    val dependenciesNode = node.child.filter(item => item.label == "dependencies")
    dependencies = parseDependencies(dependenciesNode, version)

    //Get licenses
    val licencesNode = node.child.filter(item => item.label == "licenses")
    licenses = parseLicenses(licencesNode)

    //Get repositories (often empty)
    val repositoryNode = node.child.filter(item => item.label == "repositories")
    repositories = parseRepositories(repositoryNode)

    Some(MavenProject(modelVersion, groupId, artifactId, version, dependencies, licenses, repositories, node.toString()))
  }


  def parseRepositories(nodes: Seq[scala.xml.Node]): Option[List[Repository]] = {
    //If there are repositories parse them
    if (nodes.nonEmpty) {
      var repoList = List[Repository]()
      for (n <- nodes.head.child) {
        breakable {
          //The first node in the xml structure is always empty so we skip this one as it contains no info
          if (n.toString().startsWith("\n")) {
            break()
          }
          val currentId = (n \ "id").text
          val currentName = (n \ "name").text
          val currentUrl = (n \ "url").text
          repoList = Repository(currentId, currentName, currentUrl) :: repoList
        }
      }
      return Some(repoList.reverse)
    }
    //If there are no repositories, return None
    None
  }

  def parseLicenses(nodes: Seq[scala.xml.Node]): Option[List[License]] = {
    //If there are licenses parse them
    if (nodes.nonEmpty) {
      var licensesList = List[License]()
      for (n <- nodes.head.child) {
        breakable {
          //The first node in the xml structure is always empty so we skip this one as it contains no info
          if (n.toString().startsWith("\n")) {
            break()
          }
          val currentName = (n \ "name").text
          val currentUrl = (n \ "url").text
          val currentDistribution = (n \ "distribution").text
          var currentComments: Option[String] = Some((n \ "comments").text)
          if (currentComments.contains("")) {
            currentComments = None
          }

          licensesList = License(currentName, currentUrl, currentDistribution, currentComments) :: licensesList
        }
      }
      return Some(licensesList.reverse)
    }
    //If there are no licenses, return None
    None
  }

  /**
    * Returns a list of Dependencies if the xml node contains them
    *
    * @param node the list of nodes from which the depencies will be extracted
    * @return An option of a list of dependencies, Some() if @node contained them, None otherwise
    */
  def parseDependencies(node: Seq[scala.xml.Node], projectVersion: String): Option[List[Dependency]] = {
    //If there are dependencies parse them
    if (node.nonEmpty) {
      var dependencyList = List[Dependency]()
      for (n <- node.head.child) {
        breakable {
          //The first node in the xml structure is always empty so we skip this one as it contains no info
          if (n.toString().startsWith("\n")) {
            break()
          }
          val currentGroupId = (n \ "groupId").text
          val currentArtifactId = (n \ "artifactId").text
          var currentVersion: Option[String] = Some((n \ "version").text)
          if (currentVersion.contains("")) {
            currentVersion = None
          }
          if (currentVersion.get == "${project.version}"){
            currentVersion = Some(projectVersion)
          }
          dependencyList = Dependency(currentGroupId, currentArtifactId, currentVersion) :: dependencyList
        }
      }
      return Some(dependencyList.reverse)
    }
    //If there are no dependencies, return None
    None
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

  /** Add a user-agent with contact details. */
  def getHeaders: List[(String, String)] =
    ("User-Agent", "CodeFeedr-Maven/1.0 Contact: zonneveld.noordwijk@gmail.com") :: Nil

}