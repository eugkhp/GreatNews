package main.bot

import com.redis.RedisClient
import com.typesafe.scalalogging.Logger
import main.tdapi.TgApi
import org.drinkless.tdlib.TdApi.MessageText
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.{Message, Update}
import params._
import regex._

class Bot extends BotHelper {

  val logger: Logger = Logger("Command handler")

  //  docker run -p:6379:6379 redis
  val channelsToSubscribers = new RedisClient(config.channelsToSubscribers.host, config.channelsToSubscribers.port)
  //  docker run -p:6380:6379 redis
  val subscribersToChannels = new RedisClient(config.subscribersToChannels.host, config.channelsToSubscribers.port)

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
            "Your subscriptions" + "\n" + showSubscriptions(subscriberId)
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
    val channels = channelsToSubscribers.keys()
    if (channels.isDefined) {
      channels.get.foreach { chatId =>
        val chatName = TgApi.getChatInfoById(chatId.get.toLong).title
        val subscribersIds =
          channelsToSubscribers.lrange(chatId.getOrElse(""), 0, -1).get
        val lastViewedMessageId = subscribersIds.head.get.toLong
        val newMessages = getNewMessages(chatId.get.toLong, lastViewedMessageId)
        if (newMessages.messages.head.id != lastViewedMessageId) {
          channelsToSubscribers.lset(
            chatId.get,
            0,
            newMessages.messages.head.id
          )
          newMessages.messages
            .dropRight(1)
            .foreach { message =>
              message.content match {
                case content: MessageText =>
                  redirectMessage(subscribersIds, chatName, content)
              }
            }
        }
      }
    }
    Thread.sleep(3000)
  }

  override def getBotUsername: String = config.userName

  override def getBotToken: String = config.token

}
