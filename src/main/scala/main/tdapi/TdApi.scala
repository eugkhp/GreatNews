package main.tdapi

import com.typesafe.scalalogging.Logger
import org.drinkless.tdlib.Client.{ExceptionHandler, ResultHandler}
import org.drinkless.tdlib.example.Example
import org.drinkless.tdlib.{Client, TdApi}

class TdApi {
  val logger: Logger = Logger("TdApi")

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
        parameters.apiHash = "a3406de8d171bb422bb6ddf3bbd800e2"
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
          client = Client.create(new Example.UpdatesHandler, null, null) // recreate client after previous has closed

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

  val tdApi: Client = Client.create(resultHandler, null, defHandler)
  def joinChat(): Unit = {}
}

public static void onAuthorizationStateUpdated(TdApi.AuthorizationState authorizationState) {
  if (authorizationState != null) {
    Example.authorizationState = authorizationState;
  }
  switch (Example.authorizationState.getConstructor()) {
    case TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR:
    TdApi.TdlibParameters parameters = new TdApi.TdlibParameters();
    parameters.databaseDirectory = "tdlib";
    parameters.useMessageDatabase = true;
    parameters.useSecretChats = true;
    parameters.apiId = 94575;
    parameters.apiHash = "a3406de8d171bb422bb6ddf3bbd800e2";
    parameters.systemLanguageCode = "en";
    parameters.deviceModel = "Desktop";
    parameters.systemVersion = "Unknown";
    parameters.applicationVersion = "1.0";
    parameters.enableStorageOptimizer = true;

    client.send(new TdApi.SetTdlibParameters(parameters), new AuthorizationRequestHandler());
    break;
    case TdApi.AuthorizationStateWaitEncryptionKey.CONSTRUCTOR:
      client.send(new TdApi.CheckDatabaseEncryptionKey(), new AuthorizationRequestHandler());
    break;
    case TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR: {
      String phoneNumber = promptString("Please enter phone number: ");
      client.send(new TdApi.SetAuthenticationPhoneNumber(phoneNumber, null), new AuthorizationRequestHandler());
      break;
    }
    case TdApi.AuthorizationStateWaitCode.CONSTRUCTOR: {
      String code = promptString("Please enter authentication code: ");
      client.send(new TdApi.CheckAuthenticationCode(code), new AuthorizationRequestHandler());
      break;
    }
    case TdApi.AuthorizationStateWaitRegistration.CONSTRUCTOR: {
      String firstName = promptString("Please enter your first name: ");
      String lastName = promptString("Please enter your last name: ");
      client.send(new TdApi.RegisterUser(firstName, lastName), new AuthorizationRequestHandler());
      break;
    }
    case TdApi.AuthorizationStateWaitPassword.CONSTRUCTOR: {
      String password = promptString("Please enter password: ");
      client.send(new TdApi.CheckAuthenticationPassword(password), new AuthorizationRequestHandler());
      break;
    }
    case TdApi.AuthorizationStateReady.CONSTRUCTOR:
      haveAuthorization = true;
    authorizationLock.lock();
    try {
      gotAuthorization.signal();
    } finally {
      authorizationLock.unlock();
    }
    break;
    case TdApi.AuthorizationStateLoggingOut.CONSTRUCTOR:
      haveAuthorization = false;
    print("Logging out");
    break;
    case TdApi.AuthorizationStateClosing.CONSTRUCTOR:
      haveAuthorization = false;
    print("Closing");
    break;
    case TdApi.AuthorizationStateClosed.CONSTRUCTOR:
      print("Closed");
    if (!quiting) {
      client = Client.create(new UpdatesHandler(), null, null); // recreate client after previous has closed
    }
    break;
    default:
      System.err.println("Unsupported authorization state:" + newLine + Example.authorizationState);
  }
}

