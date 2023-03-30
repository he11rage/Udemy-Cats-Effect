package ch2IOMonad

import cats._
import cats.effect._
import cats.implicits._
import cats.effect.implicits._
import cats.effect.unsafe.implicits.global

import scala.io.StdIn

object MyApp extends IOApp{
  object Console {
    def putStrLn(s: String): IO[Unit] = IO(println(s))
    def readLine(text: String): IO[String] = IO(StdIn.readLine(text))

  }

  // read a line
  // output the line
  // repeat
  import Console._
  def echoForever: IO[Nothing] = {
    val program = for {
      str <- readLine("enter something: ")
      _ <- putStrLn(str)
    } yield ()
    program.foreverM
  }

  override def run(args: List[String]): IO[ExitCode] = {
    echoForever.as(ExitCode.Success)
  }

}
