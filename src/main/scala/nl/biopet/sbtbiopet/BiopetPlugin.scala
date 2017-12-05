package nl.biopet.sbtbiopet

import sbt._
import Keys._
import com.typesafe.sbt.site.SitePlugin
import com.typesafe.sbt.site.SitePlugin.autoImport.{
  siteDirectory,
  siteSubdirName,
  makeSite
}
import com.typesafe.sbt.site.laika.LaikaSitePlugin
import com.typesafe.sbt.site.laika.LaikaSitePlugin.autoImport.LaikaSite
import com.typesafe.sbt.site.SiteScaladocPlugin
import com.typesafe.sbt.site.SiteScaladocPlugin.autoImport.SiteScaladoc
import com.typesafe.sbt.sbtghpages.GhpagesPlugin
import com.typesafe.sbt.sbtghpages.GhpagesPlugin.autoImport.{
  ghpagesRepository,
  ghpagesCleanSite,
  ghpagesPushSite
}
import laika.sbt.LaikaSbtPlugin.LaikaKeys.{Laika, rawContent}
import sbtassembly.AssemblyPlugin
import sbtassembly.AssemblyPlugin.autoImport.assembly
import com.typesafe.sbt.SbtGit.git
import com.typesafe.sbt.SbtPgp
import sbtrelease.ReleasePlugin
import ReleasePlugin.autoImport.ReleaseTransformations._
import ReleasePlugin.autoImport.{
  ReleaseStep,
  releaseProcess,
  releaseStepCommand
}

object BiopetPlugin extends AutoPlugin {
  override val requires: Plugins = {
    SitePlugin &&
    GhpagesPlugin &&
    AssemblyPlugin &&
    SbtPgp &&
    ReleasePlugin &&
    LaikaSitePlugin &&
    SiteScaladocPlugin
  }
  override lazy val globalSettings: Seq[Setting[_]] = BiopetGlobalSettings
  override lazy val projectSettings: Seq[Setting[_]] = BiopetProjectSettings
  object autoImport extends BiopetKeys
  import autoImport._

  def BiopetGlobalSettings: Seq[Setting[_]] = {

    Seq(
      // Publication to nexus repository
      resolvers += Resolver.sonatypeRepo("snapshots"),
      publishTo := {
        val nexus = "https://oss.sonatype.org/"
        if (isSnapshot.value)
          Some("snapshots" at nexus + "content/repositories/snapshots")
        else
          Some("releases" at nexus + "service/local/staging/deploy/maven2")
      },
      SbtPgp.autoImport.useGpg := true,
      // Jar assembly
      mainClass in assembly := Some(
        s"nl.biopet.${name.value.toLowerCase()}.${name.value}"),
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
        releaseStepCommand("ghpagesPushSite"),
        releaseStepCommand("publishSigned"),
        releaseStepCommand("sonatypeReleaseAll"),
        pushChanges,
        releaseStepCommand("git checkout develop"),
        releaseStepCommand("git merge master"),
        setNextVersion,
        commitNextVersion,
        pushChanges
      ),
      // Documentation variables
      biopetUrlToolName := name.value.toLowerCase(),
      biopetDocsDir := file(target.value.toString + "/markdown"),
      sourceDirectory in LaikaSite := biopetDocsDir.value,
      sourceDirectories in Laika := Seq((sourceDirectory in LaikaSite).value),
      siteDirectory in Laika := file(target.value.toString() + "/site"),
      git.remoteRepo := s"git@github.com:biopet/${biopetUrlToolName.value}.git",
      ghpagesRepository := file(target.value.toString + "/gh"),
      siteSubdirName in SiteScaladoc := {
        if (isSnapshot.value) { "develop/api" } else s"${version.value}/api"
      },
      rawContent := true, //Laika use raw HTML content in markdown.
      includeFilter in ghpagesCleanSite := new FileFilter {
        def accept(f: File) = {
          if (isSnapshot.value) {
            f.getPath.contains("develop")
          } else {
            f.getPath.contains(s"${version.value}") ||
            f.getPath == new java.io.File(ghpagesRepository.value,
                                          "index.html").getPath
          }
        }
      }
    )
  }
  def BiopetProjectSettings: Seq[Setting[_]] = Seq(
    biopetGenerateDocs := {
      import Attributed.data
      val r = (runner in Runtime).value
      val input = Seq("--generateDocs",
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
        )
        .foreach(sys.error)
    },
    biopetGenerateReadme := {
      import sbt.Attributed.data
      val r: ScalaRun = (runner in Runtime).value
      val input = Seq("--generateReadme", biopetReadmePath.value.toString)
      val classPath = (fullClasspath in Runtime).value
      r.run(
          s"${(mainClass in assembly).value.get}",
          data(classPath),
          input,
          streams.value.log
        )
        .foreach(sys.error)
    },
    makeSite := (makeSite triggeredBy (biopetGenerateDocs)).value,
    makeSite := (makeSite dependsOn (biopetGenerateDocs)).value,
    ghpagesPushSite := (ghpagesPushSite dependsOn (makeSite)).value
  )
}
