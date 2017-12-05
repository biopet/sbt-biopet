package nl.biopet.sbtbiopet

import sbt._
import Keys._
import com.typesafe.sbt.site.SitePlugin
import com.typesafe.sbt.sbtghpages.GhpagesPlugin
import laika.sbt.LaikaSbtPlugin.{LaikaPlugin, LaikaKeys}
import sbtassembly.Assembly
import sbtassembly.AssemblyPlugin
import com.typesafe.sbt.SbtGit.git



object BiopetPlugin extends AutoPlugin {
  override val requires: Plugins =
    SitePlugin &&
    GhpagesPlugin &&
    AssemblyPlugin
  override lazy val globalSettings: Seq[Setting[_]] = BiopetGlobalSettings
  override lazy val projectSettings: Seq[Setting[_]] = BiopetProjectSettings
  object autoImport extends BiopetKeys
  import autoImport._

  // Settings for other plugins



  def BiopetGlobalSettings: Seq[Setting[_]] =
      Seq (
        LaikaKeys.rawContent := true,
        biopetUrlToolName := name.value.toLowerCase(),
        git.remoteRepo := s"git@github.com:biopet/${biopetUrlToolName.value}.git",
        GhpagesPlugin.autoImport.ghpagesRepository := sbt.file("target/site"),
        mainClass in AssemblyPlugin.autoImport.assembly := Some(s"nl.biopet.${name.value.toLowerCase()}.${name.value}")

   ,
      )
  def BiopetProjectSettings: Seq[Setting[_]] = Seq (
    biopetGenerateDocs := {
      import Attributed.data
      val r = (runner in Runtime).value
      val input = Seq(
        "--generateDocs",
        s"outputDir=${biopetDocsDir.value.toString}," +
          s"version=${version.val ue}," +
          s"release=${!isSnapshot.value}",
        version.value)
      val classPath = (fullClasspath in Runtime).value
      r.run(
        s"${(mainClass in Assembly.assembly).value.get}",
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