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
    val text = markdown.split(lineSeparator).toList
    val chapterRegex = "(^#+)([\\t ]*)(.*)$".r("hashtags","whitespace","heading")
    val allChapters = chapterRegex.findAllIn(text.mkString("\n")).matchData.toList
    val chapterLine = allChapters.find(r => r.group("heading")== chapter)
    val headingDepth = chapterLine match {
      case Some(r) => r.group("hashtags").length
      case _ => throw new Exception("Chapter not found")
    }
    val regex = s"(^#{1,$headingDepth})([\\t ]*)(.*)$$".r
    def matcher(string: String): Boolean = {
      string match {
        case regex => true
        case _ => false
      }
    }
    val chapters = splitStringList(text, matcher)
    val correctChapter = chapters.find(lines => lines.headOption match {
      case Some(line) => chapterRegex.findFirstMatchIn(line) match {
        case Some(matcherObject) => matcherObject.group("heading") == chapter
        case _ => false
      }
      case _ => false
    })
    correctChapter match {
      case Some(x) => x.mkString(lineSeparator)
      case _ => throw new Exception()
    }
  }

  def splitStringList(stringList:List[String], splitter: String => Boolean): List[List[String]] = {

    val buffers =stringList.foldLeft(ListBuffer(ListBuffer[String]())) { case (result, line) =>
      if (splitter(line)) result += ListBuffer(line)
      else result.last += line
        result
    }
    buffers.map(buffer => buffer.toList).toList
  }
}
