package org.tudelft.plugins.npm.stages

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.pipeline.Context
import org.codefeedr.stages.InputStage
import org.tudelft.plugins.npm.protocol.Protocol.NpmRelease
import org.tudelft.plugins.npm.operators.{NpmReleasesSource, NpmSourceConfig}

/** Fetches real-time releases from Npm. */
class NpmReleasesStage(stageId: String = "npm_releases_min", sourceConfig: NpmSourceConfig = NpmSourceConfig()) extends InputStage[NpmRelease](Some(stageId)) {

  /** Fetches [[NpmRelease]] from real-time Npm feed.
    *
    * @param context The context to add the source to.
    * @return The stream of type [[NpmRelease]].
    */
  override def main(context: Context): DataStream[NpmRelease] = {
    implicit val typeInfo: TypeInformation[NpmRelease] = TypeInformation.of(classOf[NpmRelease])
    context.env
      .addSource(new NpmReleasesSource(sourceConfig))(typeInfo)
  }
}
