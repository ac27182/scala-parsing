import io.circe._, io.circe.generic.auto._, io.circe.parser._,
io.circe.syntax._, io.circe.generic.semiauto._

import cats.effect.IOApp
import cats.effect.{ExitCode, IO}

import cats.data._
import cats.data.Validated._
import cats.implicits._

import cats._
import cats.instances.future._
import cats.syntax.either._
// import cats.syntax.cartesian._
import cats.instances.list._
import cats.syntax.traverse._
import Experiment3.FormValidatorNec
import eu.timepit.refined.numeric.Interval
import eu.timepit.refined.collection.Size
import eu.timepit.refined.api.RefType
import eu.timepit.refined.boolean.AllOf
import shapeless.{HNil, ::}
import eu.timepit.refined.boolean.Not
import eu.timepit.refined.collection.Tail
import eu.timepit.refined.boolean.Or
import eu.timepit.refined.boolean.And

object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    IO(println("> operational")) as ExitCode.Success
}

object Experiment1 {
  sealed trait Weekday                  extends Product with Serializable
  final case class Monday(day: String)  extends Weekday
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
  final case class Bar(xs: Vector[String])        extends Foo
  final case class Qux(i: Int, d: Option[Double]) extends Foo

  // val foo: Foo = Qux(13, None)
  // val json = foo.asJson.noSpaces
  // val decodedFoo = decode[Foo](json)

}

// validation
// using cats documentation
object Experiment3 {
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

//
object Experiment4 {

  case class Contact(phone: String, formattedPhone: String) {
    require(phone.length == 11, "phone number must have a length of 11")
  }
  // object Contact {
  //   def apply(phone: String, formattedPhone: String): Option[Contact] =
  //     if (phone.length < 11) {
  //       None
  //     } else {
  //       Some(new Contact(phone, formattedPhone))
  //     }
  // }
  // val contact = Contact("86767", "cccc")

  case class GeoLocation(lat: String, lng: String)
  case class Address(
      street: String,
      number: String,
      postalCode: String,
      county: Option[String],
      country: String
  )
  case class Listing(
      id: Option[String],
      contact: Contact,
      address: Address,
      location: GeoLocation
  )
  case class Property(listing: Listing)

  // val contact = Contact("123456", "ssss")

  // meaningful names
  class Name private (val name: String) extends AnyVal
  object Name {
    def apply(name: String): Option[Name] =
      if (name.nonEmpty) Some(new Name(name)) else None
  }

  // val name = Name("alex")

}

object Experiment5 {

  sealed trait Weekday extends Product with Serializable
  object Weekday {
    final case object Monday    extends Weekday
    final case object Tuesday   extends Weekday
    final case object Wednesday extends Weekday
    final case object Thursday  extends Weekday
    final case object Friday    extends Weekday
    final case object Saturday  extends Weekday
    final case object Sunday    extends Weekday

    implicit val weekdayDecoder: Decoder[Weekday] = Decoder[String].emap {
      case "monday"  => Right(Monday)
      case "tuesday" => Right(Tuesday)
      case e         => Left(e)
    }

  }

  val json = """
    
      {
        "firstName": "alex",
        "lastName": "cameron",
        "username": "alexcameron6969",
        "password": "ab9972975196e02hidugu",
        "email": "a.cameron177@gmail.com",
        "dob": "07/07/1997",
        "address": {
          "postcode": "bl52sw",
          "addressLine1": "14 brambling drive",
          "addressLine2": "westhoughton",
          "addressLine3": "bolton"
        }
      }
    
  """

  sealed trait UserDetails                    extends Product with Serializable
  final case class Name(name: String)         extends UserDetails
  final case class Username(username: String) extends UserDetails
  final case class Password(password: String) extends UserDetails
  final case class Email(email: String)       extends UserDetails
  // object UserDetails {
  //   implicit val weekdayDecoder: Decoder[UserDetails] = Decoder[String].
  // }

  sealed trait AddressDetails                 extends Product with Serializable
  final case class Postcode(postcode: String) extends AddressDetails
  final case class AddressLine(s: String)     extends AddressDetails

  final case class Address(
      postcode: Postcode,
      addressLine1: AddressLine,
      addressLine2: AddressLine,
      addressLine3: AddressLine
  )

  final case class User(
      firstName: Name,
      lastName: Name,
      userName: Username,
      password: Password,
      email: Email,
      dob: String,
      address: Address
  )

  val address = Address(
    Postcode("aaaaaa"),
    AddressLine("aaaaaa"),
    AddressLine("aaaaaa"),
    AddressLine("aaaaaa")
  )

  val user = User(
    Name("aa"),
    Name("aa"),
    Username("alexcameron69"),
    Password("aaaaaa"),
    Email("aaaaaa"),
    "aaaaa",
    address
  )
  val d = decode[User](json)
}

import eu.timepit.refined.api.Refined
import eu.timepit.refined._
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.auto._
import eu.timepit.refined.types.numeric._
import eu.timepit.refined.numeric.Positive
import eu.timepit.refined.string.MatchesRegex
import eu.timepit.refined.string.StartsWith
import eu.timepit.refined.types.numeric._
import eu.timepit.refined.collection.MaxSize

// refinements
object Experiment6 {

  type TwitterHandle =
    String Refined AllOf[
      StartsWith[W.`"@"`.T] ::
        MaxSize[W.`16`.T] ::
        Not[MatchesRegex[W.`"(?i:.*twitter.*)"`.T]] ::
        Not[MatchesRegex[W.`"(?i:.*admin.*)"`.T]] ::
        HNil
    ]

  //** xyz */
  type Name = Refined[String, NonEmpty]

  final case class Developer(name: Name, twitterHandle: TwitterHandle)

  val x: Developer                   = Developer("ss", "@hello")
  val y: Refined[Int, Positive]      = 1
  val z: Refined[String, MaxSize[5]] = "aaaaa"

}

object Experiment7 {
  final case class Rules(attribute: Attribute)

  sealed trait Attribute
  final case class AttributeList(values: List[String]) extends Attribute
  final case class AttributeString(values: String)     extends Attribute

  object Rules {
    implicit val encodeAttributeList: ObjectEncoder[AttributeList] =
      deriveEncoder
    implicit val decodeAttributeList: Decoder[AttributeList] = deriveDecoder

    implicit val encodeAttributeString: ObjectEncoder[AttributeString] =
      deriveEncoder
    implicit val decodeAttributeString: Decoder[AttributeString] = deriveDecoder

    implicit val encodeAttribute: Encoder[Attribute] = ObjectEncoder.instance {
      case l @ AttributeList(_) =>
        l.asJsonObject.add("type", "attributeList".asJson)
      case s @ AttributeString(_) =>
        s.asJsonObject.add("type", "attributeString".asJson)
    }

    implicit val decodeAttribute: Decoder[Attribute] = for {
      visitorType <- Decoder[String].prepare(_.downField("type"))
      value <- visitorType match {
        case "attributeList"   => Decoder[AttributeList]
        case "attributeString" => Decoder[AttributeString]
        case other             => Decoder.failedWithMessage(s"invalid type: $other")
      }
    } yield value

  }

  val json1 = """
    {
      "attribute" : "abcdefg"
    }
  """
}

object Experiment8 {
  sealed trait TestEvent
  case class MyEvent1(field: String) extends TestEvent
  case class MyEvent2(field: Int)    extends TestEvent

  val json1 = """
    {
      "type" : "e1",
      "field" : "xyz"
    }
  """

  val json2 = """
    {
      "type" : "e2",
      "field" : 123
    }
  """

  val e1 = MyEvent1("xyz")
  val e2 = MyEvent2(123)

  implicit val decodeTestEvent: Decoder[TestEvent] = for {
    eventType <- Decoder[String].prepare(_.downField("type"))
    value <- eventType match {
      case "e1"  => Decoder[MyEvent1]
      case "e2"  => Decoder[MyEvent2]
      case other => Decoder.failedWithMessage(s"invalid type: $other")
    }
  } yield value

  implicit val decodeMyEvent1: Decoder[MyEvent1] = deriveDecoder
  implicit val decodeMyEvent2: Decoder[MyEvent2] = deriveDecoder

  implicit val encodeMyEvent1: Encoder[MyEvent1] =
    _.asJsonObject.add("type", "e1".asJson).asJson

  implicit val encodeMyEvent2: Encoder[MyEvent2] =
    _.asJsonObject.add("type", "e2".asJson).asJson

  implicit val encodeTestEvent: Encoder[TestEvent] = ObjectEncoder.instance {
    case e1 @ MyEvent1(_) => {
      println("xxx")
      e1.asJsonObject.add("type", "e1".asJson)
    }
    case e2 @ MyEvent2(_) => e2.asJsonObject.add("type", "e2".asJson)
  }
}
