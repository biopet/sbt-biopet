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
    markdownExtractChapter(markdown,"Onedotone") shouldBe s"Onedotone${n}1.1${n}${n}"
  }
  @Test
  def testSplitStringList(): Unit = {
    val a = List("a","ab","bc","ac")
    splitStringList(a, x => x.startsWith("a")) shouldBe List(List("a"),List("ab","bc"), List("ac"))
    val b = List("c","c","a","ab","bc","ac")
    splitStringList(b, x => x.startsWith("a")) shouldBe List(List("c","c"),List("a"),List("ab","bc"), List("ac"))
  }
}
