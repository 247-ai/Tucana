package ml.combust.mleap.runtime.transformer.feature

import ml.combust.mleap.core.feature.CaseNormalizationModel
import ml.combust.mleap.core.types.NodeShape
import ml.combust.mleap.runtime.function.UserDefinedFunction
import ml.combust.mleap.runtime.frame.{SimpleTransformer, Transformer}


case class CaseNormalization(override val uid: String = Transformer.uniqueName("case_norm"),
                           override val shape: NodeShape,
                           override val model: CaseNormalizationModel) extends SimpleTransformer {
  override val exec: UserDefinedFunction = (label: String) => model(label)
}
