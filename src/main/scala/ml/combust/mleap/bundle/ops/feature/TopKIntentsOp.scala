package ml.combust.mleap.bundle.ops.feature

import ml.combust.bundle.BundleContext
import ml.combust.mleap.core.feature.TopKIntentsModel
import ml.combust.mleap.runtime.transformer.feature.TopKIntents
import ml.combust.bundle.op.OpModel
import ml.combust.bundle.dsl._
import ml.combust.mleap.bundle.ops.MleapOp
import ml.combust.mleap.runtime.MleapContext
import ml.combust.mleap.runtime.types.BundleTypeConverters._

/**
  * Serializer for Gram assembler to run in the mleap platform
  */
class TopKIntentsOp extends MleapOp[TopKIntents, TopKIntentsModel] {
  override val Model: OpModel[MleapContext, TopKIntentsModel] = new OpModel[MleapContext, TopKIntentsModel] {
    override val klazz: Class[TopKIntentsModel] = classOf[TopKIntentsModel]

    override def opName: String = "topK"

    override def store(model: Model, obj: TopKIntentsModel)
                      (implicit context: BundleContext[MleapContext]): Model = {
      model.withValue("labels", Value.stringList(obj.labels))
        .withValue("kValue",Value.int(obj.kValue))
    }

    override def load(model: Model)
                     (implicit context: BundleContext[MleapContext]): TopKIntentsModel = {
      val labels = model.value("labels").getStringList
      val kValue = model.value("kValue").getInt
      TopKIntentsModel(labels,kValue)
    }
  }

  override def model(node: TopKIntents): TopKIntentsModel = node.model
}
