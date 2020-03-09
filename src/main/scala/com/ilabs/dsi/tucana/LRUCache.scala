package com.ilabs.dsi.tucana

import java.util.LinkedList

import com.ilabs.dsi.tucana.utils.PredictServerConfig
import ml.combust.mleap.runtime.frame.Transformer

import scala.collection.mutable

class LRUCache {

  val dq = new LinkedList[(String,String)]()
  val size = PredictServerConfig.get("model.cache.size").toInt
  val modelCache = new mutable.HashMap[(String, String), (Any, String, String)]()

  def rearrangeList(key: (String, String)): Unit = {
    dq.remove(key)
    dq.addFirst(key)
  }

  def get(key: (String, String)): Option[(Any, String, String)] = {
    val modelOutput = modelCache.get(key)
    modelOutput.foreach(value => rearrangeList(key))
    modelOutput
  }

  def put(key: (String, String), value: (Any, String, String)): Unit = {
    if (modelCache.size >= size) {
      val leastAccessedNode = dq.removeLast()
      modelCache.remove(leastAccessedNode)
    }
    dq.addFirst(key)
    modelCache.put(key, value)
  }

}
