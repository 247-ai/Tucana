package ml.combust.mleap.core.feature

import ml.combust.mleap.core.Model
import ml.combust.mleap.core.types.{ScalarType, StructType}

/**
  * Core logic for regex replace
  */
case class RegexReplaceModel(labels:Map[String,String]) extends Model {
  def apply(label: String): String = {
    labels.toArray.foldLeft(label)((accumulator, regexTuple) => {
      val regexObj = regexTuple._2.r
      regexObj.replaceAllIn(accumulator, regexTuple._1)
    })
  }

  override def inputSchema: StructType = StructType("input" -> ScalarType.String).get

  override def outputSchema: StructType = StructType("output" -> ScalarType.String).get
}
