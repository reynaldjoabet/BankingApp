package db

import io.getquill.{CamelCase, H2JdbcContext}
import model.commands.Credentials
import model.{Card, Company, Currency, Transfer, Users, Wallet}

import java.time.{LocalDate, LocalDateTime}
import java.util.UUID

object H2BankingRepository  extends BankingRepository {
  private[this] object ctx extends H2JdbcContext(CamelCase, "database")

  import ctx._

  override def authenticate(credentials: Credentials): Option[(UUID, UUID)] = {{
      val q=quote {
        query[Users].join(query[Company]
        ).on {
          case (user, company) => user.companyId == company.companyId
        }.filter { case (user, company) => user.userId == lift(credentials.userId) && company.companyId == lift(credentials.companyId )}
          .map{case (user,company)=>(user.userId,company.companyId)}
      }
      ctx.run(q).headOption
    }
  }

  override def listCompanies: List[Company] = {
    val q=quote{
      query[Company]
    }
    ctx.run(q)
  }

  override def listUsers: List[Users] = {
    val q=quote{
      query[Users]
    }
    ctx.run(q)
  }

  override def listCards(userId: UUID): List[Card] = {
    val q=quote{
      query[Card].filter(_.userId==lift(userId))
    }
    ctx.run(q)
  }


  override def listWallets(companyId: UUID): List[Wallet] = {
    val q=quote{
      query[Wallet].filter(_.companyId==lift(companyId))
    }
    ctx.run(q)
  }

  override def createWallet(id: UUID)(companyId: UUID)(balance: BigDecimal, currency: Currency, isMaster: Boolean): Wallet = {
    val wallet=Wallet(id,balance,currency, companyId, isMaster)
    val q=quote{
      query[Wallet].insert(lift(wallet))
    }
    ctx.run(q)
    wallet
  }

  override def queryWallet(companyId: UUID, walletId: UUID): Option[(UUID, BigDecimal, Currency)] = {
    val q=quote{
      query[Wallet].filter(c=>(c.walletId==lift(walletId))&&(c.companyId==lift(companyId)))
        .map(c=>(c.walletId,c.balance,c.currency))
    }
    ctx.run(q).headOption
  }

  override def queryMasterWallet(currency: Currency): (UUID, BigDecimal)= {
    val q=quote{
      query[Wallet].join(query[Company])
        .on{case (wallet,company)=>company.companyId==wallet.companyId}
        .filter{case (wallet,company)=>company.name=="Holding" && wallet.currency==lift(currency)}
        .map{case (w,_)=>(w.walletId,w.balance)}
    }
    ctx.run(q).head
  }



  override def createCard(currency: Currency)(cardId: UUID, number: String, expirationDate: LocalDate, ccv: String)(userId: UUID)(walletId: UUID): Int = {
    val card= Card(cardId,walletId,currency,balance=0 ,number,expirationDate,ccv,userId,false)
    val q=quote{
      query[Card].insert(lift(card))
    }
   ( ctx.run(q).toInt)
  }

  override def queryCard(cardId: UUID): Option[(UUID, UUID, UUID, BigDecimal, Currency, Boolean)] = {
    val q=quote{
      query[Card].filter(_.cardId==lift(cardId)).map(c=>(c.cardId,c.userId,c.walletId,c.balance,c.currency,c.isBlocked))
    }
    ctx.run(q).headOption
  }

  override def queryWalletBalance(walletId: UUID): BigDecimal = {
    val q=quote{
      query[Wallet].filter(_.walletId==lift(walletId))
        .map(_.balance)
    }
    ctx.run(q).head
  }

  override def setWalletBalance(walletId: UUID)(balance: BigDecimal): Int = {
    val q=quote{
      query[Wallet].filter(_.walletId==lift(walletId)).update(_.balance->lift(balance))
    }
    ctx.run(q).toInt
  }

  override def setCardBalance(cardId: UUID)(balance: BigDecimal): Int = {
    val q=quote{
      query[Card].filter(_.cardId==lift(cardId)).update(_.balance->lift(balance))
    }
    ctx.run(q).toInt
  }

  override def setTransfer(id: UUID, timestamp: LocalDateTime, amount: BigDecimal, sourceCurrency: Currency, targetCurrency: Currency, fees: Option[BigDecimal], source: Transfer.TransferEntity, target: Transfer.TransferEntity): Int = {
    val transfer=Transfer(id , timestamp , amount , sourceCurrency , targetCurrency, fees, source, target)
    val q=quote{
      query[Transfer].insert(lift(transfer))
    }
    ctx.run(q).toInt
  }

  override def blockCard(cardId: UUID): Int = {
    val q=quote{
      query[Card].filter(_.cardId==lift(cardId)).update(_.isBlocked->true)
    }
    ctx.run(q).toInt
  }

  override def unblockCard(cardId: UUID): Int = {
    val q=quote{
      query[Card].filter(_.cardId==lift(cardId)).update(_.isBlocked->false)
    }
    ctx.run(q).toInt
  }
}