package main.tdapi

import cats.effect.{ContextShift, IO}
import com.typesafe.scalalogging.Logger
import main.tdapi.TgLogin.onAuthorizationStateUpdated
import org.drinkless.tdlib.{Client, TdApi}

import scala.concurrent.ExecutionContext

object Handlers {

  val logger: Logger = Logger("Handlers")
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  class DefaultHandler[A] extends Client.ResultHandler {
    @volatile
    var response: A = _

    override def onResult(obj: TdApi.Object): Unit = {
      logger.info(obj.toString)
      obj match {
        case obj: A => response = obj
        case _      => logger.error("Wrong expected type")
      }
    }

    def getResponse: A = {
      while (response == null) {}
      response
    }
  }

  class AuthorizationRequestHandler extends Client.ResultHandler {
    override def onResult(obj: TdApi.Object): Unit = {
      obj.getConstructor match {
        case TdApi.Error.CONSTRUCTOR =>
          logger.error("Receive an error:" + "\n" + obj)
          onAuthorizationStateUpdated(null) // repeat last action

        case TdApi.Ok.CONSTRUCTOR =>
        // result is already received through UpdateAuthorizationState, nothing to do

        case _ =>
          logger.error("Receive wrong response from TDLib:" + "\n" + obj)
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
        //print("Unsupported update:" + "\n" + obj);
      }
    }
  }

  class PrintHandler extends Client.ExceptionHandler with Client.ResultHandler {
    override def onException(e: Throwable): Unit =
      logger.error(e.getCause.getMessage, e.getMessage)

    override def onResult(obj: TdApi.Object): Unit = {
      logger.info(obj.toString)
    }
  }

}
