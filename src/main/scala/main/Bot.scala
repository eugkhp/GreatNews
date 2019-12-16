package main

import com.redis.RedisClient
import main.tdapi.TgApi
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.{Message, Update}

import scala.util.matching.Regex

object regex {
  val addSubscribe: Regex = "/add\\shttps://t.me/([A-Za-z0-9]+)".r
  val deleteSubscribe: Regex = "/delete\\s([A-Za-z0-9]+)".r
  val deleteAllSubscribes: Regex = "/deleteall".r
  val showSubscribes: Regex = "/show".r
  val info: Regex = "info".r
  val slashInfo: Regex = "/info".r
}

class Bot extends TelegramLongPollingBot {
  val redis_DB = new RedisClient("localhost", 6379)

  override def onUpdateReceived(update: Update): Unit = {
    //    println(update.getMessage.getFrom.getId + " " + update.getMessage.getFrom.getFirstName) //358029352

    if (update.hasMessage & update.getMessage.hasText) {
      val message = new SendMessage()
      message.setChatId(update.getMessage.getChatId)
      update.getMessage.getText match {
        case regex.deleteSubscribe(some_channel) =>
          println(
            update.getMessage.getFrom.getId + " " + update.getMessage.getFrom.getFirstName + " delete " + some_channel
          )
          deleteChannel(update.getMessage.getFrom.getId, some_channel)
          message.setText(s"You deleted $some_channel from your subscribes")
        case regex.addSubscribe(some_channel) =>
          println(
            update.getMessage.getFrom.getId + " " + update.getMessage.getFrom.getFirstName + " add " + some_channel
          )
          addSubscribe(update.getMessage.getFrom.getId, some_channel)
          message.setText(s"You added $some_channel to your subscribes")
        case regex.deleteAllSubscribes() =>
          println(
            update.getMessage.getFrom.getId + " " + update.getMessage.getFrom.getFirstName + " delete all"
          )
          deleteAllSubscribes(update.getMessage.getFrom.getId)
          message.setText("You don't have subscribes")

        case regex.showSubscribes() =>
          println(
            update.getMessage.getFrom.getId + " " + update.getMessage.getFrom.getFirstName + " show"
          )
          message.setText(
            "Your subscribes" + "\n" + showSubscribes(
              update.getMessage.getFrom.getId
            )
          )
        case regex.slashInfo() =>
          println(
            update.getMessage.getFrom.getId + " " + update.getMessage.getFrom.getFirstName + " info"
          )
          message.setText(AllComands)
        case _ =>
          println(
            update.getMessage.getFrom.getId + " " + update.getMessage.getFrom.getFirstName + " unknown command"
          )
          message.setText("Unknown comand")
      }
      sendApiMethod[Message, SendMessage](message)
    }

  }

  def deleteChannel(ID: Int, channel: String): Boolean = {
    redis_DB.set(
      ID.toString,
      redis_DB
        .get(ID.toString)
        .getOrElse("")
        .split(" ")
        .toList
        .filter(_ != channel)
        .foldLeft("")(_ + _ + " ")
    )
  }

  def addSubscribe(ID: Int, channel: String): AnyVal = {
    if (!showSubscribes(ID).contains(channel)) {
      redis_DB.set(
        ID.toString,
        redis_DB.get(ID.toString).getOrElse("") + channel + " "
      )
    }

  }

  def showSubscribes(ID: Int): String = {
    redis_DB.get(ID.toString).getOrElse("")

  }

  def deleteAllSubscribes(ID: Int): Boolean = {
    //    println("deleteAllSubscribes")
    redis_DB.set(ID.toString, "")
  }

  def AllComands: String =
    "/add channel_link - add channel_link to your subscribes" + "\n\n" +
      "/delete channel_link - delete channel_name from your subscribes" + "\n\n" +
      "/delete all - delete all subscribes" + "\n\n" +
      "/show - show all subscribes"

  override def getBotUsername: String = "GreatNews"

  override def getBotToken: String =
    "823708273:AAEsJrfv8U8kgw3zrM8izOCal_ybaMjGfNw"

  def ChannelsByPass(): Unit = {
    redis_DB.keys().foreach{ key =>
      val value = redis_DB.get(key)
      println(value)
//      val message = new SendMessage()
//      message.setChatId(value.getOrElse(""))
//      message.setText("hello")
//      sendApiMethod[Message, SendMessage](message)
    }
    Thread.sleep(3000)
  }
}
