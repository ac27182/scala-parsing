scalaVersion := "2.13.1"

val circeVersion  = "0.12.0"
val http4sVersion = "0.21.0-SNAPSHOT"

// val circeDependencies = Seq(
//   "io.circe" %% "circe-core",
//   "io.circe" %% "circe-generic",
//   "io.circe" %% "circe-parser",
//   "io.circe" %% "circe-optics"
// ).map(_ % circeVersion)

// val http4sDependencies = Seq(
//   "org.http4s" %% "http4s-dsl",
//   "org.http4s" %% "http4s-blaze-server",
//   "org.http4s" %% "http4s-blaze-client"
// ).map(_ % http4sVersion)

// val miscDependencies = Seq(
//   "io.taig"       %% "circe-validation" % "0.2.0",
//   "org.typelevel" %% "cats-effect"      % "2.0.0",
//   "eu.timepit"    %% "refined"          % "0.9.10",
//   "com.chuusai"   %% "shapeless"        % "2.3.3"
// )

// libraryDependencies ++= circeDependencies ++ http4sDependencies ++ miscDependencies
resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser",
  "io.circe" %% "circe-optics"
).map(_ % circeVersion) ++ Seq(
  "org.http4s" %% "http4s-dsl",
  "org.http4s" %% "http4s-blaze-server",
  "org.http4s" %% "http4s-blaze-client",
  "org.http4s" %% "http4s-dsl"
).map(_           % http4sVersion) ++ Seq(
  "io.taig"       %% "circe-validation" % "0.2.0",
  "org.typelevel" %% "cats-effect" % "2.0.0",
  "eu.timepit"    %% "refined" % "0.9.10",
  "com.chuusai"   %% "shapeless" % "2.3.3"
)

scalacOptions ++= Seq(
  "-Ymacro-annotations"
)
