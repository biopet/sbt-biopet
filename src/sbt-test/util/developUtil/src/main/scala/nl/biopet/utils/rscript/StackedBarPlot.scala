/**
  * Biopet is built on top of GATK Queue for building bioinformatic
  * pipelines. It is mainly intended to support LUMC SHARK cluster which is running
  * SGE. But other types of HPC that are supported by GATK Queue (such as PBS)
  * should also be able to execute Biopet tools and pipelines.
  *
  * Copyright 2014 Sequencing Analysis Support Core - Leiden University Medical Center
  *
  * Contact us at: sasc@lumc.nl
  *
  * A dual licensing mode is applied. The source code within this project is freely available for non-commercial use under an AGPL
  * license; For commercial users or users who do not want to follow the AGPL
  * license, please contact us to obtain a separate license.
  */
package nl.biopet.utils.rscript

import java.io.File

/**
  * Extension for en general stackedbar plot with R
  *
  * Created by pjvan_thof on 4/29/15.
  */
case class StackedBarPlot(input: File,
                          output: File,
                          width: Int = 1200,
                          height: Int = 1200,
                          xlabel: Option[String] = None,
                          ylabel: Option[String] = None,
                          llabel: Option[String] = None,
                          title: Option[String] = None)
    extends Rscript {
  protected def scriptPath: String = "stackedBar.R"

  override def cmd: Seq[String] =
    super.cmd ++
      Seq("--input", input.getAbsolutePath) ++
      Seq("--output", output.getAbsolutePath) ++
      Seq("--width", width.toString) ++
      Seq("--height", height.toString) ++
      xlabel.map(Seq("--xlabel", _)).getOrElse(Seq()) ++
      ylabel.map(Seq("--ylabel", _)).getOrElse(Seq()) ++
      llabel.map(Seq("--llabel", _)).getOrElse(Seq()) ++
      title.map(Seq("--title", _)).getOrElse(Seq())
}
