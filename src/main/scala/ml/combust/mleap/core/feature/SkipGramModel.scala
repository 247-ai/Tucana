package ml.combust.mleap.core.feature

import ml.combust.mleap.core.Model
import ml.combust.mleap.core.types.{BasicType, ListType, StructType}

case class SkipGramModel(windowSize: Int) extends Model{
  def apply(value: Seq[String]): Seq[String] = {
    (3 to windowSize)
      .flatMap(value
        .iterator
        .sliding(_)
        .withPartial(false)
        .map(w =>
        {
          if (w.head != w.last)
          {
            w.head + " & " + w.last
          }
          else ""
        })
      ).filter(!_.isEmpty)
  }

  override def inputSchema: StructType = StructType("input" -> ListType(BasicType.String)).get


  override def outputSchema: StructType = StructType("output" -> ListType(BasicType.String)).get
}
