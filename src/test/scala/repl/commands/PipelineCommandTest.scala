package repl.commands

import org.codefeedr.pipeline.PipelineBuilder
import org.scalatest.FunSuite
import org.tudelft.plugins.cargo.stages.CargoReleasesStage
import org.tudelft.repl.ReplEnv
import org.tudelft.repl.commands.PipelineCommand

import scala.collection.mutable.ArrayBuffer

class PipelineCommandTest extends FunSuite {

  val emptyEnv = ReplEnv(Nil)

  val pipeline = new PipelineBuilder().setPipelineName("test").append(new CargoReleasesStage()).build()
  val filledEnvNotRunning = ReplEnv(List((pipeline, false)))
  val filledEnvRunning = ReplEnv(List((pipeline, true)))

  val buffer = new ArrayBuffer[String]()
  buffer += "test"

  test("applyTestDefault") {
    val res = PipelineCommand.apply(emptyEnv, "test")
    assert(res._2.isFailure)
  }

  test("applyTestCreate") {
    val res = PipelineCommand.apply(emptyEnv, "create test MavenReleases")
    assert(res._2.isSuccess)
    assert(res._1.pipelines.exists(x => x._1.name == "test"))
  }

  test("applyTestDelete") {
    val res = PipelineCommand.apply(filledEnvNotRunning, "delete test")
    assert(res._2.isSuccess)
    assert(res._1.pipelines.isEmpty)
  }

  test("applyTestStart") {
    val res = PipelineCommand.apply(filledEnvNotRunning, "start test")
    assert(res._2.isSuccess)
    assert(res._1.pipelines.head._2 == true)
  }

  test("applyTestStop") {
    val res = PipelineCommand.apply(filledEnvRunning, "stop test")
    assert(res._2.isSuccess)
    assert(res._1.pipelines.head._2 == false)
  }

  test("createPipelineExistingTest") {
    val res = PipelineCommand.createPipeline(buffer, filledEnvRunning)
    assert(res.isEmpty)
  }

  test("createPipelineNoStagesTest") {
    val res = PipelineCommand.createPipeline(buffer, emptyEnv)
  }

  test("addEmptyPipelineToEnvTest") {
    val res = PipelineCommand.addPipelineToEnv(None, emptyEnv)
    assert(res._2.isFailure)
  }

  test("startNonExistingPipelineTest") {
    val res = PipelineCommand.startPipeline(buffer, emptyEnv)
    assert(res._2.isFailure)
  }

  test("startPipelineAlreadyRunningTest") {
    val res = PipelineCommand.startPipeline(buffer, filledEnvRunning)
    assert(res._2.isFailure)
  }

  test("deletePipelineNonExistingTest") {
    val res = PipelineCommand.deletePipeline(buffer, emptyEnv)
    assert(res._2.isFailure)
  }

  test("deleteRunningPipelineTest") {
    val res = PipelineCommand.deletePipeline(buffer, filledEnvRunning)
    assert(res._2.isFailure)
  }

  test("stopNonExistingTest") {
    val res = PipelineCommand.stopPipeline(buffer, emptyEnv)
    assert(res._2.isFailure)
  }

  test("stopNonRunningPipelineTest") {
    val res = PipelineCommand.stopPipeline(buffer, filledEnvNotRunning)
    assert(res._2.isFailure)
  }

  test("transformInputTest") {
    val res = PipelineCommand.transformInput("pipeline create test SQL(a b)")
    assert(res(0) == "create")
    assert(res(1) == "test")
    assert(res(2) == "SQL(a b)")
  }

  test("applyListEmptyTest"){
    val res = PipelineCommand.apply(emptyEnv, "pipeline list")
    assert(res._2.isSuccess)
  }

  test("listPipelinesEmptyTest"){
    val res = PipelineCommand.listPipelines(emptyEnv)
    assert(res == "There are no pipelines")
  }

  test("listPipelinesStoppedTest"){
    val res = PipelineCommand.listPipelines(filledEnvNotRunning)
    assert(res == "test, Status: Stopped\n")
  }

  test("listPipelinesRunningTest"){
    val res = PipelineCommand.listPipelines(filledEnvRunning)
    assert(res == "test, Status: Running\n")
  }

  //TODO testing of buildStage method is difficult due to the properties of pipelineBuilder being protected

}
