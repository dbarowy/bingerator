name := "Bingerator"

version := "0.2.3"

scalaVersion := "2.11.7"

exportJars := true

// SUPPORTED SCALA VERSIONS
crossScalaVersions := Seq("2.10.6", "2.11.7")

// DEPENDENCIES
libraryDependencies += "org.scalatest" %%  "scalatest" % "2.2.4" % "test"

// add scala-xml if scala major version >= 11
libraryDependencies := {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, scalaMajor)) if scalaMajor >= 11 =>
      libraryDependencies.value :+ "org.scala-lang.modules" %% "scala-xml" % "1.0.2"
    case _ =>
      libraryDependencies.value
  }
}

// TESTING
concurrentRestrictions in Global := Seq(
  Tags.limit(Tags.Test, 1)
)

parallelExecution in Test := false

// MAVEN
organization := "net.ettinsmoor"

licenses := Seq("BSD-style" -> url("http://opensource.org/licenses/bsd-license"))

homepage := Some(url("https://github.com/dbarowy/bingerator"))

publishMavenStyle := true

pomIncludeRepository := { _ => false }

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false

pomExtra := (
  <scm>
    <url>scm:git:git@github.com:dbarowy/bingerator.git</url>
    <connection>scm:git:git@github.com:dbarowy/bingerator.git</connection>
  </scm>
  <developers>
    <developer>
      <id>dbarowy</id>
      <name>Daniel Barowy</name>
      <url>http://people.cs.umass.edu/~dbarowy</url>
    </developer>
  </developers>)
