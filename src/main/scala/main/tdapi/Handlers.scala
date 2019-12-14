package main.tdapi

import cats.effect.concurrent.MVar
import cats.effect.{ContextShift, IO}
import com.typesafe.scalalogging.Logger
import main.tdapi.TgLogin.onAuthorizationStateUpdated
import org.drinkless.tdlib.{Client, TdApi}

import scala.concurrent.ExecutionContext

object Handlers {

  val logger: Logger = Logger("Handlers")
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  class DefaultHandler[A] extends Client.ResultHandler {
    val response: IO[MVar[IO, A]] = MVar.empty[IO, A]
    override def onResult(obj: TdApi.Object): Unit = {
      logger.info(obj.toString)
      obj match {
        case obj: A => response.flatMap(_.put(obj))
        case _      => logger.error("Wrong expected type")
      }
    }
    def getResponse: A = {
      response.flatMap(_.take).unsafeRunSync()
    }
  }

  class AuthorizationRequestHandler extends Client.ResultHandler {
    override def onResult(obj: TdApi.Object): Unit = {
      obj.getConstructor match {
        case TdApi.Error.CONSTRUCTOR =>
          System.err.println("Receive an error:" + "\n" + obj)
          onAuthorizationStateUpdated(null) // repeat last action

        case TdApi.Ok.CONSTRUCTOR =>
        // result is already received through UpdateAuthorizationState, nothing to do

        case _ =>
          System.err.println("Receive wrong response from TDLib:" + "\n" + obj)
      }
    }
  }

  class UpdatesHandler extends Client.ResultHandler {
    override def onResult(obj: TdApi.Object): Unit = {
      obj.getConstructor match {
        case TdApi.UpdateAuthorizationState.CONSTRUCTOR =>
          onAuthorizationStateUpdated(
            obj
              .asInstanceOf[TdApi.UpdateAuthorizationState]
              .authorizationState
          )
        case _ =>
        // print("Unsupported update:" + "\n" + object);
      }
    }
  }
}
