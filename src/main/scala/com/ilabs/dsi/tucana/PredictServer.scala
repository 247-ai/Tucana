package com.ilabs.dsi.tucana

import akka.actor.{ActorSystem, OneForOneStrategy, Props, SupervisorStrategy}
import akka.http.scaladsl.Http
import akka.routing.RoundRobinPool
import akka.stream.ActorMaterializer
import com.ilabs.dsi.tucana.utils.PredictServerConfig
import org.slf4j.LoggerFactory

object PredictServer extends App with PredictRoute
{

  val log = LoggerFactory.getLogger("PredictServer")

  override implicit val system = ActorSystem("PredictServer", PredictServerConfig.config)
  override implicit val materializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  implicit val executor = system.dispatcher
  // LRU Cache Actor
  val cacheActor = system.actorOf(Props[LRUCacheActor],name="LRUCacheActor")

  //Spark Actor props block
  val sparkActorStrategy = OneForOneStrategy(){
    case _ => SupervisorStrategy.restart
  }
  val sparkProps = Props(new SparkPredictionActor(cacheActor))
  val sparkRouterProps = RoundRobinPool(
    PredictServerConfig.get("sparkActor.pool.concurrency").toInt,
    supervisorStrategy = sparkActorStrategy).props(routeeProps = sparkProps)
  val sparkRouter = system.actorOf(sparkRouterProps, name = "SparkPredictorActor")

  val bindingFuture1 = Http().bindAndHandle(route, PredictServerConfig.get("http.interface"), PredictServerConfig.get("http.port").toInt)
  println(s"Predict Server online at http://${PredictServerConfig.get("http.interface")}:${PredictServerConfig.get("http.port")}/")

}