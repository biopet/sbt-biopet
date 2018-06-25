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

import sbt._

trait BiopetKeys {
  lazy val biopetDocsDir = SettingKey[File](
    "Where the markdown docs are generated that can be processed with LAIKA")
  lazy val biopetReadmePath =
    settingKey[File]("Where the project's readme is stored")
  lazy val biopetUrlName =
    settingKey[String]("The name of the tool or util in github URLS")
  lazy val biopetGenerateDocs = taskKey[Unit]("Generate documentation files")
  lazy val biopetGenerateReadme = taskKey[Unit]("Generate readme")
  lazy val biopetIsTool = settingKey[Boolean]("Whether the project is a tool")
  lazy val biopetReleaseInSonatype =
    settingKey[Boolean]("Whether the tool should be released in sonatype")
  lazy val biopetReleaseInBioconda =
    settingKey[Boolean]("Whether the tool should be released in bioconda")
  lazy val githubOrganization =
    settingKey[String]("The organization name on github")
  lazy val biopetEnableCodacyCoverage = settingKey[Boolean](
    "Whether codacy coverage should be enabled in bopetTest")
}
