package org.tudelft.plugins.npm.operators

import java.util.Calendar
import org.scalatest.FunSuite
import org.tudelft.plugins.npm.protocol.Protocol.NpmRelease

/**
 * Class to 'smoke test' build of class RetrieveProjectASync,
 * the class repsonsible for asynchronous handling of retrieval of
 * individual projects for the NPM package manager
 *
 * @author Roald van der Heijden
 * Date: 2019-12-01 (YYYY-MM-DD)
 */
class RetrieveProjectASyncTest extends FunSuite{

  test("smoketest") {
    val now = Calendar.getInstance().getTime()
    val release = NpmRelease("@microservices/cli", now)
    assert(release.name=="@microservices/cli")
  }
}

