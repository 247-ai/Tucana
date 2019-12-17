package ml.combust.mleap.bundle.ops.feature

import ml.combust.bundle.BundleContext
import ml.combust.bundle.dsl._
import ml.combust.bundle.op.OpModel
import ml.combust.mleap.bundle.ops.MleapOp
import ml.combust.mleap.core.feature.UpliftModel
import ml.combust.mleap.runtime.MleapContext
import ml.combust.mleap.runtime.transformer.feature.Uplift
import org.apache.spark.ml.linalg.Vectors

/**
  * Serializer for Uplift transformer to run in the mleap platform
  */
class UpliftOp extends MleapOp[Uplift, UpliftModel] {
  override val Model: OpModel[MleapContext, UpliftModel] = new OpModel[MleapContext, UpliftModel] {
    override val klazz: Class[UpliftModel] = classOf[UpliftModel]

    override def opName: String = "uplift"

    override def store(model: Model, obj: UpliftModel)
                      (implicit context: BundleContext[MleapContext]): Model = {
      model
    }

    override def load(model: Model)
                     (implicit context: BundleContext[MleapContext]): UpliftModel = {
      val baseCoefficients = Vectors.dense(model.value(s"baseCoefficients").getTensor[Double].toArray)
      val baseIntercept = model.value(s"baseIntercept").getDouble
      val modelAtt = model.attributes.lookup
      if(modelAtt.contains("plattCoefficients")) {
        val plattCoefficients = Vectors.dense(model.value(s"plattCoefficients").getTensor[Double].toArray)
        val plattIntercept = model.value(s"plattIntercept").getDouble
        UpliftModel(baseCoefficients,baseIntercept,plattCoefficients,plattIntercept)
      }
      else
        UpliftModel(baseCoefficients,baseIntercept)

    }
  }

  override def model(node: Uplift): UpliftModel = node.model
}
