package org.tudelft.plugins.npm.stages

import java.util.concurrent.TimeUnit

import org.apache.flink.streaming.api.scala.DataStream
import org.apache.flink.streaming.api.datastream.{
  AsyncDataStream => JavaAsyncDataStream
}
import org.tudelft.plugins.npm.protocol.Protocol.{
  NpmRelease,
  NpmReleaseExt
}
import org.codefeedr.stages.TransformStage
import org.apache.flink.api.scala._
import org.tudelft.plugins.npm.operators.RetrieveProjectAsync

/** Transform a [[NpmRelease]] to [[NpmReleaseExt]].
  *
  * @param stageId the name of this stage.
  */
class NpmReleasesExtStage(stageId: String = "npm_releases") extends TransformStage[NpmRelease, NpmReleaseExt](Some(stageId)) {

  /** Transform a [[NpmRelease]] to [[NpmReleaseExt]].
    *
    * @param source The input source with type [[NpmRelease]].
    * @return The transformed stream with type [[NpmReleaseExt]].
    */
  override def transform(source: DataStream[NpmRelease]): DataStream[NpmReleaseExt] = {

    // Retrieve project from release asynchronously.
    val async = JavaAsyncDataStream.orderedWait(source.javaStream,
      new RetrieveProjectAsync,
      5,
      TimeUnit.SECONDS,
      100)

    new org.apache.flink.streaming.api.scala.DataStream(async)
            .map(x => (x.project.name, x.project.license, x.project.licenseObject, x.project.licenses))
      .print()

    new org.apache.flink.streaming.api.scala.DataStream(async)
  }
}