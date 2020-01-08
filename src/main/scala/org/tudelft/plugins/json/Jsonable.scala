package org.tudelft.plugins.json

import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization.write

/**
  * This trait is used for the JsonStage, it implements a method toJson which turns an object into a String json
  * representation
  */
trait Jsonable {
  implicit val formats = DefaultFormats
  def toJson(): String = {
    write(this)
  }
}
