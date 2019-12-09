package main.tdapi

import java.io.{BufferedReader, IOException, InputStreamReader}
import java.util.concurrent.locks.{Condition, Lock, ReentrantLock}

import com.typesafe.scalalogging.Logger
import org.drinkless.tdlib.Client.{ExceptionHandler, ResultHandler}
import org.drinkless.tdlib.example.Example
import org.drinkless.tdlib.{Client, TdApi}

class TdApi {
  val authorizationLock: Lock = new ReentrantLock
  val logger: Logger = Logger("TdApi")
  val gotAuthorization: Condition = authorizationLock.newCondition
  val newLine: String = System.getProperty("line.separator")
  val commandsLine: String =
    "Enter command (gcs - GetChats, gc <chatId> - GetChat, me - GetMe, sm <chatId> <message> - SendMessage, lo - LogOut, q - Quit): "
  var client: Client = _
  var authorizationState: TdApi.AuthorizationState = _
  var haveAuthorization: Boolean = false
  var quiting: Boolean = false
  var currentPrompt: String = _

  def onAuthorizationStateUpdated(
    authorizationState: TdApi.AuthorizationState
  ): Unit = {
    if (authorizationState != null) {
      Example.authorizationState = authorizationState
    }
    Example.authorizationState.getConstructor match {
      case TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR =>
        val parameters = new TdApi.TdlibParameters
        parameters.databaseDirectory = "tdlib"
        parameters.useMessageDatabase = true
        parameters.useSecretChats = false
        parameters.apiId = 1157399
        parameters.apiHash = "50563ed845128e3e34c2d01f3a9ffc86"
        parameters.systemLanguageCode = "en"
        parameters.deviceModel = "Desktop"
        parameters.systemVersion = "Unknown"
        parameters.applicationVersion = "1.0"
        parameters.enableStorageOptimizer = true
        client.send(
          new TdApi.SetTdlibParameters(parameters),
          new Example.AuthorizationRequestHandler
        )

      case TdApi.AuthorizationStateWaitEncryptionKey.CONSTRUCTOR =>
        client.send(
          new TdApi.CheckDatabaseEncryptionKey,
          new Example.AuthorizationRequestHandler
        )

      case TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR =>
        val phoneNumber = promptString("Please enter phone number: ")
        client.send(
          new TdApi.SetAuthenticationPhoneNumber(phoneNumber, null),
          new Example.AuthorizationRequestHandler
        )

      case TdApi.AuthorizationStateWaitCode.CONSTRUCTOR =>
        val code = promptString("Please enter authentication code: ")
        client.send(
          new TdApi.CheckAuthenticationCode(code),
          new Example.AuthorizationRequestHandler
        )

      case TdApi.AuthorizationStateWaitRegistration.CONSTRUCTOR =>
        val firstName = promptString("Please enter your first name: ")
        val lastName = promptString("Please enter your last name: ")
        client.send(
          new TdApi.RegisterUser(firstName, lastName),
          new Example.AuthorizationRequestHandler
        )

      case TdApi.AuthorizationStateWaitPassword.CONSTRUCTOR =>
        val password = promptString("Please enter password: ")
        client.send(
          new TdApi.CheckAuthenticationPassword(password),
          new Example.AuthorizationRequestHandler
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
          client = Client.create(resultHandler, null, defHandler) // recreate client after previous has closed

      case _ =>
        System.err.println(
          "Unsupported authorization state:" + newLine + Example.authorizationState
        )
    }
  }

  val resultHandler: ResultHandler = (res: TdApi.Object) =>
    logger.info(res.toString)
  val defHandler: ExceptionHandler = (res: TdApi.Object) =>
    logger.info(res.toString)

  def promptString(prompt: String): String = {
    print(prompt)
    currentPrompt = prompt
    val reader = new BufferedReader(new InputStreamReader(System.in))
    var str = ""
    try str = reader.readLine
    catch {
      case e: IOException =>
        e.printStackTrace()
    }
    currentPrompt = null
    str
  }
}
