val Organization = "be.wegenenverkeer"

val Version = "1.1.1-SNAPSHOT"

val ScalaVersion = "2.12.8"

val ScalaBuildOptions = Seq("-unchecked",
                            "-deprecation",
                            "-feature",
                            "-language:reflectiveCalls",
                            "-language:implicitConversions",
                            "-language:postfixOps")

lazy val testSettings = Seq(
  libraryDependencies ++= mainTestDependencies,
  parallelExecution in Test := false
)

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

//  lazy val siteSettings =
//    site.settings ++
//      site.includeScaladoc()

lazy val extraJavaSettings = Seq(
  crossPaths := false,
  autoScalaLibrary := false,
  //    javacOptions ++= Seq("-Xlint:deprecation"),
  testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "-v")
)

def moduleSettings(extraDependencies: Seq[ModuleID] = Seq()) = {
  Seq(
    organization := "be.wegenenverkeer",
    version := Version,
    scalaVersion := ScalaVersion,
    scalacOptions := ScalaBuildOptions,
    parallelExecution := false,
    resolvers += "Local Maven" at Path.userHome.asFile.toURI.toURL + ".m2/repository",
    resolvers += Resolver.typesafeRepo("releases"),
    libraryDependencies ++= extraDependencies
  ) ++ testSettings ++ publishSettings //++ jacoco.settings
}

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

val asyncClient = "org.asynchttpclient" % "async-http-client" % "2.8.1"
val rxjava = "io.reactivex" % "rxjava" % "1.2.4"
val rxscala = "io.reactivex" %% "rxscala" % "0.26.5"
val slf4j = "org.slf4j" % "slf4j-api" % "1.7.25"
val commonsCodec = "commons-codec" % "commons-codec" % "1.10"
val json = "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.8" % "provided"

val junit = "junit" % "junit" % "4.11" % "test"
val specs2 = "org.specs2" %% "specs2-core" % "3.8.6" % "test"
val slf4jSimple = "org.slf4j" % "slf4j-simple" % "1.7.6" % "test"
val wiremock = "com.github.tomakehurst" % "wiremock-jre8" % "2.23.2" % "test"
val junitInterface = "com.novocode" % "junit-interface" % "0.11" % Test
val jsonPath = "com.jayway.jsonpath" % "json-path" % "1.2.0" % "test"

val commonDependencies = Seq(
  asyncClient,
  rxjava,
  slf4j,
  commonsCodec,
  json
)

val javaDependencies = commonDependencies ++ Seq(slf4jSimple, junitInterface)

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

lazy val disablePublishingRoot = Seq(
  Keys.publishLocal := {},
  Keys.publish := {},
  publish / skip := true
)

lazy val javaModule = (project in file("modules/java")).settings(
  name := "RxHttpClient-java",
  moduleSettings(javaDependencies),
  extraJavaSettings
)

lazy val scalaModule = (project in file("modules/scala")).settings(
  name := "RxHttpClient-scala",
  moduleSettings(),
  //crossScalaVersions := Seq("2.12.8"),
  libraryDependencies ++= scalaDependencies
) dependsOn javaModule

lazy val main = (project in file("."))
  .settings(
    moduleSettings() ++ disablePublishingRoot,
    name := "RxHttpClient"
  )
  .aggregate(javaModule, scalaModule)
