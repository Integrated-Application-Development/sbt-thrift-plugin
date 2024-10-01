package au.com.integradev.sbt.plugins

import sbt.{ Def, _ }
import Keys._

import java.io.File
import scala.sys.process._

object ThriftPlugin extends AutoPlugin {

  def compileThrift(
    sourceDir: File,
    outputDir: File,
    thriftBin: String,
    language: String,
    extension: String,
    options: Seq[String],
    logger: Logger,
    cache: File,
    postProcessor: (File, Logger) => File,
    returnGeneratedFiles: Boolean
  ): Seq[File] = {
    val doIt =
      FileFunction
        .cached(cache, inStyle = FilesInfo.lastModified, outStyle = FilesInfo.lastModified) {
          files =>
            if (!outputDir.exists)
              outputDir.mkdirs
            files.foreach { schema =>
              val cmd =
                s"""$thriftBin -gen ${language +
                    options.mkString(":", ",", "")} -out $outputDir $schema"""
              logger.info(s"Compiling thrift schema with command: $cmd")
              val code = Process(cmd) ! logger
              if (code != 0) {
                sys.error(s"Thrift compiler exited with code $code")
              }
            }
            (outputDir ** s"*.$extension").get.toSet.map(postProcessor(_, logger))
        }
    val generatedFiles = doIt((sourceDir ** "*.thrift").get.toSet).toSeq
    if (returnGeneratedFiles) {
      generatedFiles
    } else {
      Seq.empty
    }
  }

  object autoImport {
    val thrift = SettingKey[String]("thrift", "thrift executable")
    val thriftSourceDir = SettingKey[File](
      "source-directory",
      "Source directory for thrift files. Defaults to src/main/thrift"
    )
    val thriftPostProcessor = SettingKey[(File, Logger) => File](
      "post-processor",
      "The function to post-process generated source files. Scoped to the language-specific generate function. Defaults to do nothing."
    )
    val thriftCompileGeneratedSources = SettingKey[Boolean](
      "compile-generated-sources",
      "Whether the generated sources are returned from the generate function to be compiled in this build. Scoped to the language-specific generate function. Defaults to do yes for java and no otherwise."
    )

    val thriftJavaEnabled = SettingKey[Boolean](
      "java-enabled",
      "java generation is enabled. Default - yes"
    )
    val thriftGenerateJava = TaskKey[Seq[File]](
      "generate-java",
      "Generate java sources from thrift files"
    )
    val thriftJavaOptions = SettingKey[Seq[String]](
      "thrift-java-options",
      "additional options for java thrift generation"
    )
    val thriftOutputDir = SettingKey[File](
      "java-output-directory",
      "Directory where the java files should be placed. Defaults to sourceManaged"
    )

    val thriftJsEnabled = SettingKey[Boolean](
      "js-enabled",
      "javascript generation is enabled. Default - no"
    )
    val thriftGenerateJs = TaskKey[Seq[File]](
      "generate-js",
      "Generate javascript sources from thrift files"
    )
    val thriftJsOptions = SettingKey[Seq[String]](
      "thrift-js-options",
      "additional options for js thrift generation"
    )
    val thriftJsOutputDir = SettingKey[File](
      "js-output-directory",
      "Directory where generated javascript files should be placed. default target/thrift-js"
    )

    val thriftRubyEnabled = SettingKey[Boolean](
      "ruby-enabled",
      "ruby generation is enabled. Default - no"
    )
    val thriftGenerateRuby = TaskKey[Seq[File]](
      "generate-ruby",
      "Generate ruby sources from thrift files."
    )
    val thriftRubyOptions = SettingKey[Seq[String]](
      "thrift-ruby-options",
      "additional options for ruby thrift generation"
    )
    val thriftRubyOutputDir = SettingKey[File](
      "ruby-output-directory",
      "Directory where generated ruby files should be placed. default target/thrift-ruby"
    )

    val thriftPythonEnabled = SettingKey[Boolean](
      "python-enabled",
      "python generation is enabled. Default - no"
    )
    val thriftGeneratePython = TaskKey[Seq[File]](
      "generate-python",
      "Generate python sources from thrift files."
    )
    val thriftPythonOptions = SettingKey[Seq[String]](
      "thrift-python-options",
      "additional options for python thrift generation"
    )
    val thriftPythonOutputDir = SettingKey[File](
      "python-output-directory",
      "Directory where generated python files should be placed. default target/thrift-python"
    )

    val thriftDelphiEnabled = SettingKey[Boolean](
      "delphi-enabled",
      "delphi generation is enabled. Default - no"
    )
    val thriftGenerateDelphi = TaskKey[Seq[File]](
      "generate-delphi",
      "Generate delphi sources from thrift files."
    )
    val thriftDelphiOptions = SettingKey[Seq[String]](
      "thrift-delphi-options",
      "additional options for delphi thrift generation"
    )
    val thriftDelphiOutputDir = SettingKey[File](
      "delphi-output-directory",
      "Directory where generated delphi files should be placed. default target/thrift-delphi"
    )
  }

  import autoImport._

  override def requires = sbt.plugins.JvmPlugin
  override def trigger = allRequirements

  val Thrift = config("thrift")

  val thriftSettings: Seq[Setting[_]] =
    inConfig(Thrift)(
      Seq(
        thrift := "thrift",
        thriftSourceDir := {
          sourceDirectory.value / "main" / "thrift"
        },
        thriftPostProcessor := { (file, logger) =>
          file
        },
        thriftCompileGeneratedSources := false,
        thriftJavaEnabled := true,
        thriftJavaOptions := Seq(),
        thriftOutputDir := {
          sourceManaged.value / "main"
        },
        thriftGenerateJava / thriftCompileGeneratedSources := true,
        thriftGenerateJava :=
          (
            Def.taskDyn {
              if (thriftJavaEnabled.value)
                Def.task {
                  compileThrift(
                    thriftSourceDir.value,
                    thriftOutputDir.value,
                    thrift.value,
                    "java",
                    "java",
                    thriftJavaOptions.value,
                    streams.value.log,
                    streams.value.cacheDirectory / "thrift-java",
                    (thriftGenerateJava / thriftPostProcessor).value,
                    (thriftGenerateJava / thriftCompileGeneratedSources).value,
                  )
                }
              else
                Def.task {
                  Seq.empty[File]
                }
            }
          ).value,
        thriftJsEnabled := false,
        thriftJsOptions := Seq(),
        thriftJsOutputDir := file("target/gen-js"),
        thriftGenerateJs :=
          (
            Def.taskDyn {
              if (thriftJsEnabled.value)
                Def.task {
                  compileThrift(
                    thriftSourceDir.value,
                    thriftJsOutputDir.value,
                    thrift.value,
                    "js",
                    "js",
                    thriftJsOptions.value,
                    streams.value.log,
                    streams.value.cacheDirectory / "thrift-js",
                    (thriftGenerateJs / thriftPostProcessor).value,
                    (thriftGenerateJs / thriftCompileGeneratedSources).value
                  )
                }
              else
                Def.task {
                  Seq.empty[File]
                }
            }
          ).value,
        thriftRubyEnabled := false,
        thriftRubyOptions := Seq(),
        thriftRubyOutputDir := file("target/gen-ruby"),
        thriftGenerateRuby :=
          (
            Def.taskDyn {
              if (thriftRubyEnabled.value)
                Def.task {
                  compileThrift(
                    thriftSourceDir.value,
                    thriftRubyOutputDir.value,
                    thrift.value,
                    "rb",
                    "rb",
                    thriftRubyOptions.value,
                    streams.value.log,
                    streams.value.cacheDirectory / "thrift-rb",
                    (thriftGenerateRuby / thriftPostProcessor).value,
                    (thriftGenerateRuby / thriftCompileGeneratedSources).value
                  )
                }
              else
                Def.task {
                  Seq.empty[File]
                }
            }
          ).value,
        thriftPythonEnabled := false,
        thriftPythonOptions := Seq(),
        thriftPythonOutputDir := file("target/gen-python"),
        thriftGeneratePython :=
          (
            Def.taskDyn {
              if (thriftPythonEnabled.value)
                Def.task {
                  compileThrift(
                    thriftSourceDir.value,
                    thriftPythonOutputDir.value,
                    thrift.value,
                    "py",
                    "py",
                    thriftPythonOptions.value,
                    streams.value.log,
                    streams.value.cacheDirectory / "thrift-py",
                    (thriftGeneratePython / thriftPostProcessor).value,
                    (thriftGeneratePython / thriftCompileGeneratedSources).value
                  )
                }
              else
                Def.task {
                  Seq.empty[File]
                }
            }
          ).value,
        thriftDelphiEnabled := false,
        thriftDelphiOptions := Seq(),
        thriftDelphiOutputDir := file("target/gen-delphi"),
        thriftGenerateDelphi :=
          (
            Def.taskDyn {
              if (thriftDelphiEnabled.value)
                Def.task {
                  compileThrift(
                    thriftSourceDir.value,
                    thriftDelphiOutputDir.value,
                    thrift.value,
                    "delphi",
                    "pas",
                    thriftDelphiOptions.value,
                    streams.value.log,
                    streams.value.cacheDirectory / "thrift-delphi",
                    (thriftGenerateDelphi / thriftPostProcessor).value,
                    (thriftGenerateDelphi / thriftCompileGeneratedSources).value,
                  )
                }
              else
                Def.task {
                  Seq.empty[File]
                }
            }
          ).value,
        managedClasspath := Classpaths.managedJars(Thrift, classpathTypes.value, update.value)
      )
    ) ++
      Seq[Setting[_]](
        watchSources ++= {
          (thriftSourceDir.value ** "*").get
        },
        Compile / sourceGenerators +=
          (Thrift / thriftGenerateJava).taskValue,
        Compile / sourceGenerators +=
          (Thrift / thriftGenerateJs).taskValue,
        Compile / sourceGenerators +=
          (Thrift / thriftGenerateRuby).taskValue,
        Compile / sourceGenerators +=
          (Thrift / thriftGeneratePython).taskValue,
        Compile / sourceGenerators +=
          (Thrift / thriftGenerateDelphi).taskValue,
        ivyConfigurations += Thrift
      )

  override lazy val projectSettings: Seq[Def.Setting[_]] = thriftSettings
}
