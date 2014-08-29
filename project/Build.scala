import sbt._
import Keys._

object Settings {
  val buildOrganization = "templemore"
  val buildScalaVersion = "2.11.2"
  val crossBuildScalaVersions = Seq("2.9.3", "2.10.4", "2.11.2")
  val buildVersion      = "0.8.1"

  val buildSettings = Defaults.defaultSettings ++
                      Seq (organization  := buildOrganization,
                           scalaVersion  := buildScalaVersion,
                           version       := buildVersion,
                           scalacOptions ++= Seq("-deprecation", "-unchecked", "-encoding", "utf8"),
                           publishTo     := Some(Resolver.file("file",  new File("deploy-repo"))))

  def pluginScalaVersion(scalaVersion : String) =
    if (scalaVersion.startsWith("2.11")) "2.10.4"
    else scalaVersion
}

object Dependencies {

  private val CucumberVersion = "1.1.4"

  def cucumberJvm(scalaVersion: String) =
    if ( scalaVersion.startsWith("2.9") ) "info.cukes" % "cucumber-scala_2.9" % CucumberVersion % "compile"
    else if ( scalaVersion.startsWith("2.11")) "info.cukes" % "cucumber-scala_2.10" % CucumberVersion % "compile"
    else "info.cukes" %% "cucumber-scala" % CucumberVersion % "compile"

  val testInterface = "org.scala-tools.testing" % "test-interface" % "0.5" % "compile"

  def scalaXml(scalaVersion: String) =
    if (scalaVersion.startsWith("2.11")) Seq("org.scala-lang.modules" %% "scala-xml" % "1.0.2")
    else Seq()
}

object Build extends Build {
  import Dependencies._
  import Settings._

  lazy val parentProject = Project("sbt-cucumber-parent", file ("."),
    settings = buildSettings)

  lazy val pluginProject = Project("sbt-cucumber-plugin", file ("plugin"),
    settings = buildSettings ++
               Seq(
                 scalaVersion := pluginScalaVersion(buildScalaVersion),
                 crossScalaVersions := Seq.empty,
                 sbtPlugin := true,
                 libraryDependencies <++= scalaVersion { sv => scalaXml(sv) }
                 ))

  lazy val integrationProject = Project ("sbt-cucumber-integration", file ("integration"),
    settings = buildSettings ++
               Seq(crossScalaVersions := crossBuildScalaVersions,
               libraryDependencies <+= scalaVersion { sv => cucumberJvm(sv) },
               libraryDependencies += testInterface))
}

