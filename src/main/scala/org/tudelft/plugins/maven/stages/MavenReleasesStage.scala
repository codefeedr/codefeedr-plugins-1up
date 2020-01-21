package org.tudelft.plugins.maven.stages

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.pipeline.Context
import org.codefeedr.stages.InputStage
import org.tudelft.plugins.maven.protocol.Protocol.MavenRelease
import org.tudelft.plugins.maven.operators.{MavenReleasesSource, MavenSourceConfig}

/** Fetches real-time releases from Maven. */
class MavenReleasesStage(stageId: String = "maven_releases_min",
                         sourceConfig: MavenSourceConfig = MavenSourceConfig(1000, -1, 4))
  extends InputStage[MavenRelease](Some(stageId)) {

  /** Fetches [[MavenRelease]] from real-time Maven feed.
   *
   * @param context The context to add the source to.
   * @return The stream of type [[MavenRelease]].
   */
  override def main(context: Context): DataStream[MavenRelease] = {
    implicit val typeInfo: TypeInformation[MavenRelease] = TypeInformation.of(classOf[MavenRelease])
    context.env
      .addSource(new MavenReleasesSource(sourceConfig))(typeInfo)
  }
}