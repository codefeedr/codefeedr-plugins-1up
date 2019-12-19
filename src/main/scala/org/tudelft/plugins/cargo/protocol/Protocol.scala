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

    def fromCrateRelease(crateRelease: CrateRelease): CrateReleasePojo = {
      val cratePojo = new CratePojo
      this.crate = cratePojo.fromCrate(crateRelease.crate)

      // Map the CrateVersions
      this.versions = crateRelease.versions.map(x => {
        val pojo = new CrateVersionPojo
        pojo.fromCrateVersion(x)
      })

      // Map the CrateKeywords
      this.keywords = crateRelease.keywords.map(x => {
        val pojo = new CrateKeywordPojo
        pojo.fromCrateKeyword(x)
      })

      // Map the CrateCategories
      this.categories = crateRelease.categories.map(x => {
        val pojo = new CrateCategoryPojo
        pojo.fromCrateCategory(x)
      })
      this
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

    def fromCrate(crate: Crate): CratePojo = {
      this.id = crate.id
      this.name = crate.name
      //this.updated_at =
      this.versions = crate.versions
      this.keywords = crate.keywords
      this.categories = crate.categories
      //this.created_at =
      this.downloads = crate.downloads
      this.recent_downloads = crate.recent_downloads
      this.max_version = crate.max_version
      this.description = crate.description
      this.homepage = crate.homepage
      this.documentation = crate.documentation
      this.repository = crate.repository
      val crateLinksPojo = new CrateLinksPojo
      this.links = crateLinksPojo.fromCrateLinks(crate.links)
      this.exact_match = crate.exact_match
      this
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

    def fromCrateLinks(crateLinks: CrateLinks): CrateLinksPojo = {
      this.version_downloads = crateLinks.version_downloads
      this.versions = crateLinks.versions
      this.owners = crateLinks.owners
      this.owner_team = crateLinks.owner_team
      this.owner_user = crateLinks.owner_user
      this.reverse_dependencies = crateLinks.reverse_dependencies
      this
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
                          crate_size: Int,
                          published_by: CrateVersionPublishedBy
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
    var crate_size: Int = _
    var published_by: CrateVersionPublishedByPojo = _

    def fromCrateVersion(crateVersion: CrateVersion): CrateVersionPojo = {
      this.id = crateVersion.id
      this.crate = crateVersion.crate
      this.num = crateVersion.num
      this.dl_path = crateVersion.dl_path
      this.readme_path = crateVersion.readme_path
      this.downloads = crateVersion.downloads
      val crateVersionFeaturesPojo = new CrateVersionFeaturesPojo
      this.features = crateVersionFeaturesPojo.fromCrateVersionFeatures(crateVersion.features)
      this.yanked = crateVersion.yanked
      this.license = crateVersion.license
      val crateVersionLinksPojo = new CrateVersionLinksPojo
      this.links = crateVersionLinksPojo.fromCrateVersionLinks(crateVersion.links)
      this.crate_size = crateVersion.crate_size
      val crateVersionPublishedByPojo = new CrateVersionPublishedByPojo
      this.published_by = crateVersionPublishedByPojo.fromCrateVersionPublishedBy(crateVersion.published_by)
      this
    }
  }

  // checked ~5 repos this is all JSON {}, which means empty complex object...
  case class CrateVersionFeatures()

  class CrateVersionFeaturesPojo extends Serializable {
    def fromCrateVersionFeatures(crateVersionFeatures: CrateVersionFeatures): CrateVersionFeaturesPojo = {
      this
    }
  }

  case class CrateVersionLinks(dependencies: String,
                               version_downloads: String,
                               authors: String)

  class CrateVersionLinksPojo extends Serializable {
    var dependencies: String = _
    var version_downloads: String = _
    var authors: String = _

    def fromCrateVersionLinks(crateVersionLinks: CrateVersionLinks): CrateVersionLinksPojo = {
      this.dependencies = crateVersionLinks.dependencies
      this.version_downloads = crateVersionLinks.version_downloads
      this.authors = crateVersionLinks.authors
      this
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

    def fromCrateVersionPublishedBy(crateVersionPublishedBy: CrateVersionPublishedBy): CrateVersionPublishedByPojo = {
      this.id = crateVersionPublishedBy.id
      this.login = crateVersionPublishedBy.login
      this.name = crateVersionPublishedBy.name
      this.avatar = crateVersionPublishedBy.avatar
      this.url = crateVersionPublishedBy.url
      this
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

    def fromCrateKeyword(crateKeyword: CrateKeyword): CrateKeywordPojo = {
      this.id = crateKeyword.id
      this.keyword = crateKeyword.keyword
      this.created_at = crateKeyword.created_at
      this.crates_cnt = crateKeyword.crates_cnt
      this
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

    def fromCrateCategory(crateCategory: CrateCategory): CrateCategoryPojo = {
      this.id = crateCategory.id
      this.category = crateCategory.category
      this.slug = crateCategory.slug
      this.description = crateCategory.description
      this.created_at = crateCategory.created_at
      this.crates_cnt = crateCategory.crates_cnt
      this
    }
  }

}
