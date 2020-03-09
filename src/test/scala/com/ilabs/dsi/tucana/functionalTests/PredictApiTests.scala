package com.ilabs.dsi.tucana.functionalTests

import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date

import akka.actor.ActorSystem
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpEntity, HttpMethods, HttpRequest, MediaTypes}
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.testkit.{TestActorRef, TestDuration}
import akka.util.ByteString
import com.ilabs.dsi.tucana.utils.{DBManager, PredictServerConfig}
import com.ilabs.dsi.tucana.{LRUCacheActor, PredictRoute, SparkPredictionActor}
import com.typesafe.config.ConfigFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.reflect.io.File

class PredictApiTests extends WordSpec with ScalatestRouteTest with Matchers with ScalaFutures with PredictRoute {

  implicit def default(implicit system: ActorSystem): RouteTestTimeout = RouteTestTimeout(100.seconds dilated)

  //This is to initialize the database with the preloaded models, so that all our predict requests will use some model by default.
  def initializeDB(): Unit ={
    val DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS"
    val dateStorageformat = new SimpleDateFormat(DATETIME_FORMAT)
    val db = Database.forConfig("db", PredictServerConfig.config)
    Await.result(db.run(sqlu"create table if not exists users ( userId varchar(200) not null primary key,devKey varchar(200) not null unique, registerDate DATETIME not null, isAdmin boolean);"),Duration.Inf)
    Await.result(db.run(sqlu"create table if not exists tucanamodels ( modelId varchar(200) not null, version varchar(100) not null, userId varchar(200) not null references users (userId), description varchar(250), lastUpdateTimestamp text, modelType varchar(250), model longblob, dataSchema varchar(20000), primary key (modelId, version));"),Duration.Inf)
    Await.result(db.run(sqlu"delete from users where userId='tucanaUser'"), Duration.Inf)
    Await.result(db.run(sqlu"insert into users values('tucanaUser','a998274b98', ${dateStorageformat.format(Date.from(Instant.now))},true)"), Duration.Inf)
    Await.result(db.run(sqlu"delete from tucanamodels where modelId='tucana1' and version = 'v1'"), Duration.Inf)
    val modelFile = File("flashml-noPage.zip")
    val modelBytes = modelFile.bytes().toArray
    DBManager.setModel("tucana1","v1","tucanaUser","Tucana model testing",modelBytes,"""{"fields":[{"type":"string","name":"lineText"}],"topKCol":"top_intents"}""".stripMargin,"Spark")
  }
  initializeDB()
  val cacheRef = TestActorRef[LRUCacheActor](new LRUCacheActor())
  override val sparkRouter = TestActorRef[SparkPredictionActor](new SparkPredictionActor(cacheRef))
  val webInput = ByteString("""{"row":["_class_hello i want to cancel my _class_personal broadband connection"]}""".stripMargin)

  //Test case for intent prediction request
  "Path /predictserver/v1/models/tucana1/versions/v1/predict " should {

    val predictRequest = HttpRequest(
      HttpMethods.GET,
      uri = "/predictserver/v1/models/tucana1/versions/v1/predict",
      entity = HttpEntity(MediaTypes.`application/json`, webInput))

    "return the response with following probability " in {
      // tests:
      predictRequest ~> RawHeader("tucana-devKey", "a998274b98") ~> route ~> check {
        responseAs[String] shouldEqual """{"prediction_label":"my_account","probability":"0.14276698715633568"}""".stripMargin
      }
    }

    "return user not found for the given key" in {
      // tests:
      predictRequest ~> RawHeader("tucana-devKey", "a998274b99") ~> route ~> check {
        responseAs[String] shouldEqual "{\"error\":{\"code\":1,\"message\":\"User not found for the given devkey. Devkey may not be registered one.\",\"type\":\"SQLException\"}}".stripMargin
      }
    }

    "return tucana devkey is not valid" in {
      predictRequest ~> RawHeader("tucana-devKey", "a998274b-9") ~> route ~> check {
        responseAs[String] shouldEqual "{\"error\":{\"code\":3,\"message\":\"DevKey is not valid. It should contain only alphanumeric values.\",\"type\":\"Invalid\"}}".stripMargin
      }
    }
  }

  //Test case for topk intent prediction request
  "Path /predictserver/v1/models/tucana1/versions/v1/predict-topk" should {

    val predictTopKRequest = HttpRequest(
      HttpMethods.GET,
      uri = "/predictserver/v1/models/tucana1/versions/v1/predict-topk",
      entity = HttpEntity(MediaTypes.`application/json`, webInput))

    "return the response with following top n intents and probability" in {
      predictTopKRequest ~> RawHeader("tucana-devKey","a998274b98") ~> route ~> check {
        responseAs[String] shouldEqual "{\"intents\":[{\"className\":\"my_account\",\"score\":0.14276698715633568},{\"className\":\"billing_or_credit_enquiry_or_action\",\"score\":0.13222848400914222},{\"className\":\"contract_enquiry\",\"score\":0.11781919343768565},{\"className\":\"sales_support_order_or_delivery_enquiry_or_action\",\"score\":0.07941565227027043},{\"className\":\"pcs_order_or_delivery_enquiry_or_action\",\"score\":0.07591690097058783},{\"className\":\"data_query\",\"score\":0.0749987330578579},{\"className\":\"sales_mobile\",\"score\":0.06307488460836588},{\"className\":\"payment_or_action\",\"score\":0.057214574259720964},{\"className\":\"network_issue_or_faults\",\"score\":0.051763510342926276},{\"className\":\"mobile_insurance_or_warranty\",\"score\":0.042019859032301515}]}".stripMargin
      }
    }

    "return tucana devkey is not found in the header" in {
      predictTopKRequest ~> route ~> check {
        responseAs[String] shouldEqual "{\"error\":{\"code\":2,\"message\":\"Unauthorized due to missing tucana-devKey\",\"type\":\"NotAuthorized\"}}".stripMargin
      }
    }
  }
}
