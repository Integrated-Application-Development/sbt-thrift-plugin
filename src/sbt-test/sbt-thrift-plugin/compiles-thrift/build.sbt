lazy val root = (project in file(".")).settings(
  version := "0.1",
  scalaVersion := "2.12.17",
  libraryDependencies ++=
    Seq(
      "org.apache.thrift" % "libthrift" % "0.17.0",
      "javax.annotation" % "javax.annotation-api" % "1.3.2"
    )
)
