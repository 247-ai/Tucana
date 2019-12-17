package ml.combust.mleap.bundle.ops.feature

import ml.combust.bundle.BundleContext
import ml.combust.bundle.dsl._
import ml.combust.bundle.op.OpModel
import ml.combust.mleap.bundle.ops.MleapOp
import ml.combust.mleap.core.feature.WordSubstitutionModel
import ml.combust.mleap.runtime.MleapContext
import ml.combust.mleap.runtime.transformer.feature.WordSubstitution
import ml.combust.mleap.runtime.types.BundleTypeConverters.{bundleToMleapShape, mleapToBundleShape}

/**
  * Serializer for word substitution transformer to execute it in mleap platform
  */

class WordSubstitutionOp extends MleapOp[WordSubstitution, WordSubstitutionModel] {
  override val Model: OpModel[MleapContext, WordSubstitutionModel] = new OpModel[MleapContext, WordSubstitutionModel] {
    override val klazz: Class[WordSubstitutionModel] = classOf[WordSubstitutionModel]

    override def opName: String = "word_substitute"

    override def store(model: Model, obj: WordSubstitutionModel)
                      (implicit context: BundleContext[MleapContext]): Model = {
      val (labels, values) = obj.dictionary.toSeq.unzip
      val pattern = obj.delimiter

      // add the labels and values to the Bundle model that
      // will be serialized to our MLeap bundle
      model.withValue("labels", Value.stringList(labels)).
        withValue("values", Value.stringList(values)).
        withValue("pattern",Value.string(pattern))
    }

    override def load(model: Model)
                     (implicit context: BundleContext[MleapContext]): WordSubstitutionModel = {
      val labels = model.value("labels").getStringList

      // retrieve our list of values
      val values = model.value("values").getStringList
      val pattern = model.value("pattern").getString

      // reconstruct the model using the parallel labels and values
      WordSubstitutionModel(labels.zip(values).toMap,pattern)
    }
  }

  override def model(node: WordSubstitution): WordSubstitutionModel = node.model
}
