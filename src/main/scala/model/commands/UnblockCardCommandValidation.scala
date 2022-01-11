package model.commands


import java.util.UUID


sealed trait UnblockCardCommandValidation

object UnblockCardCommandValidation {

  final case class CardUnknown(cardId: UUID) extends UnblockCardCommandValidation

  final case class CardAlreadyUnblocked(cardId: UUID) extends UnblockCardCommandValidation

  final case class NotCardOwner(userId: UUID, cardId: UUID) extends UnblockCardCommandValidation

  final case class CardUnblocked(cardId: UUID) extends UnblockCardCommandValidation

  def cardUnknown(cardId: UUID): UnblockCardCommandValidation = CardUnknown(cardId)

  def cardAlreadyUnblocked(cardId: UUID): UnblockCardCommandValidation = CardAlreadyUnblocked(cardId)

  def notCardOwner(userId: UUID, cardId: UUID): UnblockCardCommandValidation = NotCardOwner(userId: UUID, cardId: UUID)

  def cardUnblocked(cardId: UUID): UnblockCardCommandValidation = CardUnblocked(cardId)
}