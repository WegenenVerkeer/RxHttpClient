resolvers ++= Seq(
  "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"
)

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.9.2")
addSbtPlugin("com.geirsson" % "sbt-scalafmt" % "1.5.1")

// supports release in maven central
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.4")
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.1")
