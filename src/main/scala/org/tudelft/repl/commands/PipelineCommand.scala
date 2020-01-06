package org.tudelft.repl.commands

import java.util.concurrent.Executors

import org.codefeedr.pipeline.{Pipeline, PipelineBuilder}
import org.tudelft.plugins.cargo.stages.CargoReleasesStage
import org.tudelft.plugins.clearlydefined.stages.ClearlyDefinedReleasesStage
import org.tudelft.plugins.maven.stages.{MavenReleasesExtStage, MavenReleasesStage, SQLStage}
import org.tudelft.plugins.npm.stages.{NpmReleasesExtStage, NpmReleasesStage}
import org.tudelft.repl.{Command, Parser, ReplEnv}

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import scala.concurrent.JavaConversions.asExecutionContext
import scala.util.{Failure, Success, Try}

/**
  * Class for running code in a different Thread
  */
class ApplicationThread {
  protected implicit val context =
    asExecutionContext(Executors.newSingleThreadExecutor())

  def run(code: => Unit) = Future(code)

}

object PipelineCommand extends Parser with Command {
  //Check regex
  val regex =
    """(?i)^pipeline.*$"""

  override def parse(expr: String): Option[Command] = matches(expr) match {
    case true => Option(PipelineCommand)
    case _ => None
  }

  override def apply(env: ReplEnv, input: String): (ReplEnv, Try[String]) = {
    //Remove the word 'pipeline' since it no longer serves a use
    val newInput = input.replaceFirst("pipeline ", "")

    //Inside the (first) [], remove the spaces
    //    val stages = input.split("\\[(.*?)\\]").head.replace(" ", "")

    val splitted: ArrayBuffer[String] = newInput.split(" ").to[ArrayBuffer]

    //Match second argument to determine what operation to perform
    splitted(0) match {
      case "create" => addPipelineToEnv(createPipeline(splitted -= "create", env), env)
      case "start" => startPipeline(splitted -= "start", env)
      case "delete" => deletePipeline(splitted -= "delete", env)
      case "stop" => stopPipeline(splitted -= "stop", env)
      case x => (env, Failure(new IllegalArgumentException(s"Couldn't match second argument: $x")))
    }
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
      case "SQL" => Some(builder.append(new SQLStage()))
      //TODO all the other stages from existing plugins, e.g. ghtorrent
      case _ => None
    }
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
      //These are 2 completely different errors, maybe try to catch them at different points
      (env, Failure(new IllegalArgumentException("Pipeline with that name already exists or one of its stages is not recognised")))
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
}
