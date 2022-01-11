package model

import java.time.LocalDate
import java.util.UUID
import cats.Show
import cats.effect.IO
import cats.implicits.showInterpolator
import io.circe.Encoder
import org.http4s.circe.jsonEncoderOf
import  io.circe.generic.semiauto._

final case class Card(
                       cardId: UUID,
                       walletId: UUID,
                       currency: Currency,
                       balance: BigDecimal,
                       number: String,
                       expirationDate: LocalDate,
                       ccv: String,
                       userId: UUID,
                       isBlocked: Boolean
                     )

object Card {



    implicit val show: Show[Card] = (card: Card) => show"${card.cardId}"

    implicit val encoder: Encoder[Card] = deriveEncoder[Card]
  implicit  val entityEncoder=jsonEncoderOf[IO,Card]




}