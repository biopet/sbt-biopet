lazy val root = (project in file(".")).settings(
  version := "0.1",
  name := "DummyTool",
  biopetUrlName := "dummytool",
  biopetIsTool := true,
  mainClass in assembly := Some(s"nl.biopet.tools.dummytool.DummyTool"),
  scalaVersion := "2.11.11"
  )

libraryDependencies += "com.github.biopet" %% "tool-utils" % "0.2"
libraryDependencies += "com.github.biopet" %% "tool-test-utils" % "0.1" % Test
