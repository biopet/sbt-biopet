# sbt-biopet

This is the plugin to be used in most of biopet's projects.

It exists to import other plugins and define sbt settings which are 
shared between biopet's projects.

Plugins that are imported are:
- sbt-site
- sbt-ghpages
- laika-sbt
- sbt-assembly
- sbt-pgp
- sbt-release
- sbt-sonatype
- sbt-coveralls
- sbt-coverage
- sbt-scalafmt
- sbt-header
- sbt-update

Common settings are defined for:
- release
- Publication and generation of documentation
- Assembly
- Publication of JAR
- Formatting (scalafmt)
- License headers
