package ml.combust.mleap.core.feature

import com.github.aztek.porterstemmer.PorterStemmer
import ml.combust.mleap.core.Model
import ml.combust.mleap.core.types.{ScalarType, StructType}

/**
  * Core logic for porter stemmer.
  */
case class PorterStemmerModel(exceptions:Array[String],delimiter:String) extends Model {
  def apply(label: String): String = {
    def replacement(word: String): String = {
      if(!exceptions.isEmpty) {
        if (!exceptions.contains(word) && !word.contains("_class_")) {
          val update = Option(PorterStemmer.stem(word))
          update match {
            case Some(str) => str
            case None => word
          }
        }
        else
          word
      }
      else {
        if (!word.contains("_class_")) {
          val update = Option(PorterStemmer.stem(word))
          update match {
            case Some(str) => str
            case None => word
          }
        }
        else
          word
      }
    }

    (delimiter + "|(" +" "+")").r.split(label).toSeq.map(word => replacement(word)).mkString(" ")
  }

  override def inputSchema: StructType = StructType("input" -> ScalarType.String).get

  override def outputSchema: StructType = StructType("output" -> ScalarType.String).get
}