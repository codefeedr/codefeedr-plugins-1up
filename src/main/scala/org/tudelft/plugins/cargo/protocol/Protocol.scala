package org.tudelft.plugins.cargo.protocol

import java.util.Date

object Protocol extends Enumeration {


  case class CrateRelease(crate: Crate,
                          versions: List[CrateVersion],
                          keywords: List[CrateKeyword],
                          categories: List[CrateCategory])

  class CrateReleasePojo extends Serializable {
    var crate: CratePojo = _
    var versions: List[CrateVersionPojo] = _
    var keywords: List[CrateKeywordPojo] = _
    var categories: List[CrateCategoryPojo] = _
  }

  object CrateReleasePojo {
    def fromCrateRelease(crateRelease: CrateRelease): CrateReleasePojo = {
      val pojo = new CrateReleasePojo

      pojo.crate = CratePojo.fromCrate(crateRelease.crate)

      // Map the CrateVersions
      pojo.versions = crateRelease.versions.map(x => {
        CrateVersionPojo.fromCrateVersion(x)
      })

      // Map the CrateKeywords
      pojo.keywords = crateRelease.keywords.map(x => {
        CrateKeywordPojo.fromCrateKeyword(x)
      })

      // Map the CrateCategories
      pojo.categories = crateRelease.categories.map(x => {
        CrateCategoryPojo.fromCrateCategory(x)
      })
      pojo
    }
  }

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

  class CratePojo extends Serializable {
    var id: String = _
    var name: String = _
    //var updated_at: Date = _
    var versions: List[Int] = _
    var keywords: List[String] = _
    var categories: List[String] = _
    //var created_at: Date = _
    var downloads: Int = _
    var recent_downloads: Option[Int] = _
    var max_version: String = _
    var description: String = _
    var homepage: Option[String] = _
    var documentation: Option[String] = _
    var repository: Option[String] = _
    var links: CrateLinksPojo = _
    var exact_match: Boolean = _
  }

  object CratePojo {
    def fromCrate(crate: Crate): CratePojo = {
      val pojo = new CratePojo
      pojo.id = crate.id
      pojo.name = crate.name
      //pojo.updated_at =
      pojo.versions = crate.versions
      pojo.keywords = crate.keywords
      pojo.categories = crate.categories
      //pojo.created_at =
      pojo.downloads = crate.downloads
      pojo.recent_downloads = crate.recent_downloads
      pojo.max_version = crate.max_version
      pojo.description = crate.description
      pojo.homepage = crate.homepage
      pojo.documentation = crate.documentation
      pojo.repository = crate.repository
      pojo.links = CrateLinksPojo.fromCrateLinks(crate.links)
      pojo.exact_match = crate.exact_match
      pojo
    }
  }

  case class CrateLinks(version_downloads: String,
                        versions: Option[String],
                        owners: String,
                        owner_team: String,
                        owner_user: String,
                        reverse_dependencies: String)

  class CrateLinksPojo extends Serializable {
    var version_downloads: String = _
    var versions: Option[String] = _
    var owners: String = _
    var owner_team: String = _
    var owner_user: String = _
    var reverse_dependencies: String = _
  }

  object CrateLinksPojo {
    def fromCrateLinks(crateLinks: CrateLinks): CrateLinksPojo = {
      val pojo = new CrateLinksPojo
      pojo.version_downloads = crateLinks.version_downloads
      pojo.versions = crateLinks.versions
      pojo.owners = crateLinks.owners
      pojo.owner_team = crateLinks.owner_team
      pojo.owner_user = crateLinks.owner_user
      pojo.reverse_dependencies = crateLinks.reverse_dependencies
      pojo
    }
  }

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
                          crate_size: Option[Int],
                          published_by: Option[CrateVersionPublishedBy]
                         )

  class CrateVersionPojo extends Serializable {
    var id: Int = _
    var crate: String = _
    var num: String = _
    var dl_path: String = _
    var readme_path: String = _
    //var updated_at: Date = _
    //var created_at: Date = _
    var downloads: Int = _
    var features: CrateVersionFeaturesPojo = _
    var yanked: Boolean = _
    var license: String = _
    var links: CrateVersionLinksPojo = _
    var crate_size: Option[Int] = _
    var published_by: Option[CrateVersionPublishedByPojo] = _
  }

  object CrateVersionPojo {
    def fromCrateVersion(crateVersion: CrateVersion): CrateVersionPojo = {
      val pojo = new CrateVersionPojo
      pojo.id = crateVersion.id
      pojo.crate = crateVersion.crate
      pojo.num = crateVersion.num
      pojo.dl_path = crateVersion.dl_path
      pojo.readme_path = crateVersion.readme_path
      pojo.downloads = crateVersion.downloads
      pojo.features = CrateVersionFeaturesPojo.fromCrateVersionFeatures(crateVersion.features)
      pojo.yanked = crateVersion.yanked
      pojo.license = crateVersion.license
      pojo.links = CrateVersionLinksPojo.fromCrateVersionLinks(crateVersion.links)
      pojo.crate_size = crateVersion.crate_size
      if(crateVersion.published_by.isEmpty) {
        pojo.published_by = None
      }
      else {
        pojo.published_by = Some(CrateVersionPublishedByPojo.fromCrateVersionPublishedBy(crateVersion.published_by.get))
      }
      pojo
    }
  }

  // checked ~5 repos this is all JSON {}, which means empty complex object...
  case class CrateVersionFeatures()

  class CrateVersionFeaturesPojo extends Serializable {
  }

  object CrateVersionFeaturesPojo {
    def fromCrateVersionFeatures(crateVersionFeatures: CrateVersionFeatures): CrateVersionFeaturesPojo = {
      new CrateVersionFeaturesPojo
    }
  }

  case class CrateVersionLinks(dependencies: String,
                               version_downloads: String,
                               authors: String)

  class CrateVersionLinksPojo extends Serializable {
    var dependencies: String = _
    var version_downloads: String = _
    var authors: String = _
  }

  object CrateVersionLinksPojo {
    def fromCrateVersionLinks(crateVersionLinks: CrateVersionLinks): CrateVersionLinksPojo = {
      val pojo = new CrateVersionLinksPojo
      pojo.dependencies = crateVersionLinks.dependencies
      pojo.version_downloads = crateVersionLinks.version_downloads
      pojo.authors = crateVersionLinks.authors
      pojo
    }
  }

  case class CrateVersionPublishedBy(id: Int,
                                     login: String,
                                     name: Option[String],
                                     avatar: String,
                                     url: String)

  class CrateVersionPublishedByPojo extends Serializable {
    var id: Int = _
    var login: String = _
    var name: Option[String] = _
    var avatar: String = _
    var url: String = _
  }

  object CrateVersionPublishedByPojo {
    def fromCrateVersionPublishedBy(crateVersionPublishedBy: CrateVersionPublishedBy): CrateVersionPublishedByPojo = {
      val pojo = new CrateVersionPublishedByPojo
      pojo.id = crateVersionPublishedBy.id
      pojo.login = crateVersionPublishedBy.login
      pojo.name = crateVersionPublishedBy.name
      pojo.avatar = crateVersionPublishedBy.avatar
      pojo.url = crateVersionPublishedBy.url
      pojo
    }
  }

  case class CrateKeyword(id: String,
                          keyword: String,
                          created_at: String,
                          crates_cnt: Int)

  class CrateKeywordPojo extends Serializable {
    var id: String = _
    var keyword: String = _
    var created_at: String = _
    var crates_cnt: Int = _
  }

  object CrateKeywordPojo {
    def fromCrateKeyword(crateKeyword: CrateKeyword): CrateKeywordPojo = {
      val pojo = new CrateKeywordPojo
      pojo.id = crateKeyword.id
      pojo.keyword = crateKeyword.keyword
      pojo.created_at = crateKeyword.created_at
      pojo.crates_cnt = crateKeyword.crates_cnt
      pojo
    }
  }

  case class CrateCategory(id: String,
                           category: String,
                           slug: String,
                           description: String,
                           created_at: String,
                           crates_cnt: Int)

  class CrateCategoryPojo extends Serializable {
    var id: String = _
    var category: String = _
    var slug: String = _
    var description: String = _
    var created_at: String = _
    var crates_cnt: Int = _
  }

  object CrateCategoryPojo {
    def fromCrateCategory(crateCategory: CrateCategory): CrateCategoryPojo = {
      val pojo = new CrateCategoryPojo
      pojo.id = crateCategory.id
      pojo.category = crateCategory.category
      pojo.slug = crateCategory.slug
      pojo.description = crateCategory.description
      pojo.created_at = crateCategory.created_at
      pojo.crates_cnt = crateCategory.crates_cnt
      pojo
    }
  }
}
