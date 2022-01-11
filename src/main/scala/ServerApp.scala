import cats.effect.{ExitCode, IO, IOApp}
import org.http4s.blaze.server.BlazeServerBuilder
import routes.BankingRoutes

object ServerApp extends IOApp{

  val routes= BankingRoutes.routes
  val server=BlazeServerBuilder[IO]
    .bindHttp(8080,"localhost")
    .withHttpApp(routes.orNotFound)
    .resource
    .use(_ =>IO.never)
    .as(ExitCode.Success)


  override def run(args: List[String]): IO[ExitCode] = server
}
