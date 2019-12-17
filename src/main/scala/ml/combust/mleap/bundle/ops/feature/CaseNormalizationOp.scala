package ml.combust.mleap.bundle.ops.feature

import ml.combust.bundle.BundleContext
import ml.combust.bundle.dsl._
import ml.combust.bundle.op.OpModel
import ml.combust.mleap.bundle.ops.MleapOp
import ml.combust.mleap.core.feature.CaseNormalizationModel
import ml.combust.mleap.runtime.MleapContext
import ml.combust.mleap.runtime.transformer.feature.CaseNormalization
import ml.combust.mleap.runtime.types.BundleTypeConverters.{bundleToMleapShape, mleapToBundleShape}

/**
  * Serialization for Case normalization transformer
  */

class CaseNormalizationOp extends MleapOp[CaseNormalization, CaseNormalizationModel] {
  override val Model: OpModel[MleapContext, CaseNormalizationModel] = new OpModel[MleapContext, CaseNormalizationModel] {
    override val klazz: Class[CaseNormalizationModel] = classOf[CaseNormalizationModel]

    override def opName: String = "case_norm"

    override def store(model: Model, obj: CaseNormalizationModel)
                      (implicit context: BundleContext[MleapContext]): Model = {
      // add the labels and values to the Bundle model that
      // will be serialized to our MLeap bundle
      model
    }

    override def load(model: Model)
                     (implicit context: BundleContext[MleapContext]): CaseNormalizationModel = {

      // reconstruct the model using the parallel labels and values
      new CaseNormalizationModel()
    }
  }

  override def model(node: CaseNormalization): CaseNormalizationModel = node.model
}