package org.tudelft

import org.codefeedr.pipeline.PipelineBuilder
import org.tudelft.plugins.SQLStage
import org.tudelft.plugins.cargo.protocol.Protocol.CrateRelease
import org.tudelft.plugins.cargo.stages.CargoReleasesStage
import org.tudelft.plugins.clearlydefined.protocol.Protocol.ClearlyDefinedRelease
import org.tudelft.plugins.clearlydefined.stages.ClearlyDefinedReleasesStage
import org.tudelft.plugins.maven.protocol.Protocol.MavenReleaseExt
import org.tudelft.plugins.maven.stages.{MavenReleasesExtStage, MavenReleasesStage}
import org.tudelft.plugins.npm.protocol.Protocol.NpmReleaseExt
import org.tudelft.plugins.npm.stages.{NpmReleasesExtStage, NpmReleasesStage}

/**
  * Object used to create the correct pipeline from a user input
  */
object InputParser {

  /**
    * Appends stages to the builder corresponding with the incoming query
    *
    * @param query The incoming query
    * @param builder The builder to which append stages
    * @return A pipeline builder with stages corresponding to the incoming query
    */
  def createCorrespondingStages(query: String, builder: PipelineBuilder): PipelineBuilder = {
    if (query.contains("Maven")){
      builder
        .append(new MavenReleasesStage())
        .append(new MavenReleasesExtStage())
        .append(new SQLStage[MavenReleaseExt](query))
    } else if (query.contains("Npm")){
      builder
        .append(new NpmReleasesStage())
        .append(new NpmReleasesExtStage())
        .append(new SQLStage[NpmReleaseExt](query))
    } else if (query.contains("Cargo")){
      builder
        .append(new CargoReleasesStage())
        .append(new SQLStage[CrateRelease](query))
    }
    else if (query.contains("ClearlyDefined") || query.contains("CDLF")){
      builder
        .append(new ClearlyDefinedReleasesStage())
        .append(new SQLStage[ClearlyDefinedRelease](query))
    }
    else throw new IllegalArgumentException("No supported plugin detected")
  }

}
