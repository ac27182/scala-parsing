package parsing
import fs2.Stream._
import fs2.Stream
import java.io.{ByteArrayOutputStream, ByteArrayInputStream}
import java.util.zip.{GZIPOutputStream, GZIPInputStream}

import scala.util.Try
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import cats.implicits._
import fs2.text
import fs2.Chunk
import fs2.compress._
import SampleData.data
import cats.effect.IO
import fs2.Fallible
object Compression extends App {
  val s0 = Stream.empty
  val s1 = Stream.emit(1)
  val s2 = Stream(1, 2, 3, 4, 6, "a")
  val s3 = Stream.emits(List(1, 2, 3))
  val s4 = Stream.range(0, 100).intersperse(1).toList

  // effects
  val eff = Stream.eval(IO {
    println("doing an IO....")
    1 + 1
  })

  val ra = eff.compile.toVector
  val rb = eff.compile.drain
  val rc = eff.compile.fold(0)(_ + _)
  // ra.unsafeRunSync
  // rb.unsafeRunSync
  // rc.unsafeRunSync

  // chonks
  val s1c =
    Stream.chunk(Chunk.doubles(Array(1.0, 2.0, 3.0)))

  s1c.mapChunks { ds =>
    val doubles = ds.toDoubles
    println(doubles)
    doubles
  }

  // basic stream operations
  val appendEx1 = Stream(1, 2, 3) ++ Stream.emit(42)
  val appendEx2 = Stream(1, 2, 3) ++ Stream.eval(IO.pure(4))
  println(appendEx1.toVector)
  println(appendEx2.compile.toVector.unsafeRunSync)
  println(appendEx1.intersperse(1).toVector)
  println(appendEx1.flatMap(i => Stream.emits(List(i, i))).toVector)

  // error handling
  val err  = Stream.raiseError[IO](new Exception("oopsie 1"))
  val err2 = Stream(1, 2, 3) ++ (throw new Exception("oopsie 2"))
  val err3 = Stream.eval(IO(throw new Exception("oopsie 3")))

  try err.compile.toList.unsafeRunSync
  catch {
    case e: Exception => println(e)
  }

  try err2.toList
  catch { case e: Exception => println(e) }

  try err3.compile.drain.unsafeRunSync
  catch { case e: Exception => println(e) }

  err
    .handleErrorWith { e =>
      Stream.emit(e.getMessage)
    }
    .compile
    .toList
    .unsafeRunSync

}

object Compression1 {
  val bytes     = data.getBytes
  val newString = new String(bytes, StandardCharsets.UTF_8)
  val s0        = Stream(data)

  val compressed0 =
    Stream
      .chunk(Chunk.bytes(bytes))
      .through(gzip(bytes.length))
      .toVector
      .toArray

  val compressed1 =
    Stream
      .chunk(Chunk.bytes(bytes))
      .through(deflate(nowrap = true, level = 9))
      .toVector
      .toArray

  val decompressed1 =
    Stream
      .chunk(Chunk.bytes(compressed1))
      .covary[Fallible]
      .through(
        inflate(nowrap = true)
      )
      .compile
      .toVector match {
      case Left(error)  => None
      case Right(value) => Some(value.toArray.length)
    }

  println(data)
  println(bytes.length)
  println(compressed0.length)
  println(compressed1.length)
  println(decompressed1 getOrElse None)

}
object Compression2 {
  def compress: Array[Byte] => Array[Byte] =
    b =>
      Stream
        .chunk(Chunk.bytes(b))
        .through(deflate(level = 9, nowrap = true))
        .toVector
        .toArray

  def decompress: Array[Byte] => Either[Throwable, Vector[Byte]] =
    b =>
      Stream
        .chunk(Chunk.bytes(b))
        .covary[Fallible]
        .through(inflate(nowrap = true))
        .toVector

  val compressedData = compress(data.getBytes)
  val decompressedData = decompress(compressedData) match {
    case Right(vector) => vector.toArray.length
    case Left(error)   => error
  }
  println(compressedData.length)
  println(decompressedData)
}

object Compression3 {}

object SampleData {
  val data =
    """
The unanimous Declaration of the thirteen united States of America, When in the Course of human events, it becomes necessary for one people to dissolve the political bands which have connected them with another, and to assume among the powers of the earth, the separate and equal station to which the Laws of Nature and of Nature's God entitle them, a decent respect to the opinions of mankind requires that they should declare the causes which impel them to the separation.

We hold these truths to be self-evident, that all men are created equal, that they are endowed by their Creator with certain unalienable Rights, that among these are Life, Liberty and the pursuit of Happiness.--That to secure these rights, Governments are instituted among Men, deriving their just powers from the consent of the governed, --That whenever any Form of Government becomes destructive of these ends, it is the Right of the People to alter or to abolish it, and to institute new Government, laying its foundation on such principles and organizing its powers in such form, as to them shall seem most likely to effect their Safety and Happiness. Prudence, indeed, will dictate that Governments long established should not be changed for light and transient causes; and accordingly all experience hath shewn, that mankind are more disposed to suffer, while evils are sufferable, than to right themselves by abolishing the forms to which they are accustomed. But when a long train of abuses and usurpations, pursuing invariably the same Object evinces a design to reduce them under absolute Despotism, it is their right, it is their duty, to throw off such Government, and to provide new Guards for their future security.--Such has been the patient sufferance of these Colonies; and such is now the necessity which constrains them to alter their former Systems of Government. The history of the present King of Great Britain is a history of repeated injuries and usurpations, all having in direct object the establishment of an absolute Tyranny over these States. To prove this, let Facts be submitted to a candid world.

He has refused his Assent to Laws, the most wholesome and necessary for the public good.

He has forbidden his Governors to pass Laws of immediate and pressing importance, unless suspended in their operation till his Assent should be obtained; and when so suspended, he has utterly neglected to attend to them.

He has refused to pass other Laws for the accommodation of large districts of people, unless those people would relinquish the right of Representation in the Legislature, a right inestimable to them and formidable to tyrants only.

He has called together legislative bodies at places unusual, uncomfortable, and distant from the depository of their public Records, for the sole purpose of fatiguing them into compliance with his measures.

He has dissolved Representative Houses repeatedly, for opposing with manly firmness his invasions on the rights of the people.

He has refused for a long time, after such dissolutions, to cause others to be elected; whereby the Legislative powers, incapable of Annihilation, have returned to the People at large for their exercise; the State remaining in the mean time exposed to all the dangers of invasion from without, and convulsions within.

He has endeavoured to prevent the population of these States; for that purpose obstructing the Laws for Naturalization of Foreigners; refusing to pass others to encourage their migrations hither, and raising the conditions of new Appropriations of Lands.

He has obstructed the Administration of Justice, by refusing his Assent to Laws for establishing Judiciary powers.

He has made Judges dependent on his Will alone, for the tenure of their offices, and the amount and payment of their salaries.

He has erected a multitude of New Offices, and sent hither swarms of Officers to harrass our people, and eat out their substance.

He has kept among us, in times of peace, Standing Armies without the Consent of our legislatures.

He has affected to render the Military independent of and superior to the Civil power.

He has combined with others to subject us to a jurisdiction foreign to our constitution, and unacknowledged by our laws; giving his Assent to their Acts of pretended Legislation:

For Quartering large bodies of armed troops among us:

For protecting them, by a mock Trial, from punishment for any Murders which they should commit on the Inhabitants of these States:

For cutting off our Trade with all parts of the world:

For imposing Taxes on us without our Consent:

For depriving us in many cases, of the benefits of Trial by Jury:

For transporting us beyond Seas to be tried for pretended offences

For abolishing the free System of English Laws in a neighbouring Province, establishing therein an Arbitrary government, and enlarging its Boundaries so as to render it at once an example and fit instrument for introducing the same absolute rule into these Colonies:

For taking away our Charters, abolishing our most valuable Laws, and altering fundamentally the Forms of our Governments:

For suspending our own Legislatures, and declaring themselves invested with power to legislate for us in all cases whatsoever.

He has abdicated Government here, by declaring us out of his Protection and waging War against us.

He has plundered our seas, ravaged our Coasts, burnt our towns, and destroyed the lives of our people.

He is at this time transporting large Armies of foreign Mercenaries to compleat the works of death, desolation and tyranny, already begun with circumstances of Cruelty & perfidy scarcely paralleled in the most barbarous ages, and totally unworthy the Head of a civilized nation.

He has constrained our fellow Citizens taken Captive on the high Seas to bear Arms against their Country, to become the executioners of their friends and Brethren, or to fall themselves by their Hands.

He has excited domestic insurrections amongst us, and has endeavoured to bring on the inhabitants of our frontiers, the merciless Indian Savages, whose known rule of warfare, is an undistinguished destruction of all ages, sexes and conditions.

In every stage of these Oppressions We have Petitioned for Redress in the most humble terms: Our repeated Petitions have been answered only by repeated injury. A Prince whose character is thus marked by every act which may define a Tyrant, is unfit to be the ruler of a free people.

Nor have We been wanting in attentions to our Brittish brethren. We have warned them from time to time of attempts by their legislature to extend an unwarrantable jurisdiction over us. We have reminded them of the circumstances of our emigration and settlement here. We have appealed to their native justice and magnanimity, and we have conjured them by the ties of our common kindred to disavow these usurpations, which, would inevitably interrupt our connections and correspondence. They too have been deaf to the voice of justice and of consanguinity. We must, therefore, acquiesce in the necessity, which denounces our Separation, and hold them, as we hold the rest of mankind, Enemies in War, in Peace Friends.

We, therefore, the Representatives of the united States of America, in General Congress, Assembled, appealing to the Supreme Judge of the world for the rectitude of our intentions, do, in the Name, and by Authority of the good People of these Colonies, solemnly publish and declare, That these United Colonies are, and of Right ought to be Free and Independent States; that they are Absolved from all Allegiance to the British Crown, and that all political connection between them and the State of Great Britain, is and ought to be totally dissolved; and that as Free and Independent States, they have full Power to levy War, conclude Peace, contract Alliances, establish Commerce, and to do all other Acts and Things which Independent States may of right do. And for the support of this Declaration, with a firm reliance on the protection of divine Providence, we mutually pledge to each other our Lives, our Fortunes and our sacred Honor.
"""
}
