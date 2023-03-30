package ch5ResourceSafety

import cats.effect._
import cats.implicits._

import java.io._

object Intro {
  trait RowEncoder[A] {
    def encode(a: A): String
  }

  case class Person(name: String, age: Int)

  implicit val personEncoder: RowEncoder[Person] = new RowEncoder[Person] {
    override def encode(p: Person): String = s"${p.name}, ${p.age}"
  }

  /*def writeAll[A](objects: List[A], file: File)(implicit encoder: RowEncoder[A]): IO[Unit] = {
    for {
      fw <- IO.blocking(new FileWriter(file))
      contents = objects.map(encoder.encode).mkString("\n")
      _ <- IO.blocking(fw.write(contents)) // error here
      _ <- IO.blocking(fw.flush())
      _ <- IO.blocking(fw.close()) // close is never called
    } yield ()
  }*/

  def writeAll[A](objects: List[A], file: File)(implicit encoder: RowEncoder[A]): IO[Unit] = {
    val contents = objects.map(encoder.encode).mkString("\n")

    def use(fw: FileWriter): IO[Unit] =
      IO.blocking(fw.write(contents)) *> IO.blocking(fw.flush())

    def release(fw: FileWriter): IO[Unit] =
      IO.blocking(fw.close())

    IO.blocking(new FileWriter(file)).bracket(use)(release) // called even if use fails
  }
}

object IntroSecond {
  def write(bytes: Array[Byte], fos: FileOutputStream): IO[Unit] = ???

  def read(fis: FileInputStream): IO[Array[Byte]] = ???

  def encrypt(bytes: Array[Byte]): IO[Array[Byte]] = ???

  def close(ac: AutoCloseable): IO[Unit] = IO.blocking(ac.close())

  def encryptFile(sourceFile: File, destFile: File): IO[Unit] = {
    val acquireReader = IO.blocking(new FileInputStream(sourceFile))
    val acquireWriter = IO.blocking(new FileOutputStream(destFile))

    acquireReader.bracket { reader =>
      acquireWriter.bracket { writer =>
        read(reader).flatMap(encrypt).flatMap(write(_, writer))
      }(close)
    }(close)
  }
}