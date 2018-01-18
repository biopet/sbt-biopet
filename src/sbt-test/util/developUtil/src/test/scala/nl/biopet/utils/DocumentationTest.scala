package nl.biopet.utils

import nl.biopet.test.BiopetTest
import org.testng.annotations.Test
import java.io.File

import scala.io.Source

class DocumentationTest extends BiopetTest {
  @Test
  def testTableMethod(): Unit = {
    the [java.lang.IllegalArgumentException] thrownBy {
      Documentation.htmlTable(
        List("Column1", "Column2"),
        List(
          List("1","2"),
          List("a","b","c")
        )
      ) } should have message "requirement failed: Number of items in each row should be equal number of items in header."
    val table: String = Documentation.htmlTable(List("Column1", "Column2"),
    List(
      List("1","2"),
      List("a","b")))
    table should contain
    """<table>
      |  <thead>
      |    <tr>
      |      <th>Column1</th>
      |      <th>Column2</th>
      |    </tr>
      |  </thead>
      |  <tbody>
      |    <tr>
      |      <td>1</td>
      |      <td>2</td>
      |    </tr>
      |    <tr>
      |      <td>a</td>
      |      <td>b</td>
      |    </tr>
      |  </tbody>
      |</table>
    """.stripMargin
  }

  @Test
  def testContentToFile(): Unit = {
    val testMd = File.createTempFile("test.",".md")
    Documentation.contentsToMarkdown(List(
    ("# Test",
      "Lorem ipsum dolor sit amet, consectetur adipiscing elit")
  ), testMd
  )
  testMd should exist
  val reader = Source.fromFile(testMd)
  reader.mkString should include ("Lorem ipsum dolor sit amet, consectetur adipiscing elit")
  }

  @Test
  def testHtmlRedirector(): Unit = {
    val testRedirect = File.createTempFile("test.", ".html")
    Documentation.htmlRedirector(
      outputFile = testRedirect,
      link = "bla/index.html",
      title = "Project X",
      redirectText = "Click here for X")

    testRedirect should exist
    val reader = Source.fromFile(testRedirect)
    val htmlPage = reader.mkString

    htmlPage should contain
    """<!DOCTYPE html>
      |<html lang="en">
      |<head>
      |    <meta charset="UTF-8">
      |    <title>Project X</title>
      |    <script language="JavaScript">
      |        <!--
      |        function doRedirect()
      |        {
      |            window.location.replace("bla/index.html");
      |        }
      |        doRedirect();
      |        //-->
      |    </script>
      |</head>
      |<body>
      |<a href="bla/index.html">Click here for X
      |</a>
      |</body>
      |</html>""".stripMargin
  }
}
