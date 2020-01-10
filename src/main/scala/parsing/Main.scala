import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._

import cats.effect.IOApp
import cats.effect.{ExitCode, IO}

import cats.data._
import cats.data.Validated._
import cats.implicits._

import cats._
import cats.instances.future._
import cats.syntax.either._
import cats.syntax.cartesian._
import cats.instances.list._
import cats.syntax.traverse._
import Experiment3.FormValidatorNec

object Main extends IOApp {

  val response =
    FormValidatorNec
      .validateForm(
        username  = "alexcameron6969",
        password  = "xxxxxxxxx",
        firstName = "captain",
        lastName  = "turtleneck",
        age       = 69
      )
      .toEither

  def run(args: List[String]): IO[ExitCode] =
    IO {
      println(response)
    } as ExitCode.Success
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
object Experiment3 {
  sealed abstract class Validated[+E, +A] extends Product with Serializable {
    // implementation elided
  }
  final case class Valid[+A](a: A) extends Validated[Nothing, A]
  final case class Invalid[+E](e: E) extends Validated[E, Nothing]

  final case class RegistrationData(
      username: String,
      password: String,
      firstName: String,
      lastNAme: String,
      age: Int
  )

  //error model
  sealed trait DomainValidation {
    def errorMessage: String
  }
  case object UsernameHasSpecialCharacters extends DomainValidation {
    def errorMessage: String = "username cannot have special characters"
  }
  case object PasswordDoesNotMeetCriteria extends DomainValidation {
    def errorMessage: String = "Password must at least do..."
  }
  case object FirstNameHasSpecialCharacters extends DomainValidation {
    def errorMessage: String = "first name cannot contain special characters"
  }
  case object LastNameHasSpecialCharacters extends DomainValidation {
    def errorMessage: String = "last name cannot contain special characters"
  }
  case object AgeIsInvalid extends DomainValidation {
    def errorMessage: String =
      "you must be aged 18 and not older than 75 to use our services."
  }

  sealed trait FormValidatorNec {

    type ValidationResult[A] = ValidatedNec[DomainValidation, A]

    private def validateUserName(userName: String): ValidationResult[String] =
      if (userName.matches("^[a-zA-Z0-9]+$")) userName.validNec
      else UsernameHasSpecialCharacters.invalidNec

    private def validatePassword(password: String): ValidationResult[String] =
      if (password.matches("^[a-zA-Z]+$")) password.validNec
      else PasswordDoesNotMeetCriteria.invalidNec

    private def validateFirstName(firstName: String): ValidationResult[String] =
      if (firstName.matches("^[a-zA-Z]+$")) firstName.validNec
      else FirstNameHasSpecialCharacters.invalidNec

    private def validateLastName(lastName: String): ValidationResult[String] =
      if (lastName.matches("^[a-zA-Z]+$")) lastName.validNec
      else LastNameHasSpecialCharacters.invalidNec

    private def validateAge(age: Int): ValidationResult[Int] =
      if (age >= 18 && age <= 75) age.validNec else AgeIsInvalid.invalidNec

    def validateForm(
        username: String,
        password: String,
        firstName: String,
        lastName: String,
        age: Int
    ): ValidationResult[RegistrationData] = {
      (
        validateUserName(username),
        validatePassword(password),
        validateFirstName(firstName),
        validateLastName(lastName),
        validateAge(age)
      ).mapN(RegistrationData)
    }

  }

  object FormValidatorNec extends FormValidatorNec
}
