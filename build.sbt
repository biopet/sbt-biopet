organization := "com.github.biopet"
name := "sbt-biopet"


scalaVersion := "2.10.6"

resolvers += Resolver.sonatypeRepo("snapshots")

useGpg := true

sbtPlugin := true

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
  )

)