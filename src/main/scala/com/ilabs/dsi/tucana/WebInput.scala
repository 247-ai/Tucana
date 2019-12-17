package com.ilabs.dsi.tucana

import com.ilabs.dsi.tucana.utils.Json

import scala.collection.immutable
import scala.util.parsing.json.JSONArray

/**
  * Helper class for processing POST data.
  * Created by samik on 26/3/17.
  */
class WebInput(input: Map[String, Json.Value])
{
    /**
      * Constructor accepting a JSON string
      * @param body
      * @return
      */
    def this(body: String) = this(Json.parse(body).asMap)

    /**
      * Check for the required parameters.
      *
      * @param params
      * @return A sequence of parameters that are required but missing.
      */
    def required(params: List[String]): immutable.Seq[String] =
        for{ param <- params if (!input.contains(param)) } yield param

    def get():Map[String,Json.Value] = {
        return input
    }

    def get(key:String):String={
        val temp = input.getOrElse(key,"")
        return input.getOrElse(key,"").toString
    }

    def getList(key:String)={
    }

    /**
      * Extract string value for a key.
      *
      * @param key
      * @return
      */
    def stringVal(key: String) =
    {
        input.getOrElse(key, "") match
        {
            case value: Json.Value => if (value.isNumeric) value.asLong.toString else value.asString
            case value: Any => value.toString
        }
    }

    def jsonVal(key: String) =
    {
        input.getOrElse(key, "") match
        {
            case v: Json.Value => v
            case _ => throw new IllegalArgumentException
        }
    }

    def byteArray(key: String) =
    {
        input.getOrElse(key, "") match
        {
            case v: Json.Value =>
            case _ => throw new IllegalArgumentException
        }
    }

}
