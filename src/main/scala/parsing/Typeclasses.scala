import scala.annotation.tailrec
import monocle.std.byte
object Typeclasses extends App {

  val s0: String    = "aaabbbccc"
  val s1: String    = "aaabbb"
  val s2: String    = "aaa"
  val s3: String    = "alex cameron"
  val s4: String    = "cameron alex"
  val l0: List[Int] = List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10) map (_ + 1)

  def remove0(s: String, c: String = ""): String =
    c match {
      case c if c == "" => s
      case _            => s.replaceAll(c, "")
    }

  // type class definition
  trait Show[A] {
    def show(a: A, b: A): String
  }

  // type class interface
  object Show {
    // implicit val intCanShow: Show[Int] =
    //   (int1, int2) => s"$int1 and $int2"

    implicit val stringCanShow: Show[String] =
      (str1, str2) => s"$str1 and $str2"

    object ops {

      // context bound syntax
      // the below two statements are eqivilent

      // implicit class ShowOps[A: Show](a: A) {
      //   def show(b: A) = Show[A].show(a, b)
      // }
      // def show[A: Show](a: A, b: A) = Show[A].show(a, b)

      implicit class ShowOps[A](a: A)(implicit sh: Show[A]) {
        def show(b: A) = sh.show(a, b)
      }
      def show[A](a: A, b: A)(implicit sh: Show[A]) = sh.show(a, b)

    }

    // def apply[A](implicit sh: Show[A]): Show[A] = sh
    def apply[A](implicit sh: Show[A]): Show[A] = sh

  }

  // import Show.
  import Show.ops._

  case class S3Path(path: String)

  trait RulesParser[A] {
    def toRuleSet(s: A): String
  }
  object RulesParser {
    object Ops {

      implicit class RulesParserOps[A](a: A)(
          implicit rp: RulesParser[A]
      ) {
        def toRuleSet = rp.toRuleSet(a)
      }
      // implicit class RulesParserOps[A: RulesParser](a: A) {
      //   def toRuleSet = RulesParser[A].toRuleSet(a)
      // }

      def toRuleSet[A: RulesParser](a: A) = RulesParser[A].toRuleSet(a)
    }

    implicit val stringToRuleset: RulesParser[String] = (string: String) =>
      s"$string -> ruleset: mission complete\n"
    // implicit val stringToRuleset = (s: String) =>
    //   s"$s -> ruleset: mission complete\n"

    implicit val byteArrayToRuleset: RulesParser[Array[Byte]] =
      (byteArray: Array[Byte]) =>
        s"${byteArray.mkString} -> ruleset: mission complete\n"

    implicit val s3PathToRuleset: RulesParser[S3Path] =
      (s3path: S3Path) =>
        s"${s3path.path} -> ruleset:\n -> aws instance\nmission complete\n"

    def apply[A](implicit rp: RulesParser[A]): RulesParser[A] = rp
  }
  import RulesParser.Ops.RulesParserOps

  val ev1 = "hello world".toRuleSet
  println(ev1)

  val ev2 = "hello world".getBytes.toRuleSet
  println(ev2)

  val ev3 = S3Path("/bin/x/y/z").toRuleSet _
  println(ev3)

}
