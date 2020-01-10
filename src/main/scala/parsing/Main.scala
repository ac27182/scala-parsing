import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
object Main extends App {
  sealed trait Foo
  final case class Bar(xs: Vector[String]) extends Foo
  final case class Qux(i: Int, d: Option[Double]) extends Foo

  val foo: Foo = Qux(13, None)

  // comes from io.circe
  val json = foo.asJson.noSpaces
  println("> app running")
  println(json)
  val decodedFoo = decode[Foo](json)
  println(decodedFoo)

}

object Experiment1 {}

// 1. come up with some funky json
// 2. parse it
// 3. come up with some funky data types
// 4. parse some json
// AST = ABSTRACT SYNTAX TREE
// lenses?
// opetics?
