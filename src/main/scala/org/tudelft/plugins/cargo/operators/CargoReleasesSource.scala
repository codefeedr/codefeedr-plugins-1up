package org.tudelft.plugins.cargo.operators

import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.codefeedr.stages.utilities.{HttpRequester, RequestException}
import org.tudelft.plugins.{PluginReleasesSource, PluginSourceConfig}
import org.tudelft.plugins.cargo.protocol.Protocol.CrateRelease
import scalaj.http.Http
import spray.json._

case class CargoSourceConfig(pollingInterval: Int = 10000,
                             maxNumberOfRuns: Int = -1,
                             timeout : Int = 32)
    extends PluginSourceConfig

/**
 * Important to note in retrieving data from the stream of crates in Cargo is the following:
 *  - The stream URL is https://crates.io/api/v1/summary
 *  - A list of 10 of the most recent crates can be found there under the name "new_crates"
 *  - This information per crate is minimal, so the id/name is taken and used in a separate URL
 *  - This URL is https://crates.io/api/v1/crates/{name}
 * @param config the cargo source configuration, has pollingInterval and maxNumberOfRuns fields
 */
class CargoReleasesSource(config: CargoSourceConfig = CargoSourceConfig())
  extends PluginReleasesSource[CrateRelease](config) {

  /** url for the stream of updated and new crates */
  val url = "https://crates.io/api/v1/summary"

  /**
   * Main fetcher of new items in the Crates.io package source
   * @param ctx
   */
  override def run(ctx: SourceFunction.SourceContext[CrateRelease]): Unit = {
    val lock = ctx.getCheckpointLock

    /** While is running or #runs left. */
    while (isRunning && runsLeft != 0) {
      lock.synchronized { // Synchronize to the checkpoint lock.
        try {
          // Polls the RSS feed
          val rssAsString:      String = getRSSAsString.get
          // Parses the received rss items
          val items:            Seq[CrateRelease] = parseRSSString(rssAsString)
          // Collect right items and update last item
          val validSortedItems: Seq[CrateRelease] = sortAndDropDuplicates(items)
          // Decrease runs left
          super.decreaseRunsLeft()
          // Add a timestamp to the item
          validSortedItems.foreach(x =>
            ctx.collectWithTimestamp(x, x.crate.updated_at.getTime))
          // Call the parent run
          super.runPlugin(ctx, validSortedItems)
        } catch {
          case _: Throwable =>
        }
      }
    }
  }

  /**
   * Drops items that already have been collected and sorts them based on times
   *
   * @param items Potential items to be collected
   * @return Valid sorted items
   */
  def sortAndDropDuplicates(items: Seq[CrateRelease]): Seq[CrateRelease] = {
    items
      .filter((x: CrateRelease) => {
        if (lastItem.isDefined)
          lastItem.get.crate.updated_at.before(x.crate.updated_at)
        else
          true
      })
      .sortWith((x: CrateRelease, y: CrateRelease) => x.crate.updated_at.before(y.crate.updated_at))
  }

  /**
   * Requests the RSS feed and returns its body as a string.
   * Will keep trying with increasing intervals if it doesn't succeed
   *
   * @return Body of requested RSS feed
   */
  @throws[RequestException]
  def getRSSAsString: Option[String] = {
    try {
      Some(new HttpRequester().retrieveResponse(Http(url)).body)
    }
    catch {
      case _: Throwable => None
    }
  }

  /**
   * Gets the body response of a specific crate as String
   * @param crateName Name of the crate
   * @return Http Response body
   */
  def getRSSFromCrate(crateName: String): Option[String] = {
    try {
      Some(new HttpRequester().retrieveResponse(Http("https://crates.io/api/v1/crates/".concat(crateName))).body)
    }
    catch {
      case _: Throwable => None
    }
  }

  /**
   * Parses a string that contains JSON with RSS items into a list of CrateReleases
   *
   * @param rssString JSON string with RSS items
   * @return Sequence of RSS items in type CrateRelease
   */
  def parseRSSString(rssString: String): Seq[CrateRelease] = {
    try {
      // Parse the big release string as a Json object
      val json          : JsObject         = rssString.parseJson.asJsObject

      // Retrieve 2x10 JsObjects of Crates
      val newCrates     : Vector[JsObject] = JsonParser.getNewCratesFromSummary(json).get
      val updatedCrates : Vector[JsObject] = JsonParser.getUpdatedCratesFromSummary(json).get

      // Translate 2x10 JSObjects into CrateReleases
      val newCratesObjects     : Seq[CrateRelease] = this.transformJsonToCrateReleases(newCrates)
      val updatedCratesObjects : Seq[CrateRelease] = this.transformJsonToCrateReleases(updatedCrates)

      // Return the desired list of CrateReleases
      newCratesObjects ++ updatedCratesObjects
    } catch {
      // If the string cannot be parsed return an empty list
      case _: Throwable =>
        printf("Failed parsing the RSSString in the CargoReleasesSource.scala file")
        Nil
    }
  }

  /**
   * Method which retrieves the Json of each individual crate, then parses it into an object and put into Seq
   * @param json Must be a Vector of JsObjects with internal Crate structure
   * @return Parsed into CrateRelease objects
   */
  def transformJsonToCrateReleases(json: Vector[JsObject]): Seq[CrateRelease] = {
    try {
      for (crate <- json) yield {
        val crateId :String = JsonParser.getStringFieldFromCrate(crate, "id").get
        val crateRSS :String = getRSSFromCrate(crateId).get
        val crateJson :JsObject = crateRSS.parseJson.asJsObject
        JsonParser.parseCrateJsonToCrateRelease(crateJson).get
      }
    }
    catch {
      case _: Throwable =>
        printf("\nFailed transforming json craterelease to its case class")
        Nil
    }
  }
}
