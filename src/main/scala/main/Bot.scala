package main

import java.util.concurrent.LinkedTransferQueue

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
  val deleteSubscribe: Regex = "/delete\\shttps://t.me/([a-zA-Z0-9-_]+)".r
  val deleteAllSubscribes: Regex = "/deleteall".r
  val showSubscribes: Regex = "/show".r
  val info: Regex = "info".r
  val slashInfo: Regex = "/info".r
}

class Bot extends TelegramLongPollingBot {

  val logger: Logger = Logger("Command handler")

  // (channelId, subscriberId)
  val subscriptionsToRemove = new LinkedTransferQueue[(Long, Long)]

  //  docker run -p:6379:6379 redis
  val channelsToChats = new RedisClient("localhost", 6379)
  //  docker run -p:6380:6379 redis
  val subscribersToChannels = new RedisClient("localhost", 6380)

  override def onUpdateReceived(update: Update): Unit = {
    logger.info(
      s"Message from: ${update.getMessage.getFrom.getFirstName}, command: ${update.getMessage.getText}"
    )

    if (update.hasMessage & update.getMessage.hasText) {
      val message = new SendMessage()
      val subscriberId = update.getMessage.getChatId // id of the chat to respond
      message.setChatId(subscriberId)
      update.getMessage.getText match {
        case regex.addSubscribe(some_channel) =>
          val channel = TgApi.findChannelByName(some_channel)
          addSubscription(channel, subscriberId)
          message.setText(s"You added $some_channel to your subscriptions")

        case regex.deleteSubscribe(some_channel) =>
          deleteChannel(TgApi.findChannelByName(some_channel).id, subscriberId)
          message.setText(s"You deleted $some_channel from your subscriptions")

        case regex.showSubscribes() =>
          message.setText(
            "Your subscriptions" + "\n" + showSubscriptions(subscriberId)
          )
        case regex.slashInfo() =>
          message.setText(AllCommands)
        case _ =>
          message.setText("Unknown command")
      }
      sendApiMethod[Message, SendMessage](message)
    }

  }

  def addSubscription(channel: Chat, subscriberId: Long): Unit = {
    if (channelsToChats.exists(channel.id)) {
      channelsToChats.rpush(channel.id, subscriberId)
    } else {
      channelsToChats.rpush(channel.id, channel.lastMessage.id) // first elem is lastMessageId
      channelsToChats.rpush(channel.id, subscriberId)
    }
    subscribersToChannels.sadd(subscriberId, channel.id)
  }

  def showSubscriptions(subscriberId: Long): String = {
    var answer: StringBuilder = new StringBuilder
    subscribersToChannels.smembers(subscriberId) match {
      case Some(set) =>
        set.foreach(v => {
          v.map(_.toLong) match {
            case Some(id) =>
              answer.addAll(TgApi.getChatInfoById(id).title + "\n")
          }
        })
      case None =>
        answer = new StringBuilder
        answer.addAll("No subscriptions")
    }
    answer.toString()
  }

  def deleteChannel(channelIdToRemove: Long, subscriberId: Long): String = {
    subscribersToChannels.srem(subscriberId, channelIdToRemove) match {
      case Some(_) =>
        subscriptionsToRemove.add((channelIdToRemove, subscriberId))
        "Channel removed"
      case None => "No such channel"
      case Some(0) =>
        "No such channel" // хз что там возвращается(None или 0) если ничего не находит
    }
  }

  def AllCommands: String =
    "/add channel_link - add channel_link to your subscribes" + "\n\n" +
      "/delete channel_link - delete channel_name from your subscribes" + "\n\n" +
      "/delete all - delete all subscribes" + "\n\n" +
      "/show - show all subscribes"

  override def getBotUsername: String = "GreatNews"

  override def getBotToken: String =
    "1003839370:AAE9GDOjmlefiA4r_yE9HFyrHfnVe7GwCLg"
}
