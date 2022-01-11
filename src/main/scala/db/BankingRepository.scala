package db

import cats.effect.IO
import model.Transfer.TransferEntity

import java.time.{LocalDate, LocalDateTime}
import java.util.UUID
import model._
import model.commands._
sealed trait AuthenticationStatus

trait BankingRepository{
  //def transact[A](query: Q[A])(implicit T: Q ~> F): F[A] = T(query)

  def authenticate(credentials: Credentials):Option[(UUID, UUID)]

  def listCompanies: List[Company]

  def listUsers: List[Users]

  def listCards(userId: UUID): List[Card]

  def listWallets(companyId: UUID): List[Wallet]

  def createWallet(id: UUID)(companyId: UUID)(balance: BigDecimal, currency: Currency, isMaster: Boolean): Wallet

  def queryWallet(companyId: UUID, walletId: UUID): Option[(UUID, BigDecimal, Currency)]

  def queryMasterWallet(currency: Currency): (UUID, BigDecimal)

  def createCard(currency: Currency)(cardId: UUID, number: String, expirationDate: LocalDate, ccv: String)(userId: UUID)(walletId: UUID): Int

  def queryCard(cardId: UUID): Option[(UUID, UUID, UUID, BigDecimal, Currency, Boolean)]

  def queryWalletBalance(walletId: UUID): BigDecimal

  def setWalletBalance(walletId: UUID)(balance: BigDecimal): Int

  def setCardBalance(cardId: UUID)(balance: BigDecimal): Int

  def setTransfer(id: UUID, timestamp: LocalDateTime, amount: BigDecimal, sourceCurrency: Currency, targetCurrency: Currency, fees: Option[BigDecimal], source: TransferEntity, target: TransferEntity): Int

  def blockCard(cardId: UUID): Int

  def unblockCard(cardId: UUID): Int
}
