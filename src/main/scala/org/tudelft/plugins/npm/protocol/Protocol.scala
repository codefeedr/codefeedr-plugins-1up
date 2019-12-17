package org.tudelft.plugins.npm.protocol

import java.util.Date

object Protocol {

  case class NpmRelease(name: String,
                        retrieveDate: Date)

  case class NpmReleaseExt(name: String,
                           retrieveDate: Date,
                           project: NpmProject)

  case class NpmProject(name: String,
                        version: String,
                        license: Option[String], //sometimes license is simply a string
                        licenseObject: Option[License], //sometimes license is an object
                        licenses: Option[List[License]], //somtimes multiple licenses
                        repository: Option[Repository],
//                        dependencies: Option[List[Dependency]]
                        )

  case class License(lType: String,
                     url: String)

//  case class DraftNPMProject(name : String, // what about enforcing <= 214 chars?, can't start with _ or . and new packages no Uppercase in the name? & have to be url-safe chars
//                            version : String, // combination of name/version should be unique, can it occur that this is violated?
//                            description : Option[String], // it doesn't say it's required, only to 'put in a description, to help ppl discover your package'
//                            keywords: Option[Array[String]],
//                            homepage : Option[String]) // url


  case class Dependency(iterall: String)

  case class Engine(node: String)

  case class Repository(rType: String,
                        url: String)

  case class Bug(url: String)
}
