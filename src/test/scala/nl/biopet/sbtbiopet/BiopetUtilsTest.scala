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
    val markdown: String = Source
      .fromResource("nl/biopet/sbtbiopet/test.md")
      .getLines()
      .mkString("\n")
    val n = lineSeparator
    markdownExtractChapter(markdown, "Onedotone") shouldBe """## Onedotone
                                                            |1.1
                                                            |
                                                            |### Onedotonedotone
                                                            |1.1.1
                                                            |""".stripMargin
    markdownExtractChapter(markdown, "One") shouldBe
      """# One
        |1
        |
        |## Onedotone
        |1.1
        |
        |### Onedotonedotone
        |1.1.1
        |
        |## Onedottwo
        |1.2
        |
        |## Onedotthree
        |1.3
        |
        |## Onedotfour
        |1.4
        |""".stripMargin
    markdownExtractChapter(markdown, "Onedotonedotone") shouldBe
      """### Onedotonedotone
      |1.1.1
      |""".stripMargin
    markdownExtractChapter(markdown, "Onedotonedotone", includeHeader = false) shouldBe
      """|1.1.1
         |""".stripMargin
  }
  @Test
  def testSplitStringList(): Unit = {
    val a = List("a", "ab", "bc", "ac")
    splitStringList(a, x => x.startsWith("a")) shouldBe List(List("a"),
                                                             List("ab", "bc"),
                                                             List("ac"))
    val b = List("c", "c", "a", "ab", "bc", "ac")
    splitStringList(b, x => x.startsWith("a")) shouldBe List(List("c", "c"),
                                                             List("a"),
                                                             List("ab", "bc"),
                                                             List("ac"))
  }
}
