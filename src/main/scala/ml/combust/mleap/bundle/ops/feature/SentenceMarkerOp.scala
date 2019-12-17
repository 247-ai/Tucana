package ml.combust.mleap.bundle.ops.feature

import ml.combust.bundle.{BundleContext}
import ml.combust.bundle.dsl._
import ml.combust.bundle.op.OpModel
import ml.combust.mleap.bundle.ops.MleapOp
import ml.combust.mleap.core.feature.SentenceMarkerModel
import ml.combust.mleap.runtime.MleapContext
import ml.combust.mleap.runtime.transformer.feature.SentenceMarker

/**
  * Serializer for sentence marker transformer to execute it in mleap platform
  */
class SentenceMarkerOp extends MleapOp[SentenceMarker, SentenceMarkerModel] {
  override val Model: OpModel[MleapContext, SentenceMarkerModel] = new OpModel[MleapContext, SentenceMarkerModel] {
    // the class of the model is needed for when we go to serialize JVM objects
    override val klazz: Class[SentenceMarkerModel] = classOf[SentenceMarkerModel]

    // a unique name for our op: "string_map"
    override def opName: String = "sentence_marker"

    override def store(model: Model, obj: SentenceMarkerModel)
                      (implicit context: BundleContext[MleapContext]): Model = {
      model
    }

    override def load(model: Model)
                     (implicit context: BundleContext[MleapContext]): SentenceMarkerModel = {
      
      new SentenceMarkerModel()
    }
  }

  // the core model that is used by the transformer
  override def model(node: SentenceMarker): SentenceMarkerModel = node.model
}