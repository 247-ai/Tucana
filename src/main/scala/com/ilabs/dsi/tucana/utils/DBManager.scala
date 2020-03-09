package com.ilabs.dsi.tucana.utils

import java.sql.DriverManager

import org.slf4j.LoggerFactory
import java.sql.DriverManager
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date

import org.slf4j.LoggerFactory
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration.Duration


/**
  * Class to coordinate API calls with the backend.
  * Created by samik on 15/3/17.
  */
object DBManager
{
    // Set up logger
    val log = LoggerFactory.getLogger("DBManager")
    // Open the database with respecting foreign key constraints
    //private val db = Database.forConfig("ourdb")
    //val conn = DriverManager.getConnection(ConfigFactory.load().getString("ourdb.url"))

    def checkIfUserRegistered(devKey: String) =
    {
        val db = Database.forConfig("db", PredictServerConfig.config)
        val res = Await.result(db.run(sql"""select case when exists ( select userId from users where devKey = $devKey ) then 'TRUE' else 'FALSE' end""".as[Boolean]), Duration.Inf)(0)
        db.close()
        res
    }

    def getModel(modelId: String,version:String) = {

        Class.forName(PredictServerConfig.get("db.driver"))
        val conn = DriverManager.getConnection(PredictServerConfig.get("db.url"),PredictServerConfig.get("db.user"),PredictServerConfig.get("db.password"))
        val sql = "select model,dataSchema from tucanamodels where modelId=? and version=?"
        val stmt = conn.prepareStatement(sql)
        stmt.setString(1, modelId)
        stmt.setString(2,version)
        val result = stmt.executeQuery()
        var model = Array[Byte]()
        var schema = ""
        while(result.next()) {
            model = result.getBytes(1)
            schema = result.getString(2)
        }
        /*stmt.close()
        conn.close()*/
        (model,schema)
    }

    def getSchema(modelId:String,version:String) ={
      Class.forName(PredictServerConfig.get("db.driver"))
      val conn = DriverManager.getConnection(PredictServerConfig.get("db.url"),PredictServerConfig.get("db.user"),PredictServerConfig.get("db.password"))
      val sql = "select dataSchema from tucanamodels where modelId=? and version=?"
      val stmt = conn.prepareStatement(sql)
      stmt.setString(1, modelId)
      stmt.setString(2,version)
      val result = stmt.executeQuery()
      var schema = ""
      while(result.next()) {
        schema = result.getString(1)
      }
      /*stmt.close()
      conn.close()*/
      (schema)
    }

    def setModel(modelId: String, version: String, userId: String, description: String, model: Array[Byte], schema: String, modelType: String): Unit =
    {
        Class.forName(PredictServerConfig.get("db.driver"))
        val conn = DriverManager.getConnection(PredictServerConfig.get("db.url"), PredictServerConfig.get("db.user"), PredictServerConfig.get("db.password"))
        val sql = "insert into tucanamodels values(?,?,?,?,?,?,?,?)"
        val stmt = conn.prepareStatement(sql)
        stmt.setString(1, modelId)
        stmt.setString(2, version)
        stmt.setString(3, userId)
        stmt.setString(4, description)
        stmt.setString(5, s"${Date.from(Instant.now).toString}")
        stmt.setString(6,modelType)
        stmt.setBytes(7, model)
        stmt.setString(8, schema)
        //stmt.setString(8, modelType)
        stmt.executeUpdate()
        /*stmt.close()
        conn.close()*/
    }

    def getModelType(modelId:String,version:String) = {
        val db = Database.forConfig("mysqldb", PredictServerConfig.config)
        Await.result(db.run(sql"""select modelType from tucanamodels where modelId = $modelId and version = $version""".as[String]), Duration.Inf)(0)
    }

  def getModelDependancy(modelId:String,version:String) = {
    val db = Database.forConfig("mysqldb", PredictServerConfig.config)
    Await.result(db.run(sql"""select dependancy from tucanamodels where modelId = $modelId and version = $version""".as[String]), Duration.Inf)(0)
  }

    //def storeModel()

    /*def inactivateProject(devKey: String, projectId: String) =
    {
        //TODO
    }*/
}
