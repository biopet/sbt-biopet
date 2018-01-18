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
package nl.biopet.utils

import java.io.{File, IOException, PrintWriter}

import nl.biopet.utils.rscript.LinePlot

import scala.concurrent.ExecutionContext
import scala.io.Source

class Histogram[T](_counts: Map[T, Long] = Map[T, Long]())(
    implicit ord: Numeric[T])
    extends Counts[T](_counts) {
  def aggregateStats: Map[String, Any] = {
    val values = this.counts.keys.toList
    val counts = this.counts.values.toList
    require(values.size == counts.size)
    if (values.nonEmpty) {
      val modal = values(counts.indexOf(counts.max))
      val totalCounts = counts.sum
      val mean: Double = values
        .zip(counts)
        .map(x => ord.toDouble(x._1) * x._2)
        .sum / totalCounts
      val median = values(
        values
          .zip(counts)
          .zipWithIndex
          .sortBy(_._1._1)
          .foldLeft((0L, 0)) {
            case (a, b) =>
              val total = a._1 + b._1._2
              if (total >= totalCounts / 2) (total, a._2)
              else (total, b._2)
          }
          ._2)
      Map("min" -> values.min,
          "max" -> values.max,
          "median" -> median,
          "mean" -> mean,
          "modal" -> modal)
    } else Map()
  }

  /** Write histogram to a tsv/count file */
  def writeAggregateToTsv(file: File): Unit = {
    val writer = new PrintWriter(file)
    aggregateStats.foreach(x => writer.println(x._1 + "\t" + x._2))
    writer.close()
  }

  def writeFilesAndPlot(outputDir: File,
                        prefix: String,
                        xlabel: String,
                        ylabel: String,
                        title: String)(implicit ec: ExecutionContext): Unit = {
    writeHistogramToTsv(new File(outputDir, prefix + ".histogram.tsv"))
    writeAggregateToTsv(new File(outputDir, prefix + ".stats.tsv"))
    val plot = LinePlot(new File(outputDir, prefix + ".histogram.tsv"),
                        new File(outputDir, prefix + ".histogram.png"),
                        xlabel = Some(xlabel),
                        ylabel = Some(ylabel),
                        title = Some(title))
    try {
      plot.runLocal()
    } catch {
      // If plotting fails the tools should not fail, this depens on R to be installed
      case e: IOException =>
        Logging.logger.warn(
          s"Error found while plotting ${plot.output}: ${e.getMessage}")
    }
  }
}

object Histogram {

  /** Reading a single histogram from a file */
  def fromFile[T](file: File, converter: String => T)(
      implicit ord: Numeric[T]): Histogram[T] = {
    val map = fromMultiHistogramFile(file, converter)
    require(map.nonEmpty, s"File does not contain a histogram: $file")
    require(map.size == 1, s"File has multiple histograms: $file")
    map.head._2
  }

  /** Reading Multiple histograms from a single file */
  def fromMultiHistogramFile[T](file: File, converter: String => T)(
      implicit ord: Numeric[T]): Map[String, Histogram[T]] = {
    val reader = Source.fromFile(file)
    val it = reader.getLines().map(_.split("\t"))
    val header = it.next().tail.zipWithIndex
    val values = it
      .map(x =>
        converter(x.head) -> x.tail.map(x =>
          if (x.nonEmpty) Some(x.toLong) else None))
      .toMap
    reader.close()
    (for ((name, idx) <- header)
      yield
        name -> {
          new Histogram[T](
            values.flatMap(x => x._2.lift(idx).flatten.map(x._1 -> _)))
        }).toMap
  }
}
