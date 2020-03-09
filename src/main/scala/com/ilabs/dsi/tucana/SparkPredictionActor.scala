package com.ilabs.dsi.tucana

import java.io.{File, FileOutputStream}
import java.nio.file.Files
import java.util.Calendar

import akka.actor.{Actor, ActorRef}
import akka.pattern.ask
import akka.util.Timeout
import com.ilabs.dsi.tucana.utils.{DBManager, Json, PredictServerConfig}
import ml.combust.bundle.BundleFile
import ml.combust.mleap.runtime.MleapSupport._
import ml.combust.mleap.runtime.frame.Transformer
import ml.combust.mleap.runtime.serialization.FrameReader
import ml.combust.mleap.tensor.DenseTensor
import resource._

import scala.concurrent.Await
import scala.concurrent.duration._


class SparkPredictionActor(cacheActor:ActorRef) extends Actor {

  import com.ilabs.dsi.tucana.LRUCacheActor._
  def receive = {
    case (modelId: String, version: String, input: String, request:String) => {

      implicit val timeout = Timeout(30.seconds)
      val (model, schema, topKCol):(Transformer,String,String) = Await.result(cacheActor ? getModel((modelId,version)),timeout.duration) match {
        case Some((oModel:Transformer,oSchema:String,oTopKCol:String)) => (oModel,oSchema,oTopKCol)
        case None => {
          val (compressedModel,savedSchema) = DBManager.getModel(modelId, version)
          val folder = Files.createTempDirectory("TucanaTemp")
          val file = new File(s"$folder/tucana.zip")
          val fos = new FileOutputStream(file.getPath)
          fos.write(compressedModel)
          val bundle = (for (bundleFile <- managed(BundleFile(s"jar:file:${file.getAbsolutePath}"))) yield {
            bundleFile.loadMleapBundle().get
          }).opt.get
          if (file.exists()) file.delete()
          if (folder.toFile.exists())folder.toFile.delete()
          val mleapPipeline = bundle.root
          val schema = savedSchema.substring(0,savedSchema.indexOf("]"))+"]}"
          val topK = Json.parse(savedSchema).asMap.drop(1).getOrElse("topKCol","").toString
          cacheActor ! putModel((modelId, version), (mleapPipeline, schema, topK))
          (mleapPipeline, schema, topK)
        }
      }
      val data = input.replace("row", "rows").replace("[", "[[").replace("]", "]]")
      val inputWithSchema = data.substring(0, data.length - 1) + ",\"schema\":{" + schema.substring(1) + "}"
      val inputBytes = inputWithSchema.getBytes("UTF-8")
      val inputLeapFrame = FrameReader("ml.combust.mleap.json").fromBytes(inputBytes).get
      val predictLeapFrame = model.transform(inputLeapFrame).get
      val predictionColumn = if (predictLeapFrame.schema.hasField("prediction_label")) "prediction_label" else "prediction"
      val resultJson = if(request.equals("predict")) {
        val resultDataset = predictLeapFrame.select(predictionColumn, "probability", "isHotLead").get.dataset
        val resultMap = resultDataset.map(record => Map(predictionColumn -> record(0).toString,
                                                        "probability" -> (if(record(1).asInstanceOf[DenseTensor[Double]].size > 2) record(1).asInstanceOf[DenseTensor[Double]].values.sorted(Ordering.Double.reverse)(0).toString else record(1).asInstanceOf[DenseTensor[Double]].values(record(0).asInstanceOf[Double].toInt)),
                                                        "isHotLead" -> (if(record.size > 2) record(2).asInstanceOf[Boolean].toString else "NA"))).head
        new Json.Value(resultMap).write
      } else if (request.equals("topk")) {
        val resultDataset = predictLeapFrame.select(topKCol).get.dataset
        val resultMap = Map("intents" -> resultDataset.map(record => record(0)).toArray.head.asInstanceOf[Array[(String,Double)]].map(x => Map("className"->x._1,"score"->x._2)))
        val res = new Json.Value(resultMap).write
        res
      } else { "" }
      sender() ! resultJson
    }
  }
}