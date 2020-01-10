scalaVersion := "2.13.1"

val circeVersion = "0.12.0"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser",
  "io.circe" %% "circe-optics"
).map(_ % circeVersion) ++ Seq(
  "io.taig" %% "circe-validation" % "0.2.0",
  "org.typelevel" %% "cats-effect" % "2.0.0"
)

scalacOptions ++= Seq(
  "-Ymacro-annotations"
)
