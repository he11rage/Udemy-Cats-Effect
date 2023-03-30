package ch4ConcurrencyAndParallelism

import cats._
import cats.implicits._
import cats.effect._
import cats.effect.implicits._

import scala.concurrent.duration.DurationInt

object ParTraverse extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    case class Person(name: String)

    def save(person: Person): IO[Long] = IO.sleep(100.millis) *> person.name.length.toLong.pure[IO]

    val people = (1 to 50).toList.map(i => Person(i.toString))
    people.parTraverse(p => save(p)).flatTap(IO.println).as(ExitCode.Success)
  }

}
