import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
import cats.effect.IOApp
import cats.implicits._
import cats.effect.{ExitCode, IO}
import cats.data.Validated
import cats.data.ValidatedNel
import cats.implicits._
import Experiment3.FormValidator
object Main extends IOApp {

  val response: String = FormValidator.validateForm(
    username  = "alexcameron6969",
    password  = "password123",
    firstName = "captain",
    lastName  = "turtleneck",
    age       = 69
  ) match {
    case Left(error) => error.errorMessage
    case Right(data) => data.toString
  }

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

  sealed trait FormValidator {
    def validateUserName(userName: String): Either[DomainValidation, String] =
      Either.cond(
        userName.matches("^[a-zA-Z0-9]+$"),
        userName,
        UsernameHasSpecialCharacters
      )

    def validatePassword(password: String): Either[DomainValidation, String] =
      Either.cond(
        password.matches(
          "(?=^.{10,}$)((?=.*\\d)|(?=.*\\W+))(?![.\\n])(?=.*[A-Z])(?=.*[a-z]).*$"
        ),
        password,
        PasswordDoesNotMeetCriteria
      )

    def validateFirstName(firstName: String): Either[DomainValidation, String] =
      Either.cond(
        firstName.matches("^[a-zA-Z]+$"),
        firstName,
        FirstNameHasSpecialCharacters
      )

    def validateLastName(lastName: String): Either[DomainValidation, String] =
      Either.cond(
        lastName.matches("^[a-zA-Z]+$"),
        lastName,
        LastNameHasSpecialCharacters
      )

    def validateAge(age: Int): Either[DomainValidation, Int] =
      Either.cond(
        age >= 18 && age <= 75,
        age,
        AgeIsInvalid
      )

    def validateForm(
        username: String,
        password: String,
        firstName: String,
        lastName: String,
        age: Int
    ): Either[DomainValidation, RegistrationData] = {
      for {
        validatedUserName <- validateUserName(username)
        validatedPassword <- validatePassword(password)
        validatedFirstName <- validateFirstName(firstName)
        validatedLastName <- validateLastName(lastName)
        validatedAge <- validateAge(age)
      } yield RegistrationData(
        validatedUserName,
        validatedPassword,
        validatedFirstName,
        validatedLastName,
        validatedAge
      )
    }
  }
  object FormValidator extends FormValidator
}
