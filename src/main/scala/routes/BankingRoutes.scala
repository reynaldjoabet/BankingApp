package routes

import cats.Applicative
import cats.data.OptionT.liftF
import cats.data.{Kleisli, ValidatedNel}
import cats.effect.{IO, Sync}
import cats.implicits.{catsSyntaxApplicativeId, catsSyntaxEitherId, catsSyntaxTuple2Semigroupal, catsSyntaxValidatedId, showInterpolator, toSemigroupKOps}
import ch.qos.logback.classic.Logger
import db.H2BankingService
import org.typelevel.ci.CIString
import model.AuthenticationStatus.{Authenticated, NotAllowed}
import model.commands.CredentialsValidation.FieldMissing
import model.commands.CreateCardCommand._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.`Content-Type`
import org.http4s.server.AuthMiddleware
import org.http4s.util.CaseInsensitiveString
import org.http4s.{AuthedRoutes, HttpRoutes, MediaType, Request}

import java.time.LocalDate.now
import java.util.UUID.randomUUID
import scala.util.Random
import model.commands._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.slf4j.LoggerFactory
object BankingRoutes extends Http4sDsl[IO] {
  private val logger=LoggerFactory.getLogger(this.getClass)
 private val service=H2BankingService
  private def randomCcv = LazyList.iterate(Random.nextInt(10))(_ => Random.nextInt(10)).take(3).mkString("")

  private def randomNumber = LazyList.iterate(Random.nextInt(10))(_ => Random.nextInt(10)).take(16).mkString("")

  private def checkCredentials(request: Request[IO]): ValidatedNel[CredentialsValidation, Credentials] = Applicative[Option].map2(
    request.headers.get(CIString("User-Id")).map(_.head.value),
    request.headers.get(CIString("Company-Id")).map(_.head.value)
  ) {
    case (userId, companyId) => (CredentialsValidation.validateUserId(userId), CredentialsValidation.validateCompanyId(companyId)).mapN(Credentials(_,_))
  }.getOrElse(FieldMissing.invalidNel[Credentials])

  private val authenticate: Kleisli[IO, Request[IO], Either[String, Credentials]] = Kleisli { request =>
    checkCredentials(request).fold(
      errors => Sync[IO].point(errors.map(_.message).reduce.asLeft[Credentials]),
      c => service.authenticate(c).flatMap {
        case NotAllowed(userId, companyId) =>
          logger.info(show"$userId is not part of $companyId").pure[IO] *> Sync[IO].point(show"$userId is not part of $companyId".asLeft[Credentials])
        case Authenticated(userId, _) =>
          logger.info(show"User $userId authenticated").pure[IO] *> Sync[IO].point(c.asRight[String])
      }
    )
  }

  private val onFailure: AuthedRoutes[String, IO] = Kleisli(req => liftF(Forbidden(req.context)))
 private val middleware: AuthMiddleware[IO, Credentials] = AuthMiddleware(authenticate, onFailure)

  private val nonAuthedRoutes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "companies" => service.listCompanies.flatMap(companies => Ok(companies, `Content-Type`(MediaType.application.json)))
    case GET -> Root / "users" => service.listUsers.flatMap(users => Ok(users, `Content-Type`(MediaType.application.json)))
  }

 private val authedRoutes: AuthedRoutes[Credentials, IO] = AuthedRoutes.of[Credentials, IO] {
    case GET -> Root / "cards" as credentials =>
      service.listCards(credentials.userId).flatMap(cards => Ok(cards, `Content-Type`(MediaType.application.json)))

    case GET -> Root / "wallets" as credentials =>
      service.listWallets(credentials.companyId).flatMap(wallets => Ok(wallets, `Content-Type`(MediaType.application.json)))

    case request@POST -> Root / "wallets" as credentials =>
      request.req.as[CreateWalletCommand].flatMap {
        command =>
          service.createWallet(randomUUID())(credentials.companyId)(command)
            .flatMap { wallet =>
              logger.info(show"Wallet created")
              Created(wallet, `Content-Type`(MediaType.application.json))
            }
      }

    case request@POST -> Root / "cards" / UUIDVar(id) / "load" as credentials =>
      request.req.as[LoadCardCommand].flatMap {
        command =>
          service.loadCard(credentials.userId, id, command.amount)
            .flatMap {
              case LoadCardCommandValidation.CardUnknown(cardId) =>
                logger.info(show"Card $cardId unknown")
                NotFound(show"Card $cardId unknown")
              case LoadCardCommandValidation.NotCardOwner(userId, cardId) =>
                logger.info(show"$userId is not card $cardId owner")
                Forbidden(show"$userId is not card $cardId owner")
              case LoadCardCommandValidation.CardBlocked(cardId) =>
                logger.info(show"Card $cardId is blocked")
                BadRequest(show"Card $cardId is blocked")
              case LoadCardCommandValidation.WalletBalanceTooLow(walletId, balance) =>
                logger.info(show"Wallet $walletId has a too low balance : $balance")
                BadRequest(show"Wallet $walletId has a too low balance : $balance")
              case LoadCardCommandValidation.CardCredited(cardId, balance) =>
                logger.info(show"Card $cardId is now $balance")
                  Ok(show"Card $cardId is now $balance")
            }
      }

    case POST -> Root / "cards" / UUIDVar(id) / "block" as credentials =>
      service.blockCard(credentials.userId, id)
        .flatMap {
          case BlockCardCommandValidation.CardUnknown(cardId) =>
            logger.info(show"Card $cardId unknown")
              NotFound(show"Card $cardId unknown")
          case BlockCardCommandValidation.NotCardOwner(userId, cardId) =>
            logger.info(show"$userId is not card $cardId owner")
              Forbidden(show"$userId is not card $cardId owner")
          case BlockCardCommandValidation.CardAlreadyBlocked(cardId) =>
            logger.info(show"Card $cardId is already blocked")
              BadRequest(show"Card $cardId is already blocked")
          case BlockCardCommandValidation.CardBlocked(cardId) =>
            logger.info(show"Card $cardId is now blocked")
              Ok(show"Card $cardId is now blocked")
        }

    case POST -> Root / "cards" / UUIDVar(id) / "unblock" as credentials =>
      service.unblockCard(credentials.userId, id)
        .flatMap {
          case UnblockCardCommandValidation.CardUnknown(cardId) =>
            logger.info(show"Card $cardId unknown")
            NotFound(show"Card $cardId unknown")
          case UnblockCardCommandValidation.NotCardOwner(userId, cardId) =>
            logger.info(show"$userId is not card $cardId owner")
            Forbidden(show"$userId is not card $cardId owner")
          case UnblockCardCommandValidation.CardAlreadyUnblocked(cardId) =>
            logger.info(show"Card $cardId is already unblocked")
            BadRequest(show"Card $cardId is already unblocked")
          case UnblockCardCommandValidation.CardUnblocked(cardId) =>
            logger.info(show"Card $cardId is now unblocked")
            Ok(show"Card $cardId is now unblocked")
        }

    case request@POST -> Root / "cards" as credentials =>
      request.req.as[CreateCardCommand].flatMap {
        command =>
          service.createCard(randomUUID(), randomNumber, now().plusMonths(1), randomCcv, credentials.userId, credentials.companyId)(command)
            .flatMap {
              case CreateCardCommandValidation.NotWalletOwner(walletId) =>
                logger.info(show"${credentials.userId} is not wallet $walletId owner")
                Forbidden(show"${credentials.userId} is not wallet $walletId owner")
              case CreateCardCommandValidation.CardCreated(card) =>
                logger.info(show"Card created")
                  Created(card, `Content-Type`(MediaType.application.json))
            }
      }

    case request@POST -> Root / "transfer" as credentials =>
      request.req.as[TransferCommand].flatMap {
        command =>
          service.transfer(credentials.companyId)(command.amount, command.source, command.target)
            .flatMap {
              case TransferCommandValidation.WalletUnknown(walletId) =>
                logger.info(show"Wallet $walletId unknown")
                NotFound(show"Wallet $walletId unknown")
              case TransferCommandValidation.NotWalletOwner(walletId) =>
                logger.info(show"${credentials.userId} is not wallet $walletId owner")
                Forbidden(show"${credentials.userId} is not wallet $walletId owner")
              case TransferCommandValidation.WalletBalanceTooLow(walletId, balance) =>
                logger.info(show"Wallet $walletId has a too low balance : $balance")
                BadRequest(show"Wallet $walletId has a too low balance : $balance")
              case TransferCommandValidation.Transfered(transfer) =>
                logger.info(show"Transfer processed")
                Ok(transfer, `Content-Type`(MediaType.application.json))
            }
      }
  }

  val routes: HttpRoutes[IO] = nonAuthedRoutes <+> middleware(authedRoutes)

}
