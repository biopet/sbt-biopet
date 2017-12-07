package nl.biopet.sbtbiopet

import com.lucidchart.sbt.scalafmt.ScalafmtSbtPlugin
import com.typesafe.sbt.SbtGit.git
import com.typesafe.sbt.SbtPgp.autoImport.useGpg
import com.typesafe.sbt.sbtghpages.GhpagesPlugin.autoImport.{ghpagesCleanSite, ghpagesPushSite, ghpagesRepository}
import com.typesafe.sbt.sbtghpages.GhpagesPlugin
import com.typesafe.sbt.site.SitePlugin.autoImport.{makeSite, siteDirectory, siteSubdirName}
import com.typesafe.sbt.site.{SitePlugin, SiteScaladocPlugin}
import com.typesafe.sbt.site.SiteScaladocPlugin.autoImport.SiteScaladoc
import com.typesafe.sbt.site.laika.LaikaSitePlugin
import com.typesafe.sbt.site.laika.LaikaSitePlugin.autoImport.LaikaSite
import laika.sbt.LaikaSbtPlugin.LaikaKeys.{Laika, rawContent}
import sbt.Keys._
import sbt.{Def, _}
import sbtassembly.AssemblyPlugin.autoImport
import sbtassembly.AssemblyPlugin.autoImport.{Assembly, assembly, assemblyMergeStrategy,PathList}
import sbtassembly.MergeStrategy
import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._
import sbtrelease.ReleasePlugin.autoImport.{ReleaseStep, releaseProcess, releaseStepCommand}

object BiopetPlugin extends AutoPlugin {
  override def trigger: PluginTrigger = AllRequirements

  override def requires: Plugins = empty

  override lazy val globalSettings: Seq[Setting[_]] = biopetGlobalSettings
  override lazy val projectSettings: Seq[Setting[_]] = biopetProjectSettings
  override lazy val buildSettings: Seq[Setting[_]] = biopetBuildSettings

  object autoImport extends BiopetKeys

  import autoImport._
  def biopetGlobalSettings: Seq[Setting[_]] = Nil

  def biopetBuildSettings: Seq[Setting[_]] = Nil

  def biopetProjectSettings: Seq[Setting[_]] = {
    GhpagesPlugin.projectSettings ++
      // Importing globalSettings into projectSettings,
      // it does not change functionality, and removes those nasty
      // global variables.
      GhpagesPlugin.globalSettings ++
      SitePlugin.projectSettings ++
      SiteScaladocPlugin.projectSettings ++
      LaikaSitePlugin.projectSettings ++
      ScalafmtSbtPlugin.projectSettings ++
      biopetDocumentationSettings ++
      biopetReleaseSettings ++
      biopetAssemblySettings ++
      biopetReleaseSettings ++
      biopetProjectInformationSettings
  }
  private def biopetAssemblySettings: Seq[Setting[_]] =
    Seq(mainClass in assembly := {
      if (biopetIsTool.value)
        Some(s"nl.biopet.tools.${name.value.toLowerCase()}.${name.value}")
      else None
    },
      assemblyMergeStrategy in assembly := biopetMergeStrategy)
  private def biopetProjectInformationSettings: Seq[Setting[_]] = Seq(
    homepage := Some(url(s"https://github.com/biopet/${biopetUrlName.value}")),
    licenses := Seq("MIT" -> url("https://opensource.org/licenses/MIT")),
    scmInfo := Some(
      ScmInfo(
        url(s"https://github.com/biopet/${biopetUrlName.value}"),
        s"scm:git@github.com:biopet/${biopetUrlName.value}.git"
      )),
    git.remoteRepo := s"git@github.com:biopet/${biopetUrlName.value}.git",
    biopetIsTool := false // This should not have to be defined for utils.
  )

  private def biopetReleaseSettings: Seq[Setting[_]] = Seq(
    resolvers += Resolver.sonatypeRepo("snapshots"),
    publishTo := biopetPublishTo.value,
    publishMavenStyle := true,
    useGpg := true,
    releaseProcess := biopetReleaseProcess
  )
  private def biopetDocumentationSettings: Seq[Setting[_]] = Seq(
    biopetDocsDir := file("%s/markdown".format(target.value.toString)),
    biopetReadmePath := file("README.md").getAbsoluteFile,
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
  private def biopetMergeStrategy: String => MergeStrategy = {
    case PathList(ps @ _*) if ps.last endsWith "pom.properties" =>
      MergeStrategy.first
    case PathList(ps @ _*) if ps.last endsWith "pom.xml" =>
      MergeStrategy.first
    case x if Assembly.isConfigFile(x) =>
      MergeStrategy.concat
    case PathList(ps @ _*) if Assembly.isReadme(ps.last) || Assembly.isLicenseFile(ps.last) =>
      MergeStrategy.rename
    case PathList("META-INF", xs @ _*) =>
      (xs map {_.toLowerCase}) match {
        case ("manifest.mf" :: Nil) | ("index.list" :: Nil) | ("dependencies" :: Nil) =>
          MergeStrategy.discard
        case ps @ (x :: xs) if ps.last.endsWith(".sf") || ps.last.endsWith(".dsa") =>
          MergeStrategy.discard
        case "plexus" :: xs =>
          MergeStrategy.discard
        case "services" :: xs =>
          MergeStrategy.filterDistinctLines
        case ("spring.schemas" :: Nil) | ("spring.handlers" :: Nil) =>
          MergeStrategy.filterDistinctLines
        case _ => MergeStrategy.deduplicate
      }
    case _ => MergeStrategy.first
  }
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
            f.getPath == new java.io.File(ghpagesRepository.value,
                                          "index.html").getPath
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
