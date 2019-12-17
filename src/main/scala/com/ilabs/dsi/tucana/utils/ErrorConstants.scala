package com.ilabs.dsi.tucana.utils

object ErrorConstants {

  val USER_NOT_FOUND = "User not found for the given devkey. Devkey may not be registered one."
  val MISSING_KEY = "Unauthorized due to missing tucana-devKey"
  val SERVICE_DOWN = "Service is down"
  val INVALID_DEVKEY = "DevKey is not valid. It should contain only alphanumeric values."
  val MODEL_LOAD_ERROR = "Model which got loaded is not proper. Try using some other model or try uploading it again."
}
