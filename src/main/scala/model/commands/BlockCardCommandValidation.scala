package model.commands

import java.util.UUID


sealed trait BlockCardCommandValidation

object BlockCardCommandValidation {

  final case class CardUnknown(cardId: UUID) extends BlockCardCommandValidation

  final case class CardAlreadyBlocked(cardId: UUID) extends BlockCardCommandValidation

  final case class NotCardOwner(userId:UUID, cardId: UUID) extends BlockCardCommandValidation

  final case class CardBlocked(cardId: UUID) extends BlockCardCommandValidation

  def cardUnknown(cardId: UUID): BlockCardCommandValidation = CardUnknown(cardId)

  def cardAlreadyBlocked(cardId: UUID): BlockCardCommandValidation = CardAlreadyBlocked(cardId)

  def notCardOwner(userId: UUID, cardId: UUID): BlockCardCommandValidation = NotCardOwner(userId: UUID, cardId: UUID)

  def cardBlocked(cardId: UUID): BlockCardCommandValidation = CardBlocked(cardId)
}