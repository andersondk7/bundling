name          := "bundling"
organization  := "org.aea"
version       := "0.0.1"
scalaVersion  := "2.11.8"
scalacOptions := Seq("-unchecked", "-feature", "-deprecation", "-encoding", "utf8")


libraryDependencies += "org.scalatest"     %% "scalatest"       % "3.0.0"       % "test"

lazy val bundle = project.in(file("."))


