package model.commands

import cats.Show
import cats.effect.{IO, Sync}
import cats.implicits.showInterpolator
import io.circe.generic.semiauto._
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf

import java.util.UUID


final case class CreateCardCommand(
                                    walletId: UUID,
                                  )

object CreateCardCommand {
  implicit  val codec=deriveCodec[CreateCardCommand]
  implicit def entityDecoder: EntityDecoder[IO, CreateCardCommand] = jsonOf[IO, CreateCardCommand]
}

sealed trait CreateCardCommandValidation

object CreateCardCommandValidation {

  import model._

  implicit val show: Show[CreateCardCommandValidation] = {
    case NotWalletOwner(walletId) => show"NotWalletOwner($walletId)"
    case CardCreated(card) => show"CardCreated($card)"
  }

  final case class NotWalletOwner(walletId: UUID) extends CreateCardCommandValidation

  final case class CardCreated(card: Card) extends CreateCardCommandValidation

  def notWalletOwner(walletId: UUID): CreateCardCommandValidation = NotWalletOwner(walletId)

  def cardCreated(card: Card): CreateCardCommandValidation = CardCreated(card)

}