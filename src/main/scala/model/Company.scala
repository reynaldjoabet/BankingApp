package model

import java.util.UUID
import cats.Show
import cats.effect.IO
import io.circe.Encoder
import org.http4s.circe.{CirceEntityEncoder, jsonEncoderOf}
import io.circe.generic.semiauto._

final case class Company(
                          companyId: UUID,
                          name: String
                        )

object Company {


    implicit  val entityEncoder=jsonEncoderOf[IO,Company]
    implicit val encoder: Encoder[Company] = deriveEncoder[Company]



}