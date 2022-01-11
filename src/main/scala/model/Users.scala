package model

import java.util.UUID

import cats.effect.IO
import io.circe.Encoder
import io.circe.generic.semiauto._
import org.http4s.circe.jsonEncoderOf
final case class Users(userId: UUID, companyId: UUID)

object Users {

    implicit val encoder: Encoder[Users] = deriveEncoder[Users]
    implicit  val entityEncoder=jsonEncoderOf[IO,Users]

}
