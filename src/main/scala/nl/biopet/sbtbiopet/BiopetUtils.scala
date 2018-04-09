package nl.biopet.sbtbiopet



import scala.collection.mutable.ListBuffer
import util.Properties.lineSeparator
object BiopetUtils {
  /**
    * Extracts a chapter from a markdown string
    * @param markdown the input string
    * @param chapter the chapter heading. (without #)
    * @return The chapter including header and subheaders as a string.
    */
  def markdownExtractChapter(markdown: String, chapter: String, includeHeader: Boolean = true): String = {
    // Regex is anchored with ^pattern$ by default.
    // Matches any number of #'s, a whitespace (tab or space) that does not have to exist and the chapter.
    val chapterRegex = s"(#+)([\\t ]*)($chapter)".r("hashtags","whitespace","heading")
    val headingDepth = chapterRegex.findFirstMatchIn(markdown) match {
      case Some(r) => r.group("hashtags").length
      case None => throw new Exception(s"Chapter not found: $chapter")
    }

    // General matcher that returns true if the line is
    // a. a markdown chapter (starting with #)
    // b. Starting with a maximum of headingDepth hashtags.
    // If the maximum dept is exceeded (another # was found) then retuns false.
    // This way we can split the markdown at a desired depth.
    def matcher(string: String): Boolean = {
      val regex = s"^(#{1,$headingDepth})[^#]([\\t ]*)(.*)$$".r
      regex.findFirstMatchIn(string).isDefined
      }

    val text = markdown.split(lineSeparator).toList
    val chapters = splitStringList(text, matcher)
    val correctChapter = chapters.find(lines => {
      lines.headOption match {
        case Some(string) => chapterRegex.findFirstMatchIn(string).isDefined
        case _ => false
      }
    })
    correctChapter match {
      case Some(x) => {
        val drop: Int = if (includeHeader) 0 else 1
        x.drop(drop).mkString(lineSeparator)
        }
      case _ => throw new Exception("Cannot validate correct chapter after parsing. " +
        "Please contact application maintainers.")
    }
  }

  /**
    * Split a list of strings at a certain element.
    * @param stringList The list of strings
    * @param splitter Function that determines whether the list should be split at this line.
    * @return A list of lists of string.
    */
  def splitStringList(stringList:List[String], splitter: String => Boolean): List[List[String]] = {
       val buffers =stringList.foldLeft(ListBuffer[ListBuffer[String]]()) { case (result, line) =>
      if (splitter(line)) result += ListBuffer(line)
      else result.lastOption match {
        case Some(lb) => result.last += line
        case None => result += ListBuffer(line)
      }
        result
    }
    buffers.map(buffer => buffer.toList).toList
  }
}
