package model.commands

import cats.Show
import cats.effect.{IO, Sync}
import cats.implicits.showInterpolator

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf

import java.util.UUID



final case class LoadCardCommand(
                                  amount: BigDecimal ,
                                )

object LoadCardCommand {
  implicit val  codec:Codec[LoadCardCommand]=deriveCodec[LoadCardCommand]
  implicit def entityDecoder: EntityDecoder[IO, LoadCardCommand] = jsonOf[IO, LoadCardCommand]
}

sealed trait LoadCardCommandValidation

object LoadCardCommandValidation {

  implicit val show: Show[LoadCardCommandValidation] = {
    case CardUnknown(cardId) => show"CardUnknown($cardId)"
    case CardBlocked(cardId) => show"CardBlocked($cardId)"
    case NotCardOwner(userId, cardId) => show"NotCardOwner($userId, $cardId)"
    case WalletBalanceTooLow(cardId, balance) => show"WalletBalanceTooLow($cardId, $balance)"
    case CardCredited(cardId, balance) => show"CardCredited($cardId, $balance)"
  }

  final case class CardUnknown(cardId: UUID) extends LoadCardCommandValidation

  final case class CardBlocked(cardId: UUID) extends LoadCardCommandValidation

  final case class NotCardOwner(userId: UUID, cardId: UUID) extends LoadCardCommandValidation

  final case class WalletBalanceTooLow(walletId: UUID, balance: BigDecimal) extends LoadCardCommandValidation

  final case class CardCredited(cardId: UUID, balance: BigDecimal) extends LoadCardCommandValidation

  def cardUnknown(cardId: UUID): LoadCardCommandValidation = CardUnknown(cardId)

  def cardBlocked(cardId: UUID): LoadCardCommandValidation = CardBlocked(cardId)

  def notCardOwner(userId: UUID, cardId: UUID): LoadCardCommandValidation = NotCardOwner(userId: UUID, cardId: UUID)

  def walletBalanceTooLow(walletId: UUID, balance: BigDecimal): LoadCardCommandValidation = WalletBalanceTooLow(walletId, balance)

  def cardCredited(cardId: UUID, balance: BigDecimal): LoadCardCommandValidation = CardCredited(cardId, balance)
}