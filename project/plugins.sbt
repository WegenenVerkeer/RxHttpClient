resolvers ++= Seq(
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
)

addSbtPlugin("de.johoop" % "jacoco4sbt" % "2.1.4")

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.5.2")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.4")

