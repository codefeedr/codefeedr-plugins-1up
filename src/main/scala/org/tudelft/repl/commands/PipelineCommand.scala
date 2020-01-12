package org.tudelft.repl.commands

import java.util.concurrent.Executors
import java.util.regex.Pattern

import org.codefeedr.pipeline.{Pipeline, PipelineBuilder}
import org.tudelft.plugins.SQLStage
import org.tudelft.plugins.cargo.stages.CargoReleasesStage
import org.tudelft.plugins.clearlydefined.stages.ClearlyDefinedReleasesStage
import org.tudelft.plugins.json.JsonExitStage
import org.tudelft.plugins.maven.protocol.Protocol.{MavenRelease, MavenReleaseExt}
import org.tudelft.plugins.maven.stages.{MavenReleasesExtStage, MavenReleasesStage}
import org.tudelft.plugins.npm.stages.{NpmReleasesExtStage, NpmReleasesStage}
import org.tudelft.repl.{Command, Parser, ReplEnv}

import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.concurrent.Future
import scala.concurrent.JavaConversions.asExecutionContext
import scala.util.{Failure, Success, Try}

/**
  * Class for running code in a different Thread
  */
class ApplicationThread {
  protected implicit val context =
    asExecutionContext(Executors.newSingleThreadExecutor())

  /**
    * Run incoming code as a Future
    * @param code the code to run
    */
  def run(code: => Unit) = Future(code)

}

/**
  * The pipeline command handles all input which has to do with managing pipelines
  */
object PipelineCommand extends Parser with Command {
  //Check regex
  val regex =
    """(?i)^pipeline.*$"""

  override def parse(expr: String): Option[Command] = matches(expr) match {
    case true => Option(PipelineCommand)
    case _ => None
  }

  override def apply(env: ReplEnv, input: String): (ReplEnv, Try[String]) = {
    val splitted = transformInput(input)

    //Match second argument to determine what operation to perform
    splitted(0) match {
      case "create" => addPipelineToEnv(createPipeline(splitted -= "create", env), env)
      case "start" => startPipeline(splitted -= "start", env)
      case "delete" => deletePipeline(splitted -= "delete", env)
      case "stop" => stopPipeline(splitted -= "stop", env)
      case "list" => (env, Success(listPipelines(env)))
      case x => (env, Failure(new IllegalArgumentException(s"Couldn't match second argument: $x")))
    }
  }

  /**
    * Transforms the user input to something which can be used properly
    *
    * @param str the user input
    * @return the transformed user input split into meaningful parts
    */
  def transformInput(str: String): ArrayBuffer[String] = {
    //Remove the word 'pipeline' since it no longer serves a use
    val in = str.replaceFirst("pipeline ", "")

    //Capture all groups within brackets
    val captured = new ListBuffer[String]()
    val matcher = Pattern.compile("\\((.*?)\\)").matcher(in)
    while (matcher.find()) {
      captured += matcher.group(1)
    }

    //Remove all whitespaces between ()
    val test = in.replaceAll(" (?=[^(]*\\))", "")

    //Split the input
    val splitted = test.split(" ").to[ArrayBuffer]

    //Set back the captured groups between brackets
    var counter = 0
    var i = 0
    for (s <- splitted) {
      if (s.contains("(")) {
        splitted(i) = s.replace(s.substring(s.indexOf("(") + 1, s.indexOf(")")), captured(counter))
        counter = counter + 1
      }
      i = i + 1
    }
    return splitted
  }

  /**
    * Create pipeline from user input
    *
    * @param input the user input
    * @return the resulting env
    */
  def createPipeline(input: ArrayBuffer[String], env: ReplEnv): Option[Pipeline] = {
    //Check if the name of the pipeline is already in use
    if (env.pipelines.exists(x => x._1.name == input(0))) {
      return None
    }

    //if the input is smaller than 2, there are no stages defined
    if (input.size < 2) {
      return None
    }

    //TODO currently only 'simple' pipelines are supported, need to add support for DAGs
    //Setup builder
    var builder = new PipelineBuilder().setPipelineName(input(0))
    //For each stage, build a stage
    for (i <- 1 until input.size) {
      val maybeBuilder = buildStage(input(i), builder)
      if (maybeBuilder.isEmpty) return None
      else builder = maybeBuilder.get
    }
    //Build the pipeline
    //TODO maybe add try catch here
    val pipeline = builder.build()

    //Return the new pipeline added to the env
    Some(pipeline)
  }

  /**
    * Builds a stage from a string
    *
    * @param str     the input which should correspond to a stage
    * @param builder the incoming PipelineBuilder
    * @return Some(builder) if added stage successfully, else None
    */
  def buildStage(str: String, builder: PipelineBuilder): Option[PipelineBuilder] = {
    str match {
      case "CargoReleases" => Some(builder.append(new CargoReleasesStage()))
      case "MavenReleases" => Some(builder.append(new MavenReleasesStage()))
      case "MavenReleasesExt" => Some(builder.append(new MavenReleasesExtStage()))
      case "NpmReleases" => Some(builder.append(new NpmReleasesStage()))
      case "NpmReleasesExt" => Some(builder.append(new NpmReleasesExtStage()))
      case "ClearlyDefinedReleases" => Some(builder.append(new ClearlyDefinedReleasesStage()))
      case x if x.startsWith("SQL") => {
        val query = x.substring(x.indexOf("(") + 1, x.indexOf(")"))
        //TODO MavenRelease is now hardcoded, but this should be somehow inferred
        Some(builder.append(SQLStage.createSQLStage[MavenRelease](query)))
      }
      case x if x.startsWith("Json") => buildJsonStage(x.substring(x.indexOf("[") + 1, x.indexOf("]")), builder)

      //TODO all the other stages from existing plugins, e.g. ghtorrent
      case _ => None
    }
  }


  /**
    * Takes in a string representing the type of json (exit) stage to append to the builder
    *
    * @param s The string representing the type of json (exit) stage to append
    * @param builder The builder containing all the previously built stages
    * @return a new builder with appended a json (exit) stage
    */
  def buildJsonStage(s: String, builder: PipelineBuilder): Option[PipelineBuilder] = s match {
    case "MavenRelease" => Some(builder.append(new JsonExitStage[MavenRelease]()))
    case "MavenReleaseExt" => Some(builder.append(new JsonExitStage[MavenReleaseExt]()))
    //TODO all the other stuff, but they first need to implement Jsonable
    case _ => None
  }



  /**
    * Add a pipeline to the env
    *
    * @param maybePipeline the option of pipeline to add
    * @param env           the current env
    * @return the new env containing maybePipeline
    */
  def addPipelineToEnv(maybePipeline: Option[Pipeline], env: ReplEnv): (ReplEnv, Try[String]) = {
    if (maybePipeline.isEmpty) {
      //These are 3 completely different errors, maybe try to catch them at different points
      (env, Failure(new IllegalArgumentException("Pipeline with that name already exists " +
        "or one of its stages is not recognised or no stages are defined")))
    } else {
      (ReplEnv(env.pipelines :+ (maybePipeline.get, false)), Success("Pipeline " + maybePipeline.get.name + " created"))
    }
  }

  /**
    * Start a given pipeline from $env
    *
    * @param input the pipeline to start
    * @param env   the env containing all the existing pipelines
    * @return the resulting env
    */
  def startPipeline(input: ArrayBuffer[String], env: ReplEnv): (ReplEnv, Try[String]) = {
    val pipeline = env.pipelines.find(x => x._1.name == input(0))
    if (pipeline.isEmpty) {
      (env, Failure(new IllegalArgumentException("Pipeline with name " + input(0) + " does not exist")))
    } else if (pipeline.get._2 == true) {
      (env, Failure(new IllegalArgumentException("Pipeline with name " + input(0) + " is already running")))
    } else {
      //Start the pipeline in a new thread
      val thread = new ApplicationThread
      //TODO maybe add try catch here
      thread.run(runPipeline(pipeline.get._1))
      //Return pipeline now indicated as running
      (ReplEnv(env.pipelines.filterNot(x => x._1.name == input(0)) :+ (pipeline.get._1, true)), Success("Successfully started pipeline"))
    }
  }

  //TODO select option for what mode to run in, for now startMock
  /**
    * Runs a pipeline
    *
    * @param pipeline the pipeline to be run
    */
  def runPipeline(pipeline: Pipeline): Unit = {
    pipeline.startMock()
  }

  /**
    * Deletes a given pipeline from $env
    *
    * @param input the pipeline to delete
    * @param env   the env containing all the existing pipelines
    * @return the resulsting env
    */
  def deletePipeline(input: ArrayBuffer[String], env: ReplEnv): (ReplEnv, Try[String]) = {
    //Duplicate code with start pipeline, refactor?
    val pipeline = env.pipelines.find(x => x._1.name == input(0))
    if (pipeline.isEmpty) {
      (env, Failure(new IllegalArgumentException("Pipeline with name " + input(0) + " does not exist")))
    } else if (pipeline.get._2 == true) {
      (env, Failure(new IllegalArgumentException("Pipeline with name " + input(0) + " is still running")))
    } else {
      (ReplEnv(env.pipelines.filterNot(x => x._1.name == input(0))), Success("Successfully deleted pipeline"))
    }
  }

  /**
    * Stops a given pipeline from $env
    *
    * @param input the pipeline to stop
    * @param env   the env containing all the existing pipelines
    * @return the resulting env
    */
  def stopPipeline(input: ArrayBuffer[String], env: ReplEnv): (ReplEnv, Try[String]) = {
    val pipeline = env.pipelines.find(x => x._1.name == input(0))
    if (pipeline.isEmpty) {
      (env, Failure(new IllegalArgumentException("Pipeline with name " + input(0) + " does not exist")))
    } else if (pipeline.get._2 == false) {
      (env, Failure(new IllegalArgumentException("Pipeline with name " + input(0) + " is not running")))
    } else {

      //TODO For this we need to manually stop a thread, no clue how though
      //Stop pipeline
      //      pipeline.get._1.stop()

      //Return pipeline now indicated as running
      (ReplEnv(env.pipelines.filterNot(x => x._1.name == input(0)) :+ (pipeline.get._1, false)), Success("Successfully stopped pipeline"))
    }
  }

  /**
    * Build a string containing a pretty representation of all pipelines
    *
    * @param env the environment containing all the pipelines
    * @return a pretty string represending all the pipelines
    */
  def listPipelines(env: ReplEnv): String = {
    if (env.pipelines.isEmpty) "There are no pipelines"
    else {
      var res = ""
      for ((pipeline, running) <- env.pipelines) {
        var runningString = ""
        if (running) runningString = ", Status: Running"
        else runningString = ", Status: Stopped"

        res = res + pipeline.name + runningString + "\n"
      }
      res
    }
  }
}
