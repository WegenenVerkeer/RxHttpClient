import sbt.Keys._
import sbt._

object AwvRestClientBuild extends Build 
			  	with BuildSettings {

	import Dependencies._

	val Name = "awv-rest-client"

	val javaModuleName = Name + "-java"

	lazy val javaModule = Project(
		javaModuleName,
		file("modules/java"),
		settings = buildSettings(javaModuleName, javaDependencies)
	)

	lazy val main = Project(
		Name,
		file("."),
		settings = buildSettings(Name)
	) .aggregate(javaModule)

}

		
