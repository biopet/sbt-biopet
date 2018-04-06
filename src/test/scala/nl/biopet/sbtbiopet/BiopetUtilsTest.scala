package nl.biopet.sbtbiopet

import org.scalatest.Matchers
import org.scalatest.testng.TestNGSuite
import org.testng.annotations.Test
import nl.biopet.sbtbiopet.BiopetUtils._
import util.Properties.lineSeparator

import scala.io.Source

class BiopetUtilsTest extends TestNGSuite with Matchers {

  @Test
  def testMarkdownExtractChapter(): Unit = {
    val markdown: String = Source.fromResource("nl/biopet/sbtbiopet/test.md").getLines().mkString("\n")
    val n = lineSeparator
    markdownExtractChapter(markdown,".*") shouldBe s"Onedotone${n}1.1${n}${n}"
  }

}
