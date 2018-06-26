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
import com.typesafe.sbt.site.laika.LaikaSitePlugin.autoImport.LaikaSite
import laika.sbt.LaikaPlugin.autoImport.{Laika, laikaRawContent}
import nl.biopet.sbtbiopet.BiopetPlugin.autoImport._
import nl.biopet.utils.Documentation.htmlRedirector
import ohnosequences.sbt.GithubRelease.keys.TagName
import sbt.Keys._
import sbt.io.IO.relativize
import sbt.{Attributed, Compile, Def, File, FileFilter, Setting, Task, file}
import sbtassembly.AssemblyPlugin.autoImport.assembly

object BiopetDocumentationSettings {
  /*
   * A sequence of settings related to documentation.
   * This includes all the settings for
   *  - LAIKA
   *  - Ghpagesplugin
   *  - Our custom documentation generation code
   *  - SBT-site
   */
  def biopetDocumentationSettings: Seq[Setting[_]] = Seq(
    biopetDocsDir := file(
      s"%s${File.separator}markdown".format(target.value.toString)),
    biopetReadmePath := file("README.md").getAbsoluteFile,
    sourceDirectory in LaikaSite := biopetDocsDir.value,
    sourceDirectories in Laika := Seq((sourceDirectory in LaikaSite).value),
    siteDirectory in Laika := file(
      target.value.toString + s"${File.separator}site"),
    ghpagesRepository := file(target.value.toString + s"${File.separator}gh"),
    siteSubdirName in SiteScaladoc := {
      if (biopetIsTool.value) {
        if (isSnapshot.value) {
          s"develop${File.separator}api"
        } else s"${version.value}${File.separator}api"
      } else {
        if (isSnapshot.value) {
          "develop"
        } else s"${version.value}"
      }
    },
    laikaRawContent in LaikaSite := true, //Laika use raw HTML content in markdown.
    includeFilter in ghpagesCleanSite := biopetCleanSiteFilter.value,
    biopetGenerateDocs := biopetGenerateDocsFunction().value,
    biopetGenerateReadme := biopetGenerateReadmeFunction().value,
    makeSite := (makeSite triggeredBy biopetGenerateDocs).value,
    makeSite := (makeSite dependsOn biopetGenerateDocs).value,
    ghpagesPushSite := (ghpagesPushSite dependsOn makeSite).value
  )
  /*
   * Accesses the tools main method to generate documentation using our custom built-in documentation function
   */
  protected def biopetGenerateDocsFunction(): Def.Initialize[Task[Unit]] =
    Def.taskDyn {
      if (biopetIsTool.value) {
        Def
          .task[Unit] {
            val r = (runner in Compile).value
            val classPath = (fullClasspath in Compile).value

            val streamsLogValue = streams.value.log

            val args = Seq("--generateDocs",
                           s"outputDir=${biopetDocsDir.value.toString}," +
                             s"version=${version.value}," +
                             s"release=${!isSnapshot.value}",
                           version.value)

            val mainClassString = (mainClass in assembly).value match {
              case Some(x) => x
              case _ =>
                throw new IllegalStateException(
                  "Mainclass should be defined for a tool.")
            }
            import Attributed.data
            r.run(
              mainClassString,
              data(classPath),
              args,
              streamsLogValue
            )

          }
          .dependsOn(compile in Compile)
      } else
        Def.task[Unit] {
          biopetDocsDir.value.mkdirs()
          if (!isSnapshot.value) {
            val htmlRedirectFile: sbt.File =
              new File(biopetDocsDir.value, "index.html")
            htmlRedirector(
              outputFile = htmlRedirectFile,
              link = s"${version.value}${File.separator}index.html",
              title = "API documentation",
              redirectText = "Go to the API documentation")
          }
        }
    }

  /*
   * Accesses the tools main method to generate a README using our custom built-in documentation function
   */
  protected def biopetGenerateReadmeFunction(): Def.Initialize[Task[Unit]] =
    Def.taskDyn {
      if (biopetIsTool.value) {
        Def
          .task[Unit] {
            val r = (runner in Compile).value
            val classPath = (fullClasspath in Compile).value

            val args = Seq("--generateReadme", biopetReadmePath.value.toString)

            val streamsLogValue = streams.value.log

            val mainClassString = (mainClass in assembly).value match {
              case Some(x) => x
              case _ =>
                throw new IllegalStateException(
                  "Mainclass should be defined for a tool.")
            }
            import Attributed.data
            r.run(
              mainClassString,
              data(classPath),
              args,
              streamsLogValue
            )

          }
          .dependsOn(compile in Compile)
      } else Def.task[Unit] {}
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
        def accept(f: File): Boolean = {
          // Take the relative path, so only values within the
          // ghpagesRepository are taken into account.
          val empty: File = new File("")
          val relativePath: TagName =
            relativize(ghpagesRepository.value, f).getOrElse(empty).toString
          if (isSnapshot.value) {
            relativePath.contains("develop")
          } else {
            relativePath.contains(s"${version.value}") ||
            // Also index.html needs to deleted to point to a new version.
            f.getPath == new java.io.File(ghpagesRepository.value, "index.html").getPath
          }
        }
      }
    }
}
