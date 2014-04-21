name := "Bingerator"

version := "0.2.1"

scalaVersion := "2.10.4"

exportJars := true

publishMavenStyle := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false

pomExtra := (
  <url>https://github.com/dbarowy/bingerator</url>
  <licenses>
    <license>
      <name>BSD-style</name>
      <url>http://opensource.org/licenses/bsd-license.php</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:dbarowy/bingerator.git</url>
    <connection>scm:git:git@github.com:dbarowy/bingerator.git</connection>
  </scm>
  <developers>
    <developer>
      <id>dbarowy</id>
      <name>Daniel Barowy</name>
      <url>http://people.cs.umass.edu/~dbarowy</url>
    </developer>
  </developers>)

libraryDependencies += "org.scalatest" % "scalatest_2.10" % "2.0" % "test"
