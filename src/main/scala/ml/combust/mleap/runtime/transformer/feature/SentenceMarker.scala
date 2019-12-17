package ml.combust.mleap.runtime.transformer.feature

import ml.combust.mleap.core.feature.{CaseNormalizationModel, SentenceMarkerModel}
import ml.combust.mleap.core.types.NodeShape
import ml.combust.mleap.runtime.function.UserDefinedFunction
import ml.combust.mleap.runtime.frame.{SimpleTransformer, Transformer}

/**
  * Class which will be called by mleap during runtime for sentence marker
  */
case class SentenceMarker(override val uid: String = Transformer.uniqueName("sentence_marker"),
                             override val shape: NodeShape,
                             override val model: SentenceMarkerModel) extends SimpleTransformer {
  override val exec: UserDefinedFunction = (label: String) => model(label)
}
