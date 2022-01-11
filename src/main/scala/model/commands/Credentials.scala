package model.commands

import java.util.UUID
import cats.data.ValidatedNel
import cats.effect.IO
import cats.implicits.catsSyntaxValidatedId
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import model.Transfer
import org.http4s.circe.jsonEncoderOf

import scala.util.Try

final case class Credentials(
                              userId: UUID,
                              companyId: UUID
                            )

object Credentials {

  implicit val codec: Codec[Credentials] = deriveCodec[Credentials]

  implicit  val entityEncoder=jsonEncoderOf[IO,Credentials]
}

sealed trait CredentialsValidation {
  val message: String
}

object CredentialsValidation {

  final case object FieldMissing extends CredentialsValidation {
    override val message: String = "A field is missing"
  }

  final case object UserIdMalformed extends CredentialsValidation {
    override val message: String = "userId is malformed"
  }

  final case object CompanyIdMalformed extends CredentialsValidation {
    override val message: String = "companyId is malformed"
  }

  def validateUserId(value: String): ValidatedNel[CredentialsValidation, UUID] =
    Try(UUID.fromString(value)).map(_.validNel).getOrElse(UserIdMalformed.invalidNel)

  def validateCompanyId(value: String): ValidatedNel[CredentialsValidation, UUID] =
    Try(UUID.fromString(value)).map(_.validNel).getOrElse(CompanyIdMalformed.invalidNel)
}

