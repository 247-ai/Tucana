package ml.combust.mleap.core.feature

import ml.combust.mleap.core.Model
import ml.combust.mleap.core.annotation.SparkCode
import ml.combust.mleap.core.types._
import ml.combust.mleap.tensor.{DenseTensor, SparseTensor}
import org.apache.spark.ml.linalg.{Vector, Vectors}

import scala.collection.mutable

case class CategoricalConcatModel(inputShapes: Seq[DataShape]) extends Model {

  //concating the column name with the value
  def apply(valueArray:Seq[Any],nameArray:Seq[String]) : Seq[String] = {
    val values = mutable.ArrayBuilder.make[String]
    nameArray.zip(valueArray).foreach {
      case (a: String, b: String) => values += a + "_" + b
      case _ => ""
    }
    values.result().toSeq
  }

  override def inputSchema: StructType = {
    // defining schema for all the input variables
    val inputFields = inputShapes.zipWithIndex.map {
      case (shape, i) => StructField(s"input$i", DataType(BasicType.String, shape))
    }

    StructType(inputFields).get
  }

  override def outputSchema: StructType = StructType(StructField("output" -> ListType(BasicType.String))).get
}
