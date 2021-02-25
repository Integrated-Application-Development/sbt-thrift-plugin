lazy val root = (project in file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    sbtPlugin := true,
    organization := "com.intenthq.sbt",
    name := "sbt-thrift-plugin",
    version := "1.1.1-SNAPSHOT",
    scalaVersion := "2.12.8",
    scalacOptions ++= Seq("-deprecation", "-feature"),

    // Settings for running SBT scripted tests for this plugin.
    // See https://www.scala-sbt.org/1.x/docs/Testing-sbt-plugins.html for more
    // information.
    scriptedLaunchOpts := { scriptedLaunchOpts.value ++
      Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
    },
    publishMavenStyle := false
  )
