package main.tdapi
import org.drinkless.tdlib.TdApi.{Chat, Messages}
import org.drinkless.tdlib.{Client, TdApi}

import scala.concurrent.Future

object TgApi {

  //loading .so files (jni libraries)
  try System.loadLibrary("tdjni")
  catch {
    case e: UnsatisfiedLinkError =>
      e.printStackTrace()
  }

  var client: Client = _
  def initClient(): Unit = {
    client = TgLogin.init()
  }

  def findChannelByName(channelName: String): Future[Chat] = {
    val handler = new Handlers.DefaultHandler[Chat]
    client.send(new TdApi.SearchPublicChat(channelName), handler)
    handler.eventualResponse
  }

  /**
    * Get Messages from the Channel(or Chat) by Id.
    *
    * @param channelId     Id of the channel you want to get messages from.
    * @param fromMessageId Id of the starting point message(to select older or/and newer messages use 'offset' and 'count').
    *                      Put 0 if you want to get last message only (offset and count will not affect result).
    *                      To get more than only last message specify 'fromMessageId', than with offset and count you can get channel history.
    * @param offset        Always should be negative. If < 0  you get specified number of newer messages from 'fromMessageId'.
    * @param count         Max number of returned messages if offset < 0, than 'count' > '-offset'. To get past messages 'count' should be > 0.
    *                      (if 'fromMessageId' not 0)
    * @return Array of messages.
    */
  def getLastMessagesOfChannel(channelId: Long,
                               fromMessageId: Long,
                               offset: Int,
                               count: Int): Future[Messages] = {
    val handler = new Handlers.DefaultHandler[Messages]
    client.send(
      new TdApi.GetChatHistory(channelId, fromMessageId, offset, count, false),
      handler
    )
    handler.eventualResponse
  }

  def getChatInfoById(chatId: Long): Future[Chat] = {
    val handler = new Handlers.DefaultHandler[Chat]
    client.send(new TdApi.GetChat(chatId), handler)
    handler.eventualResponse
  }

  def getMessageById(channelId: Long, messageId: Long): Future[Messages] = {
    getLastMessagesOfChannel(channelId, messageId, 0, 1)
  }
}
