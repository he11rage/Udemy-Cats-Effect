package ch3ErrorHandling

import cats._
import cats.data.Validated.{Invalid, Valid}
import cats.data.{Validated, ValidatedNec}
import cats.implicits._
import cats.effect._
import cats.effect.implicits._
import ch3ErrorHandling.Controller.{Request, postTransfer}
import ch3ErrorHandling.Models.Account
import ch3ErrorHandling.Validations.{validateAccountNumber, validateDouble}

import scala.util.control.NonFatal

object Controller {
  case class Request(fromAccount: String, toAccount: String, amount: String)

  case class Response(status: Int, body: String)

  // Validate the from account number, the to account number to the amount
  // If validations fail, return a Response with code 400 and some error message
  // Otherwise, call transfer
  // If there is any domain error, return a Response with code 400 and some error message
  // If there is any other error, return a Response with code 500 and Internal Server Error message
  // Otherwise, return a Response with code 200 and Transfer successfully executed
  def postTransfer(request: Request): IO[Response] = {
    val response = (validateAccountNumber(request.fromAccount),
      validateAccountNumber(request.toAccount),
      validateDouble(request.amount)).tupled match {
      case Valid((fromAccountNumber, toAccountNumber, amount)) =>
        Service.transfer(fromAccountNumber, toAccountNumber, amount).map {
          case Right(()) =>
            Response(200, "Transfer successfully executed")
          case Left(error) =>
            Response(400, error.toString)
        }
      case Invalid(errors) => Response(400, errors.mkString_(", ")).pure[IO]
    }
    response.handleErrorWith {
      case NonFatal(e) => Response(500, "Internal server error").pure[IO]
    }
  }
}

object ErrorHandlingApp extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    import Controller._
    import Repository._
    import Models._

    val request = Request("12345", "56789", "2000")

    saveAccount(Account("12345", 5000)).flatMap { _ =>
      saveAccount(Account("56789", 2000)).flatMap { _ =>
        postTransfer(request)
          .flatTap(IO.println)
          .as(ExitCode.Success)
      }
    }
  }
}

object Validations {
  type Valid[A] = ValidatedNec[String, A]

  def validateDouble(s: String): Valid[Double] = Validated.fromOption(s.toDoubleOption, s"$s is not a valid double").toValidatedNec

  def validateAccountNumber(accountNumber: String): Valid[String] =
    Validated.condNec(accountNumber.forall(_.isLetterOrDigit), accountNumber, s"The account number $accountNumber must only contain letters or digits")
}

trait DomainError

case class InsufficientBalanceError(actualBalance: Double, amountToWithdraw: Double) extends DomainError

case class MaximumBalanceExceededError(actualBalance: Double, amountToDeposit: Double) extends DomainError

case class AccountNotFound(accountNumber: String) extends DomainError

object Models {
  case class Account(number: String, balance: Double) {
    def withdraw(amount: Double): Either[DomainError, Account] =
      Either.cond(balance >= amount, Account(number, balance - amount), InsufficientBalanceError(balance, amount))

    // Add MaximumBalanceExceededError to the error ADT
    // Implement this method checking the balance
    val maximumBalance = 10000

    def deposit(amount: Double): Either[DomainError, Account] =
      Either.cond((balance + amount) <= maximumBalance, Account(number, balance + amount), MaximumBalanceExceededError(balance, amount))
  }
}

object Repository {
  var data = Map.empty[String, Account]

  def findAccountByNumber(number: String): IO[Option[Account]] = data.get(number).pure[IO]

  def saveAccount(account: Account): IO[Unit] = IO {
    data = data + (account.number -> account)
  }
}

object Service {
  def transfer(fromAccountNumber: String, toAccountNumber: String, amount: Double): IO[Either[DomainError, Unit]] = {
    Repository.findAccountByNumber(fromAccountNumber).flatMap {
      fromAccountOpt =>
        Repository.findAccountByNumber(toAccountNumber).flatMap {
          toAccountOpt =>
            val accounts: Either[DomainError, (Account, Account)] = for {
              fromAccount <- fromAccountOpt.toRight(AccountNotFound(fromAccountNumber))
              toAccount <- fromAccountOpt.toRight(AccountNotFound(toAccountNumber))
              updatedFromAccount <- fromAccount.withdraw(amount)
              updatedToAccount <- toAccount.deposit(amount)
            } yield (updatedFromAccount, updatedToAccount)

            accounts.traverse {
              case (fromAccount, toAccount) =>
                Repository.saveAccount(fromAccount) >> Repository.saveAccount(toAccount)
            }
        }
    }

  }

}