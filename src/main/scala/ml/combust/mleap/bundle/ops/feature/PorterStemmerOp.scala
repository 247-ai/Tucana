package ml.combust.mleap.bundle.ops.feature

import ml.combust.bundle.BundleContext
import ml.combust.bundle.dsl._
import ml.combust.bundle.op.OpModel
import ml.combust.mleap.bundle.ops.MleapOp
import ml.combust.mleap.core.feature.PorterStemmerModel
import ml.combust.mleap.runtime.MleapContext
import ml.combust.mleap.runtime.transformer.feature.PorterStemmer

/**
  * Serializes the porter stemmer transformer for executing in mleap platform.
  */

class PorterStemmerOp extends MleapOp[PorterStemmer, PorterStemmerModel] {
  override val Model: OpModel[MleapContext, PorterStemmerModel] = new OpModel[MleapContext, PorterStemmerModel] {
    override val klazz: Class[PorterStemmerModel] = classOf[PorterStemmerModel]

    override def opName: String = "porter_stemmer"

    override def store(model: Model, obj: PorterStemmerModel)
                      (implicit context: BundleContext[MleapContext]): Model = {
      val exceptions = obj.exceptions
      val delimiter = obj.delimiter

      // add the labels and values to the Bundle model that
      // will be serialized to our MLeap bundle
      model.withValue("exceptions", Value.stringList(exceptions)).
        withValue("delimiter", Value.string(delimiter))
    }

    override def load(model: Model)
                     (implicit context: BundleContext[MleapContext]): PorterStemmerModel = {
      val exceptions = model.value("exceptions").getStringList

      // retrieve our list of values
      val delimiter = model.value("delimiter").getString

      // reconstruct the model using the parallel labels and values
      PorterStemmerModel(exceptions.toArray,delimiter)
    }
  }

  override def model(node: PorterStemmer): PorterStemmerModel = node.model
}