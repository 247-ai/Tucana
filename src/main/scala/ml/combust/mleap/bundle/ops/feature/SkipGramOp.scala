package ml.combust.mleap.bundle.ops.feature

import ml.combust.bundle.BundleContext
import ml.combust.bundle.dsl._
import ml.combust.bundle.op.OpModel
import ml.combust.mleap.bundle.ops.MleapOp
import ml.combust.mleap.core.feature.SkipGramModel
import ml.combust.mleap.runtime.MleapContext
import ml.combust.mleap.runtime.transformer.feature.SkipGram

/**
  * Serializer for skip grams to run it in the mleap platform
  */
class SkipGramOp extends MleapOp[SkipGram, SkipGramModel]{
  override val Model: OpModel[MleapContext, SkipGramModel] = new OpModel[MleapContext, SkipGramModel] {
    override val klazz: Class[SkipGramModel] = classOf[SkipGramModel]

    override def opName: String = "skip_gram"

    override def store(model: Model, obj: SkipGramModel)
                      (implicit context: BundleContext[MleapContext]): Model = {
      model.withValue("windowSize", Value.long(obj.windowSize))
    }

    override def load(model: Model)
                     (implicit context: BundleContext[MleapContext]): SkipGramModel = {
      SkipGramModel(windowSize = model.value("windowSize").getInt)
    }
  }

  override def model(node: SkipGram): SkipGramModel = node.model
}
