package nl.biopet.sbtbiopet

import sbt._
import Keys._
import com.typesafe.sbt.site.SitePlugin
import com.typesafe.sbt.sbtghpages.GhpagesKeys
import laika.sbt.LaikaSbtPlugin.{LaikaKeys, LaikaPlugin}
import sbtassembly.AssemblyKeys
import com.typesafe.sbt.SbtGit.git



object BiopetPlugin extends AutoPlugin {
  override val requires: Plugins = SitePlugin
  override lazy val globalSettings: Seq[Setting[_]] = BiopetGlobalSettings
  override lazy val projectSettings: Seq[Setting[_]] = BiopetProjectSettings

  object autoImport extends BiopetKeys with GhpagesKeys with AssemblyKeys
  import autoImport._


  def BiopetGlobalSettings: Seq[Setting[_]] =
      Seq (
        LaikaKeys.rawContent := true,
        biopetUrlToolName := name.value.toLowerCase(),
        git.remoteRepo := s"git@github.com:biopet/${biopetUrlToolName.value}.git",
        ghpagesRepository := sbt.file("target/site"),
      )
  def BiopetProjectSettings: Seq[Setting[_]] = Seq (
    biopetGenerateDocs := {
      import Attributed.data
      val r = (runner in Runtime).value
      val input = Seq(
        "--generateDocs",
        s"outputDir=${biopetDocsDir.value.toString}," +
          s"version=${version.value}," +
          s"release=${!isSnapshot.value}",
        version.value)
      val classPath = (fullClasspath in Runtime).value
      r.run(
        s"${(mainClass in assembly).value.get}",
        data(classPath),
        input,
        streams.value.log
      ).foreach(sys.error)
    } ,
    biopetGenerateReadme := {
      import sbt.Attributed.data
      val r: ScalaRun = (runner in Runtime).value
      val input = Seq("--generateReadme", biopetReadmePath.value.toString)
      val classPath =  (fullClasspath in Runtime).value
      r.run(
        s"${(mainClass in assembly).value.get}",
        data(classPath),
        input,
        streams.value.log
      ).foreach(sys.error)
    }

  )
}