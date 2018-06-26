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

import java.io.File

import com.codacy.CodacyCoveragePlugin
import com.lucidchart.sbt.scalafmt.ScalafmtCorePlugin.autoImport.{
  scalafmt,
  scalafmtOnCompile
}
import com.lucidchart.sbt.scalafmt.ScalafmtSbtPlugin
import com.lucidchart.sbt.scalafmt.ScalafmtSbtPlugin.autoImport.Sbt
import com.typesafe.sbt.SbtGit.git
import com.typesafe.sbt.sbtghpages.GhpagesPlugin
import com.typesafe.sbt.sbtghpages.GhpagesPlugin.autoImport.ghpagesRepository
import com.typesafe.sbt.site.laika.LaikaSitePlugin
import com.typesafe.sbt.site.{SitePlugin, SiteScaladocPlugin}
import de.heikoseeberger.sbtheader.HeaderPlugin.autoImport.{
  HeaderCommentStyle,
  headerCheck,
  headerCreate,
  headerMappings
}
import de.heikoseeberger.sbtheader.{FileType, HeaderPlugin}
import nl.biopet.bioconda.BiocondaPlugin
import nl.biopet.bioconda.BiocondaPlugin.autoImport._
import nl.biopet.sbtbiopet.BiopetDocumentationSettings._
import nl.biopet.sbtbiopet.BiopetReleaseSettings._
import nl.biopet.utils.Documentation.markdownExtractChapter
import ohnosequences.sbt.SbtGithubReleasePlugin
import ohnosequences.sbt.SbtGithubReleasePlugin.autoImport._
import sbt.Keys._
import sbt.{Def, _}
import sbtassembly.AssemblyPlugin.autoImport._
import sbtassembly.{AssemblyPlugin, MergeStrategy}
import scoverage.ScoverageSbtPlugin

import scala.io.Source

object BiopetPlugin extends AutoPlugin {
  override def trigger: PluginTrigger = AllRequirements

  override def requires: Plugins =
    empty &&
      AssemblyPlugin

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
      GhpagesPlugin.globalSettings ++
      AssemblyPlugin.globalSettings ++
      ScoverageSbtPlugin.globalSettings // Having seen the source I dare not put this in project settings
  }

  /*
   * Settings to be added to the build scope. Settings here are applied only once.
   */
  def biopetBuildSettings: Seq[Setting[_]] = {
    super.buildSettings ++ AssemblyPlugin.buildSettings ++
      ScoverageSbtPlugin.buildSettings
  }

  /*
   * Settings that are project specific
   */
  def biopetProjectSettings: Seq[Setting[_]] = {
    GhpagesPlugin.projectSettings ++
      SitePlugin.projectSettings ++
      AssemblyPlugin.projectSettings ++
      SiteScaladocPlugin.projectSettings ++
      LaikaSitePlugin.projectSettings ++
      ScalafmtSbtPlugin.projectSettings ++
      ScoverageSbtPlugin.projectSettings ++
      HeaderPlugin.projectSettings ++
      SbtGithubReleasePlugin.projectSettings ++
      BiocondaPlugin.projectSettings ++
      CodacyCoveragePlugin.projectSettings ++
      biopetProjectInformationSettings ++
      biopetAssemblySettings ++
      biopetReleaseSettings ++
      biopetDocumentationSettings ++
      biopetHeaderSettings ++
      biopetScalafmtSettings ++
      biopetTestSettings ++
      biopetBiocondaSettings
  }

  /*
   * All assembly specific settings
   */
  protected def biopetAssemblySettings: Seq[Setting[_]] =
    Seq(
      assemblyMergeStrategy in assembly := biopetMergeStrategy
    )

  protected def biopetTestSettings: Seq[Setting[_]] = {
    Def.settings(
      biopetEnableCodacyCoverage := true,
      commands += Command.command("biopetTest") { state =>
        List(
          "scalafmt::test",
          "test:scalafmt::test",
          "sbt:scalafmt::test",
          "headerCreate",
          "coverage",
          "makeSite",
          "biopetGenerateReadme",
          "test"
        ).foldRight(state)(_ :: _)
      },
      commands += Command.command("biopetTestReport") { state =>
        List(
          Some("coverageReport"),
          Some("coverageAggregate"), {
            if (biopetEnableCodacyCoverage.value) Some("codacyCoverage")
            else None
          }
        ).flatten
          .foldRight(state)(_ :: _)
      }
    )
  }

  /*
   * Contains al settings related to the license header
   */
  protected def biopetHeaderSettings: Seq[Setting[_]] =
    Seq(
      // headerMappings for other filetypes
      headerMappings := headerMappings.value +
        (FileType("html", Some("^<!DOCTYPE html>$\\n".r)) -> HeaderCommentStyle.xmlStyleBlockComment) +
        (FileType("css") -> HeaderCommentStyle.cStyleBlockComment) +
        (FileType.sh -> HeaderCommentStyle.hashLineComment) +
        (FileType("yml") -> HeaderCommentStyle.hashLineComment),
      //Add resources
      unmanagedSources in (Compile, headerCreate) ++= (unmanagedResources in Compile).value,
      // add test resources
      unmanagedResources in (Test, headerCreate) ++= (unmanagedResources in Test).value,
      // Make sure headerCheck checks the same things as headerCreate
      unmanagedResources in (Compile, headerCheck) := (unmanagedResources in (Compile, headerCreate)).value,
      unmanagedResources in (Test, headerCheck) := (unmanagedResources in (Test, headerCreate)).value,
      // Run headerCreate and Headercheck on all configurations
      headerCreate := (headerCreate in Compile)
        .dependsOn(headerCreate in Test)
        .value,
      headerCheck := (headerCheck in Compile)
        .dependsOn(headerCheck in Test)
        .value
    )

  /*
   * A sequence of settings containing information such as homepage, licences and git related information.
   */
  protected def biopetProjectInformationSettings: Seq[Setting[_]] = Seq(
    githubOrganization := "biopet",
    homepage := Some(url(
      s"https://github.com/${githubOrganization.value}/${biopetUrlName.value}")),
    licenses := Seq("MIT" -> url("https://opensource.org/licenses/MIT")),
    scmInfo := Some(
      ScmInfo(
        url(
          s"https://github.com/${githubOrganization.value}/${biopetUrlName.value}"),
        s"scm:git@github.com:${githubOrganization.value}/${biopetUrlName.value}.git"
      )),
    git.remoteRepo := s"git@github.com:${githubOrganization.value}/${biopetUrlName.value}.git",
    biopetIsTool := false // This should not have to be defined for utils.
  )

  protected def biopetScalafmtSettings: Seq[Setting[_]] = Seq(
    scalafmtOnCompile := true, // make sure scalafmt is run regularly during development
    // make sure scalafmt command reformats everything
    scalafmt := (scalafmt in Compile)
      .dependsOn(scalafmt in Test)
      .dependsOn(scalafmt in Sbt)
      .value
  )

  protected def biopetBiocondaSettings: Seq[Setting[_]] = Def.settings(
    biocondaGitUrl := "git@github.com:biopet/bioconda-recipes.git",
    name in Bioconda := s"biopet-${normalizedName.value}",
    biocondaCommand := s"biopet-${normalizedName.value}",
    biocondaTestCommands := {
      val command = biocondaCommand.value
      Seq(s"$command --version", s"$command --help")
    },
    // Space beteween name and requirements is mandatory. See https://github.com/conda/conda-build/issues/2117
    biocondaRequirements := Seq("openjdk >=8,<9"), // OpenJDK should be 8. Not 9 or higher.
    biocondaDescription := Def
      .taskDyn {
        val readme = Source.fromFile(biopetReadmePath.value).mkString
        if (biopetIsTool.value) {
          Def
            .task {
              Some({
                markdownExtractChapter(readme,
                                       name.value,
                                       includeHeader = false).trim +
                  s"\n\nFor documentation and manuals visit our github.io page: " +
                  s"https://${githubOrganization.value}.github.io/${biopetUrlName.value}"
              }
              // Remove whitespace from beginning and end of string.
              .trim)
            }
        } else
          Def.task {
            Some("")
          }
      }
      .dependsOn(biopetGenerateReadme)
      .value,
    biocondaSummary := Def
      .taskDyn {
        val readme = Source.fromFile(biopetReadmePath.value).mkString
        if (biopetIsTool.value) {
          Def.task {
            val description =
              markdownExtractChapter(readme, name.value, includeHeader = false)
            // Assuming the first sentence ends with .
            description.split("\\.").headOption match {
              case Some(s) => {
                s + "."
              }.replace("\n", " ").trim
              case _ =>
                s"This summary for ${(name in Bioconda).value} was automatically generated."
            }

          }
        } else
          Def.task {
            ""
          }
      }
      .dependsOn(biopetGenerateReadme)
      .value
  )

  /*
   * The merge strategy that is used in biopet projects
   */
  protected def biopetMergeStrategy: String => MergeStrategy = {
    case PathList(ps @ _*) if ps.last endsWith "pom.properties" =>
      MergeStrategy.first
    case PathList(ps @ _*) if ps.last endsWith "pom.xml" =>
      MergeStrategy.first
    case x if Assembly.isConfigFile(x) =>
      MergeStrategy.concat
    case PathList(ps @ _*)
        if Assembly.isReadme(ps.last) || Assembly.isLicenseFile(ps.last) =>
      MergeStrategy.rename
    case PathList("META-INF", xs @ _*) =>
      xs map {
        _.toLowerCase
      } match {
        case "manifest.mf" :: Nil | "index.list" :: Nil |
            "dependencies" :: Nil =>
          MergeStrategy.discard
        case ps @ _ :: _
            if ps.last.endsWith(".sf") || ps.last.endsWith(".dsa") =>
          MergeStrategy.discard
        case "plexus" :: _ =>
          MergeStrategy.discard
        case "services" :: _ =>
          MergeStrategy.filterDistinctLines
        case "spring.schemas" :: Nil | "spring.handlers" :: Nil =>
          MergeStrategy.filterDistinctLines
        case _ => MergeStrategy.first
      }
    case _ => MergeStrategy.first
  }
}
