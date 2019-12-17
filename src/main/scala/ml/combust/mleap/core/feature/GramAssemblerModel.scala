package ml.combust.mleap.core.feature

import ml.combust.mleap.core.Model
import ml.combust.mleap.core.annotation.SparkCode
import ml.combust.mleap.core.types._
import ml.combust.mleap.tensor.{DenseTensor, SparseTensor}
import org.apache.spark.ml.linalg.{Vector, Vectors}

import scala.collection.mutable

case class GramAssemblerModel(inputShapes: Seq[DataShape]) extends Model {

  def apply(gramSeq: Seq[Any]): Seq[String] = {
    val values = mutable.ArrayBuilder.make[String]
    gramSeq.foreach {
      v =>
        val gramIterator = v.asInstanceOf[Seq[String]]
        gramIterator.foreach(gram => values += gram)
    }
    values.result().toSeq
  }

  override def inputSchema: StructType = {
    val inputFields = inputShapes.zipWithIndex.map {
      case (shape, i) => StructField(s"input$i", DataType(BasicType.String, shape))
    }

    StructType(inputFields).get
  }

  override def outputSchema: StructType = StructType(StructField("output" -> ListType(BasicType.String))).get
}
