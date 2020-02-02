package main.bot

import com.typesafe.scalalogging.Logger
import main.storage.Storage
import main.tdapi.TgApi
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.{Message, Update}
import params._
import regex._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class Bot extends BotHelper {

  val logger: Logger = Logger("Command handler")
  val storage: Storage = Storage()

  override def onUpdateReceived(update: Update): Unit = {
    logger.info(
      s"Message from: ${update.getMessage.getFrom.getFirstName}, command: ${update.getMessage.getText}"
    )

    if (update.hasMessage & update.getMessage.hasText) {
      val message = new SendMessage()
      val subscriberId = update.getMessage.getChatId // id of the chat to respond
      message.setChatId(subscriberId)
      update.getMessage.getText match {
        case addSubscribeCommand(some_channel) =>
          val channel = TgApi.findChannelByName(some_channel)
          addSubscription(channel, subscriberId)
          message.setText(s"You added $some_channel to your subscriptions")

        case deleteChannelCommand(some_channel) =>
          val channel = TgApi.findChannelByName(some_channel)
          storage.deleteChannel(channel, subscriberId).andThen {
            case Success(res)       => message.setText("deleted")
            case Failure(exception) => logger.error(exception.getMessage)
          }

        case showSubscribesCommand() =>
          message.setText(
            "Your subscriptions:" + "\n\n" + showSubscriptions(subscriberId)
          )
        case infoCommand() =>
          message.setText(AllCommands)

        case _ =>
          message.setText("Unknown command")
      }
      execute[Message, SendMessage](message)
    }
  }

  def ChannelsByPass(): Unit = {
    val channels = storage.getEveryChannel
    channels.foreach { channelId =>
      val eventualChat = TgApi.getChatInfoById(channelId.toLong)
      val subscribersIds = storage.getChannelSubscribersIds(channelId)
      val lastViewedMessageId = subscribersIds.head
      val eventualMessages =
        getNewMessages(channelId.toLong, lastViewedMessageId.toLong)

      val eventualTuple = for {
        chat <- eventualChat
        newMessages <- eventualMessages
        if newMessages.messages.head.id != lastViewedMessageId.toLong
      } yield (chat, newMessages)
      eventualTuple.onComplete {
        case Failure(exception) => throw exception
        case Success((chat, newMessages)) =>
          storage.updatedLastViewedMessage(channelId, 0, newMessages)
          sendNewMessagesToSubscribers(newMessages, subscribersIds, chat)
      }
    }
  }

  override def getBotUsername: String = config.userName

  override def getBotToken: String = config.token

}
