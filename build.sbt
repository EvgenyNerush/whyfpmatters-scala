ThisBuild / version := "0.1.0"

ThisBuild / scalaVersion := "2.13.9"

lazy val root = (project in file("."))
  .settings(
    name := "whyfpmatters-scala"
  )

libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.14.1" % "test"
