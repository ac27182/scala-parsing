package parsing

import cats.effect.IOApp
import cats.effect.IO
import cats.implicits._
import cats.effect.ExitCode
import org.http4s._
import org.http4s.dsl.io._
import scala.concurrent.ExecutionContext.Implicits.global
import cats.effect.ContextShift
import cats.effect.Timer

import org.http4s.server.Router
import org.http4s.server.blaze._
import org.http4s.implicits._

object Http4s extends IOApp {
  import Experiment2._

  def run(args: List[String]): IO[ExitCode] =
    headersIo1 as ExitCode.Success
}

object Experiment1 {

  // implicits
  implicit val cs: ContextShift[IO] = IO.contextShift(global)
  implicit val timer: Timer[IO]     = IO.timer(global)

  case class Tweet(id: Int, message: String)

  implicit def tweetEncoder: EntityEncoder[IO, Tweet]       = ???
  implicit def tweetsEncoder: EntityEncoder[IO, Seq[Tweet]] = ???

  def getTweet(tweetid: Int): IO[Tweet]  = ???
  def getPopularTweets(): IO[Seq[Tweet]] = ???

  // tweet service
  val tweetService =
    HttpRoutes.of[IO] {
      case GET -> Root / "tweets" / "popular" =>
        getPopularTweets().flatMap(Ok(_))
      case GET -> Root / "tweets" / IntVar(tweetid) =>
        getTweet(tweetid).flatMap(Ok(_))
    }

  // hello world service
  val helloWorldService =
    HttpRoutes.of[IO] {
      case GET -> Root / "hello" / name =>
        Ok(s"Hello")
    }

  val services =
    tweetService <+> helloWorldService

  val httpApp =
    Router(
      "/"    -> helloWorldService,
      "/api" -> services
    ).orNotFound

  val serverBuilder =
    BlazeServerBuilder[IO]
      .bindHttp(port = 8000, host = "localhost")
      .withHttpApp(httpApp)
      .resource

  val fiber =
    serverBuilder
      .use(_ => IO.never)
      .start

}

object Experiment2 {

  implicit val timer: Timer[IO] = IO.timer(global)

  val service   = HttpRoutes.of[IO] { case _ => IO(Response(Status.Ok)) }
  val getRoot   = Request[IO](Method.GET, uri"/")
  val io        = service.orNotFound.run(getRoot)
  val stdOutput = IO(println(">application operational"))
  val okIo      = Ok()
  val ok        = okIo.unsafeRunSync
  val service2 =
    HttpRoutes
      .of[IO] { case _ => Ok() }
      .orNotFound
      .run(getRoot)
      .unsafeRunSync

  val service3 =
    HttpRoutes
      .of[IO] { case _ => NoContent() }
      .orNotFound
      .run(getRoot)
      .unsafeRunSync

  val headersIo = IO(println(Ok("ok response").unsafeRunSync.headers))
  val headersIo1 = IO(
    println(
      Ok("ok response", Header("X-Auth-Token", "value")).unsafeRunSync.headers
    )
  )

}

// khleisli category
