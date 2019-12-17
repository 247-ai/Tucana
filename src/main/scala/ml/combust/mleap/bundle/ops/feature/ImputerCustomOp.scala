package ml.combust.mleap.bundle.ops.feature

import ml.combust.bundle.BundleContext
import ml.combust.bundle.dsl._
import ml.combust.bundle.op.OpModel
import ml.combust.mleap.bundle.ops.MleapOp
import ml.combust.mleap.core.feature.ImputerCustomModel
import ml.combust.mleap.runtime.MleapContext
import ml.combust.mleap.runtime.transformer.feature.ImputerCustom
import ml.combust.mleap.runtime.types.BundleTypeConverters.{bundleToMleapShape, mleapToBundleShape}

/**
  * Serializer for imputer transformer to execute it in mleap platform
  */
class ImputerCustomOp extends MleapOp[ImputerCustom, ImputerCustomModel] {
  override val Model: OpModel[MleapContext, ImputerCustomModel] = new OpModel[MleapContext, ImputerCustomModel] {
    override val klazz: Class[ImputerCustomModel] = classOf[ImputerCustomModel]

    override def opName: String = "imputercustom"

    override def store(model: Model, obj: ImputerCustomModel)
                      (implicit context: BundleContext[MleapContext]): Model = {
      model.withValue("input_shape", Value.dataShape(mleapToBundleShape(obj.inputShape)))
        .withValue("input_type", Value.string(obj.inputType))
        .withValue("surrogate_value", Value.string(obj.surrogateValue))

    }

    override def load(model: Model)(implicit context: BundleContext[MleapContext]): ImputerCustomModel = {

      val inputShape = bundleToMleapShape(model.value("input_shape").getDataShape)
      ImputerCustomModel(model.value("input_type").getString,
        model.value("surrogate_value").getString,inputShape)
    }

  }

  override def model(node: ImputerCustom): ImputerCustomModel = node.model
}