package com.ilabs.dsi.tucana.utils

import java.io.File
import com.typesafe.config.{Config, ConfigFactory}

/**
  * @since 4/6/18
  */
object PredictServerConfig {

  // Making var for tests override
  //This is def to support hotloading of the config file
  def config: Config = ConfigFactory.parseFile(new File("predict-server-config.conf"))

  def get(key: String): String = config.getAnyRef(key.toString).toString

}
