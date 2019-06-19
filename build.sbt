val Http4sVersion = "0.20.1"
val Specs2Version = "4.0.0"
val H2Version = "1.4.196"
val doobieVersion = "0.7.0"
val circeVersion = "0.10.0"
val ScalaMockVersion = "4.0.0"
val ZioVersion = "1.0.0-RC8-6"

lazy val root = (project in file("."))
  .settings(
    organization := "io.github.loicdescotte",
    name := "pureWebAppSample",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.12.4",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
      "dev.zio" %% "zio" % ZioVersion,
      "dev.zio" %% "zio-interop-cats" % ZioVersion,
      "org.http4s" %% "http4s-circe" % Http4sVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "org.tpolecat" %% "doobie-core" % doobieVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "com.h2database" % "h2" % H2Version,
      "org.slf4j" % "slf4j-simple" % "1.7.26",
      "org.specs2" %% "specs2-core" % Specs2Version % Test,
      "org.scalamock" %% "scalamock" % ScalaMockVersion % Test,
    )
  )

