package ml.combust.mleap.core.feature

import ml.combust.mleap.core.Model
import ml.combust.mleap.core.classification.{ClassificationModel, ProbabilisticClassificationModel}
import ml.combust.mleap.core.types._
import ml.combust.mleap.tensor.{DenseTensor, Tensor}
import org.apache.spark.ml.linalg.{Vector, Vectors}

import scala.collection.mutable
import org.apache.spark.ml.linalg.mleap.BLAS

/**
 * This is the core logic to compute the probability from the raw prediction value of LinearSVC class
 * @param coefficients platt model coefficients
 * @param intercepts platt model intercept
 * @param numClasses
 */
case class PlattScalarModel(coefficients: Array[Vector],
                            intercepts: Array[Double],
                            numClasses: Int) extends Model {

  private val score: (Vector,Int) => Double = (features,index) =>
  {
    val m = BLAS.dot(features, coefficients(index)) + intercepts(index)
    1.0 / (1.0 + math.exp(-m))
  }

  def apply(features: Any): Vector = predictProbVector(features)

  def predictProbVector(features:Any):Vector = {
    predict(features)
  }

  def predictByMaxProb(probability:Any):Double = {
    probability match{
      case x:Tensor[Double] => {
        if(x.toArray.length == 1)
          if(x(0) > 0.5) 1.0 else 0.0
        else
          x.toArray.zipWithIndex.maxBy(_._1)._2.toDouble
      }
    }
  }

  /** Predict the probability for a feature vector.
   *
   * @param features feature vector
   * @return positive probability vector
   */

  def predict(features: Any): Vector = {
    val probArray = mutable.ArrayBuilder.make[Double]
    features match{
      case x:Tensor[Double] =>
        if(numClasses > 1) {
          (0 until numClasses).map { i =>
            val prob = score(Vectors.dense(Array(x(i))), i)
            probArray += prob
          }
        }
        else {
          val prob = score(Vectors.dense(Array(x(1))), 0)
          probArray += prob
        }
    }
    val probVector = probArray.result()
    Vectors.dense(probVector)
  }

  override def inputSchema: StructType = StructType("rawPrediction" -> TensorType.Double(numClasses)).get

  override def outputSchema: StructType = StructType("probability" -> TensorType.Double(numClasses),
    "prediction" -> ScalarType.Double).get

}