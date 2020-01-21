package org.tudelft.plugins.cargo.stages

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.pipeline.Context
import org.codefeedr.stages.InputStage
import org.tudelft.plugins.cargo.operators.{CargoReleasesSource, CargoSourceConfig}
import org.tudelft.plugins.cargo.protocol.Protocol.CrateRelease

/** fetches real-time releases from Cargo */
class CargoReleasesStage(stageId: String = "cargo_releases_min",
                         sourceConfig: CargoSourceConfig = CargoSourceConfig(500, -1, 32))
  extends InputStage[CrateRelease](Some(stageId)){

  /** Fetches [[CrateRelease]] from real-time Cargo feed.
   *
   * @param context The context to add the source to.
   * @return The stream of type [[CrateRelease]].
   */
  override def main(context: Context): DataStream[CrateRelease] = {
    implicit val typeInfo: TypeInformation[CrateRelease] = TypeInformation.of(classOf[CrateRelease])
    context.env
      .addSource(new CargoReleasesSource(sourceConfig))(typeInfo)
  }

}
