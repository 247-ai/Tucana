package com.ilabs.dsi.tucana

import java.util._

import akka.actor.Actor
import com.ilabs.dsi.tucana.utils.PredictServerConfig
import ml.combust.mleap.runtime.frame.Transformer

import scala.collection.mutable

//Note : Consider Least Frequently used and also see singly linked list

class LRUCacheActor extends Actor {

  import LRUCacheActor._

  val dq = new LinkedList[(String,String)]()
  val size = PredictServerConfig.get("model.cache.size").toInt
  val modelCache = new mutable.HashMap[(String, String), (Transformer, String, String)]()

  def rearrangeList(key: (String, String)): Unit = {
    dq.remove(key)
    dq.addFirst(key)
  }

  def get(key: (String, String)): Option[(Transformer, String, String)] = {
    val modelOutput = modelCache.get(key)
    modelOutput.foreach(value => rearrangeList(key))
    modelOutput
  }

  def put(key: (String, String), value: (Transformer, String, String)): Unit = {
    if (modelCache.size >= size) {
      val leastAccessedNode = dq.removeLast()
      modelCache.remove(leastAccessedNode)
    }
    dq.addFirst(key)
    modelCache.put(key, value)
  }

  def receive = {
    case
      getModel(key) => sender() ! get(key)
    case putModel(key,value) => put(key,value)
  }

}

object LRUCacheActor {

  case class getModel(key:(String,String))

  case class putModel(key:(String,String),value:(Transformer,String, String))
}