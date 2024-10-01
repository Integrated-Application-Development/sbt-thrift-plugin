lazy val root = (project in file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    sbtPlugin := true,
    organization := "au.com.integradev.sbt",
    name := "sbt-thrift-plugin",
    version := "2.0.0-SNAPSHOT",
    scalaVersion := "2.12.17",
    scalacOptions ++= Seq("-deprecation", "-feature"),

    // Settings for running SBT scripted tests for this plugin.
    // See https://www.scala-sbt.org/1.x/docs/Testing-sbt-plugins.html for more
    // information.
    scriptedLaunchOpts := {
      scriptedLaunchOpts.value ++ Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
    },
    publishMavenStyle := false
  )
