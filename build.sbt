ThisBuild / resolvers ++= Seq(
  "Apache Development Snapshot Repository" at "https://repository.apache.org/content/repositories/snapshots/",
  Resolver.mavenLocal,
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
)

name := "codefeedr-1up"
version := "0.1-SNAPSHOT"
organization := "org.tudelft"

ThisBuild / scalaVersion := "2.12.8"

val flinkVersion = "1.9.1"
val codefeedrVersion = "0.1.4"

val flinkDependencies = Seq(
  "org.apache.flink" %% "flink-scala" % flinkVersion,
  "org.apache.flink" %% "flink-streaming-scala" % flinkVersion)

val codefeedrDependencies = Seq(
  "org.codefeedr" %% "codefeedr-core" %  codefeedrVersion
)

val sqlDependencies = Seq(
  "org.apache.flink" %% "flink-table-api-scala-bridge" % flinkVersion,
  "org.apache.flink" %% "flink-table-api-java-bridge" % flinkVersion,
  "org.apache.flink" %% "flink-table-planner" % flinkVersion,
  "org.scala-lang" % "scala-reflect" % "2.12.8"
)

val otherDependencies = Seq(
  "org.jsoup" % "jsoup" % "1.6.1"
)

lazy val dependencies =
  new {
    val scalatest          = "org.scalatest"             %% "scalatest"                      % "3.0.5"           % Test
    val spray              = "io.spray"                  %% "spray-json"                     % "1.3.4"
  }

lazy val root = (project in file(".")).
  settings(
    libraryDependencies ++= flinkDependencies,
    libraryDependencies ++= otherDependencies,
    libraryDependencies ++= codefeedrDependencies,
    libraryDependencies ++= sqlDependencies ++ Seq(
      // JSON
      dependencies.spray,

      // testing
      dependencies.scalatest
    )
  )

assembly / mainClass := Some("org.tudelft.Main")

// make run command include the provided dependencies
Compile / run  := Defaults.runTask(Compile / fullClasspath,
  Compile / run / mainClass,
  Compile / run / runner
).evaluated

// stays inside the sbt console when we press "ctrl-c" while a Flink programme executes with "run" or "runMain"
Compile / run / fork := true
Global / cancelable := true

// exclude Scala library from assembly
assembly / assemblyOption  := (assembly / assemblyOption).value.copy(includeScala = false)
