package org.tudelft.plugins.maven.operators

import java.util.Date

import org.scalatest.FunSuite
import org.tudelft.plugins.maven.protocol.Protocol.{Guid, MavenRelease}
import org.tudelft.plugins.maven.operators.RetrieveProjectAsync

class RetrieveProjectAsyncTest extends FunSuite{

  test("test"){
    val release = MavenRelease("org.neo4j.community: it-test-support 3.5.13", "desc", "link", new Date(), Guid("tag"))


  }

}
