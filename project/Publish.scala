package akka

import java.io.File

import sbt.Keys._
import sbt._

object Publish extends AutoPlugin {

    val defaultPublishTo = settingKey[File]("Default publish directory")

    override def trigger = allRequirements
    override def requires = sbtrelease.ReleasePlugin

    override lazy val projectSettings = Seq(
        crossPaths := false,
        pomExtra := akkaPomExtra,
        publishTo := {
            val corporateRepo = "http://toucan.simplesys.lan/"
            if (isSnapshot.value)
                Some("snapshots" at corporateRepo + "artifactory/libs-snapshot-local")
            else
                Some("releases" at corporateRepo + "artifactory/libs-release-local")
        },
        credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
        organizationName := "Typesafe Inc.",
        organizationHomepage := Some(url("http://www.lightbend.com")),
        homepage := Some(url("https://github.com/akka/reactive-kafka")),
        publishMavenStyle := false,
//        pomIncludeRepository := { x => false },
//        defaultPublishTo := crossTarget.value / "repository",
//        releasePublishArtifactsAction := PgpKeys.publishSigned.value
    )

    def akkaPomExtra = {
        /* The scm info is automatic from the sbt-git plugin
        <scm>
          <url>git@github.com:akka/reactive-kafka.git</url>
          <connection>scm:git:git@github.com:akka/reactive-kafka.git</connection>
        </scm>
        */
        <developers>
            <developer>
                <id>contributors</id>
                <name>Contributors</name>
                <email>akka-dev@googlegroups.com</email>
                <url>https://github.com/akka/reactive-kafka/graphs/contributors</url>
            </developer>
        </developers>
    }

    private def akkaPublishTo = Def.setting {
        sonatypeRepo(version.value) orElse localRepo(defaultPublishTo.value)
    }

    private def sonatypeRepo(version: String): Option[Resolver] =
        Option(sys.props("publish.maven.central")) filter (_.toLowerCase == "true") map { _ =>
            val nexus = "https://oss.sonatype.org/"
            if (version endsWith "-SNAPSHOT") "snapshots" at nexus + "content/repositories/snapshots"
            else "releases" at nexus + "service/local/staging/deploy/maven2"
        }

    private def localRepo(repository: File) =
        Some(Resolver.file("Default Local Repository", repository))

    private def akkaCredentials: Seq[Credentials] =
        Option(System.getProperty("akka.publish.credentials", null)).map(f => Credentials(new File(f))).toSeq

}
