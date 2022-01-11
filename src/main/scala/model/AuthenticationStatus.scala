package model
import java.util.UUID

sealed trait AuthenticationStatus extends Product with Serializable

object AuthenticationStatus {

  final case class Authenticated(userId: UUID, companyId: UUID) extends AuthenticationStatus

  final case class NotAllowed(userId: UUID, companyId: UUID) extends AuthenticationStatus

}