package ch4ConcurrencyAndParallelism

import cats.effect._
import cats.effect.implicits._
import cats.implicits._

import scala.concurrent.duration.DurationInt

object ParMapN extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    case class Image(bytes: List[Byte])

    def httpImages(n: Int): IO[List[Image]] =
      IO.sleep(100.millis) *> (1 to n).toList.map(i => Image(List(i.toByte))).pure[IO]

    def dbImages(n: Int): IO[List[Image]] =
      IO.sleep(100.millis) *> (1 to n).toList.map(i => Image(List((100 + i).toByte))).pure[IO]

    val n = 50

    (httpImages(n), dbImages(n)).parMapN { case (httpImgs, dbImgs) =>
      (httpImgs ++ dbImgs)
    }.flatTap(IO.println).as(ExitCode.Success)
  }

}
