package main.bot

import com.typesafe.scalalogging.Logger
import main.storage.Storage
import main.tdapi.TgApi
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.{Message, Update}
import params._
import regex._

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
        case addSubscribe(some_channel) =>
          val channel = TgApi.findChannelByName(some_channel)
          addSubscription(channel, subscriberId)
          message.setText(s"You added $some_channel to your subscriptions")

        case deleteSubscribe(some_channel) =>
          deleteChannel(TgApi.findChannelByName(some_channel).id, subscriberId)
          message.setText(s"You deleted $some_channel from your subscriptions")

        case showSubscribes() =>
          message.setText(
            "Your subscriptions:" + "\n\n" + showSubscriptions(subscriberId)
          )
        case slashInfo() =>
          message.setText(AllCommands)
        case _ =>
          message.setText("Unknown command")
      }
      execute[Message, SendMessage](message)
    }
  }

  def ChannelsByPass(): Unit = {
    val channels = storage.getEveryChannel
    if (channels.isDefined) {
      channels.get.foreach { chatId =>
        val chatName = TgApi.getChatInfoById(chatId.get.toLong).title
        val subscribersIds = storage.getChannelSubscribersIds(chatId)
        val lastViewedMessageId = subscribersIds.head.get.toLong
        val newMessages = getNewMessages(chatId.get.toLong, lastViewedMessageId)
        if (newMessages.messages.head.id != lastViewedMessageId) {
          storage.updatedLastViewedMessage(chatId, 0, newMessages)
          sendNewMessagesToSubscribers(newMessages, subscribersIds, chatName)
        }
      }
    }
    Thread.sleep(3000)
  }

  override def getBotUsername: String = config.userName

  override def getBotToken: String = config.token

}
