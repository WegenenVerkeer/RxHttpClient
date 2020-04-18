val Organization = "be.wegenenverkeer"

val Version = "2.0.0-SNAPSHOT"

val ScalaVersion = "2.13.0"

val ScalaBuildOptions = Seq("-unchecked",
                            "-deprecation",
                            "-feature",
                            "-language:reflectiveCalls",
                            "-language:implicitConversions",
                            "-language:postfixOps")


val asyncClient = "org.asynchttpclient" % "async-http-client" % "2.12.1"

val rxStreamsVersion = "1.0.3"
val rxJavaVersion = "3.0.1"
val reactorVersion = "3.3.3.RELEASE"

val slf4j = "org.slf4j" % "slf4j-api" % "1.7.30"
val commonsCodec = "commons-codec" % "commons-codec" % "1.10"
val json = "com.fasterxml.jackson.core" % "jackson-databind" % "2.10.3" % "provided"
val rx = "org.reactivestreams" % "reactive-streams" % rxStreamsVersion
val reactorAdapter = "io.projectreactor.addons" % "reactor-adapter" % reactorVersion

val rxJava = "io.reactivex.rxjava3" % "rxjava" % rxJavaVersion
val specs2 = "org.specs2" %% "specs2-core" % "4.9.3" % "test"
val slf4jSimple = "org.slf4j" % "slf4j-simple" % "1.7.30" % "test"
val wiremock = "com.github.tomakehurst" % "wiremock-jre8" % "2.26.3" % "test"
val junitInterface = "com.novocode" % "junit-interface" % "0.11" % Test
val jsonPath = "com.jayway.jsonpath" % "json-path" % "2.4.0" % "test"



val commonDependencies = Seq(
  asyncClient,
  slf4j,
  commonsCodec,
  json
)

val rxJavaDependencies = Seq(
  rxJava
)

lazy val interopDependencies = Seq(
  rx,
  reactorAdapter
)


val javaDependencies = commonDependencies ++ Seq(slf4jSimple)

val scalaDependencies = commonDependencies ++ Seq(
  specs2
)

val mainTestDependencies = Seq(
  slf4jSimple,
  wiremock,
  junitInterface,
  jsonPath
)

lazy val disablePublishingRoot = Seq(
  Keys.publishLocal := {},
  Keys.publish := {},
  publish / skip := true
)


lazy val moduleSettings =
  Seq(
    organization := "be.wegenenverkeer",
    version := Version,
    scalaVersion := ScalaVersion,
    scalacOptions := ScalaBuildOptions,
    parallelExecution := false,
    resolvers += "Local Maven" at Path.userHome.asFile.toURI.toURL + ".m2/repository",
    resolvers += Resolver.typesafeRepo("releases"),
    resolvers += "Spring repository" at "https://repo.spring.io/milestone"
  ) ++ testSettings ++ publishSettings //++ jacoco.settings

lazy val extraJavaSettings = Seq(
  crossPaths := false,
  autoScalaLibrary := false,
  libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % Test,
  //Test / parallelExecution := false,
  //    javacOptions ++= Seq("-Xlint:deprecation"),
  testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "-v")
)

lazy val testSettings = Seq(
  libraryDependencies ++= mainTestDependencies,
  parallelExecution in Test := false
)
lazy val javaInteropModule = (project in file("modules/java-interop")).settings(
  name := "RxHttpClient-java-interop",
  moduleSettings ++ extraJavaSettings,
  javacOptions ++= Seq("--release", "11"),
  libraryDependencies ++= javaDependencies ++ interopDependencies,
  extraJavaSettings
) dependsOn (rxJavaModule % "compile->compile;test->test")

lazy val rxJavaModule = (project in file("modules/java")).settings(
  name := "RxHttpClient-RxJava",
  moduleSettings,
  javacOptions ++= Seq("--release", "11"),
  libraryDependencies ++= javaDependencies ++ rxJavaDependencies,
  extraJavaSettings
)


lazy val main = (project in file("."))
  .settings(
    moduleSettings ++ disablePublishingRoot ++ extraJavaSettings,
    name := "RxHttpClient"
  )
  .aggregate(javaInteropModule, rxJavaModule)

lazy val pomInfo = <url>https://github.com/WegenenVerkeer/atomium</url>
  <licenses>
    <license>
      <name>MIT licencse</name>
      <url>http://opensource.org/licenses/MIT</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:WegenenVerkeer/atomium.git</url>
    <connection>scm:git:git@github.com:WegenenVerkeer/atomium.git</connection>
  </scm>
  <developers>
    <developer>
      <id>AWV</id>
      <name>De ontwikkelaars van AWV</name>
      <url>http://www.wegenenverkeer.be</url>
    </developer>
  </developers>

val publishingCredentials = (for {
  username <- Option(System.getenv().get("SONATYPE_USERNAME"))
  password <- Option(System.getenv().get("SONATYPE_PASSWORD"))
} yield
  Seq(
    Credentials("Sonatype Nexus Repository Manager",
      "oss.sonatype.org",
      username,
      password))).getOrElse(Seq())

val publishSettings = Seq(
  publishMavenStyle := true,
  pomIncludeRepository := { _ =>
    false
  },
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
  pomExtra := pomInfo,
  credentials ++= publishingCredentials
)
