package nl.biopet.sbtbiopet

import sbt._
import com.typesafe.sbt.site.laika.LaikaSitePlugin.{laikaSettings}
import com.typesafe.sbt.site.SiteScaladocPlugin.projectSettings
trait BiopetKeys {
  lazy val biopetDocsDir = SettingKey[File]("Where the markdown docs are generated that can be processed with LAIKA")
  lazy val biopetReadmePath = SettingKey[File]("Where the project's readme is stored")
  lazy val biopetUrlToolName = SettingKey[String]("The name of the tool in github URLS")
  lazy val biopetGenerateDocs = taskKey[Unit]("Generate documentation files")
  lazy val biopetGenerateReadme = taskKey[Unit]("Generate readme")
  lazy val biopetClassPrefix = taskKey[String]("The class prefix ('nl.biopet.something')")
}
