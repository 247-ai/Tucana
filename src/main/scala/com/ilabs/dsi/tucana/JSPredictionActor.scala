package com.ilabs.dsi.tucana
import java.io.{File, FileOutputStream}
import java.nio.file.Files

import akka.actor.{Actor, ActorRef}
import com.ilabs.dsi.tucana.utils.DBManager
import javax.script.{Invocable, ScriptEngineManager}

import scala.collection.mutable

class JSPredictionActor extends Actor{

  val JSCache = new LRUCache()

  def receive = {
    case (modelId: String, version: String, input: String) => {

      val (model,schema,topKCol) = JSCache.get(modelId,version) match {
        case Some((oModel:Any,oSchema:String,oTopkcol:String)) => {
          oModel match {
            case x:Array[Byte] => (x,oSchema,oTopkcol)
          }
        }
        case None => {
          val (compressedModel,savedSchema) = DBManager.getModel(modelId, version)
          /*val folder = Files.createTempDirectory("TucanaTemp")
          val file = new File(s"$folder/temp.js")
          val fos = new FileOutputStream(file.getPath)
          fos.write(compressedModel)*/
          var temp1 = new mutable.HashMap[String,Any]()
          temp1 += ("x"->1)
          val engine = new ScriptEngineManager().getEngineByName("nashorn")
          val result = engine.eval("if(x < 1) return true; else return false;")
          (result,"","")
        }
      }
    }
  }

}
