package ml.combust.mleap.runtime.transformer.feature

import ml.combust.mleap.core.feature.OneVsRestCustomModel
import ml.combust.mleap.core.types._
import ml.combust.mleap.runtime.function.UserDefinedFunction
import ml.combust.mleap.tensor.Tensor
import ml.combust.mleap.core.util.VectorConverters._
import ml.combust.mleap.runtime.frame.{MultiTransformer, Row, Transformer}

case class OneVsRestCustom(override val uid: String = Transformer.uniqueName("ovr_custom"),
                     override val shape: NodeShape,
                     override val model: OneVsRestCustomModel) extends MultiTransformer {
  override val exec: UserDefinedFunction = {
    val f = (shape.getOutput("rawPrediction"),shape.getOutput("probability")) match {
      case (Some(_),Some(_)) =>
        (features: Tensor[Double]) => {
          val rawPrediction:Tensor[Double] = model.predictRawVector(features)
          val probability:Tensor[Double] = model.predictProbabilityVector(features)
          val prediction = model.predictOutput(features)
          Row(rawPrediction,probability, prediction)
        }
      case (Some(_),None) =>
        (features: Tensor[Double]) => {
          val rawPrediction:Tensor[Double] = model.predictRawVector(features)
          val prediction = model.predictOutput(features)
          Row(rawPrediction, prediction)
        }
      case (None,Some(_)) =>
        (features: Tensor[Double]) => {
          val probability:Tensor[Double] = model.predictProbabilityVector(features)
          val prediction = model.predictOutput(features)
          Row(probability, prediction)
        }
      case (None,None) =>
        (features: Tensor[Double]) => Row(model(features))
    }

    UserDefinedFunction(f, outputSchema, inputSchema)
  }
}
