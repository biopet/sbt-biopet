/*
 * Copyright (c) 2017 Sequencing Analysis Support Core - Leiden University Medical Center
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package nl.biopet.sbtbiopet

import com.lucidchart.sbt.scalafmt.ScalafmtSbtPlugin
import com.typesafe.sbt.SbtGit.git
import com.typesafe.sbt.SbtPgp.autoImport.useGpg
import com.typesafe.sbt.sbtghpages.GhpagesPlugin
import com.typesafe.sbt.sbtghpages.GhpagesPlugin.autoImport.{
  ghpagesCleanSite,
  ghpagesPushSite,
  ghpagesRepository
}
import com.typesafe.sbt.site.SitePlugin.autoImport.{
  makeSite,
  siteDirectory,
  siteSubdirName
}
import com.typesafe.sbt.site.SiteScaladocPlugin.autoImport.SiteScaladoc
import com.typesafe.sbt.site.laika.LaikaSitePlugin
import com.typesafe.sbt.site.laika.LaikaSitePlugin.autoImport.LaikaSite
import com.typesafe.sbt.site.{SitePlugin, SiteScaladocPlugin}
import de.heikoseeberger.sbtheader.HeaderPlugin
import laika.sbt.LaikaSbtPlugin.LaikaKeys.{Laika, rawContent}
import org.scoverage.coveralls.CoverallsPlugin
import sbt.Keys._
import sbt.{Def, _}
import sbtassembly.AssemblyPlugin.autoImport.{
  Assembly,
  PathList,
  assembly,
  assemblyMergeStrategy
}
import sbtassembly.MergeStrategy
import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._
import sbtrelease.ReleasePlugin.autoImport.{
  ReleaseStep,
  releaseProcess,
  releaseStepCommand
}
import scoverage.ScoverageSbtPlugin

object BiopetPlugin extends AutoPlugin {
  override def trigger: PluginTrigger = AllRequirements

  override def requires: Plugins = empty

  override lazy val globalSettings: Seq[Setting[_]] = biopetGlobalSettings
  override lazy val projectSettings: Seq[Setting[_]] = biopetProjectSettings
  override lazy val buildSettings: Seq[Setting[_]] = biopetBuildSettings

  object autoImport extends BiopetKeys

  import autoImport._

  /*
   * Settings to be inherited globally across all projects
   */
  def biopetGlobalSettings: Seq[Setting[_]] = {
    super.globalSettings ++
      ScoverageSbtPlugin.globalSettings // Having seen the source I dare not put this in project settings
  }

  /*
   * Settings to be added to the build scope. Settings here are applied only once.
   */
  def biopetBuildSettings: Seq[Setting[_]] = {
    super.buildSettings ++
      ScoverageSbtPlugin.buildSettings
  }

  /*
   * Settings that are project specific
   */
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
      CoverallsPlugin.projectSettings ++
      ScoverageSbtPlugin.projectSettings ++
      HeaderPlugin.projectSettings ++
      biopetProjectInformationSettings ++
      biopetAssemblySettings ++
      biopetReleaseSettings ++
      biopetDocumentationSettings ++
      biopetHeaderSettings
  }

  /*
   * All assembly specific settings
   */
  protected def biopetAssemblySettings: Seq[Setting[_]] =
    Seq(
      assemblyMergeStrategy in assembly := biopetMergeStrategy
    )

  /*
   * Contains al settings related to the license header
   */
  protected def biopetHeaderSettings: Seq[Setting[_]] = Nil
  /*
   * A sequence of settings containing information such as homepage, licences and git related information.
   */
  protected def biopetProjectInformationSettings: Seq[Setting[_]] = Seq(
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

  /*
   * A sequence of settings specific to release
   */
  protected def biopetReleaseSettings: Seq[Setting[_]] = Seq(
    resolvers += Resolver.sonatypeRepo("snapshots"),
    resolvers += Resolver.sonatypeRepo("releases"),
    publishTo := biopetPublishTo.value,
    publishMavenStyle := true,
    useGpg := true,
    releaseProcess := biopetReleaseProcess
  )

  /*
   * A sequence of settings related to documentation.
   * This includes all the settings for
   *  - LAIKA
   *  - Ghpagesplugin
   *  - Our custom documentation generation code
   *  - SBT-site
   */
  protected def biopetDocumentationSettings: Seq[Setting[_]] = Seq(
    biopetDocsDir := file("%s/markdown".format(target.value.toString)),
    biopetReadmePath := file("README.md").getAbsoluteFile,
    sourceDirectory in LaikaSite := biopetDocsDir.value,
    sourceDirectories in Laika := Seq((sourceDirectory in LaikaSite).value),
    siteDirectory in Laika := file(target.value.toString + "/site"),
    ghpagesRepository := file(target.value.toString + "/gh"),
    siteSubdirName in SiteScaladoc := {
      if (isSnapshot.value) { "develop/api" } else s"${version.value}/api"
    },
    rawContent in Laika := true, //Laika use raw HTML content in markdown.
    includeFilter in ghpagesCleanSite := biopetCleanSiteFilter.value,
    biopetGenerateDocs := biopetGenerateDocsFunction().value,
    biopetGenerateReadme := biopetGenerateReadmeFunction().value,
    makeSite := (makeSite triggeredBy biopetGenerateDocs).value,
    makeSite := (makeSite dependsOn biopetGenerateDocs).value,
    ghpagesPushSite := (ghpagesPushSite dependsOn makeSite).value
  )

  /*
   * The merge strategy that is used in biopet projects
   */
  protected def biopetMergeStrategy: String => MergeStrategy = {
    case PathList(ps @ _ *) if ps.last endsWith "pom.properties" =>
      MergeStrategy.first
    case PathList(ps @ _ *) if ps.last endsWith "pom.xml" =>
      MergeStrategy.first
    case x if Assembly.isConfigFile(x) =>
      MergeStrategy.concat
    case PathList(ps @ _ *)
        if Assembly.isReadme(ps.last) || Assembly.isLicenseFile(ps.last) =>
      MergeStrategy.rename
    case PathList("META-INF", xs @ _ *) =>
      xs map {
        _.toLowerCase
      } match {
        case ("manifest.mf" :: Nil) | ("index.list" :: Nil) |
            ("dependencies" :: Nil) =>
          MergeStrategy.discard
        case ps @ (x :: xs)
            if ps.last.endsWith(".sf") || ps.last.endsWith(".dsa") =>
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

  /*
   * Biopet resolver.
   */
  protected def biopetPublishTo: Def.Initialize[Option[Resolver]] =
    Def.setting {
      if (isSnapshot.value)
        Some(Opts.resolver.sonatypeSnapshots)
      else
        Some(Opts.resolver.sonatypeStaging)
    }

  /*
   * The ReleaseProcess for use with the sbt-release plugin
   */
  protected def biopetReleaseProcess: Seq[ReleaseStep] = {
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

  /*
   * The filter that is used by the ghpages plugin.
   * All files in this filter will be removed.
   * This allows the updating of documentation for a specific version
   * All other versions will not be touched.
   */
  protected def biopetCleanSiteFilter: Def.Initialize[FileFilter] =
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

  /*
   * Accesses the tools main method to generate documentation using our custom built-in documentation function
   */
  protected def biopetGenerateDocsFunction(): Def.Initialize[Task[Unit]] =
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

  /*
   * Accesses the tools main method to generate a README using our custom built-in documentation function
   */
  protected def biopetGenerateReadmeFunction(): Def.Initialize[Task[Unit]] =
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
