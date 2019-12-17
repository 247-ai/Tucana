package ml.combust.mleap.bundle.ops.feature

import ml.combust.bundle.BundleContext
import ml.combust.mleap.core.classification.{ClassificationModel,ProbabilisticClassificationModel}
import ml.combust.mleap.core.feature.OneVsRestCustomModel
import ml.combust.mleap.runtime.transformer.feature.OneVsRestCustom
import ml.combust.bundle.op.OpModel
import ml.combust.bundle.serializer.ModelSerializer
import ml.combust.bundle.dsl._
import ml.combust.mleap.bundle.ops.MleapOp
import ml.combust.mleap.runtime.MleapContext

/**
  * Serialization for ovr custom transformer
  */
class OneVsRestCustomOp extends MleapOp[OneVsRestCustom, OneVsRestCustomModel] {
  override val Model: OpModel[MleapContext, OneVsRestCustomModel] = new OpModel[MleapContext, OneVsRestCustomModel] {
    override val klazz: Class[OneVsRestCustomModel] = classOf[OneVsRestCustomModel]

    override def opName: String = "ovr_custom"

    override def store(model: Model, obj: OneVsRestCustomModel)
                      (implicit context: BundleContext[MleapContext]): Model = {

      model.withValue("num_classes", Value.long(obj.classifiers.length)).
        withValue("num_features", Value.long(obj.numFeatures))
    }

    override def load(model: Model)
                     (implicit context: BundleContext[MleapContext]): OneVsRestCustomModel = {
      val numClasses = model.value("num_classes").getLong.toInt
      val numFeatures = model.value("num_features").getLong.toInt

      val models = (0 until numClasses).toArray.map {
        i => ModelSerializer(context.bundleContext(s"model$i")).read().get.asInstanceOf[ClassificationModel]
      }

      OneVsRestCustomModel(classifiers = models, numFeatures = numFeatures)
    }
  }

  override def model(node: OneVsRestCustom): OneVsRestCustomModel = node.model
}

