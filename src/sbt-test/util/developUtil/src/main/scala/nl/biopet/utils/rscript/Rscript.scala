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

import java.io.{File, FileOutputStream}

import nl.biopet.utils.Logging
import nl.biopet.utils.process.Sys

import scala.collection.mutable
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}
import scala.sys.process.ProcessLogger

/**
  * Trait for rscripts, can be used to execute rscripts locally
  *
  * Created by pjvanthof on 13/09/15.
  */
trait Rscript extends Logging {
  protected def scriptPath: String

  protected def rscriptExecutable: String = "rscript"

  if (!Rscript.alreadyCopied.contains(scriptPath)) {
    val rScript: File = File.createTempFile(scriptPath, ".R")
    rScript.deleteOnExit()

    val is = getClass.getResourceAsStream(scriptPath)
    val os = new FileOutputStream(rScript)

    org.apache.commons.io.IOUtils.copy(is, os)
    os.close()
    is.close()
    Rscript.alreadyCopied += scriptPath -> rScript
  }

  /** This is the default implementation, to add arguments override this */
  def cmd: Seq[String] =
    Seq(rscriptExecutable, Rscript.alreadyCopied(scriptPath).getAbsolutePath)

  /**
    * Execute rscript on local system
    * @param logger How to handle stdout and stderr
    */
  def runLocal(logger: ProcessLogger)(implicit ec: ExecutionContext): Unit = {
    Logging.logger.info("Running: " + cmd.mkString(" "))

    val results = Sys.execAsync(cmd)

    val (exitcode, stdout, stderr) =
      Await.result(results.map(x => (x._1, x._2, x._3)), Duration.Inf)

    Logging.logger.info("stdout:\n" + stdout + "\n")
    Logging.logger.info("stderr:\n" + stderr)

    Logging.logger.info(exitcode)
  }

  /**
    * Execute rscript on local system
    * Stdout and stderr will go to biopet logger
    */
  def runLocal()(implicit ec: ExecutionContext): Unit = {
    runLocal(ProcessLogger(Logging.logger.info(_)))
  }
}

object Rscript {
  protected val alreadyCopied: mutable.Map[String, File] = mutable.Map()
}
