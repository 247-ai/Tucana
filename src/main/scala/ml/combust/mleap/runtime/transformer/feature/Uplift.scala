package ml.combust.mleap.runtime.transformer.feature

import ml.combust.mleap.core.feature.UpliftModel
import ml.combust.mleap.core.types._
import ml.combust.mleap.tensor.Tensor
import ml.combust.mleap.core.util.VectorConverters._
import ml.combust.mleap.runtime.frame.{FrameBuilder, Row, Transformer}
import ml.combust.mleap.runtime.function.{StructSelector, UserDefinedFunction}

import scala.util.Try

case class Uplift(override val uid: String = Transformer.uniqueName("uplift"),
                  override val shape: NodeShape,
                  override val model: UpliftModel) extends Transformer {
  private val f = (values:Row) => model(values.head):Tensor[Double]
  val exec: UserDefinedFunction = UserDefinedFunction(f,
    outputSchema.fields.head.dataType,Seq(SchemaSpec(inputSchema)))

  private val f1 = (values: Row) => {
    values.head}
  val exec1: UserDefinedFunction = UserDefinedFunction(f1,
    outputSchema.fields.last.dataType,
    Seq(SchemaSpec(StructType("probability" -> TensorType.Double(2)).get)))

  private val f2 = (values: Row) => {
    values.head}
  val exec2: UserDefinedFunction = UserDefinedFunction(f2,
    outputSchema.fields.last.dataType,
    Seq(SchemaSpec(StructType("rawPrediction" -> TensorType.Double(2)).get)))

  private val f3 = (values: Row) => {
    values.head}
  val exec3: UserDefinedFunction = UserDefinedFunction(f3,
    ScalarType.Double,
    Seq(SchemaSpec(StructType("prediction" -> ScalarType.Double).get)))

  val outputCol: String = outputSchema.fields.head.name
  val inputCols: Seq[String] = Seq("features")
  private val inputSelector: StructSelector = StructSelector(inputCols)

  override def transform[TB <: FrameBuilder[TB]](builder: TB): Try[TB] = {
    builder
      .withColumn("modelProbability",StructSelector(Seq("probability")))(exec1).get
      .drop("probability").get
      .withColumn("modelRawPrediction",StructSelector(Seq("rawPrediction")))(exec2).get
      .withColumn("modelPrediction",StructSelector(Seq("prediction")))(exec3).get
      .drop("prediction").get
      .withColumn("prediction",StructSelector(Seq("modelPrediction")))(exec3).get
      .withColumn(outputCol, inputSelector)(exec)

  }
}
