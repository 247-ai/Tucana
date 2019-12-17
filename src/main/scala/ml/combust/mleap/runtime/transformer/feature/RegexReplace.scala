package ml.combust.mleap.runtime.transformer.feature

import ml.combust.mleap.core.feature.RegexReplaceModel
import ml.combust.mleap.core.types.NodeShape
import ml.combust.mleap.runtime.function.UserDefinedFunction
import ml.combust.mleap.runtime.frame.{SimpleTransformer, Transformer}

/**
  * Class which will be called by mleap during runtime for regex replacement
  */
case class RegexReplace(override val uid: String = Transformer.uniqueName("regex_replace"),
                        override val shape: NodeShape,
                        override val model: RegexReplaceModel) extends SimpleTransformer {
  override val exec: UserDefinedFunction = (label: String) => model(label)
}