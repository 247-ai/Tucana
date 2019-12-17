package ml.combust.mleap.core.feature

import com.github.aztek.porterstemmer.PorterStemmer
import ml.combust.mleap.core.{Model, types}
import ml.combust.mleap.core.types._
import ml.combust.mleap.tensor.Tensor

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
  * Core logic for stop word remover
  */
case class TopKIntentsModel(labels:Seq[String],kValue:Int) extends Model {
  def apply(predictions: Seq[Any]): Array[(String,Double)] = {
    //val values = mutable.ArrayBuilder.make[(String,Double)]
    val res = ArrayBuffer[(String,Double)]()
    predictions.foreach {
      case x:Tensor[Double] =>
      val scoresAndLabels = for ((score, idx) <- x.toArray.zipWithIndex) yield (labels(idx), score)
      res ++= scoresAndLabels.sortBy(-_._2).take(kValue)
      //res.foreach { case (intent, prob) => values ++= (intent,prob) }
    }
    res.toArray
  }

  override def inputSchema: StructType = StructType(StructField(s"input", DataType(BasicType.Double, types.TensorShape(Some(Seq(labels.size)), true)))).get

  override def outputSchema: StructType = StructType("output" ->  ListType(BasicType.String)).get
}
