package main.bot

import main.tdapi.TgApi
import org.drinkless.tdlib.TdApi
import org.drinkless.tdlib.TdApi.{Chat, MessageText, Messages}
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import params._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

trait BotHelper extends TelegramLongPollingBot {

  def addSubscription(channel: Future[Chat], subscriberId: Long): Unit = {
    channel.onComplete {
      case Success(value) =>
        if (config.channelsToSubscribers.exists(value.id)) {
          config.channelsToSubscribers.rpush(value.id, subscriberId)
        } else {
          config.channelsToSubscribers
            .rpush(value.id, value.lastMessage.id) // lastMessageId is first elem
          config.channelsToSubscribers.rpush(value.id, subscriberId)
        }
        config.subscribersToChannels.sadd(subscriberId, value.id)
      case Failure(exception) => throw exception
    }
  }

  def showSubscriptions(subscriberId: Long): String = {
    var answer: StringBuilder = new StringBuilder()
    config.subscribersToChannels.smembers(subscriberId) match {
      case Some(set) =>
        set.foreach(v => {
          v.map(_.toLong) match {
            case Some(id) =>
              val eventualChat = TgApi.getChatInfoById(id)
              answer.appendAll(eventualChat + "\n")
          }
        })
      case None =>
        answer = new StringBuilder
        answer.appendAll("No subscriptions")
    }
    answer.toString()
  }

  def getNewMessages(chatId: Long,
                     lastViewedMessageId: Long): Future[TdApi.Messages] =
    TgApi.getLastMessagesOfChannel(chatId, lastViewedMessageId, -99, 99)

  def sendNewMessagesToSubscribers(newMessages: Messages,
                                   subscribersIds: Seq[String],
                                   chat: TdApi.Chat): Unit = {

    newMessages.messages
      .dropRight(1) //removed lastViewedMessage
      .foreach { message =>
        message.content match {
          case content: MessageText =>
            redirectMessage(subscribersIds, chat.title, content)
        }
      }
  }

  def redirectMessage(subscribersIds: Seq[String],
                      chatName: String,
                      messageContent: MessageText): Unit = {
    val redirectedMessage = new SendMessage()
    redirectedMessage.enableHtml(true)
    redirectedMessage.setText(
      "<strong>" + chatName + "</strong>\n" + messageContent.text.text
    )

    // removed first elem because it's lastMessageId not subscriberId
    subscribersIds.drop(1).foreach { chatIdToRedirectOpt =>
      val chatIdToRedirect = chatIdToRedirectOpt.toLong
      redirectedMessage.setChatId(chatIdToRedirect)
      execute[Message, SendMessage](redirectedMessage)
    }
  }

  def AllCommands: String =
    "/add channel_link - add channel_link to your subscribes" + "\n\n" +
      "/delete channel_link - delete channel_name from your subscribes" + "\n\n" +
      "/delete all - delete all subscribes" + "\n\n" +
      "/show - show all subscribes"

}
