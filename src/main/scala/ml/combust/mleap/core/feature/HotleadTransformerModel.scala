package ml.combust.mleap.core.feature

import ml.combust.mleap.core.Model
import ml.combust.mleap.core.annotation.SparkCode
import ml.combust.mleap.core.types._
import ml.combust.mleap.tensor.{DenseTensor, SparseTensor}
import org.apache.spark.ml.linalg.{Vector, Vectors}

import scala.collection.mutable

case class HotleadTransformerModel(inputShapes: Seq[DataShape], thresholds:Seq[Double], topThresholds:Seq[Double], nPages:Int ) extends Model {

  def apply(inputSeq: Seq[Any]): Boolean = {
    /*val values = mutable.ArrayBuilder.make[String]
    gramSeq.foreach {
      v =>
        val gramIterator = v.asInstanceOf[Seq[String]]
        gramIterator.foreach(gram => values += gram)
    }
    values.result().toSeq*/

    var page = inputSeq(0).asInstanceOf[Int]
    var prob = inputSeq(1).asInstanceOf[DenseTensor[Double]](1)
    val top = if(inputSeq.length > 2) inputSeq(2).asInstanceOf[Double] else 0.0
    if(page >= nPages)
      if(prob >= thresholds.last && (if(inputSeq.length > 2){ if(top >= topThresholds.last) true else false } else true)) true else false
    else
    if(prob >= thresholds(page-1) && (if(inputSeq.length > 2){ if(top >= topThresholds(page-1)) true else false } else true)) true else false
  }

  override def inputSchema: StructType = {
    val inputFields = inputShapes.zipWithIndex.map {
      case (shape, i) => if(i==0)StructField(s"input$i" -> ScalarType.Int) else if(i == 1) StructField(s"input$i" -> TensorType.Double(2)) else StructField(s"input$i" -> ScalarType.Double)
    }

    StructType(inputFields).get
  }

  override def outputSchema: StructType = StructType(StructField("output" -> ScalarType.Boolean)).get
}
