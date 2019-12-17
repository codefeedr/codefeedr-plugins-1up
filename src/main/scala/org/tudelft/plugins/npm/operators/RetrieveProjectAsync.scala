package org.tudelft.plugins.npm.operators

import org.apache.flink.streaming.api.functions.async.{ResultFuture, RichAsyncFunction}
import org.tudelft.plugins.npm.protocol.Protocol._
import org.tudelft.plugins.npm.util.NpmService

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.Set


/** Retrieves a project related to a release asynchronously. */
class RetrieveProjectAsync
  extends RichAsyncFunction[NpmRelease, NpmReleaseExt] {

  /** Retrieve the execution context lazily. */
  implicit lazy val executor: ExecutionContext = ExecutionContext.global

  case class FaultyPlugins(plugins: mutable.Set[(String, Int)],
                           initTime: Long
                          )

  val faultyPlugins = FaultyPlugins(Set(), System.currentTimeMillis())

  /** Async retrieves the project belonging to the release.
    *
    * @param input        the release.
    * @param resultFuture the future to add the project to.
    */
  override def asyncInvoke(input: NpmRelease,
                           resultFuture: ResultFuture[NpmReleaseExt]): Unit = {

    //    val option: Option[(String, Int)] = faultyPlugins.plugins.find(x => x._1 == input.name)
    //
    //    if (option.isDefined){
    //      if (option.get._2 >= 3){
    //        println("tried 3 times")
    //      }
    //      println("tried " + option.get._2 +"times")
    //    }

    /** transform the title of a project to be retrieved by the NpmService */
    val link = input.name + "/package.json?t=" + System.currentTimeMillis()

    /** Retrieve the project in a Future. */
    val requestProject: Future[Option[NpmProject]] = Future(
      NpmService.getProject(link))

    /** Collects the result. */
    requestProject.onComplete {
      case Success(result: Option[NpmProject]) => {
        if (result.isDefined) { //If we get None, we return nothing.
          resultFuture.complete(
            List(
              NpmReleaseExt(input.name,
                input.retrieveDate,
                result.get)).asJava)
        } else {
          //          updateFaultyPlugins(input.name)
          resultFuture.complete(List().asJava)
        }
      }
      case Failure(e) =>
        resultFuture.complete(List().asJava)    
        e.printStackTrace()
    }

  }

  /**
    * Update the faultyPlugins
    *
    * @param str the name of the project which couldn't be retrieved
    */
  def updateFaultyPlugins(str: String): Unit = {
    //If faultyPlugins already contains str, increment the corresponding int
    if (faultyPlugins.plugins.map(x => x._1).contains(str)) {
      println("already exists")
      val (name, previousTries) = faultyPlugins.plugins.filter(x => x._1 == str).head
      faultyPlugins.plugins.remove((name, previousTries))
      faultyPlugins.plugins.add(name, previousTries + 1)
    }
    //else add the str to the set with 1 attempt
    //TODO for some reason this path gets taken twice instead of once per name, probably because of async calls???
    else {
      println("added new " + str + "size = " + faultyPlugins.plugins.size)
      faultyPlugins.plugins.add(str, 1)
    }
  }

  /** If we retrieve a time-out, then we just complete the future with an empty list. */
  override def timeout(input: NpmRelease,
                       resultFuture: ResultFuture[NpmReleaseExt]): Unit =
    resultFuture.complete(List().asJava)
}
