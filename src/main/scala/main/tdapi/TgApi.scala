package main.tdapi

import java.io.{BufferedReader, IOError, IOException, InputStreamReader}
import java.util.concurrent.locks.{Condition, Lock, ReentrantLock}

import org.drinkless.tdlib.TdApi.Message
import org.drinkless.tdlib.{Client, TdApi}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.language.postfixOps

object TgApi {
  val defaultHandler: Client.ResultHandler = new TgApi.DefaultHandler
  val authorizationLock: Lock = new ReentrantLock
  val gotAuthorization: Condition = authorizationLock.newCondition
  val newLine: String = System.getProperty("line.separator")
  val commandsLine: String =
    "Enter command (gcs - GetChats, gc <chatId> - GetChat, me - GetMe, sm <chatId> <message> - SendMessage, lo - LogOut, q - Quit): "
  var client: Client = _
  var authorizationState: TdApi.AuthorizationState = _
  var haveAuthorization: Boolean = false
  var quiting: Boolean = false
  var currentPrompt: String = _
  val channelsIds: ListBuffer[Long] = ListBuffer()
  val messages: mutable.HashMap[Long, List[Message]] =
    new mutable.HashMap[Long, List[Message]]()

  try try System.loadLibrary("tdjni")
  catch {
    case e: UnsatisfiedLinkError =>
      e.printStackTrace()
  }

  def onAuthorizationStateUpdated(
    authorizationState: TdApi.AuthorizationState
  ): Unit = {
    if (authorizationState != null)
      TgApi.authorizationState = authorizationState
    TgApi.authorizationState.getConstructor match {
      case TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR =>
        val parameters: TdApi.TdlibParameters = new TdApi.TdlibParameters
        parameters.databaseDirectory = "tdlib"
        parameters.useMessageDatabase = true
        parameters.useSecretChats = true
        parameters.apiId = 94575
        parameters.apiHash = "a3406de8d171bb422bb6ddf3bbd800e2"
        parameters.systemLanguageCode = "en"
        parameters.deviceModel = "Desktop"
        parameters.systemVersion = "Unknown"
        parameters.applicationVersion = "1.0"
        parameters.enableStorageOptimizer = true
        client.send(
          new TdApi.SetTdlibParameters(parameters),
          new TgApi.AuthorizationRequestHandler
        )

      case TdApi.AuthorizationStateWaitEncryptionKey.CONSTRUCTOR =>
        client.send(
          new TdApi.CheckDatabaseEncryptionKey,
          new TgApi.AuthorizationRequestHandler
        )

      case TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR =>
        val phoneNumber: String = promptString("Please enter phone number: ")
        client.send(
          new TdApi.SetAuthenticationPhoneNumber(phoneNumber, null),
          new TgApi.AuthorizationRequestHandler
        )

      case TdApi.AuthorizationStateWaitCode.CONSTRUCTOR =>
        val code: String = promptString("Please enter authentication code: ")
        client.send(
          new TdApi.CheckAuthenticationCode(code),
          new TgApi.AuthorizationRequestHandler
        )

      case TdApi.AuthorizationStateWaitRegistration.CONSTRUCTOR =>
        val firstName: String = promptString("Please enter your first name: ")
        val lastName: String = promptString("Please enter your last name: ")
        client.send(
          new TdApi.RegisterUser(firstName, lastName),
          new TgApi.AuthorizationRequestHandler
        )

      case TdApi.AuthorizationStateWaitPassword.CONSTRUCTOR =>
        val password: String = promptString("Please enter password: ")
        client.send(
          new TdApi.CheckAuthenticationPassword(password),
          new TgApi.AuthorizationRequestHandler
        )

      case TdApi.AuthorizationStateReady.CONSTRUCTOR =>
        haveAuthorization = true
        authorizationLock.lock()
        try gotAuthorization.signal()
        finally authorizationLock.unlock()

      case TdApi.AuthorizationStateLoggingOut.CONSTRUCTOR =>
        haveAuthorization = false
        print("Logging out")

      case TdApi.AuthorizationStateClosing.CONSTRUCTOR =>
        haveAuthorization = false
        print("Closing")

      case TdApi.AuthorizationStateClosed.CONSTRUCTOR =>
        print("Closed")
        if (!quiting)
          client = Client.create(new TgApi.UpdatesHandler, null, null) // recreate client after previous has closed

      case _ =>
        System.err.println(
          "Unsupported authorization state:" + newLine + TgApi.authorizationState
        )
    }
  }

  def promptString(prompt: String): String = {
    System.out.print(prompt)
    currentPrompt = prompt
    val reader: BufferedReader = new BufferedReader(
      new InputStreamReader(System.in)
    )
    var str: String = ""
    try str = reader.readLine
    catch {
      case e: IOException =>
        e.printStackTrace()
    }
    currentPrompt = null
    str
  }

  def print(str: String): Unit = {
    if (currentPrompt != null) System.out.println("")
    System.out.println(str)
    if (currentPrompt != null) System.out.print(currentPrompt)
  }

  @throws[InterruptedException]
  def main(args: Array[String]): Unit = { // disable TDLib log
    Client.execute(new TdApi.SetLogVerbosityLevel(0))
    if (Client
          .execute(
            new TdApi.SetLogStream(
              new TdApi.LogStreamFile("tdlib.log", 1 << 27)
            )
          )
          .isInstanceOf[TdApi.Error])
      throw new IOError(
        new IOException("Write access to the current directory is required")
      )
    // create client
    client = Client.create(new TgApi.UpdatesHandler, null, null)
    Thread.sleep(2000) //wait for auth
    client.send(new TdApi.GetMe, defaultHandler)
    val rememberHandler = new RememberChannel
    client.send(new TdApi.SearchPublicChat("@vas3k_channel"), rememberHandler)
    client.send(new TdApi.SearchPublicChat("@hoolinomics"), rememberHandler)
    Thread.sleep(2000)
    println(channelsIds.toList)
    client.send(
      new TdApi.GetChatHistory(channelsIds.head, 0, -7, 10, false),
      defaultHandler
    )

  }

  class DefaultHandler extends Client.ResultHandler {
    override def onResult(obj: TdApi.Object): Unit = {
      print(obj.toString)
    }
  }

  class RememberChannel extends Client.ResultHandler {
    override def onResult(obj: TdApi.Object): Unit = {
      print(obj.toString)
      obj match {
        case chat: TdApi.Chat => channelsIds.addOne(chat.id)
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
        // print("Unsupported update:" + newLine + object);
      }
    }
  }

  class AuthorizationRequestHandler extends Client.ResultHandler {
    override def onResult(obj: TdApi.Object): Unit = {
      obj.getConstructor match {
        case TdApi.Error.CONSTRUCTOR =>
          System.err.println("Receive an error:" + newLine + obj)
          onAuthorizationStateUpdated(null) // repeat last action

        case TdApi.Ok.CONSTRUCTOR =>
        // result is already received through UpdateAuthorizationState, nothing to do

        case _ =>
          System.err.println(
            "Receive wrong response from TDLib:" + newLine + obj
          )
      }
    }
  }

}
