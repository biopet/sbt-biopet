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
    val chapterRegex = s"^(#+)([\\t ]*)($chapter)\\n".r("hashtags","whitespace","heading")
    val headingDepth = chapterRegex.findFirstMatchIn(markdown) match {
      case Some(r) => r.group("hashtags").length
      case None => throw new Exception("Chapter not found")
    }
    val regex = s"(^#{1,$headingDepth})([\\t ]*)(.*)\\n".r
    def matcher(string: String): Boolean = {
      regex.findFirstMatchIn(string) match {
        case Some(r) => true
        case _ => false
      }
    }
    val chapters = splitStringList(text, matcher)
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
      else result.lastOption.getOrElse(ListBuffer()) += line
        result
    }
    buffers.map(buffer => buffer.toList).toList
  }
}
