package ch1EffectsInProgramming

object WritingToDisk extends App {

  case class Event(desc: String)

  class EventLog(filename: String) {
    def log(event: Event): Unit = ??? // appends a line to the file
  }

  case class OrderItem(desc: String, amount: Double)

  case class Order(id: Int, items: List[OrderItem])

  val items = List(OrderItem("Cellphone", 700), OrderItem("Laptop", 1400))
  val order = Order(1, items)
  val log = new EventLog("...")
  def logItemsBought(order: Order)(eventLog: EventLog): Unit = {
    order.items.foreach {_ => eventLog.log(Event("Item bought"))}
  }

}
