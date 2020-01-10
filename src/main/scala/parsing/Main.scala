import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
import cats.effect.IOApp
import cats.implicits._
import cats.effect.{ExitCode, IO}
import cats.data.Validated
import cats.data.ValidatedNel
object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    IO(println("> App Running")) as ExitCode.Success
}

// ...basic experiment
object Experiment1 {
  sealed trait Weekday extends Product with Serializable
  final case class Monday(day: String) extends Weekday
  final case class Tuesday(day: String) extends Weekday

  val json = """
  {
    "event1" : "monday"
    "event2" : "wednesday"
  }
  """

}
// circe documentation
object Experiment2 {
  sealed trait Foo
  final case class Bar(xs: Vector[String]) extends Foo
  final case class Qux(i: Int, d: Option[Double]) extends Foo

  // val foo: Foo = Qux(13, None)
  // val json = foo.asJson.noSpaces
  // val decodedFoo = decode[Foo](json)

}

// validation
// using cats documentation
object Experiment3 {}
