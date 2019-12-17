package ml.combust.mleap.runtime.transformer.feature

import ml.combust.mleap.core.feature.PlattScalarModel
import ml.combust.mleap.core.types._
import ml.combust.mleap.runtime.function.{StructSelector, UserDefinedFunction}
import ml.combust.mleap.tensor.Tensor
import ml.combust.mleap.core.util.VectorConverters._
import ml.combust.mleap.runtime.frame.{FrameBuilder, MultiTransformer, Row, Transformer}
import org.apache.spark.ml.linalg.Vectors

import scala.util.Try

case class PlattScalar(override val uid: String = Transformer.uniqueName("platt_scalar"),
                       override val shape: NodeShape,
                       override val model: PlattScalarModel) extends MultiTransformer {
  // creates the probability vector column by calling the model's apply function
  private val f = (values: Row) => {
    val t = Some(values.head)
    model(values.head):Tensor[Double]}
  val exec: UserDefinedFunction = UserDefinedFunction(f,
    outputSchema.fields.head.dataType,
    Seq(SchemaSpec(inputSchema)))

  // Copies the value of existing prediction column to new svmPrediction column
  private val f1 = (values: Row) => {
    values.head}
  val exec1: UserDefinedFunction = UserDefinedFunction(f1,
    outputSchema.fields.last.dataType,
    Seq(SchemaSpec(StructType("prediction" -> ScalarType.Double).get)))

  // creates the new prediciton column based on the probability vector
  private val f2 = (values: Row) => {
    model.predictByMaxProb(values.head)}
  val exec2: UserDefinedFunction = UserDefinedFunction(f2,
    outputSchema.fields.last.dataType,
    Seq(SchemaSpec(inputSchema)))

  val outputCol: String = outputSchema.fields.head.name
  val inputCols: Seq[String] = inputSchema.fields.map(_.name)
  val svmPredOutputCol = "svmPrediction"
  private val inputSelector: StructSelector = StructSelector(inputCols)
  private val svmPredSelector:StructSelector = StructSelector(Seq("prediction"))


  override def transform[TB <: FrameBuilder[TB]](builder: TB): Try[TB] = {
    builder
      .withColumn(outputCol, inputSelector)(exec).get // creates the probability vector column
      .withColumn(svmPredOutputCol,svmPredSelector)(exec1).get // rename existing prediction column to svmPrediction
      .drop("prediction").get // drop the original prediction column
      .withColumn("prediction",StructSelector(Seq("probability")))(exec2) // create the new prediction column based on the probability vector
  }
}
