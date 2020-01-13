package org.tudelft.plugins.json

import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization.write

object JsonService {
  implicit val formats = DefaultFormats

  def toJson(obj: AnyRef): String = {
    write(obj)
  }
}
