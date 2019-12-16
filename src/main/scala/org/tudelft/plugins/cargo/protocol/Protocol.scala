package org.tudelft.plugins.cargo.protocol

import java.util.Date

object Protocol extends Enumeration {


  case class CrateRelease(crate: Crate,
                          versions: List[CrateVersion],
                          keywords: List[CrateKeyword],
                          categories: List[CrateCategory])


  case class Crate(id: String,
                   name: String,
                   updated_at: Date,
                   versions: List[Int],
                   keywords: List[String],
                   categories: List[String],
                   //badges: Option[List[String]], //Unimportant information / Too extensive, See natvis-pdbs
                   created_at: Date,
                   downloads: Int, // Assuming no more than 2B downloads
                   recent_downloads: Option[Int],
                   max_version: String,
                   description: String,
                   homepage: Option[String],
                   documentation: Option[String],
                   repository: Option[String],
                   links: CrateLinks,
                   exact_match: Boolean
                  )

  // Is this always constant?
  case class CrateLinks(version_downloads: String,
                        versions: Option[String],
                        owners: String,
                        owner_team: String,
                        owner_user: String,
                        reverse_dependencies: String)

  case class CrateVersion(id: Int,
                          crate: String,
                          num: String,
                          dl_path: String,
                          readme_path: String,
                          updated_at: Date,
                          created_at: Date,
                          downloads: Int,
                          features: CrateVersionFeatures,
                          yanked: Boolean,
                          license: String,
                          links: CrateVersionLinks,
                          crate_size: Int,
                          published_by: CrateVersionPublishedBy
                         )

  // checked ~5 repos this is all JSON {}, which means empty complex object...
  case class CrateVersionFeatures()

  case class CrateVersionLinks(dependencies: String,
                               version_downloads: String,
                               authors: String)

  case class CrateVersionPublishedBy(id: Int,
                                     login: String,
                                     name: Option[String],
                                     avatar: String,
                                     url: String)

  case class CrateKeyword(id: String,
                          keyword: String,
                          created_at: String,
                          crates_cnt: Int)

  case class CrateCategory(id: String,
                           category: String,
                           slug: String,
                           description: String,
                           created_at: String,
                           crates_cnt: Int)


}
