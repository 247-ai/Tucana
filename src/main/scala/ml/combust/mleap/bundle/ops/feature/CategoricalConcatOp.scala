package ml.combust.mleap.bundle.ops.feature

import ml.combust.bundle.BundleContext
import ml.combust.mleap.core.feature.CategoricalConcatModel
import ml.combust.mleap.runtime.transformer.feature.CategoricalConcat
import ml.combust.bundle.op.OpModel
import ml.combust.bundle.dsl._
import ml.combust.mleap.bundle.ops.MleapOp
import ml.combust.mleap.runtime.MleapContext
import ml.combust.mleap.runtime.types.BundleTypeConverters._

/**
  * Serialization for categorical concat transformer
  */
class CategoricalConcatOp extends MleapOp[CategoricalConcat, CategoricalConcatModel] {
  override val Model: OpModel[MleapContext, CategoricalConcatModel] = new OpModel[MleapContext, CategoricalConcatModel] {
    override val klazz: Class[CategoricalConcatModel] = classOf[CategoricalConcatModel]

    override def opName: String = "cat_concat"

    override def store(model: Model, obj: CategoricalConcatModel)
                      (implicit context: BundleContext[MleapContext]): Model = {
      model.withValue("input_shapes", Value.dataShapeList(obj.inputShapes.map(mleapToBundleShape)))
    }

    override def load(model: Model)
                     (implicit context: BundleContext[MleapContext]): CategoricalConcatModel = {
      val inputShapes = model.value("input_shapes").getDataShapeList.map(bundleToMleapShape)
      CategoricalConcatModel(inputShapes)
    }
  }

  override def model(node: CategoricalConcat): CategoricalConcatModel = node.model
}