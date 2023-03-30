package ch1EffectsInProgramming

object ReferentiallyTransparentIO extends App {

  abstract class IO[+A] { // IO - describes a computation that: - can produce a value of A, fail or never terminate; - can perform side effects
    def run: A
  }

  object IO {
    def delay[A](computation: => A): IO[A] = ??? // wraps a computation in IO; the computation is NOT run
  }

  def greet(name: String): Unit =
    println(s"Hello $name")

  greet("leonardo") // Console output: Hello $name

  def greetIO(name: String): IO[Unit] =
    IO.delay(println(s"hello $name")) // Console output: nothing printing but if we call the run method, greetIO will be return Hello $name


  greetIO("leonardo") // Console output: nothing printing

  // greetIO("leonardo").run // Console output: hello $name
}
