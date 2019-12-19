package org.tudelft.plugins.npm.protocol

import java.util.Date

object Protocol {

  case class NpmRelease( name         : String,
                         retrieveDate : Date) // using ingestion time

  case class NpmReleaseExt(name         : String,
                           retrieveDate : Date,
                           project      : NpmProject)

  case class NpmProject(_id             : String,
                        _rev            : Option[String],
                        name            : String,
                        author          : Option[PersonSimple],
                        authorObject    : Option[PersonObject],
                        contributors    : Option[List[PersonObject]],
                        description     : Option[String],
                        homepage        : Option[String],
                        keywords        : Option[List[String]],
                        license         : Option[String],
                        dependencies    : Option[List[DependencyObject]],
                        maintainers     : List[PersonObject],
                        readme          : String,
                        readmeFilename  : String,
                        bugs            : Option[Bug],
                        bugString       : Option[String],
                        repository      : Option[Repository],
                        time            : TimeObject
                       )

  case class DependencyObject( packageName : String,
                               version     : String)
  case class PersonObject( name   : String,
                           email  : Option[String],
                           url    : Option[String])

  case class PersonSimple( nameAndOptEmailOptURL : String)

  case class Repository(`type`     : String,
                        url        : String,
                        directory  : Option[String])

  case class Bug( url   : Option[String],
                  email : Option[String])

  case class TimeObject( created  : String,
                         modified : Option[String])
}