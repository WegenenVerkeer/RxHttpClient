import sbt._
import com.typesafe.sbt.SbtSite._

object RxHttpClientBuild extends Build
			  	with BuildSettings {

	import Dependencies._

	val Name = "RxHttpClient"

	val javaModuleName = Name + "-java"

	lazy val javaModule = Project(
		javaModuleName,
		file("modules/java"),
		settings = buildSettings(javaModuleName, javaDependencies) ++ siteSettings ++ extraJavaSettings
	)

	val scalaModuleName = Name + "-scala"

	lazy val scalaModule = Project(
		scalaModuleName,
		file("modules/scala"),
		settings = buildSettings(scalaModuleName, scalaDependencies) ++ siteSettings
	) dependsOn javaModule


	lazy val main = Project(
		Name,
		file("."),
		settings = buildSettings(Name)
	) .aggregate(javaModule, scalaModule)

}

		
