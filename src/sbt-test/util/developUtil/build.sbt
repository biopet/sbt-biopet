lazy val root = (project in file(".")).settings(
  name := "DummyUtil",
  organizationName := "Dummy Organization",
  organization := "example.dummy",
  startYear := Some(2018),
  biopetUrlName := "dummy-util",
  biopetIsTool := false,
  scalaVersion := "2.11.11"
)
libraryDependencies += "log4j" % "log4j" % "1.2.17"
libraryDependencies += "commons-io" % "commons-io" % "2.1"
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.6.3"
libraryDependencies += "org.yaml" % "snakeyaml" % "1.17"
libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.25"
libraryDependencies += "com.github.biopet" %% "test-utils" % "0.2" % Test

