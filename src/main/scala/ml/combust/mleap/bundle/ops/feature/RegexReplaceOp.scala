package ml.combust.mleap.bundle.ops.feature

import ml.combust.bundle.BundleContext
import ml.combust.bundle.dsl._
import ml.combust.bundle.op.OpModel
import ml.combust.mleap.bundle.ops.MleapOp
import ml.combust.mleap.core.feature.RegexReplaceModel
import ml.combust.mleap.runtime.MleapContext
import ml.combust.mleap.runtime.transformer.feature.RegexReplace
import ml.combust.mleap.runtime.types.BundleTypeConverters.{bundleToMleapShape, mleapToBundleShape}

/**
  * Serializer for Regex replace transformer to execute it in mleap platform
  */

class RegexReplaceOp extends MleapOp[RegexReplace, RegexReplaceModel] {
  override val Model: OpModel[MleapContext, RegexReplaceModel] = new OpModel[MleapContext, RegexReplaceModel] {
    override val klazz: Class[RegexReplaceModel] = classOf[RegexReplaceModel]

    override def opName: String = "regex_replace"

    override def store(model: Model, obj: RegexReplaceModel)
                      (implicit context: BundleContext[MleapContext]): Model = {
      val (labels, values) = obj.labels.toSeq.unzip

      // add the labels and values to the Bundle model that
      // will be serialized to our MLeap bundle
      model.withValue("labels", Value.stringList(labels)).
        withValue("values", Value.stringList(values))
    }

    override def load(model: Model)
                     (implicit context: BundleContext[MleapContext]): RegexReplaceModel = {
      val labels = model.value("labels").getStringList

      // retrieve our list of values
      val values = model.value("values").getStringList

      // reconstruct the model using the parallel labels and values
      RegexReplaceModel(labels.zip(values).toMap)
    }
  }

  override def model(node: RegexReplace): RegexReplaceModel = node.model
}