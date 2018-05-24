import org.kohsuke.github.{GHRepository, GitHub}
import org.apache.commons.io.FileUtils
import sbt.Keys.resolvers
import sbt._
lazy val checkRepo = taskKey[Unit]("checks if repo is checked out")
lazy val checkRecipes = taskKey[Unit]("checks if recipes are created")
lazy val checkCopy = taskKey[Unit]("checks if recipes are copied")
lazy val deleteTmp = taskKey[Unit]("Deletes temporary test dir")

name := "testtool"
organizationName := "biopet"
organization := "biopet"
startYear := Some(2017)
homepage := Some(new URL("https://github.com/biopet/testtool"))
//mainClass in assembly := Some(s"nl.biopet.tools.dummytool.DummyTool")
scalaVersion := "2.11.11"
licenses := Seq("MIT" -> url("https://opensource.org/licenses/MIT"))
libraryDependencies += "com.github.biopet" %% "tool-utils" % "0.2"
libraryDependencies += "org.kohsuke" % "github-api" % "1.92"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5"
ghreleaseRepoOrg := "biopet"
ghreleaseRepoName := "testtool"
biocondaRepository := biocondaTempDir.value
biopetUrlName := "testtool"
biopetIsTool := true
biopetReleaseInBioconda := false
checkRepo := Def.task {
  filesExistInDir(
    biocondaRepository.value,
    Seq(".github", "recipes", "scripts", "recipes/biopet", "config.yml"))
}.value
checkRecipes := Def.task {
  filesExistInDir(biocondaRecipeDir.value,
                  Seq("0.1/meta.yaml",
                      "0.1/build.sh",
                      "0.1/biopet-testtool.py",
                      "meta.yaml",
                      "build.sh",
                      "biopet-testtool.py"))
}.value
checkCopy := Def.task {
  filesExistInDir(
    biocondaRepository.value,
    Seq(
      "recipes/biopet-testtool/0.1/meta.yaml",
      "recipes/biopet-testtool/0.1/build.sh",
      "recipes/biopet-testtool/0.1/biopet-testtool.py",
      "recipes/biopet-testtool/meta.yaml",
      "recipes/biopet-testtool/build.sh",
      "recipes/biopet-testtool/biopet-testtool.py"
    )
  )
}.value
deleteTmp := Def.task { FileUtils.deleteDirectory(biocondaRepository.value) }.value
resolvers += Resolver.sonatypeRepo("snapshots")

def fileExistsInDir(dir: File, file: String): Unit = {
  assert(new File(dir, file).exists(), s"$file should exist in $dir")
}
def filesExistInDir(dir: File, files: Seq[String]): Unit = {
  files.foreach(file => fileExistsInDir(dir, file))
}

// Home directory is used because using /tmp gives errors while testing
def biocondaTempDir: Def.Initialize[File] = {
  Def.setting {
    val homeTest = new File(sys.env("HOME"))
    val dir = java.io.File.createTempFile("bioconda", ".d", homeTest)
    dir.delete()
    dir.mkdirs()
    dir.deleteOnExit()
    dir
  }
}
