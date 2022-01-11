package model.commands

import cats.effect.{IO, Sync}
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import model.{Currency, Transfer}
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf

import java.util.UUID


final case class TransferCommand(
                                  amount: BigDecimal,
                                  source: UUID,
                                  target:UUID
                                )

object TransferCommand {
  implicit val  codec:Codec[TransferCommand]=deriveCodec[TransferCommand]
  implicit def entityDecoder: EntityDecoder[IO, TransferCommand] = jsonOf[IO, TransferCommand]
}

sealed trait TransferCommandValidation

object TransferCommandValidation {

  final case class WalletUnknown(walletId: UUID) extends TransferCommandValidation

  final case class NotWalletOwner(walletId: UUID) extends TransferCommandValidation

  final case class WalletBalanceTooLow(walletId: UUID, balance: BigDecimal) extends TransferCommandValidation

  final case class Transfered(transfer: Transfer) extends TransferCommandValidation

  def walletUnknown(walletId: UUID): TransferCommandValidation = WalletUnknown(walletId)

  def notWalletOwner(walletId: UUID): TransferCommandValidation = NotWalletOwner(walletId)

  def walletBalanceTooLow(walletId: UUID, balance: BigDecimal): TransferCommandValidation = WalletBalanceTooLow(walletId, balance)

  def transferred(transfer: Transfer): TransferCommandValidation = Transfered(transfer)
}