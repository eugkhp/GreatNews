package main.tdapi

import java.io.{BufferedReader, IOError, IOException, InputStreamReader}
import java.util.concurrent.locks.{Condition, Lock, ReentrantLock}

import com.typesafe.scalalogging.Logger
import org.drinkless.tdlib.{Client, TdApi}

object TgLogin {

  val logger: Logger = Logger("Login")
  val defaultHandler: Client.ResultHandler = new Handlers.DefaultHandler
  val authorizationLock: Lock = new ReentrantLock
  val gotAuthorization: Condition = authorizationLock.newCondition
  val commandsLine: String =
    "Enter command (gcs - GetChats, gc <chatId> - GetChat, me - GetMe, sm <chatId> <message> - SendMessage, lo - LogOut, q - Quit): "
  var client: Client = _
  var authorizationState: TdApi.AuthorizationState = _
  var haveAuthorization: Boolean = false
  var quiting: Boolean = false
  var currentPrompt: String = _

  Client.execute(new TdApi.SetLogVerbosityLevel(0))
  if (Client
        .execute(
          new TdApi.SetLogStream(new TdApi.LogStreamFile("tdlib.log", 1 << 27))
        )
        .isInstanceOf[TdApi.Error])
    throw new IOError(
      new IOException("Write access to the current directory is required")
    )

  def onAuthorizationStateUpdated(
    authorizationState: TdApi.AuthorizationState
  ): Unit = {
    if (authorizationState != null)
      TgLogin.authorizationState = authorizationState
    TgLogin.authorizationState.getConstructor match {
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
          new Handlers.AuthorizationRequestHandler
        )

      case TdApi.AuthorizationStateWaitEncryptionKey.CONSTRUCTOR =>
        client.send(
          new TdApi.CheckDatabaseEncryptionKey,
          new Handlers.AuthorizationRequestHandler
        )

      case TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR =>
        val phoneNumber: String = promptString("Please enter phone number: ")
        client.send(
          new TdApi.SetAuthenticationPhoneNumber(phoneNumber, null),
          new Handlers.AuthorizationRequestHandler
        )

      case TdApi.AuthorizationStateWaitCode.CONSTRUCTOR =>
        val code: String = promptString("Please enter authentication code: ")
        client.send(
          new TdApi.CheckAuthenticationCode(code),
          new Handlers.AuthorizationRequestHandler
        )

      case TdApi.AuthorizationStateWaitRegistration.CONSTRUCTOR =>
        val firstName: String = promptString("Please enter your first name: ")
        val lastName: String = promptString("Please enter your last name: ")
        client.send(
          new TdApi.RegisterUser(firstName, lastName),
          new Handlers.AuthorizationRequestHandler
        )

      case TdApi.AuthorizationStateWaitPassword.CONSTRUCTOR =>
        val password: String = promptString("Please enter password: ")
        client.send(
          new TdApi.CheckAuthenticationPassword(password),
          new Handlers.AuthorizationRequestHandler
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
          client = Client.create(new Handlers.UpdatesHandler, null, null) // recreate client after previous has closed

      case _ =>
        logger.error(
          "Unsupported authorization state:" + "\n" + TgLogin.authorizationState
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

}
