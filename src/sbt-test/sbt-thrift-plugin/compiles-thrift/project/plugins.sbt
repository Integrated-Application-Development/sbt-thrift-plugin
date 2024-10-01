sys.props.get("plugin.version") match {
  case Some(x) =>
    addSbtPlugin("au.com.integradev.sbt" % "sbt-thrift-plugin" % x)
  case _ =>
    addSbtPlugin("au.com.integradev.sbt" % "sbt-thrift-plugin" % "2.0.0")
  // sys.error("""|The system property 'plugin.version' is not defined.
  //                        |Specify this property using the scriptedLaunchOpts -D.""".stripMargin)
}
