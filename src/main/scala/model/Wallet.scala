package model

import cats.Show
import cats.effect.IO
import io.circe.generic.semiauto._
import io.circe.{Codec, Decoder, Encoder}
import org.http4s.{EntityEncoder, circe}

import java.util.UUID

final case class Wallet(
                         walletId: UUID,
                         balance: BigDecimal,
                         currency: Currency,
                         companyId: UUID,
                         isMaster: Boolean
                       )

object Wallet {

  implicit val entityEncoder: EntityEncoder[IO, Wallet] = circe.jsonEncoderOf[IO, Wallet]
  implicit  val walletEncoder: Encoder[Wallet] = deriveEncoder[Wallet]

    implicit val show: Show[UUID] = (id: UUID) => id.toString

    //implicit val encoder: Encoder[UUID] = Encoder.encodeString.contramap[UUID](_.toString)
    //implicit val decoder: Decoder[UUID] = Decoder.decodeString.map(x => UUID.fromString(x)).withErrorMessage("Error while decoding walletId")
   // implicit val encoder:Encoder[UUID]= deriveEncoder[UUID]
    //implicit val decoder:Decoder[UUID]=deriveDecoder[UUID]
    //implicit val codec:Codec[UUID]=deriveCodec[UUID]

  }

