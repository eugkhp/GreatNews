package main.storage

import com.redis.RedisClient
import com.typesafe.scalalogging.Logger
import org.drinkless.tdlib.TdApi
import params.config

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.implicitConversions

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

  val logger: Logger = Logger("Storage")

  implicit def getStorageResult(
    storageResult: Option[List[Option[String]]]
  ): List[String] = {
    storageResult match {
      case Some(value) => value.flatten
      case None =>
        logger.error("Could not retrieve data from storage")
        List[String]()
    }
  }

  def updatedLastViewedMessage(chatId: String,
                               lastViewedMessageIndex: Int,
                               newMessages: TdApi.Messages): Boolean = {
    Storage.channelsToSubscribers.lset(chatId, 0, newMessages.messages.head.id)
  }

  def getEveryChannel: List[String] = {
    val channels = Storage.channelsToSubscribers.keys()
    channels
  }

  def getChannelSubscribersIds(chatId: String): List[String] = {
    Storage.channelsToSubscribers.lrange(chatId, 0, -1)
  }

  def deleteChannel(channel: Future[TdApi.Chat],
                    subscriberId: Long): Future[String] = {
    channel.map { value =>
      config.channelsToSubscribers.lrem(value.id, -1, subscriberId) match {
        case Some(_) =>
          if (config.channelsToSubscribers.llen(value.id).get == 1)
            config.channelsToSubscribers.lpop(value.id)
          config.subscribersToChannels.srem(subscriberId, value.id)
          "Channel removed"
        case _ => "You didn't have such channel"
      }
    }
  }

}
