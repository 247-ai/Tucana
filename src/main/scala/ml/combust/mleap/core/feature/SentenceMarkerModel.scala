package ml.combust.mleap.core.feature

import ml.combust.mleap.core.Model
import ml.combust.mleap.core.types.{ScalarType, StructType}

/**
  * Core logic for sentence marker
  */
case class SentenceMarkerModel() extends Model {
  def apply(label:String): String = "_class_ss "+ label+ " _class_se"

  override def inputSchema: StructType = StructType("input" -> ScalarType.String).get

  override def outputSchema: StructType = StructType("output" -> ScalarType.String).get
}
