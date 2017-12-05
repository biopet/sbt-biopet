package nl.biopet.sbtbiopet

import sbt._

trait BiopetKeys {
  lazy val biopetDocsDir = SettingKey[File](
    "Where the markdown docs are generated that can be processed with LAIKA")
  lazy val biopetReadmePath =
    SettingKey[File]("Where the project's readme is stored")
  lazy val biopetUrlToolName =
    SettingKey[String]("The name of the tool in github URLS")
  lazy val biopetGenerateDocs = taskKey[Unit]("Generate documentation files")
  lazy val biopetGenerateReadme = taskKey[Unit]("Generate readme")
}
