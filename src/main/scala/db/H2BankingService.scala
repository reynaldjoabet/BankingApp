package db

import java.time.{LocalDate, LocalDateTime}
import java.util.UUID
import cats.effect._
import cats.implicits.{catsSyntaxApplicativeId, catsSyntaxOptionId, showInterpolator}
import model.Currency.{EUR, GBP, USD}
import model.Transfer.TransferEntity.{CardEntity, WalletEntity}
import model._
import model.commands._
import org.slf4j.LoggerFactory



object H2BankingService {
  private val logger=LoggerFactory.getLogger(this.getClass)

  val repository=H2BankingRepository
    private val Fee = 2.9

    private val CurrencyExchange: Map[(Currency, Currency), BigDecimal] = Map(
      (EUR, USD) -> 1.18,
      (EUR, GBP) -> 0.90,
      (USD, EUR) -> 0.85,
      (USD, GBP) -> 0.77,
      (GBP, EUR) -> 1.11,
      (GBP, USD) -> 1.30
    )

    def authenticate(credentials: Credentials): IO[AuthenticationStatus] =
      logger.info("List companies").pure[IO]
        .>>(IO.delay{
      repository.authenticate(credentials) match{
        case None => AuthenticationStatus.NotAllowed(credentials.userId, credentials.companyId)
        case Some((userId, companyId)) => AuthenticationStatus.Authenticated(userId, companyId)
      }
    })

    def listCompanies: IO[List[Company]] = {
      logger.info("List companies")
        .pure[IO].>>(IO.delay(repository.listCompanies))

    }


    def listUsers: IO[List[Users]] = {
      logger.info("List users").pure[IO].>>(IO.delay(repository.listUsers))
    }


  def listCards(userId: UUID): IO[List[Card]] = {
      logger.info("List cards")
        .pure[IO].>>( IO.delay( repository.listCards(userId)))

  }

  def listWallets(companyId: UUID): IO[List[Wallet]] = {
      logger.info("List wallets")
        .pure[IO].>>(IO.delay(repository.listWallets(companyId)))
  }


  def createWallet(id: UUID)(companyId: UUID)(command: CreateWalletCommand): IO[Wallet] = {
    logger.info(s"Create wallet $id")
      .pure[IO]
      .>>(IO.delay(repository.createWallet(id)(companyId)(command.balance, command.currency, command.isMaster))
      )
  }

  def createCard(cardId: UUID, number: String, expirationDate: LocalDate, ccv: String, userId: UUID, companyId: UUID)(command: CreateCardCommand): IO[CreateCardCommandValidation] =
      logger.info(show"Create card $cardId").pure[IO]
        .>>(IO.delay{
        repository.queryWallet(companyId, command.walletId)match {
          case None => CreateCardCommandValidation.notWalletOwner(command.walletId)
          case Some((walletId, _, currency)) => repository.createCard(currency)(cardId, number, expirationDate, ccv)(userId)(command.walletId)
            CreateCardCommandValidation.cardCreated(Card(cardId, walletId, currency, 0, number, expirationDate, ccv, userId, isBlocked = false))
        }
      })

    def loadCard(userId: UUID, cardId: UUID, amount: BigDecimal ): IO[LoadCardCommandValidation] =
      logger.info(show"Load card $cardId").pure[IO]
        .>>(
        repository.queryCard(cardId) match {
          case None => LoadCardCommandValidation.cardUnknown(cardId).pure[IO]
          case Some((cardId, ownerId, _, _, _, _)) if ownerId != userId => LoadCardCommandValidation.notCardOwner(userId, cardId).pure[IO]
          case Some((cardId, _, _, _, _, true)) => LoadCardCommandValidation.cardBlocked(cardId).pure[IO]
          case Some((cardId, _, walletId, cardBalance, currency, false)) =>
            repository.queryWalletBalance(walletId) match {
              case walletBalance if walletBalance < amount => LoadCardCommandValidation.walletBalanceTooLow(walletId, walletBalance).pure[IO]
              case walletBalance =>
                val newWalletBalance = walletBalance - amount
                val newCardBalance = cardBalance + amount
                repository.setWalletBalance(walletId)(newWalletBalance).pure[IO] *>
                  repository.setCardBalance(cardId)(newCardBalance).pure[IO] *>
                  repository.setTransfer(UUID.randomUUID(), LocalDateTime.now(), amount, currency, currency, Option.empty, WalletEntity(walletId), CardEntity(cardId)).pure[IO] *>
                  LoadCardCommandValidation.cardCredited(cardId, newCardBalance).pure[IO]
            }
        })


    def blockCard(userId: UUID, cardId: UUID): IO[BlockCardCommandValidation] = {
      logger.info(show"Block card $cardId")
      repository.queryCard(cardId) match {
        case None => BlockCardCommandValidation.cardUnknown(cardId).pure[IO]
        case Some((cardId, ownerId, _, _, _, _)) if ownerId != userId => BlockCardCommandValidation.notCardOwner(userId, cardId).pure[IO]
        case Some((cardId, _, _, _, _, true)) => BlockCardCommandValidation.cardAlreadyBlocked(cardId).pure[IO]
        case Some((cardId, _, walletId, cardBalance, currency, false)) => repository.blockCard(cardId).pure[IO] *>
          repository.setCardBalance(cardId)(0).pure[IO] *>
          repository.queryWalletBalance(walletId).pure[IO].flatMap(walletBalance => repository.setWalletBalance(walletId)(walletBalance + cardBalance).pure[IO])
          repository.setTransfer(UUID.randomUUID(), LocalDateTime.now(), cardBalance, currency, currency, Option.empty, CardEntity(cardId), WalletEntity(walletId)).pure[IO] *>
          BlockCardCommandValidation.cardBlocked(cardId).pure[IO]
      }

    }
    def unblockCard(userId:UUID, cardId:UUID): IO[UnblockCardCommandValidation] =
      logger.info(show"Unblock card $cardId").pure[IO] *>  {
        repository.queryCard(cardId) match  {
          case None => UnblockCardCommandValidation.cardUnknown(cardId).pure[IO]
          case Some((cardId, ownerId, _, _, _, _)) if ownerId != userId => UnblockCardCommandValidation.notCardOwner(userId, cardId).pure[IO]
          case Some((cardId, _, _, _, _, false)) => UnblockCardCommandValidation.cardAlreadyUnblocked(cardId).pure[IO]
          case Some((cardId, _, _, _, _, true)) => repository.unblockCard(cardId).pure[IO] *> UnblockCardCommandValidation.cardUnblocked(cardId).pure[IO]
        }
      }

    def transfer(companyId: UUID)(amount: BigDecimal, source: UUID, target: UUID): IO[TransferCommandValidation] =
      logger.info(show"Transfer between $source and $target").pure[IO] *> {
        repository.queryWallet(companyId, source) match {
          case None => TransferCommandValidation.notWalletOwner(source).pure[IO]
          case Some((sourceId, sourceBalance, _)) if sourceBalance < amount => TransferCommandValidation.walletBalanceTooLow(sourceId, sourceBalance).pure[IO]
          case Some((sourceId, sourceBalance, sourceCurrency)) =>
            repository.queryWallet(companyId, target) match {
              case None => TransferCommandValidation.walletUnknown(target).pure[IO]
              case Some((targetId, targetBalance, targetCurrency)) if sourceCurrency == targetCurrency =>
                val transferId =UUID.randomUUID()
                val timestamp = LocalDateTime.now()
                repository.setWalletBalance(sourceId)(sourceBalance - amount).pure[IO] *>
                  repository.setWalletBalance(targetId)(targetBalance + amount).pure[IO] *>
                  repository.setTransfer(transferId, timestamp, amount, sourceCurrency, targetCurrency, Option.empty, WalletEntity(sourceId), WalletEntity(targetId)).pure[IO] *>
                  TransferCommandValidation.transferred(Transfer(transferId, timestamp, amount, sourceCurrency, targetCurrency, Option.empty, WalletEntity(sourceId), WalletEntity(targetId))).pure[IO]
              case Some((targetId, targetBalance, targetCurrency)) =>
                val transferId = UUID.randomUUID()
                val timestamp = LocalDateTime.now()
                val exchange = CurrencyExchange((sourceCurrency, targetCurrency))
                val amountWithExchange = amount * exchange
                val fee = (Fee / 100) * amountWithExchange
                val amountToCredit = amountWithExchange - fee
                repository.setWalletBalance(sourceId)(sourceBalance - amount).pure[IO] *>
                  repository.setWalletBalance(targetId)(targetBalance + amountToCredit).pure[IO] *>
                  repository.setTransfer(transferId, timestamp, amount, sourceCurrency, targetCurrency, fee.some, WalletEntity(sourceId), WalletEntity(targetId)).pure[IO] *>
                  repository.queryMasterWallet(targetCurrency).pure[IO].flatMap {
                    case (masterWalletId, masterWalletBalance) =>
                      repository.setWalletBalance(masterWalletId)(masterWalletBalance + fee).pure[IO] *>
                        repository.setTransfer(UUID.randomUUID(), timestamp, fee, targetCurrency, targetCurrency, Option.empty, WalletEntity(targetId), WalletEntity(masterWalletId)).pure[IO]
                  } *>
                  TransferCommandValidation.transferred(Transfer(transferId, timestamp, amount, sourceCurrency, targetCurrency, fee.some, WalletEntity(sourceId), WalletEntity(targetId))).pure[IO]
            }
        }
      }



}
