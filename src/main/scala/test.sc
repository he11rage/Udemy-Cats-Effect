
case class Person(id: Int, name: String)

type Db = Map[Int, Person]

import cats.data.State

class InMemoryPersonDBState {
  def add(person: Person): State[Db, Unit] =
    State.modify(_ + (person.id -> person))

  def delete(id: Int): State[Db, Unit] =
    State.modify(_ - id)

  def find(id: Int): State[Db, Option[Person]]=
    State.inspect(_.get(id))
}

val program1: Option[Person] = {
  val db = new InMemoryPersonDBState
  for {
    _ <- db.add(Person(1, "Joe"))
    _ <- db.delete(1)
    _ <- db.add(Person(1, "Joe"))
    p <- db.find(1)
  } yield p
}.runA(Map.empty).value

val program2: Option[Person] = {
  val db = new InMemoryPersonDBState
  val addOp = db.add(Person(1, "Joe"))
  for {
    _ <- addOp
    _ <- db.delete(1)
    _ <- addOp
    p <- db.find(1)
  } yield p
}.runA(Map.empty).value
