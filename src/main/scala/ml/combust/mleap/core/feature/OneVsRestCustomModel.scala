package ml.combust.mleap.core.feature

import ml.combust.mleap.core.Model
import ml.combust.mleap.core.classification.{ClassificationModel, ProbabilisticClassificationModel}
import ml.combust.mleap.core.types._
import org.apache.spark.ml.linalg.{Vector, Vectors}

import scala.collection.mutable

/** Class for multinomial one vs rest models.
  *
  * One vs rest models are comprised of a series of
  * [[ClassificationModel]]s which are used to
  * predict each class.
  *
  * @param classifiers binary classification models
  */
case class OneVsRestCustomModel(classifiers: Array[ClassificationModel],
                          numFeatures: Int) extends Model {
  /** Alias for [[ml.combust.mleap.core.classification.OneVsRestModel#predict]].
    *
    * @param features feature vector
    * @return prediction
    */
  def apply(features: Vector): Double = predictOutput(features)

  def predictOutput(features: Vector): Double = {
    predictRaw(features)._2
  }

  def predictRawVector(features:Vector):Vector = {
    predictRaw(features)._1
  }

  def predictProbabilityVector(features:Vector):Vector = {
    predictProbability(features)
  }

  /** Predict the class and probability for a feature vector.
    *
    * @param features feature vector
    * @return (rawPredictions of all class, predicted class)
    */
  def predictRaw(features: Vector): (Vector,Double) = {
    val rawPredArray = mutable.ArrayBuilder.make[Double]
    val indices = mutable.ArrayBuilder.make[Int]
    var cur = 0
    val (prediction,probability) = classifiers.zipWithIndex.map {
      case (c: ProbabilisticClassificationModel, i) =>
        val raw = c.predictRaw(features)(1)
        indices += cur
        rawPredArray += raw
        cur += 1
        (i.toDouble,raw)
      case (c,i) =>
        val raw = c.predictRaw(features)
        indices += cur
        rawPredArray += raw(1)
        cur += 1
        (i.toDouble,raw(1))

    }.maxBy(_._2)

    (Vectors.dense(rawPredArray.result()),prediction)
  }
  /** Predict the probability for a feature vector.
    *
    * @param features feature vector
    * @return (probability for all classes)
    */
  def predictProbability(features: Vector): Vector = {
    val probArray = mutable.ArrayBuilder.make[Double]
    val indices = mutable.ArrayBuilder.make[Int]
    var cur = 0
    classifiers.zipWithIndex.foreach {
      case (c:ProbabilisticClassificationModel, i) =>
        val prob = c.predictProbabilities(features)(1)
        indices += cur
        probArray += prob
        cur += 1
      case _ => throw new Exception("Only probability models can return probability")
    }

    Vectors.dense(probArray.result())
  }

  override def inputSchema: StructType = StructType("features" -> TensorType.Double(numFeatures)).get

  override def outputSchema: StructType = StructType("rawPrediction" -> TensorType.Double(classifiers.length),
    "probability" -> TensorType.Double(classifiers.length),
    "prediction" -> ScalarType.Double).get
}