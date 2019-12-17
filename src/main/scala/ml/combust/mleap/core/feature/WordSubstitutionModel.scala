package ml.combust.mleap.core.feature

import com.github.aztek.porterstemmer.PorterStemmer
import ml.combust.mleap.core.Model
import ml.combust.mleap.core.types.{ScalarType, StructType}

/**
  * Core logic for word substitution
  */
case class WordSubstitutionModel(dictionary:Map[String,String],delimiter:String) extends Model {
  def apply(label: String): String = {
    val words = (delimiter + "|(" +" "+ ")").r.split(label).toSeq
    words.map(token => dictionary.getOrElse(token, token))
      .foldLeft(Seq[String]())((accumulator, subsToken) => accumulator ++ subsToken.split("\\s")).mkString(" ")
  }

  override def inputSchema: StructType = StructType("input" -> ScalarType.String).get

  override def outputSchema: StructType = StructType("output" -> ScalarType.String).get
}
