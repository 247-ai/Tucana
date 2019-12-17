package ml.combust.mleap.core.feature

import ml.combust.mleap.core.Model
import ml.combust.mleap.core.classification.ProbabilisticClassificationModel
import ml.combust.mleap.core.types._
import ml.combust.mleap.tensor.Tensor
import org.apache.spark.ml.linalg.mleap.BLAS
import org.apache.spark.ml.linalg.{Vector, Vectors}


case class UpliftModel(args:Any*) extends Model {
  val baseCoefficients:Vector = args(0).asInstanceOf[Vector]
  val baseIntercept:Double = args(1).asInstanceOf[Double]
  val numFeatures:Int = baseCoefficients.size
  private val baseScore: (Vector) => Double = (features) =>
  {
    val m = BLAS.dot(features, baseCoefficients)+ baseIntercept
    1.0 / (1.0 + math.exp(-m))
  }

  private val margin: Vector => Double = features =>
  {
    BLAS.dot(features, baseCoefficients) + baseIntercept
  }

  def apply(features: Any): Vector = predictProbVector(features)

  def probTreatmentNegative(features:Any):Vector = {
    var negProb = 0.0
    features match {
      case x: Tensor[Double] => {
        val featuresArr = x.toArray
        featuresArr(baseCoefficients.toArray.size - 1) = 0.0
        negProb = if (args.length<=2) baseScore(Vectors.dense(featuresArr)) else margin(Vectors.dense(featuresArr))
      }
    }
    if (args.length<=2) Vectors.dense(Array(1-negProb,negProb)) else Vectors.dense(-negProb,negProb)
  }

  def probTreatmentPositive(features:Any):Vector = {
    var posProb = 0.0
    features match {
      case x: Tensor[Double] => {
        val featuresArr = x.toArray
        featuresArr(baseCoefficients.toArray.size - 1) = 1.0
        posProb = if (args.length<=2) baseScore(Vectors.dense(featuresArr)) else margin(Vectors.dense(featuresArr))
      }
    }
    if (args.length<=2) Vectors.dense(Array(1-posProb,posProb)) else Vectors.dense(-posProb,posProb)
  }

  def predictProbVector(features:Any):Vector = {
    predict(features)
  }

  def predictByMaxProb(probability:Any):Double = {
    probability match{
      case x:Tensor[Double] => {
        x.toArray.zipWithIndex.maxBy(_._1)._2.toDouble
      }
    }
  }
  def plattProb(prob:Vector):Vector = {
    val plattCoefficients = args(2).asInstanceOf[Vector]
    val plattIntercept = args(3).asInstanceOf[Double]
    val plattRaw = (prob(1)*plattCoefficients(0))+plattIntercept
    val probability = 1.0 / (1.0 + math.exp(-plattRaw))
    Vectors.dense(Array(probability))
  }

  /** Predict the uplift probability for a feature vector.
   *
   * @param features feature vector
   * @return uplift probability
   */

  def predict(features: Any): Vector = {
    var (posProb,negProb) = (probTreatmentPositive(features),probTreatmentNegative(features))
    posProb = if(args.length>2)plattProb(posProb) else posProb
    negProb = if(args.length>2)plattProb(negProb) else negProb
    Vectors.dense(Array(posProb(0)-negProb(0)))
  }

  override def inputSchema: StructType = StructType("features" -> TensorType.Double(numFeatures)).get

  override def outputSchema: StructType = StructType("probability" -> TensorType.Double(2)).get

}