resolvers ++= Seq(
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
)

// current ly disabled -- no longer used
//addSbtPlugin("com.github.sbt" % "sbt-jacoco" % "3.0.3")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.9.0")

//currently disabled -- no longer used
//addSbtPlugin("com.typesafe.sbt" % "sbt-site" % "1.3.2")

// supports release in maven central

addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "2.3")

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.1")
