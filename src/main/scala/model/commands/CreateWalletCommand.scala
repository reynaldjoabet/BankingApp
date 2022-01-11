package model.commands

import cats.effect.{IO, Sync}

import io.circe.Codec
import io.circe.generic.auto._
import io.circe.generic.semiauto.deriveCodec
import model.Currency
import org.http4s.EntityDecoder
import org.http4s.circe._



final case class CreateWalletCommand(
                                      balance: BigDecimal,
                                      currency: Currency,
                                      isMaster: Boolean
                                    )

object CreateWalletCommand {
  implicit  val codec:Codec[CreateCardCommand]=deriveCodec[CreateCardCommand]
  implicit def entityDecoder: EntityDecoder[IO, CreateWalletCommand] = jsonOf[IO, CreateWalletCommand]
}