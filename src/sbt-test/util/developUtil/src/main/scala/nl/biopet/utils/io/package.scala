package nl.biopet.utils

import java.io._

import scala.io.Source

package object io {
  def copyFile(in: File, out: File, createDirs: Boolean = false): Unit = {
    copyStreamToFile(new FileInputStream(in), out, createDirs)
  }

  def copyStreamToFile(in: InputStream,
                       out: File,
                       createDirs: Boolean = false): Unit = {
    if (createDirs) out.getParentFile.mkdirs()
    val os = new FileOutputStream(out)

    org.apache.commons.io.IOUtils.copy(in, os)
    os.close()
    in.close()
  }

  /**
    * Converts a resource to a file
    * @param resource Which resource
    * @param outputFile The output file
    */
  def resourceToFile(resource: String, outputFile: File): Unit = {
    val source = getClass.getResourceAsStream(resource)
    copyStreamToFile(source, outputFile, createDirs = true)
  }

  def copyDir(inputDir: File, externalDir: File): Unit = {
    require(inputDir.isDirectory)
    externalDir.mkdirs()
    for (srcFile <- inputDir.listFiles) {
      if (srcFile.isDirectory)
        copyDir(new File(inputDir, srcFile.getName),
                new File(externalDir, srcFile.getName))
      else {
        val newFile = new File(externalDir, srcFile.getName)
        copyFile(srcFile, newFile)
      }
    }
  }

  /** Possible compression extensions to trim from input files. */
  val zipExtensions = Set(".gz", ".gzip", ".bzip2", ".bz", ".xz", ".zip")

  /**
    * Given a file object and a set of compression extensions, return the filename without any of the compression
    * extensions.
    *
    * Examples:
    *  - my_file.fq.gz returns "my_file.fq"
    *  - my_other_file.fastq returns "my_file.fastq"
    *
    * @param f Input file object.
    * @param exts Possible compression extensions to trim.
    * @return Filename without compression extension.
    */
  def getUncompressedFileName(f: File,
                              exts: Set[String] = zipExtensions): String =
    exts.foldLeft(f.getName) { (fname, ext) =>
      if (fname.toLowerCase.endsWith(ext)) fname.dropRight(ext.length)
      else fname
    }

  /** This return the contends of a file as a List[String] */
  def getLinesFromFile(file: File): List[String] = {
    val reader = Source.fromFile(file)
    val lines = reader.getLines().toList
    reader.close()
    lines
  }

  /** This writes a List[String] to a file */
  def writeLinesToFile(outputFile: File, lines: List[String]): Unit = {
    val writer = new PrintWriter(outputFile)
    lines.foreach(writer.println)
    writer.close()
  }
}
