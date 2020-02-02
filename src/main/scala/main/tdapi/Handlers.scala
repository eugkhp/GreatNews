package main.tdapi

import com.typesafe.scalalogging.Logger
import main.tdapi.TgLogin.onAuthorizationStateUpdated
import org.drinkless.tdlib.{Client, TdApi}

import scala.concurrent.{Future, Promise}

object Handlers {

  val logger: Logger = Logger("Handlers")

  class DefaultHandler[A] extends Client.ResultHandler {

    private val response: Promise[A] = Promise[A]()

    override def onResult(obj: TdApi.Object): Unit = {
      obj match {
        case obj: A => response.success(obj)
        case _      => logger.error("Wrong expected type")
      }
    }
    def eventualResponse: Future[A] = response.future

  }

  class AuthorizationRequestHandler extends Client.ResultHandler {

    val authStatus: Promise[Boolean] = Promise()
    def eventualAuth: Future[Boolean] = authStatus.future

    override def onResult(obj: TdApi.Object): Unit = {
      obj.getConstructor match {
        case TdApi.Error.CONSTRUCTOR =>
          logger.error("Receive an error:" + "\n" + obj)
          onAuthorizationStateUpdated(null, authStatus) // repeat last action

        case TdApi.Ok.CONSTRUCTOR =>
        // result is already received through UpdateAuthorizationState, nothing to do

        case _ =>
          logger.error("Receive wrong response from TDLib:" + "\n" + obj)
      }
    }
  }

  class UpdatesHandler extends Client.ResultHandler {

    val authStatus: Promise[Boolean] = Promise()
    def eventualAuth: Future[Boolean] = authStatus.future

    override def onResult(obj: TdApi.Object): Unit = {
      obj.getConstructor match {
        case TdApi.UpdateAuthorizationState.CONSTRUCTOR =>
          onAuthorizationStateUpdated(
            obj
              .asInstanceOf[TdApi.UpdateAuthorizationState]
              .authorizationState,
            authStatus
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
