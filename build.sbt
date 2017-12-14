organization := "com.github.biopet"
name := "sbt-biopet"

homepage := Some(url(s"https://github.com/biopet/sbt-biopet"))
licenses := Seq("MIT" -> url("https://opensource.org/licenses/MIT"))

scmInfo := Some(
  ScmInfo(
    url("https://github.com/biopet/sbt-biopet"),
    "scm:git@github.com:biopet/sbt-biopet.git"
  )
)

developers := List(
  Developer(id="ffinfo", name="Peter van 't Hof", email="pjrvanthof@gmail.com", url=url("https://github.com/ffinfo")),
  Developer(id="rhpvorderman", name="Ruben Vorderman", email="r.h.p.vorderman@lumc.nl", url=url("https://github.com/rhpvorderman"))
)

publishMavenStyle := true

sbtPlugin := true

scalaVersion := "2.10.6"

resolvers += Resolver.sonatypeRepo("snapshots")

useGpg := true

publishTo := {
  if (isSnapshot.value)
    Some(Opts.resolver.sonatypeSnapshots)
  else
    Some(Opts.resolver.sonatypeStaging)
}

ScriptedPlugin.scriptedSettings
scriptedLaunchOpts := { scriptedLaunchOpts.value ++
  Seq("-Xmx1024M",
    "-Dplugin.version=" + version.value)
}
scriptedBufferLog := false

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
