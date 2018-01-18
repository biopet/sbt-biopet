package nl.biopet.utils

import java.io.{File, PrintWriter}

/**
  * This object contains functions that can be used for writing markdown and html files
  * for documentation purposes.
  */
object Documentation {

  /**
    * Returns a HTML table.
    * @param headers A list of strings that will make up the header.
    * @param body A list of lists of strings. Each list of strings is a row.
    *             Rows should be of equal length to the headers.
    * @return A HTML table as a string. The table contains newlines and indentation for readability.
    */
  def htmlTable(headers: List[String], body: List[List[String]]): String = {

    // Validate that all rows have a length equal to the header
    body.foreach(
      row =>
        require(
          row.length == headers.length,
          "Number of items in each row should be equal number of items in header."))

    val table = new StringBuffer()

    table.append("<table class=\"table\">\n")
    table.append(
      headers.mkString("\t<thead>\n\t\t<tr>\n\t\t\t<th>",
                       "</th>\n\t\t\t<th>",
                       "</th>\n\t\t</tr>\n\t</thead>\n"))
    table.append("\t<tbody>\n")
    for (row <- body) {
      table.append(
        row.mkString("\t\t<tr>\n\t\t\t<td>",
                     "</td>\n\t\t\t<td>",
                     "</td>\n\t\t</tr>\n"))
    }
    table.append("\t</tbody>\n")
    table.append("</table>\n")
    table.toString.replace("\t", "  ")
  }

  /**
    * Generates a Markdown file from a list of chapters (heading, content) tuples.
    * @param contents A list of (string, string) tuples, where the first string is the title and the other the content.
    * @param outputFile The output file to which the markdown file is written.
    */
  def contentsToMarkdown(
      contents: List[(String, String)],
      outputFile: File
  ): Unit = {
    outputFile.getParentFile.mkdirs()
    val fileWriter = new PrintWriter(outputFile)
    for ((head, content) <- contents) {
      fileWriter.println(head)
      fileWriter.println()
      fileWriter.println(content)
      fileWriter.println()
    }
    fileWriter.close()
  }

  /**
    * Generates a htmlPage that redirects automatically to the link provided.
    * @param outputFile The file that will contain the redirect, for example: some_dir/index.html
    * @param link The file to redirect to, for example: ../index.html
    * @param title The title of the page.
    * @param redirectText If javascript does not work, this link text is displayed.
    */
  def htmlRedirector(
      outputFile: File,
      link: String,
      title: String = "Project Documentation",
      redirectText: String = "Go to the project documentation"
  ): Unit = {
    val fileWriter = new PrintWriter(outputFile)
    val redirectHtml: String =
      s"""<!DOCTYPE html>
         |<html lang="en">
         |<head>
         |    <meta charset="UTF-8">
         |    <title>${title}</title>
         |    <script language="JavaScript">
         |        <!--
         |        function doRedirect()
         |        {
         |            window.location.replace("${link}");
         |        }
         |        doRedirect();
         |        //-->
         |    </script>
         |</head>
         |<body>
         |<a href="${link}">${redirectText}
         |</a>
         |</body>
         |</html>
       """.stripMargin
    fileWriter.print(redirectHtml)
    fileWriter.close()
  }
}
