package main

import com.redis.RedisClient
import com.typesafe.scalalogging.Logger
import main.tdapi.TgApi
import org.drinkless.tdlib.TdApi.Chat
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.{Message, Update}

import scala.util.matching.Regex

object regex {
  val addSubscribe: Regex = "/add\\shttps://t.me/([a-zA-Z0-9-_]+)".r
  val deleteSubscribe: Regex = "/delete\\s([a-zA-Z0-9-_]+)".r
  val deleteAllSubscribes: Regex = "/deleteall".r
  val showSubscribes: Regex = "/show".r
  val info: Regex = "info".r
  val slashInfo: Regex = "/info".r
}

class Bot extends TelegramLongPollingBot {

  val logger: Logger = Logger("Command handler")

  val redis_DB = new RedisClient("localhost", 6379)

  override def onUpdateReceived(update: Update): Unit = {
    logger.info(
      s"Message from: ${update.getMessage.getFrom.getFirstName}, command: ${update.getMessage.getText}"
    )

    if (update.hasMessage & update.getMessage.hasText) {
      val message = new SendMessage()
      val chatIdToWorkWith = update.getMessage.getChatId
      message.setChatId(chatIdToWorkWith)
      update.getMessage.getText match {
        case regex.addSubscribe(some_channel) =>
          val chat = TgApi.findChannelByName(some_channel)
          addSubscribe(chat, chatIdToWorkWith)
          message.setText(s"You added $some_channel to your subscriptions")

        case regex.deleteSubscribe(some_channel) =>
          deleteChannel(
            TgApi.findChannelByName(some_channel).id,
            chatIdToWorkWith
          )
          message.setText(s"You deleted $some_channel from your subscriptions")

        case regex.showSubscribes() =>
          message.setText(
            "Your subscriptions" + "\n" + showSubscribes(chatIdToWorkWith)
          )
        case regex.slashInfo() =>
          message.setText(AllCommands)
        case _ =>
          message.setText("Unknown comand")
      }
      sendApiMethod[Message, SendMessage](message)
    }

  }

  def addSubscribe(channel: Chat, chatIdToRedirectNewPosts: Long): Unit = {
    if (redis_DB.exists(channel.id)) {
      redis_DB.rpush(channel.id, chatIdToRedirectNewPosts)
    } else {
      redis_DB.rpush(channel.id, channel.lastMessage.id)
      redis_DB.rpush(channel.id, chatIdToRedirectNewPosts)
    }
  }

  def showSubscribes(ID: Long): String = ???

  def deleteChannel(channelId: Long, chatIdToRemove: Long): Boolean = ???

  def AllCommands: String =
    "/add channel_link - add channel_link to your subscribes" + "\n\n" +
      "/delete channel_link - delete channel_name from your subscribes" + "\n\n" +
      "/delete all - delete all subscribes" + "\n\n" +
      "/show - show all subscribes"

  override def getBotUsername: String = "GreatNews"

  override def getBotToken: String =
    "1003839370:AAE9GDOjmlefiA4r_yE9HFyrHfnVe7GwCLg"
}
