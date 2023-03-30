package ch1EffectsInProgramming

object ErrorHandling extends App {

  case class Person(id: Int, name: String)

  case class Account(id: Int, balance: Double, owner: Person) {
    def withdraw(amount: Double): Account = Account(id, balance - amount, owner) // copy(balance = balance - amount) - same


  }

  val joe = Person(1, "Joe")

  val account = Account(1, 1000, joe)

  println(account.withdraw(300)) // Account(1, 700, joe) - OK

  println(account.withdraw(1200)) // Account(1, -200, joe) - NOT OK


  // lets make account with exception when balance < 0

  sealed abstract class AccountError(msg: String) extends Exception(msg)

  case class InsufficientBalanceError(
                                       amountToWithdraw: Double,
                                       balance: Double
                                     ) extends AccountError(
    s"Can't withdraw $amountToWithdraw. Insufficient balance: $balance"
  )

  case class AccountWithException(id: Int, balance: Double, owner: Person) {
    def withdraw(amount: Double): AccountWithException =
      if (amount > balance) throw InsufficientBalanceError(amount, balance)
      else copy(balance = balance - amount)
  }

  // этот вариант не ссылочно прозрачный

  // accounts with either

  case class AccountsWithEither(id: Int, balance: Double, owner: Person) {
    def withdraw(amount: Double): Either[AccountError, AccountsWithEither] =
      if (amount > balance)
        Left(InsufficientBalanceError(amount, balance))
      else
        Right(copy(balance = balance - amount))
  }

  // Этот вариант является ссылочно прозрачным


}
