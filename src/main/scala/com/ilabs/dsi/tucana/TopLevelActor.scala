package com.ilabs.dsi.tucana

import akka.actor.Actor
import com.ilabs.dsi.tucana.utils.DBManager

import scala.collection.mutable

class TopLevelActor extends Actor {

  var dataMap = new mutable.HashMap[String,Any]()
  def receive = {
    case(modelId:String,version:String,input:String) => {
      val dependancy = DBManager.getModelDependancy(modelId,version)
      if(dependancy.nonEmpty){
        val depArray = dependancy.split(";")
        depArray.par.foreach{x =>
        val (model,ver) = (x.split(",")(0),x.split(",")(1))
        val modelType = DBManager.getModelType(model,ver)
        }
      }
    }
    case getInput => {

    }
  }

}

object TopLevelActor {
  case object getInput
}

