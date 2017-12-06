package nl.biopet.sbtbiopet

import sbt._

trait BiopetKeys {
  lazy val biopetDocsDir = SettingKey[File](
    "Where the markdown docs are generated that can be processed with LAIKA")
  lazy val biopetReadmePath =
    SettingKey[File]("Where the project's readme is stored")
  lazy val biopetUrlName =
    SettingKey[String]("The name of the tool or util in github URLS")
  lazy val biopetGenerateDocs = taskKey[Unit]("Generate documentation files")
  lazy val biopetGenerateReadme = taskKey[Unit]("Generate readme")
  lazy val biopetIsTool = SettingKey[Boolean]("Whether the project is a tool")
}
