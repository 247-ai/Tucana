package ml.combust.mleap.runtime.transformer.feature

import ml.combust.mleap.core.feature.SkipGramModel
import ml.combust.mleap.core.types.NodeShape
import ml.combust.mleap.runtime.function.UserDefinedFunction
import ml.combust.mleap.runtime.frame.{SimpleTransformer, Transformer}

case class SkipGram(override val uid: String = Transformer.uniqueName("skip_gram"),
                 override val shape: NodeShape,
                 override val model: SkipGramModel) extends SimpleTransformer {
  override val exec: UserDefinedFunction = (value: Seq[String]) => model(value)
}
