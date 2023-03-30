package ch2IOMonad

import cats.effect.unsafe.IORuntime

// IO as a Monad
object IOStructure {
  // IO Monad Revisited
  abstract class IO[+A] {
    def unsafeRunSync()(implicit runtime: IORuntime): A
    def flatMap[B](f: => IO[B]): IO[B] // and pure -> Monad Instance
  }

  object IO {
    def delay[A](thunk: => A): IO[A] = ???
                                      // IO(...) == IO.delay(...)
    def apply[A](thunk: => A): IO[A] = ???

    def pure[A](value: A): IO[A] = ??? // only for pure values (no side-effects)
    // OK: IO.pure("hello")
    // WRONG: IO.pure(println("hello"))

    def raiseError[A](t: Throwable): IO[A] = ??? // MonadError instance

  }
}
