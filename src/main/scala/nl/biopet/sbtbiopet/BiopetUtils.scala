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
  def markdownExtractChapter(markdown: String, chapter: String): String = {
    // Regex is anchored with ^pattern$ by default.
    // Matches any number of #'s, a whitespace (tab or space) that does not have to exist and the chapter.
    val chapterRegex = s"(#+)([\\t ]*)($chapter)".r("hashtags","whitespace","heading")
    val headingDepth = chapterRegex.findFirstMatchIn(markdown) match {
      case Some(r) => r.group("hashtags").length
      case None => throw new Exception("Chapter not found")
    }
    println(headingDepth)

    // General matcher that returns true if the line is
    // a. a markdown chapter (starting with #)
    // b. Starting with a maximum of headingDepth hashtags.
    // If the maximum dept is exceeded (another # was found) then retuns false.
    // This way we can split the markdown at a desired depth.
    def matcher(string: String): Boolean = {
      val regex = s"^(#{1,$headingDepth})[^#]([\\t ]*)(.*)$$".r
      regex.findFirstMatchIn(string) match {
        case Some(r) => true
        case _ => false
      }
    }

    val text = markdown.split(lineSeparator).toList
    println(text)
    val chapters = splitStringList(text, matcher)
    println(chapters)
    val correctChapter = chapters.find(lines => {
      chapterRegex.findFirstMatchIn(lines.mkString) match {
        case Some(r) => true
        case _ => false
      }
    })
    correctChapter match {
      case Some(x) => x.mkString(lineSeparator)
      case _ => throw new Exception()
    }
  }

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
