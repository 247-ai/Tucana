package ml.combust.mleap.core.feature

import com.github.aztek.porterstemmer.PorterStemmer
import ml.combust.mleap.core.Model
import ml.combust.mleap.core.types.{ScalarType, StructType}

/**
  * Core logic for stop word remover
  */
case class StopWordsRemoverModel(stopwords:Seq[String],delimiter:String) extends Model {
  def apply(line: String): String = {
    val toLower = (s: String) => if (s != null) s.toLowerCase() else s
    val lowerStopWords = stopwords.map(toLower(_)).toSet
      val tokens = (delimiter + "|(" +" "+ ")").r.split(line).toSeq
      tokens.filter(s => !lowerStopWords.contains(toLower(s))).mkString(" ")
  }

  override def inputSchema: StructType = StructType("input" -> ScalarType.String).get

  override def outputSchema: StructType = StructType("output" -> ScalarType.String).get
}
