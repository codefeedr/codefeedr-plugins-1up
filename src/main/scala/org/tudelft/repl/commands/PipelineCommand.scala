package org.tudelft.repl.commands

import org.codefeedr.pipeline.{Pipeline, PipelineBuilder}
import org.tudelft.CrateDownloadsOutput
import org.tudelft.plugins.cargo.stages.CargoReleasesStage
import org.tudelft.repl.{Command, Parser, ReplEnv}

import scala.Option
import scala.collection.mutable.ArrayBuffer
import scala.util.{Failure, Success, Try}

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
    if (env.pipelines.exists(x => x._1.name == input(0))){
      return None
    }

    //TODO actually generate pipelines
    //Temporary implementation to test
    val pipeline = new PipelineBuilder()
      .setPipelineName(input(0))
      .append(new CargoReleasesStage())
      .append (new CrateDownloadsOutput)
      .build()

    //Return the new pipeline added to the env
    Some(pipeline)
  }

  /**
    * Add a pipeline to the env
    * @param maybePipeline the option of pipeline to add
    * @param env the current env
    * @return the new env containing maybePipeline
    */
  def addPipelineToEnv(maybePipeline: Option[Pipeline], env: ReplEnv): (ReplEnv, Try[String]) = {
    if (maybePipeline.isEmpty){
      (env, Failure(new IllegalArgumentException("Pipeline with that name already exists")))
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
    if (pipeline.isEmpty){
      (env, Failure(new IllegalArgumentException("Pipeline with name " + input(0) + " does not exist")))
    } else if (pipeline.get._2 == true) {
      (env, Failure(new IllegalArgumentException("Pipeline with name " + input(0) + " is already running")))
    } else {

      //TODO select option for what mode to run in, for now startMock
      //Start pipeline
//      pipeline.get._1.startMock()

      //Return pipeline now indicated as running
      (ReplEnv(env.pipelines.filterNot(x => x._1.name == input(0)) :+ (pipeline.get._1, true)), Success("Successfully started pipeline"))
      }
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
    if (pipeline.isEmpty){
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
    if (pipeline.isEmpty){
      (env, Failure(new IllegalArgumentException("Pipeline with name " + input(0) + " does not exist")))
    } else if (pipeline.get._2 == false) {
      (env, Failure(new IllegalArgumentException("Pipeline with name " + input(0) + " is not running")))
    } else {

      //TODO currently stopping pipelines is not supported in codefeedr, this is something we may need to add ourself
      //Stop pipeline
      //      pipeline.get._1.stop()

      //Return pipeline now indicated as running
      (ReplEnv(env.pipelines.filterNot(x => x._1.name == input(0)) :+ (pipeline.get._1, false)), Success("Successfully stopped pipeline"))
    }
  }
}
