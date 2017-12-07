organization := "com.github.biopet"
name := "sbt-biopet"

sbtPlugin := true

scalaVersion := "2.10.6"

resolvers += Resolver.sonatypeRepo("snapshots")

useGpg := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

import ReleaseTransformations._
releaseProcess := Seq[ReleaseStep](
  releaseStepCommand("git fetch"),
  releaseStepCommand("git checkout master"),
  releaseStepCommand("git pull"),
  releaseStepCommand("git merge origin/develop"),
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  //releaseStepCommand("ghpagesPushSite"),
  releaseStepCommand("publishSigned"),
  releaseStepCommand("sonatypeReleaseAll"),
  pushChanges,
  releaseStepCommand("git checkout develop"),
  releaseStepCommand("git merge master"),
  setNextVersion,
  commitNextVersion,
  pushChanges
)

libraryDependencies ++= Seq(
  Defaults.sbtPluginExtra(
    "com.typesafe.sbt" % "sbt-ghpages" % "0.6.2",
    (sbtBinaryVersion in pluginCrossBuild).value,
    (scalaBinaryVersion in pluginCrossBuild).value
  ),
  Defaults.sbtPluginExtra(
    "org.planet42" % "laika-sbt" % "0.7.0",
    (sbtBinaryVersion in pluginCrossBuild).value,
    (scalaBinaryVersion in pluginCrossBuild).value
  ),
  Defaults.sbtPluginExtra(
    "com.eed3si9n" % "sbt-assembly" % "0.14.5",
    (sbtBinaryVersion in pluginCrossBuild).value,
    (scalaBinaryVersion in pluginCrossBuild).value
  ),
  Defaults.sbtPluginExtra(
    "com.jsuereth" % "sbt-pgp" % "1.0.0",
    (sbtBinaryVersion in pluginCrossBuild).value,
    (scalaBinaryVersion in pluginCrossBuild).value
  ),
  Defaults.sbtPluginExtra(
    "org.xerial.sbt" % "sbt-sonatype" % "1.1",
    (sbtBinaryVersion in pluginCrossBuild).value,
    (scalaBinaryVersion in pluginCrossBuild).value
  ),
  Defaults.sbtPluginExtra(
    "com.typesafe.sbt" % "sbt-site" % "1.3.1",
    (sbtBinaryVersion in pluginCrossBuild).value,
    (scalaBinaryVersion in pluginCrossBuild).value
  ),
  Defaults.sbtPluginExtra(
    "com.github.gseitz" % "sbt-release" % "1.0.6",
    (sbtBinaryVersion in pluginCrossBuild).value,
    (scalaBinaryVersion in pluginCrossBuild).value
  ),
  Defaults.sbtPluginExtra(
    "org.scoverage" % "sbt-coveralls" % "1.2.2",
    (sbtBinaryVersion in pluginCrossBuild).value,
    (scalaBinaryVersion in pluginCrossBuild).value
  ),
  Defaults.sbtPluginExtra(
    "org.scoverage" % "sbt-scoverage" % "1.5.1",
    (sbtBinaryVersion in pluginCrossBuild).value,
    (scalaBinaryVersion in pluginCrossBuild).value
  ),
  Defaults.sbtPluginExtra(
    "com.lucidchart" % "sbt-scalafmt" % "1.10",
    (sbtBinaryVersion in pluginCrossBuild).value,
    (scalaBinaryVersion in pluginCrossBuild).value
  )
)

