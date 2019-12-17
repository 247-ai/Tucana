package ml.combust.mleap.bundle.ops.feature

import ml.combust.bundle.BundleContext
import ml.combust.mleap.core.feature.GramAssemblerModel
import ml.combust.mleap.runtime.transformer.feature.GramAssembler
import ml.combust.bundle.op.OpModel
import ml.combust.bundle.dsl._
import ml.combust.mleap.bundle.ops.MleapOp
import ml.combust.mleap.runtime.MleapContext
import ml.combust.mleap.runtime.types.BundleTypeConverters._

/**
  * Serializer for Gram assembler to run in the mleap platform
  */
class GramAssemblerOp extends MleapOp[GramAssembler, GramAssemblerModel] {
  override val Model: OpModel[MleapContext, GramAssemblerModel] = new OpModel[MleapContext, GramAssemblerModel] {
    override val klazz: Class[GramAssemblerModel] = classOf[GramAssemblerModel]

    override def opName: String = "gram_assembler"

    override def store(model: Model, obj: GramAssemblerModel)
                      (implicit context: BundleContext[MleapContext]): Model = {
      model.withValue("input_shapes", Value.dataShapeList(obj.inputShapes.map(mleapToBundleShape)))
    }

    override def load(model: Model)
                     (implicit context: BundleContext[MleapContext]): GramAssemblerModel = {
      val inputShapes = model.value("input_shapes").getDataShapeList.map(bundleToMleapShape)
      GramAssemblerModel(inputShapes)
    }
  }

  override def model(node: GramAssembler): GramAssemblerModel = node.model
}
