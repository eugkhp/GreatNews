package main

import com.typesafe.scalalogging.Logger
import org.drinkless.tdlib.example.Example
import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException

object Main extends App {

  //to call tdlib Api
  // -Djava.library.path=<absolute path to these sources>/src/main/java/org/drinkless/tdlib/example
  // needs to be added to VM options
  //class Example - is the example of usage
  val logger = Logger("Main")

  Example.main(arguments)

  val bot = new Bot()
  ApiContextInitializer.init()
  val botsApi = new TelegramBotsApi()
  val arguments = Array[String]()

  try botsApi.registerBot(bot)
  catch {
    case ex: TelegramApiRequestException =>
      logger.error(s"Error $ex")
      ex.printStackTrace()
  }
  logger.info("Bot Started")

}
