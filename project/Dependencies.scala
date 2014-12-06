
import sbt._

object Dependencies {

  val asyncClient = "com.ning" % "async-http-client" % "1.9.0"
  val rxjava = "io.reactivex" % "rxjava" % "1.0.2"
  val slf4j = "org.slf4j" % "slf4j-api" % "1.7.6"
  val jsonPath = "com.jayway.jsonpath" % "json-path" % "1.2.0"


  val junit = "junit" % "junit" % "4.11" % "test"
  val specs2 = "org.specs2" %% "specs2-core" % "2.4.14" % "test"
  val slf4jSimple = "org.slf4j" % "slf4j-simple" % "1.7.6" % "test"
  val wiremock = "com.github.tomakehurst" % "wiremock" % "1.52" % "test"
  val junitInterface = "com.novocode" % "junit-interface" % "0.10" % "test"

  val javaDependencies = Seq(
    asyncClient,
    rxjava,
    slf4j,
    jsonPath
  )

  val mainTestDependencies = Seq(
    junit,
    specs2,
    slf4jSimple,
    wiremock,
    junitInterface
  )


}
