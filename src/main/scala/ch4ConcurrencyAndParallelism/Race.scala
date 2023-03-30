package ch4ConcurrencyAndParallelism


import cats._
import cats.implicits._
import cats.effect._
import cats.effect.implicits._

import scala.concurrent.duration.DurationInt

object Race extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    case class Image(bytes: List[Byte])

    def fetchHttp(n: Int): IO[List[Image]] =
      IO.sleep(10.millis) *> (1 to n).toList.map(i => Image(List(i.toByte))).pure[IO]

    def fetchDb(n: Int) =
      IO.sleep(100.millis) *> (1 to n).toList.map(i => Image(List((100 + i).toByte))).pure[IO]

    val n = 50

    IO.race(fetchHttp(n), fetchDb(n)).map(_.fold(identity, identity)).flatTap(IO.println).as(ExitCode.Success)

    IO.race(fetchHttp(n), fetchDb(n)).map {
      case Right(dbImgs) => s"db won $dbImgs"
      case Left(httpImgs) => s"http won $httpImgs"
    }.flatTap(IO.println).as(ExitCode.Success)

  }

}
