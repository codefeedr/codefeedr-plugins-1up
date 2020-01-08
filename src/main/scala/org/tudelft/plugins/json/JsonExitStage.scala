package org.tudelft.plugins.json

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.stages.OutputStage

import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

/**
  * A stage which prints a Jsonable object to json representation of the objects in the incoming datastream
  *
  * @param classTag$T The classTag of the input stream
  * @param typeTag$T  The typeTag of the input stream
  * @tparam T The type of input stream
  */
class JsonExitStage[T <: Serializable with AnyRef with Jsonable : ClassTag : TypeTag] extends OutputStage[T] {
  override def main(source: DataStream[T]): Unit = {
    implicit val typeInfo: TypeInformation[String] = TypeInformation.of(classOf[String])
    source.map(x => x.toJson())
  }

}
