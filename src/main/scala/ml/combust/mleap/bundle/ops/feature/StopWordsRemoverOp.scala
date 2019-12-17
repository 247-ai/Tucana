package ml.combust.mleap.bundle.ops.feature

import ml.combust.bundle.BundleContext
import ml.combust.mleap.core.feature.StopWordsRemoverModel
import ml.combust.mleap.runtime.transformer.feature.StopWordsRemover
import ml.combust.bundle.op.OpModel
import ml.combust.bundle.dsl._
import ml.combust.mleap.bundle.ops.MleapOp
import ml.combust.mleap.runtime.MleapContext
import ml.combust.mleap.runtime.types.BundleTypeConverters._

/**
  * Serializer for stop word remover transformer
  */
class StopWordsRemoverOp extends MleapOp[StopWordsRemover, StopWordsRemoverModel] {
  override val Model: OpModel[MleapContext, StopWordsRemoverModel] = new OpModel[MleapContext, StopWordsRemoverModel] {
    override val klazz: Class[StopWordsRemoverModel] = classOf[StopWordsRemoverModel]

    override def opName: String = "stopwords_remove"

    override def store(model: Model, obj: StopWordsRemoverModel)
                      (implicit context: BundleContext[MleapContext]): Model = {
      model.withValue("stopwords", Value.stringList(obj.stopwords))
        .withValue("pattern",Value.string(obj.delimiter))
    }

    override def load(model: Model)
                     (implicit context: BundleContext[MleapContext]): StopWordsRemoverModel = {
      val stopwords = model.value("stopwords").getStringList
      val pattern = model.value("pattern").getString
      StopWordsRemoverModel(stopwords,pattern)
    }
  }

  override def model(node: StopWordsRemover): StopWordsRemoverModel = node.model
}
