
import sbt._

object Dependencies {

  val asyncClient = "com.ning" % "async-http-client" % "1.9.40"
  val rxjava = "io.reactivex" % "rxjava" % "1.0.16"
  val rxscala = "io.reactivex" %% "rxscala" % "0.25.0"
  val slf4j = "org.slf4j" % "slf4j-api" % "1.7.6"
  val commonsCodec =  "commons-codec" % "commons-codec" % "1.10"
  val json  = "com.fasterxml.jackson.core" % "jackson-databind" % "2.4.2" % "provided"


  val junit = "junit" % "junit" % "4.11" % "test"
  val specs2 = "org.specs2" %% "specs2-core" % "2.4.14" % "test"
  val slf4jSimple = "org.slf4j" % "slf4j-simple" % "1.7.6" % "test"
  val wiremock = "com.github.tomakehurst" % "wiremock" % "1.52" % "test"
  val junitInterface = "com.novocode" % "junit-interface" % "0.10" % "test"
  val jsonPath = "com.jayway.jsonpath" % "json-path" % "1.2.0" % "test"

  val commonDependencies = Seq(
    asyncClient,
    rxjava,
    slf4j,
    commonsCodec,
    json
  )


  val javaDependencies = commonDependencies ++ Seq()

  val scalaDependencies = commonDependencies ++ Seq(
    rxscala,
    specs2
  )

  val mainTestDependencies = Seq(
    junit,
    slf4jSimple,
    wiremock,
    junitInterface,
    jsonPath
  )

}
