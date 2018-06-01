import Dependencies._
import BuildSettings._


val Name = "RxHttpClient"

val javaModuleName = Name + "-java"

lazy val disablePublishingRoot = Seq(
  Keys.publishLocal := {},
  Keys.publish := {},
  publish/skip := true
)

lazy val javaModule = (project in file("modules/java")).settings(
  name := javaModuleName,
  buildSettings(javaModuleName, javaDependencies),
  extraJavaSettings
)

val scalaModuleName = Name + "-scala"

lazy val scalaModule =(project in file("modules/scala")). settings(
  name := scalaModuleName,
  buildSettings(scalaModuleName),
  crossScalaVersions := Seq("2.11.8", "2.12.1"),
  libraryDependencies ++= scalaDependencies
) dependsOn javaModule


lazy val main = (project in file(".")).settings(
  buildSettings(Name) ++ disablePublishingRoot,
  name := Name
).aggregate(javaModule, scalaModule)

