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

import java.io.File

import org.scalatest.Matchers
import org.scalatest.testng.TestNGSuite
import org.testng.annotations.Test

import scala.io.Source

/**
  * Created by pjvan_thof on 19-7-16.
  */
class HistogramTest extends TestNGSuite with Matchers {
  @Test
  def testValues(): Unit = {
    val data: Map[Int, Long] = Map(1 -> 1, 2 -> 2, 3 -> 3)
    val c1 = new Histogram[Int](data)
    c1.countsMap shouldBe data
    c1.get(1) shouldBe Some(1)
    c1.get(2) shouldBe Some(2)
    c1.get(3) shouldBe Some(3)
    c1.get(4) shouldBe None

    c1.add(1)
    c1.get(1) shouldBe Some(2)
    c1.add(4)
    c1.get(4) shouldBe Some(1)

    val c2 = new Counts[Int](data)
    c1 += c2

    c1.get(1) shouldBe Some(3)
    c1.get(2) shouldBe Some(4)
    c1.get(3) shouldBe Some(6)
    c1.get(4) shouldBe Some(1)
  }

  @Test
  def testEmpty(): Unit = {
    val c1 = new Histogram[Int]()
    c1.countsMap.isEmpty shouldBe true
  }

  @Test
  def testTsv(): Unit = {
    val data: Map[Int, Long] = Map(1 -> 1, 2 -> 2, 3 -> 3)
    val c1 = new Histogram[Int](data)

    val tsvFile = File.createTempFile("counts.", ".tsv")
    tsvFile.deleteOnExit()

    c1.writeHistogramToTsv(tsvFile)

    val reader = Source.fromFile(tsvFile)
    reader.getLines().toList shouldBe List("value\tcount", "1\t1", "2\t2", "3\t3")
    reader.close()
  }

  @Test
  def testAggregateStats(): Unit = {
    val data: Map[Int, Long] = Map(1 -> 1, 2 -> 2, 3 -> 3)
    val c1 = new Histogram[Int](data)
    c1.aggregateStats shouldBe Map("modal" -> 3, "mean" -> 2.3333333333333335, "min" -> 1, "max" -> 3, "median" -> 1)
  }

  @Test
  def testAggregateStatsFile(): Unit = {
    val data: Map[Int, Long] = Map(1 -> 1, 2 -> 2, 3 -> 3)
    val c1 = new Histogram[Int](data)
    val outputFile = File.createTempFile("test.", ".txt")
    outputFile.deleteOnExit()
    c1.writeAggregateToTsv(outputFile)

    Source.fromFile(outputFile).getLines().toList shouldBe List(
      "modal\t3",
      "mean\t2.3333333333333335",
      "min\t1",
      "max\t3",
      "median\t1"
    )
  }

  @Test
  def testRead(): Unit = {
    val c1 = new Histogram[Int](Map(1 -> 1, 2 -> 2, 3 -> 3))
    val outputFile = File.createTempFile("test.", ".tsv")
    outputFile.deleteOnExit()
    Counts.writeMultipleCounts(Map("c1" -> c1), outputFile)

    Histogram.fromFile(outputFile, _.toInt)

    val histograms = Histogram.fromMultiHistogramFile(outputFile, _.toInt)

    histograms("c1").countsMap shouldBe Map(1 -> 1, 2 -> 2, 3 -> 3)
    histograms.get("c2") shouldBe None
  }

  @Test
  def testReadMulti(): Unit = {
    val c1 = new Histogram[Int](Map(1 -> 1, 2 -> 2, 3 -> 3))
    val c2 = new Histogram[Int](Map(1 -> 2, 2 -> 1, 4 -> 2))
    val outputFile = File.createTempFile("test.", ".tsv")
    outputFile.deleteOnExit()
    Counts.writeMultipleCounts(Map("c1" -> c1, "c2" -> c2), outputFile)

    intercept[IllegalArgumentException] {
      Histogram.fromFile(outputFile, _.toInt)
    }.getMessage shouldBe s"requirement failed: File has multiple histograms: $outputFile"

    val histograms = Histogram.fromMultiHistogramFile(outputFile, _.toInt)

    histograms("c1").countsMap shouldBe Map(1 -> 1, 2 -> 2, 3 -> 3)
    histograms("c2").countsMap shouldBe Map(1 -> 2, 2 -> 1, 4 -> 2)
  }
}
