name          := "bundling"
organization  := "org.aea"
version       := "0.0.1"
scalaVersion  := "2.11.8"
scalacOptions := Seq("-unchecked", "-feature", "-deprecation", "-encoding", "utf8")

//resolvers += Resolver.jcenterRepo

libraryDependencies ++= {
  val akkaV      = "2.4.11"
  val scalaTestV = "3.0.0"
  Seq(
    "com.typesafe.akka" %% "akka-actor"      % akkaV,
    "org.scalatest"     %% "scalatest"       % scalaTestV       % "it,test",
    "com.typesafe.akka" %% "akka-testkit"    % akkaV            % "it,test"
  )
}

lazy val bundle = project.in(file(".")).configs(IntegrationTest)
Defaults.itSettings
Revolver.settings


