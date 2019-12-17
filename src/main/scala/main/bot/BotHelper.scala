package main.bot

import main.tdapi.TgApi
import org.drinkless.tdlib.TdApi.Chat
import params._

trait BotHelper {

  def addSubscription(channel: Chat, subscriberId: Long): Unit = {
    if (config.channelsToSubscribers.exists(channel.id)) {
      config.channelsToSubscribers.rpush(channel.id, subscriberId)
    } else {
      config.channelsToSubscribers.rpush(channel.id, channel.lastMessage.id) // first elem is lastMessageId
      config.channelsToSubscribers.rpush(channel.id, subscriberId)
    }
    config.subscribersToChannels.sadd(subscriberId, channel.id)
  }

  def showSubscriptions(subscriberId: Long): String = {
    var answer: StringBuilder = new StringBuilder()
    config.subscribersToChannels.smembers(subscriberId) match {
      case Some(set) =>
        set.foreach(v => {
          v.map(_.toLong) match {
            case Some(id) =>
              answer.appendAll(TgApi.getChatInfoById(id).title + "\n")
          }
        })
      case None =>
        answer = new StringBuilder
        answer.appendAll("No subscriptions")
    }
    answer.toString()
  }


  def deleteChannel(channelIdToRemove: Long, subscriberId: Long): String = {
    config.channelsToSubscribers.lrem(channelIdToRemove, -1, subscriberId) match {
      case Some(0) => "You didn't have such channel"
      case Some(_) =>
        if (config.channelsToSubscribers.llen(channelIdToRemove).get == 1)
          config.channelsToSubscribers.lpop(channelIdToRemove)
        config.subscribersToChannels.srem(subscriberId, channelIdToRemove)
        "Channel removed"
      case None => "You didn't have such channel"
    }
  }

  def AllCommands: String =
    "/add channel_link - add channel_link to your subscribes" + "\n\n" +
      "/delete channel_link - delete channel_name from your subscribes" + "\n\n" +
      "/delete all - delete all subscribes" + "\n\n" +
      "/show - show all subscribes"


}
