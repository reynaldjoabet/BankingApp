ThisBuild / version := "0.1.0-SNAPSHOT"
val Http4s="0.23.7"
val Circe="0.14.1"
val Logback= "1.2.10"
ThisBuild / scalaVersion := "2.13.7"

lazy val root = (project in file("."))
  .settings(
    name := "BankingApp",
    libraryDependencies ++=Seq(
      "org.http4s" %% "http4s-blaze-server" % Http4s,
      "org.http4s" %% "http4s-circe" % Http4s,
      "org.http4s" %% "http4s-dsl" % Http4s,
      "org.http4s" %% "http4s-blaze-client" % Http4s % "it,test",
      "io.circe" %% "circe-generic" % Circe,
      "ch.qos.logback" % "logback-classic" % Logback,
      "com.h2database" % "h2" % "2.0.204",
      "io.getquill" %% "quill-jdbc" % "3.12.0",
    )

  )
