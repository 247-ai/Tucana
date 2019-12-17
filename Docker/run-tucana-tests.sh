#!/bin/bash -e

predictServerMainJar=$1
predictServerTestJar=$2
scalaTestJars="scalatest_2.11-3.0.5.jar:scalactic_2.11-3.0.5.jar"


predictTestClass=com.ilabs.dsi.tucana.functionalTests.PredictApiTests

scala -J-Xmx2g -cp "$scalaTestJars:$predictServerMainJar" org.scalatest.tools.Runner -o -R $predictServerTestJar -s $predictTestClass