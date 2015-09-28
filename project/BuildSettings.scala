import com.typesafe.sbt.SbtSite.site
import sbt._
import sbt.Configuration
import sbt.Keys._
import de.johoop.jacoco4sbt._
import JacocoPlugin._
import scala.util.Properties

trait BuildSettings {

  import Dependencies._
  val Organization = "be.wegenenverkeer"
  
  val Version = "0.2.1-SNAPSHOT"
  val ScalaVersion = "2.11.7"
  val ScalaBuildOptions = Seq("-unchecked", "-deprecation", "-feature",
    "-language:reflectiveCalls",
    "-language:implicitConversions",
    "-language:postfixOps")


  lazy val testSettings = Seq(
    libraryDependencies ++= mainTestDependencies,
    parallelExecution in Test := false
  )

  
  def projectSettings(projectName:String, extraDependencies:Seq[ModuleID]) = Seq(
    organization := Organization,
    name := projectName,
    version := Version,
    scalaVersion := ScalaVersion,
    crossScalaVersions := Seq("2.10.3", "2.11.7"),
    scalacOptions := ScalaBuildOptions,
    parallelExecution := false,
    resolvers +=  "Local Maven" at Path.userHome.asFile.toURI.toURL + ".m2/repository",
    resolvers += Resolver.typesafeRepo("releases"),
    libraryDependencies ++= extraDependencies

  )

  val publishingCredentials = (for {
    username <- Option(System.getenv().get("SONATYPE_USERNAME"))
    password <- Option(System.getenv().get("SONATYPE_PASSWORD"))
  } yield
    Seq(Credentials(
      "Sonatype Nexus Repository Manager",
      "oss.sonatype.org",
      username,
      password)
    )).getOrElse(Seq())


  val publishSettings = Seq(
    publishMavenStyle := true,
    pomIncludeRepository := { _ => false},
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

  lazy val siteSettings =
    site.settings ++
      site.includeScaladoc()

  lazy val extraJavaSettings = Seq(
    crossPaths := false
  )


  def buildSettings(projectName:String, extraDependencies:Seq[ModuleID] = Seq()) = {
    Defaults.defaultSettings ++
      projectSettings(projectName, extraDependencies) ++
      testSettings ++
      publishSettings ++
      jacoco.settings
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


}
