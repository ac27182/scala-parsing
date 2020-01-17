package parsing
import fs2._
import io.circe._, io.circe.generic.auto._, io.circe.parser._,
io.circe.syntax._, io.circe.generic.semiauto._
import fs2.io.file.readAll
import fs2.Stream._

import java.nio.file.Paths
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
// import java.util.concurrent.Executors

import cats.effect.{IO, Timer, ContextShift}
import cats.effect.Blocker
import java.nio.file.Path
import cats.Show
import java.io.PrintStream

object FS2 extends App {
  val s0: Stream[Pure, Int] = Stream(1, 2, 2)

  val repeat0 = s0.repeat.take(7).toList
  // println(repeat0)

  // convert to anny collection type, map over it
  // list like monadic instance
  // pull based?
  // effects: pure, IO, Task
  val s1 = Stream.unfold(1)(i => Some((i, i + 1)))
  // println(s1.take(10).toList)
  // println(s1.zip(s0.repeat).take(20).toList)

  // genarate a random stream
  val s2 = Stream.randomSeeded(0L)
  // println(s2.take(10).toList)

  // IO
  // IO provides compsability
  val io0 = IO(println("hello world"))
  val io1 = IO.pure(100)
  val io2 = io1.map(_ * 100)
  // io0.unsafeRunSync

  // reading  in citylotsjson
  val e = ExecutionContext.fromExecutor(
    java.util.concurrent.Executors.newCachedThreadPool
  )
  val blockingEc: Blocker         = Blocker.liftExecutionContext(e)
  implicit val timerIO: Timer[IO] = IO.timer(ExecutionContext.global)
  implicit val contextShiftIO: ContextShift[IO] =
    IO.contextShift((ExecutionContext.global))

  // reads from src
  val byteStream =
    readAll[IO](Paths.get("src/main/resources/citylots.json"), blockingEc, 4096)

  // compile exists to separate the stream from ther methods
  // very bad way to count bytes in a file using that funky fold function
  // fs2 does everything in chunks
  val numberOfBytes =
    byteStream.compile.fold(0)((acc, b) => acc + 1)
  // println(numberOfBytes.unsafeRunSync)

  // what is a chonk?
  // stripped collections with fast index access
  // a bit like vector, but domain specific for streaming
  // lots of interesting subtypes for chonk
  // why is vector better than list?
  val chunks =
    byteStream.chunks

  val numberOfBytes0 =
    byteStream.chunks.compile.fold(0)((acc, c) => acc + c.size)

  // decoding
  // whats a pipe
  // doing things incrementally in constant memory space
  val text0 =
    byteStream.through(text.utf8Decode)
  val text1 =
    text0.head.compile.toList

  val text2 =
    byteStream
      .through(text.utf8Decode)
      .through(text.lines)
      .head
      .compile
      .toList

  // more effects...
  // scheduling
  // we call this 'semantic blocking'
  val s3             = Stream.sleep(1.second)
  val greet          = IO(println("hello world"))
  val streamGreeting = Stream.eval(greet)
  val evalPure       = Stream.eval(IO.pure(69))
  val sgList         = streamGreeting.compile.toList
  val drain0         = streamGreeting.compile.drain

  val s4 = (Stream.sleep(1.second).drain ++ Stream.eval(greet)).repeat.take(5)
  // s4.compile.drain.unsafeRunSync()

  // schedule at fixed rate
  // schedule at fixed delay
  val s5 = Stream.awakeEvery[IO](1.second)
  // s5.take(5).compile.toVector.unsafeRunSync()
  // println(
  //   )

  import cats.instances.all._
  import cats.implicits._
  import cats._

  val seconds =
    Stream
      .awakeEvery[IO](1.second)
      .scan(1)((acc, _) => acc + 1)

}

object TestStream extends App {
  val executionContext = ExecutionContext.global
  implicit val contextShift: ContextShift[IO] =
    IO.contextShift(executionContext)

  val blocker: Blocker =
    Blocker.liftExecutionContext(executionContext)

  val path      = Paths.get("src/main/resources/test0.json")
  val chunkSize = 8

  val bytes = readAll[IO](
    path      = path,
    blocker   = blocker,
    chunkSize = chunkSize
  )

  // implicit val show0: Show[String] = Show[String]

  // val printStream: PrintStream = new PrintStream(_)

  val jsonFile = bytes
    .through(text.utf8Decode)
  // println(run.unsafeRunSync)
}
