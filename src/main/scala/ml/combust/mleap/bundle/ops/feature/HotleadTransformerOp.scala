package ml.combust.mleap.bundle.ops.feature

import ml.combust.bundle.BundleContext
import ml.combust.mleap.core.feature.HotleadTransformerModel
import ml.combust.mleap.runtime.transformer.feature.HotleadTransformer
import ml.combust.bundle.op.OpModel
import ml.combust.bundle.dsl._
import ml.combust.mleap.bundle.ops.MleapOp
import ml.combust.mleap.runtime.MleapContext
import ml.combust.mleap.runtime.types.BundleTypeConverters._

/**
 * Serializer for Gram assembler to run in the mleap platform
 */
class HotleadTransformerOp extends MleapOp[HotleadTransformer, HotleadTransformerModel] {
  override val Model: OpModel[MleapContext, HotleadTransformerModel] = new OpModel[MleapContext, HotleadTransformerModel] {
    override val klazz: Class[HotleadTransformerModel] = classOf[HotleadTransformerModel]

    override def opName: String = "hotlead_predictor"

    override def store(model: Model, obj: HotleadTransformerModel)
                      (implicit context: BundleContext[MleapContext]): Model = {
      model.withValue("input_shapes", Value.dataShapeList(obj.inputShapes.map(mleapToBundleShape)))
    }

    override def load(model: Model)
                     (implicit context: BundleContext[MleapContext]): HotleadTransformerModel = {
      val inputShapes = model.value("input_shapes").getDataShapeList.map(bundleToMleapShape)
      val thresholds = model.value("thresholds").getDoubleList
      val topThresholds = model.value("topThresholds").getDoubleList
      val nPages = model.value("nPages").getInt
      HotleadTransformerModel(inputShapes,thresholds,topThresholds,nPages)
    }
  }

  override def model(node: HotleadTransformer): HotleadTransformerModel = node.model
}
