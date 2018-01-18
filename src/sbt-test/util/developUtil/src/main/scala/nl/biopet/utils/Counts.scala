package nl.biopet.utils

import java.io.{File, PrintWriter}

import scala.collection.mutable

/**
  * Created by pjvanthof on 05/07/16.
  */
class Counts[T](_counts: Map[T, Long] = Map[T, Long]())(
    implicit ord: Ordering[T])
    extends Serializable {
  protected[Counts] val counts: mutable.Map[T, Long] = mutable.Map() ++ _counts

  /** Returns histogram as map */
  def countsMap: Map[T, Long] = counts.toMap

  /** Returns value if it does exist */
  def get(key: T): Option[Long] = counts.get(key)

  /** This will add an other histogram to `this` */
  def +=(other: Counts[T]): Counts[T] = {
    other.counts.foreach(x =>
      this.counts += x._1 -> (this.counts.getOrElse(x._1, 0L) + x._2))
    this
  }

  /** With this a value can be added to the histogram */
  def add(value: T): Unit = {
    counts += value -> (counts.getOrElse(value, 0L) + 1)
  }

  /** Write histogram to a tsv/count file */
  def writeHistogramToTsv(file: File): Unit = {
    val writer = new PrintWriter(file)
    writer.println("value\tcount")
    counts.keys.toList.sorted.foreach(x => writer.println(s"$x\t${counts(x)}"))
    writer.close()
  }

  def toSummaryMap: Map[String, List[Any]] = {
    val values = counts.keySet.toList.sortWith(sortAnyAny)
    Map("values" -> values, "counts" -> values.map(counts(_)))
  }

  override def equals(other: Any): Boolean = {
    other match {
      case c: Counts[T] => this.counts == c.counts
      case _ => false
    }
  }

  /** Returns acumolated counts */
  def acumolateCounts(reverse: Boolean = false): Map[T, Long] = {
    val map = countsMap
    val keys = map.keys.toList.sorted
    var total = 0L
    (for (key <- if (reverse) keys.reverse else keys) yield {
      if (map(key) != 0L) {
        total += map(key)
        Some(key -> total)
      } else None
    }).flatten.toMap
  }
}

object Counts {

  /** This will write multiple counts into a single file */
  def writeMultipleCounts[T](
      countMap: Map[String, Counts[T]],
      outputFile: File,
      headerPrefix: String = "Sample",
      acumolate: Boolean = false,
      reverse: Boolean = false)(implicit ord: Ordering[T]): Unit = {
    val writer = new PrintWriter(outputFile)
    writer.println(countMap.keys.mkString(s"$headerPrefix\t", "\t", ""))
    val keys =
      countMap
        .foldLeft(Set[T]())((a, b) => a ++ b._2.counts.keys)
        .toList
        .sorted
    val counts = if (acumolate) {
      countMap.map(x => x._1 -> x._2.acumolateCounts(reverse))
    } else countMap.map(x => x._1 -> x._2.countsMap)
    for (value <- if (reverse) keys.reverse else keys) {
      writer.println(
        countMap
          .map(s => counts(s._1).getOrElse(value, ""))
          .mkString(value + "\t", "\t", ""))
    }
    writer.close()
  }
}
