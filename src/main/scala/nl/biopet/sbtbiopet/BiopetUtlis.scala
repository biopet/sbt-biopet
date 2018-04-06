package nl.biopet.sbtbiopet

import java.io.File

import scala.collection.mutable.ListBuffer
import scala.io.Source
object BiopetUtlis {
  def markdownExtractChapter(markdownFile: File, chapter: String): String = {
    val text = Source.fromFile(markdownFile).getLines().toList
    val chapters = splitStringList(text,line => line.startsWith("#"))

    ""
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
