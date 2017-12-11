version := "0.1"
name := "DummyTool"

biopetUrlName := "dummytool"
biopetIsTool := true

target := file("target")

mainClass in assembly := Some(s"nl.biopet.tools.${name.value.toLowerCase()}.${name.value}")

scalaVersion := "2.11.11"

libraryDependencies += "com.github.biopet" %% "tool-utils" % "0.2"
libraryDependencies += "com.github.biopet" %% "tool-test-utils" % "0.1" % Test
