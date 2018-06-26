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

import nl.biopet.sbtbiopet.BiopetPlugin.autoImport.{
  biopetIsTool,
  biopetReleaseInBioconda,
  biopetReleaseInSonatype
}
import sbt.Def
import sbt.Keys.name
import sbtrelease.ReleasePlugin.autoImport.{ReleaseStep, releaseStepCommand}
import sbtrelease.ReleaseStateTransformations._

object BiopetReleaseSettings {
  protected def biopetReleaseStepsStart: Def.Initialize[Seq[ReleaseStep]] = {
    Def.setting[Seq[ReleaseStep]] {
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
        pushChanges
      )
    }
  }

  protected def biopetReleaseStepsSonatype: Def.Initialize[Seq[ReleaseStep]] = {
    Def.setting[Seq[ReleaseStep]] {
      Seq[ReleaseStep](
        releaseStepCommand(s"sonatypeOpen ${name.value}"),
        releaseStepCommand("publishSigned"),
        releaseStepCommand("sonatypeReleaseAll")
      )
    }
  }

  protected def biopetReleaseStepsGithub: Def.Initialize[Seq[ReleaseStep]] = {
    Def.setting[Seq[ReleaseStep]] {
      Seq[ReleaseStep](
        releaseStepCommand("ghpagesPushSite"),
        releaseStepCommand("githubRelease")
      )
    }
  }

  protected def biopetReleaseStepsBioconda: Def.Initialize[Seq[ReleaseStep]] = {
    Def.setting[Seq[ReleaseStep]] {
      Seq[ReleaseStep](
        releaseStepCommand(
          "set biocondaVersion := releaseTagName.value.stripPrefix(\"v\")"), //Dynamically gets the version in the release process.
        releaseStepCommand("biocondaRelease")
      )
    }
  }

  protected def biopetReleaseStepsAssembly: Def.Initialize[Seq[ReleaseStep]] = {
    Def.setting[Seq[ReleaseStep]] {
      Seq[ReleaseStep](
        releaseStepCommand("set test in assembly := {}"),
        releaseStepCommand("assembly")
      )
    }
  }
  protected def biopetReleaseStepsNextVersion
    : Def.Initialize[Seq[ReleaseStep]] = {
    Def.setting[Seq[ReleaseStep]] {
      Seq[ReleaseStep](
        releaseStepCommand("git checkout develop"),
        releaseStepCommand("git merge master"),
        setNextVersion,
        commitNextVersion,
        pushChanges
      )
    }
  }
  /*
   * The ReleaseProcess for use with the sbt-release plugin
   */
  def biopetReleaseProcess: Def.Initialize[Seq[ReleaseStep]] = {
    Def.setting[Seq[ReleaseStep]] {
      biopetReleaseStepsStart.value ++ {
        if (biopetIsTool.value) biopetReleaseStepsAssembly.value else Seq()
      } ++ {
        if (biopetReleaseInSonatype.value) biopetReleaseStepsSonatype.value
        else Seq()
      } ++
        biopetReleaseStepsGithub.value ++ {
        if (biopetReleaseInBioconda.value) biopetReleaseStepsBioconda.value
        else Seq()
      } ++
        biopetReleaseStepsNextVersion.value
    }
  }
}
