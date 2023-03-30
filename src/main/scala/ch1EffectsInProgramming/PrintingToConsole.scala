package ch1EffectsInProgramming

object PrintingToConsole extends App {

  case class OrderItem(desc: String, amount: Double)
  case class Order(id: Int, items: List[OrderItem]) {
    def total: Double = items.map(x => x.amount).sum

    def totalModified: Double = {
      println(s"Computing total for order $id") // не ссылочно прозрачный метод
      items.map(_.amount).sum
    }
  }

  def discountedPrice(order: Order): Double = {
    if (order.total > 2000) order.total * 0.8
    else order.total
  }

  val items = List(OrderItem("Cellphone", 700), OrderItem("Laptop", 1400))
  val order = Order(1, items)

}
