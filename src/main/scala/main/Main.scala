package main

import com.typesafe.scalalogging.Logger
import main.tdapi.TgApi
import org.drinkless.tdlib.TdApi
import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException

object Main extends App {

  //to call tdlib Api
  // -Djava.library.path=<absolute path to these sources>/src/main/java/org/drinkless/tdlib/example
  // needs to be added to VM options

  val logger = Logger("Main")

//  TgApi.init()
//  Thread.sleep(3000) // waiting for client authorization
//
//  //example of usage
//  private val chat: TdApi.Chat = TgApi.findChannelByName("vas3k_channel")
//  private val id: Long = chat.id
//  println(id)
//  private val messages: TdApi.Messages =
//    TgApi.getLastMessagesOfChannel(id, 727711744, 0, 3)
//  println(messages.totalCount)
//
//  ApiContextInitializer.init()
  val bot = new Bot()
//  val botsApi = new TelegramBotsApi()
//  try botsApi.registerBot(bot)
//  catch {
//    case ex: TelegramApiRequestException =>
//      logger.error(s"Error $ex")
//      ex.printStackTrace()
//  }
//  logger.info("Bot Started")

  while(true) {
    bot.ChannelsByPass()
  }
}
