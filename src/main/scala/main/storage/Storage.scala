package main.storage

import com.redis.RedisClient
import org.drinkless.tdlib.TdApi
import params.config

object Storage {
  //  docker run -p:6379:6379 redis
  val channelsToSubscribers = new RedisClient(
    config.channelsToSubscribers.host,
    config.channelsToSubscribers.port
  )
  //  docker run -p:6380:6379 redis
  val subscribersToChannels = new RedisClient(
    config.subscribersToChannels.host,
    config.channelsToSubscribers.port
  )

}

case class Storage() {

  def updatedLastViewedMessage(chatId: Option[String],
                               lastViewedMessageIndex: Int,
                               newMessages: TdApi.Messages): Boolean = {
    Storage.channelsToSubscribers.lset(
      chatId.get,
      0,
      newMessages.messages.head.id
    )
  }

  def getEveryChannel: Option[List[Option[String]]] = {
    val channels = Storage.channelsToSubscribers.keys()
    channels
  }

  def getChannelSubscribersIds(chatId: Option[String]): List[Option[String]] = {
    val subscribersIds =
      Storage.channelsToSubscribers.lrange(chatId.getOrElse(""), 0, -1).get
    subscribersIds
  }

}
