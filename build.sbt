scalaVersion := "2.13.1"
resolvers += Resolver.sonatypeRepo("snapshots")

val circeVersion  = "0.12.0"
val http4sVersion = "0.21.0-SNAPSHOT"

val miscellaneous = Seq(
  "io.taig"       %% "circe-validation" % "0.2.0",
  "org.typelevel" %% "cats-effect"      % "2.0.0",
  "eu.timepit"    %% "refined"          % "0.9.10",
  "com.chuusai"   %% "shapeless"        % "2.3.3"
)
val circe = Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser",
  "io.circe" %% "circe-optics"
).map(_ % circeVersion)

val http4s = Seq(
  "org.http4s" %% "http4s-dsl",
  "org.http4s" %% "http4s-blaze-server",
  "org.http4s" %% "http4s-blaze-client",
  "org.http4s" %% "http4s-dsl"
).map(_ % http4sVersion)

libraryDependencies ++= circe ++ http4s ++ miscellaneous

scalacOptions ++= Seq(
  "-Ymacro-annotations"
)
