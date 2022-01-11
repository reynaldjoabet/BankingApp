package model

import cats.effect.IO

import java.time.LocalDateTime
import java.util.UUID
import io.circe.Encoder
import model.Transfer.TransferEntity
import io.circe.generic.semiauto.deriveEncoder
import io.getquill.MappedEncoding
import org.http4s.circe.jsonEncoderOf
final case class Transfer(
                           id: UUID,
                           tstamp: LocalDateTime,
                           amount: BigDecimal,
                           originCurrency: Currency,
                           targetCurrency: Currency,
                           conversionFee: Option[BigDecimal],
                           origin: TransferEntity,
                           target: TransferEntity
                         )

object Transfer {


  implicit val encoder:Encoder[Transfer]=deriveEncoder[Transfer]
implicit  val entityEncoder=jsonEncoderOf[IO,Transfer]

  sealed trait TransferEntity {
    val entity: String
    val id: UUID
  }

  object TransferEntity {
    implicit val encoder:Encoder[TransferEntity]=deriveEncoder[TransferEntity]
    final case class CardEntity(cardId: UUID) extends TransferEntity {
      override val entity: String = "Card"
      override val id: UUID = cardId
    }

    final case class WalletEntity(walletId: UUID) extends TransferEntity {
      override val entity: String = "Wallet"
      override val id: UUID = walletId
    }


    def fromString(str: String)=
      if(str.contains("Wallet")) WalletEntity(UUID.fromString(str))
      else CardEntity(UUID.fromString(str))
    implicit val encodeCurrency=MappedEncoding[TransferEntity,String](_.toString)

    implicit val decodeCurrency=MappedEncoding[String,TransferEntity](TransferEntity.fromString(_))
  }



}