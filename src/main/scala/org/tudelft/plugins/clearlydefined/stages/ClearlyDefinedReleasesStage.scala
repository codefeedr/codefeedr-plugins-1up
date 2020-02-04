package org.tudelft.plugins.clearlydefined.stages

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.pipeline.Context
import org.codefeedr.stages.InputStage
import org.tudelft.plugins.clearlydefined.operators.{ClearlyDefinedReleasesSource, ClearlyDefinedSourceConfig}
import org.tudelft.plugins.clearlydefined.protocol.Protocol.ClearlyDefinedRelease

/** fetches real-time releases from ClearlyDefined */
class ClearlyDefinedReleasesStage(stageId: String = "clearlydefined_releases_min",
                         sourceConfig: ClearlyDefinedSourceConfig = ClearlyDefinedSourceConfig(30000, -1, 32))
  extends InputStage[ClearlyDefinedRelease](Some(stageId)){

  /** Fetches [[ClearlyDefinedRelease]] from real-time ClearlyDefined feed.
   *
   * @param context The context to add the source to.
   * @return The stream of type [[ClearlyDefinedRelease]].
   */
  override def main(context: Context): DataStream[ClearlyDefinedRelease] = {
    implicit val typeInfo: TypeInformation[ClearlyDefinedRelease] = TypeInformation.of(classOf[ClearlyDefinedRelease])
    context.env
      .addSource(new ClearlyDefinedReleasesSource(sourceConfig))(typeInfo)
  }

}
