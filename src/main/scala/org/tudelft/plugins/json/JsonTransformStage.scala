package org.tudelft.plugins.json

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.stages.TransformStage

import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

/**
  * Since CodeFeedr stages require an input with AnyRef, this wrapper class is used to wrap a String
  *
  * @param s The string inside the wrapper class
  */
case class StringWrapper(s: String)

/**
  * This stage transforms any Jsonable stream to a stream of StringWrapper with the json representation
  * of the incoming stream
  *
  * @param classTag$T The classTag of the input stream
  * @param typeTag$T The typeTag of the input stream
  * @tparam T Type of the input stream
  */
class JsonTransformStage[T <: Serializable with AnyRef with Jsonable : ClassTag : TypeTag] extends TransformStage[T, StringWrapper] {
  override def transform(source: DataStream[T]): DataStream[StringWrapper] = {
    implicit val typeInfo: TypeInformation[StringWrapper] = TypeInformation.of(classOf[StringWrapper])
    source.map((x: T) => StringWrapper(x.toJson()))(typeInfo)
  }
}
