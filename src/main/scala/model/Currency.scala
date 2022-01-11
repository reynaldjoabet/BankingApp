package model

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import io.getquill.MappedEncoding

sealed trait Currency extends Product with Serializable

object Currency {


  final case object USD extends Currency
implicit val  currencyCodec:Codec[Currency]=deriveCodec[Currency]
  final case object GBP extends Currency

  final case object EUR extends Currency

def fromString(str: String)= str match {
  case "USD"=>USD
  case "GBP"=>GBP
  case "EUR"=>EUR
}
  implicit val encodeCurrency=MappedEncoding[Currency,String](_.toString)

  implicit val decodeCurrency=MappedEncoding[String,Currency](Currency.fromString(_))
}
