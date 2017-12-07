package nl.biopet.sbtbiopet

import sbt.{Def, _}
import Keys._
import com.typesafe.sbt.site.SitePlugin
import com.typesafe.sbt.site.SitePlugin.autoImport.{makeSite, siteDirectory, siteSubdirName}
import com.typesafe.sbt.site.laika.LaikaSitePlugin
import com.typesafe.sbt.site.laika.LaikaSitePlugin.autoImport.LaikaSite
import com.typesafe.sbt.site.SiteScaladocPlugin
import com.typesafe.sbt.site.SiteScaladocPlugin.autoImport.SiteScaladoc
import com.typesafe.sbt.sbtghpages.GhpagesPlugin
import com.typesafe.sbt.sbtghpages.GhpagesPlugin.autoImport.{ghpagesCleanSite, ghpagesPushSite, ghpagesRepository}
import laika.sbt.LaikaSbtPlugin.LaikaKeys.{Laika, rawContent}
import sbtassembly.AssemblyPlugin
import sbtassembly.AssemblyPlugin.autoImport.assembly
import com.typesafe.sbt.SbtGit.git
import com.typesafe.sbt.SbtPgp
import com.typesafe.sbt.SbtPgp.autoImport.useGpg
import sbtrelease.ReleasePlugin
import ReleasePlugin.autoImport.ReleaseTransformations._
import ReleasePlugin.autoImport.{ReleaseStep, releaseProcess, releaseStepCommand}

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

  def BiopetGlobalSettings: Seq[Setting[_]] = Seq(
    // Publication to nexus repository
    resolvers += Resolver.sonatypeRepo("snapshots"),
    publishTo := biopetPublishTo.value,
    useGpg := true,
    // Jar assembly
    releaseProcess := biopetReleaseProcess,
    // Documentation variables
    biopetDocsDir := file("%s/markdown".format(target.value.toString)),
    sourceDirectory in LaikaSite := biopetDocsDir.value,
    sourceDirectories in Laika := Seq((sourceDirectory in LaikaSite).value),
    siteDirectory in Laika := file(target.value.toString + "/site"),
    ghpagesRepository := file(target.value.toString + "/gh"),
    siteSubdirName in SiteScaladoc := {
      if (isSnapshot.value) { "develop/api" } else s"${version.value}/api"
    },
    rawContent := true, //Laika use raw HTML content in markdown.
    includeFilter in ghpagesCleanSite := biopetCleanSiteFilter.value,
    biopetGenerateDocs := biopetGenerateDocsFunction().value,
    biopetGenerateReadme := biopetGenerateReadmeFunction().value,
    makeSite := (makeSite triggeredBy biopetGenerateDocs).value,
    makeSite := (makeSite dependsOn biopetGenerateDocs).value,
    ghpagesPushSite := (ghpagesPushSite dependsOn makeSite).value
  )

  def BiopetProjectSettings: Seq[Setting[_]] = Seq(
    mainClass in assembly := {
      if (biopetIsTool.value)
        Some(s"nl.biopet.tools.${name.value.toLowerCase()}.${name.value}")
      else None
    },
    git.remoteRepo := s"git@github.com:biopet/${biopetUrlName.value}.git"
  )
  private def biopetPublishTo: Def.Initialize[Option[Resolver]] =
  Def.setting {
    if (isSnapshot.value)
      Some(Opts.resolver.sonatypeSnapshots)
    else
      Some(Opts.resolver.sonatypeStaging)
  }

  private def biopetReleaseProcess: Seq[ReleaseStep] = {
    Seq[ReleaseStep](
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
      releaseStepCommand("publishSigned"),
      releaseStepCommand("sonatypeReleaseAll"),
      releaseStepCommand("ghpagesPushSite"),
      pushChanges,
      releaseStepCommand("git checkout develop"),
      releaseStepCommand("git merge master"),
      setNextVersion,
      commitNextVersion,
      pushChanges
    )
  }
  private def biopetCleanSiteFilter: Def.Initialize[FileFilter] =
  Def.setting {
    new FileFilter {
      def accept(f: File) = {
        if (isSnapshot.value) {
          f.getPath.contains("develop")
        } else {
          f.getPath.contains(s"${version.value}") ||
          f.getPath == new java.io.File(ghpagesRepository.value, "index.html").getPath
        }
      }
    }
  }
  private def biopetGenerateDocsFunction(): Def.Initialize[Task[Unit]] =
    Def.task[Unit] {
    if (biopetIsTool.value) {
      import Attributed.data
      val r = (runner in Runtime).value
      val args = Seq("--generateDocs",
                     s"outputDir=${biopetDocsDir.value.toString}," +
                       s"version=${version.value}," +
                       s"release=${!isSnapshot.value}",
                     version.value)
      val classPath = (fullClasspath in Runtime).value
      r.run(
          s"${(mainClass in assembly).value.get}",
          data(classPath),
          args,
          streams.value.log
        )
        .foreach(sys.error)
    }
  }
  private def biopetGenerateReadmeFunction(): Def.Initialize[Task[Unit]] =
    Def.task[Unit] {
    if (biopetIsTool.value) {
      import sbt.Attributed.data
      val r: ScalaRun = (runner in Runtime).value
      val args = Seq("--generateReadme", biopetReadmePath.value.toString)
      val classPath = (fullClasspath in Runtime).value
      r.run(
          s"${(mainClass in assembly).value.get}",
          data(classPath),
          args,
          streams.value.log
        )
        .foreach(sys.error)
    }
  }
}
