import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "play-jpa"
    val appVersion      = "1.0.1-SNAPSHOT"

    val appDependencies = Seq(
      jdbc, javaJpa,
      // Add your project dependencies here,  (<group> % <module> % <version>)
      "org.hibernate" % "hibernate-entitymanager" % "4.1.2.Final",
      "mysql" % "mysql-connector-java" % "5.1.12"

      // need following line only if using FileBasedMailboxType
      //,"com.typesafe.akka" % "akka-actor" % "2.0.1",  "com.typesafe.akka" % "akka-file-mailbox" % "2.0.1"
    )

    val main = play.Project(appName, appVersion, appDependencies).settings(
      // Add your own project settings here      
    )

}
