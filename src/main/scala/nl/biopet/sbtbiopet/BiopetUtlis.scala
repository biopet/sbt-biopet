package nl.biopet.sbtbiopet

import java.io.File

import scala.collection.mutable.ListBuffer
import scala.io.Source
object BiopetUtlis {
  def markdownExtractChapter(markdownFile: File, chapter: String): String = {
    val text = Source.fromFile(markdownFile).getLines().toList
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
      case Some(x) => x.mkString("\n")
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
