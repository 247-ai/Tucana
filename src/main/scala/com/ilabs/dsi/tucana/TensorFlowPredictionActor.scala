/*
package com.ilabs.dsi.tucana

import java.io.{File, FileOutputStream}
import java.util

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.http.scaladsl.model.headers.RawHeader
import akka.pattern.ask
import com.google.gson.{Gson, JsonObject, JsonParser, JsonPrimitive}

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import com.ilabs.dsi.tucana.utils.{Json, PredictServerConfig}
import ml.combust.mleap.runtime.frame.Transformer
import ml.combust.mleap.tensor.DenseTensor

import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}
//import org.apache.spark.ml._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpResponse, HttpRequest, StatusCodes}
import akka.http.scaladsl.server.Directives.{complete, get, handleExceptions, headerValueByName, path, pathPrefix, _}
import akka.http.scaladsl.server.ExceptionHandler
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, PredefinedFromEntityUnmarshallers,Unmarshal}
import akka.stream.ActorMaterializer
import akka.util.Timeout
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import com.ilabs.dsi.tucana.utils.{DBManager, Json}
import ml.combust.bundle.BundleFile
import ml.combust.mleap.runtime.MleapSupport._
import ml.combust.mleap.runtime.serialization.FrameReader
import org.slf4j.LoggerFactory
import resource.managed

import scala.collection.mutable
import scala.concurrent.duration._

class TensorFlowPredictionActor(cacheActor: ActorRef) extends Actor{

  implicit val materializer = ActorMaterializer()
  implicit val executionContext = context.dispatcher
  implicit val tensorflowResult: FromEntityUnmarshaller[String] = PredefinedFromEntityUnmarshallers.stringUnmarshaller

  def receive = {
    case (modelId: String, version: String, input: String, compressedModel: Array[Byte], savedSchema: String) => {
      val inputText = getInputText(input)
      val responseFuture = tensorflowResponse(modelId, inputText)
      val tfOutput = Await.result(processTensorflowServingOutput(responseFuture), Duration.Inf)
      sender() ! tfOutput
    }
  }
  private def processTensorflowServingOutput(responseFuture: Future[HttpResponse]) =
  {
    val finalResponse = responseFuture.flatMap
    {
      response => Unmarshal(response.entity).to[String]
    }.map
    {
      response =>
      {
        val outputs = Json.parse(response).asMap("outputs")
        outputs.asMap("scores").asArray(0).asArray.map(_.asDouble)
          .zip(outputs.asMap("classes").asArray(0).asArray.map(_.asString))
          .sortBy(_._2)
          .map(v => s"${v._2}, ${v._1}")
          .head
      }
    }

    finalResponse.onComplete
    {
      case Success(res) => res
      case Failure(x) => s"ERROR $x"
    }

    finalResponse
  }

  private def getInputText(input: String) =
  {
    //expecting json of form - '{"row":["awesome experience"]}'
    //new JsonParser().parse(input).getAsJsonObject.get("row").getAsJsonArray.get(0).getAsString
    Json.parse(input).asMap("row").asArray(0).asString
  }

  def tensorflowResponse(modelName: String, inputText: String) =
  {
    val hostname = PredictServerConfig.get("tensorflow.host")
    val port = PredictServerConfig.get("tensorflow.port")
    val data = "{\"inputs\" :  {\"inputs\":\"" + inputText + "\"}}"
    println(s"Sending: $data")
    val responseFuture: Future[HttpResponse] =
      Http(context.system).singleRequest(
        HttpRequest(
          HttpMethods.POST,
          s"http://$hostname:$port/v1/models/${modelName}:predict",
          entity = HttpEntity(ContentTypes.`application/json`, data.getBytes())
        ).withHeaders(RawHeader("X-Access-Token", "access token"))
      )
    responseFuture
  }

}
*/
