package com.ilabs.dsi.tucana

import java.util.Calendar

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes.{BadRequest, Forbidden, InternalServerError, NotFound}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCode, StatusCodes}
import akka.http.scaladsl.server.Directives.{complete, get, handleExceptions, headerValueByName, path, pathPrefix, _}
import akka.http.scaladsl.server.{ExceptionHandler, MissingHeaderRejection, RejectionHandler, RequestContext, Route, StandardRoute, ValidationRejection}
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, PredefinedFromEntityUnmarshallers}
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import com.ilabs.dsi.tucana.utils.{DBManager, ErrorConstants, PredictServerConfig}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait PredictRoute extends SprayJsonSupport with DefaultJsonProtocol{

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer

  implicit def executor: ExecutionContextExecutor

  // Define an exception handler for the possible exceptions that arise.
  val serverExceptionHandler = ExceptionHandler {
    case ex: Exception =>
      complete(HttpResponse(StatusCodes.InternalServerError, entity = ex.getMessage))
  }

  implicit val ErrorInfoFormat: RootJsonFormat[ErrorInfo] = jsonFormat3(ErrorInfo)
  implicit val ErrorFormat: RootJsonFormat[Error] = jsonFormat1(Error)

  val serverRejectionHandler: RejectionHandler =
    RejectionHandler.newBuilder()
      .handle { case ValidationRejection(msg, _) => completeWithError(msg)}
      .handle { case MissingHeaderRejection(str) =>
        if(str == "tucana-devKey")
          completeWithError(ErrorConstants.MISSING_KEY)
        else
          completeWithError(s"Missing Header: $str")}
      .result()

  val sparkRouter : ActorRef
  val route : Route = (handleExceptions(serverExceptionHandler)
    & handleRejections(serverRejectionHandler)
    & pathPrefix("predictserver"/"v1")
    & decodeRequest
    & cors()
    & withSizeLimit(PredictServerConfig.get("request.bytes.size").toLong)) {
    headerValueByName("tucana-devKey") {
      devKey => {
        validate(checkIfValidDevkey(devKey),ErrorConstants.INVALID_DEVKEY) {
          validate(DBManager.checkIfUserRegistered(devKey), ErrorConstants.USER_NOT_FOUND) {
            pathPrefix("models" / Segment / "versions" / Segment) {
              (modelId, version) =>
                path("predict") {
                  {
                    get {
                      entity(as[String]) {
                        webInput => {
                            implicit val timeout = Timeout(180 seconds)
                            val resultJson = Await.result(sparkRouter ? (modelId, version, webInput, "predict"), timeout.duration).asInstanceOf[String]
                            validate (!resultJson.equals(""),ErrorConstants.MODEL_LOAD_ERROR) {
                              complete(HttpEntity(ContentTypes.`application/json`, resultJson))
                            }
                        }
                      }
                    }
                  }
                } ~
                  path("predict-topk") {
                    get {
                      entity(as[String]) {
                        webInput => {
                            implicit val timeout = Timeout(180 seconds)
                            val resultJson = Await.result(sparkRouter ? (modelId, version, webInput, "topk"), timeout.duration).asInstanceOf[String]
                          validate (!resultJson.equals(""),ErrorConstants.MODEL_LOAD_ERROR) {
                            complete(HttpEntity(ContentTypes.`application/json`, resultJson))
                          }
                        }
                      }
                    }
                  }
            }
          }
        }
      }
    }
  }

  def checkIfValidDevkey(devKey:String):Boolean = {
    devKey.matches("^[a-zA-Z0-9]*$")
  }

  def completeWithError(error: String): StandardRoute = {
    complete(errorDecoder(error)._2,Error(ErrorInfo(errorDecoder(error)._1, errorDecoder(error)._3, error)))
  }

  case class ErrorInfo(code: Int, `type`: String, message: String)

  case class Error(error: ErrorInfo)

  def errorDecoder(error: String): (Int, StatusCode, String) = {
    error match {
      case ErrorConstants.USER_NOT_FOUND => (1, NotFound, "SQLException")
      case ErrorConstants.MISSING_KEY => (2, Forbidden, "NotAuthorized")
      case ErrorConstants.INVALID_DEVKEY => (3, Forbidden, "Invalid")
      case ErrorConstants.SERVICE_DOWN => (500, InternalServerError, "InternalServerError")
      case _ => (10, InternalServerError, "InternalServerError")
    }
  }
}