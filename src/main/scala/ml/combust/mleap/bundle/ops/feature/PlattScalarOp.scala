package ml.combust.mleap.bundle.ops.feature

import ml.combust.bundle.BundleContext
import ml.combust.mleap.core.classification.{ClassificationModel, ProbabilisticClassificationModel}
import ml.combust.mleap.core.feature.PlattScalarModel
import ml.combust.mleap.runtime.transformer.feature.PlattScalar
import ml.combust.bundle.op.OpModel
import ml.combust.bundle.serializer.ModelSerializer
import ml.combust.bundle.dsl._
import ml.combust.mleap.bundle.ops.MleapOp
import ml.combust.mleap.runtime.MleapContext
import org.apache.spark.ml.linalg.{Vectors,Vector}

import scala.collection.mutable.ArrayBuffer

/**
  * Serialization for plattscalar transformer to generate probabilities
  */
class PlattScalarOp extends MleapOp[PlattScalar, PlattScalarModel] {
  override val Model: OpModel[MleapContext, PlattScalarModel] = new OpModel[MleapContext, PlattScalarModel] {
    override val klazz: Class[PlattScalarModel] = classOf[PlattScalarModel]

    override def opName: String = "platt_scalar"

    override def store(model: Model, obj: PlattScalarModel)
                      (implicit context: BundleContext[MleapContext]): Model = {

      model.withValue("num_classes", Value.long(obj.coefficients.length))
    }

    override def load(model: Model)
                     (implicit context: BundleContext[MleapContext]): PlattScalarModel = {
      val numClasses = model.value("num_classes").getLong.toInt
      val numFeatures = model.value("num_features").getLong.toInt

      var coefficients = ArrayBuffer[Vector]()
      var intercepts = ArrayBuffer[Double]()
      (0 until numClasses).toArray.foreach {i =>
        coefficients += Vectors.dense(model.value(s"coefficients$i").getTensor[Double].toArray)
        intercepts += model.value(s"intercept$i").getDouble
      }

      PlattScalarModel(coefficients = coefficients.toArray,intercepts = intercepts.toArray, numClasses = numClasses)
    }
  }

  override def model(node: PlattScalar): PlattScalarModel = node.model
}

