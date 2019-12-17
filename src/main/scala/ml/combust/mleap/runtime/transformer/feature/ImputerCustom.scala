package ml.combust.mleap.runtime.transformer.feature

import ml.combust.mleap.core.feature.ImputerCustomModel
import ml.combust.mleap.core.types._
import ml.combust.mleap.tensor.Tensor
import ml.combust.mleap.core.util.VectorConverters._
import ml.combust.mleap.runtime.frame.{FrameBuilder, Row, Transformer}
import ml.combust.mleap.runtime.function.{StructSelector, UserDefinedFunction}

import scala.util.Try

case class ImputerCustom(override val uid: String = Transformer.uniqueName("imputercustom"),
                         override val shape: NodeShape,
                         override val model: ImputerCustomModel) extends Transformer {
  private val f = (values: Row) => {
    model(values.head)}
  val exec: UserDefinedFunction = UserDefinedFunction(f,
    outputSchema.fields.head.dataType,
    Seq(SchemaSpec(inputSchema)))
  val f1 = (values:Row) => values.head
  val exec1: UserDefinedFunction = UserDefinedFunction(f1,
    outputSchema.fields.head.dataType,
    Seq(SchemaSpec(inputSchema)))

  val outputCol: String = outputSchema.fields.head.name
  val inputCols: Seq[String] = inputSchema.fields.map(_.name)
  private val inputSelector: StructSelector = StructSelector(inputCols)
  private val outputSelector:StructSelector = StructSelector(Seq(outputCol))

  override def transform[TB <: FrameBuilder[TB]](builder: TB): Try[TB] = {
    builder
      .withColumn(outputCol, inputSelector)(exec).get
      .drop(inputCols.head).get
      .withColumn(inputCols.head,outputSelector)(exec1).get
      .drop(outputCol)
  }
}