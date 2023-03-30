package ch4ConcurrencyAndParallelism

import cats.effect.IO.race
import cats.effect._
import cats.implicits._

object Intro {

  case class Person(name: String)

  class PersonService {
    def createPerson(name: String): IO[Person] = Person(name).pure[IO]

    def createAll(names: List[String]): IO[List[Person]] = {
      names.parTraverse(createPerson) // runs createPerson in parallel; level of parallelism depends on underlying thread pools
    }
  }
}

object anotherExample {
  case class Quote(author: String, text: String)

  class QuotesService{
    def kittensQuotes(n: Int): IO[List[Quote]] = ???
    def puppiesQuotes(n: Int): IO[List[Quote]] = ???
    def mixedQuotes(n: Int): IO[List[Quote]] =
      (kittensQuotes(n), puppiesQuotes(n)).parMapN {(kittens, puppies) =>
        kittens ++ puppies
      }
  }
}

object ImagesFetch {
  case class Image(data: List[Byte])

  class ImagesService {
    def fetchFromDb(n: Int): IO[List[Image]] = ???
    def fetchFromHttp(n: Int): IO[List[Image]] = ???

    def fetchFromFastest(n: Int): IO[List[Image]] = {
      race(fetchFromDb(n), fetchFromHttp(n)).map(_.fold(identity, identity))
    }

  }
}
