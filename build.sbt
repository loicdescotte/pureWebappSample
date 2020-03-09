val Http4sVersion = "0.21.1"
val H2Version = "1.4.196"
val DoobieVersion = "0.8.8"
val CirceVersion = "0.13.0"
val ScalaMockVersion = "4.0.0"
val ZioVersion = "1.0.0-RC18"

organization := "io.github.loicdescotte"
name := "pureWebAppSample"
version := "0.0.1-SNAPSHOT"
scalaVersion := "2.13.1"
libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
  "dev.zio" %% "zio" % ZioVersion,
  "dev.zio" %% "zio-interop-cats" % "2.0.0.0-RC11",
  "org.http4s" %% "http4s-circe" % Http4sVersion,
  "org.http4s" %% "http4s-dsl" % Http4sVersion,
  "org.tpolecat" %% "doobie-core" % DoobieVersion,
  "io.circe" %% "circe-generic" % CirceVersion,
  "com.h2database" % "h2" % H2Version,
  "dev.zio" %% "zio-logging-slf4j" % "0.2.3",
)

//tests
libraryDependencies ++= Seq(
  "dev.zio" %% "zio-test" % ZioVersion % "test",
  "dev.zio" %% "zio-test-sbt" % ZioVersion % "test",
)

testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
