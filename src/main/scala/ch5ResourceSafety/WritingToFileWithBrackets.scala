package ch5ResourceSafety

import cats._
import cats.effect._
import cats.effect.implicits._
import java.io._

object WritingToFileWithBrackets extends IOApp {
  trait RowEncoder[A] {
    def encode(a: A): String
  }

  case class Person(name: String, age: Int)

  implicit val personEncoder: RowEncoder[Person] = new RowEncoder[Person] {
    override def encode(p: Person): String = s"${p.name}, ${p.age}"
  }

  def writeAll[A](objects: List[A], file: File)(implicit encoder: RowEncoder[A]): IO[Unit] = {
    val contents = objects.map(encoder.encode).mkString("\n")

    def use(fw: FileWriter): IO[Unit] =
      IO.blocking(fw.write(contents)) *> IO.blocking(fw.flush())

    def release(fw: FileWriter): IO[Unit] =
      IO.blocking(fw.close())

    IO.blocking(new FileWriter(file)).bracket(use)(release) // called even if use fails
  }
  override def run(args: List[String]): IO[ExitCode] = {
    val file = new File("test")
    val objects = List(Person("leandro", 31), Person("Maria", 24))
    writeAll(objects, file).as(ExitCode.Success)
  }

}
