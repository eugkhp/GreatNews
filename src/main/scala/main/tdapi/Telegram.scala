package main.tdapi

import org.drinkless.tdlib.TdApi.{Chat, Messages}
import org.drinkless.tdlib.{Client, TdApi}

class Telegram(private[this] val client: Client) {
  def findChannelByName(channelName: String): Chat = {
    val handler = new Handlers.DefaultHandler[Chat]
    client.send(new TdApi.SearchPublicChat(channelName), handler)
    handler.getResponse
  }
  def getLastMessagesOfChannel(channelId: Long, count: Int): Messages = {
    val handler = new Handlers.DefaultHandler[Messages]
    client.send(
      new TdApi.GetChatHistory(channelId, 0, 0, count, false),
      handler
    )
    handler.getResponse
  }

}

object Telegram {

  //loading .so files (jni libraries)
  try System.loadLibrary("tdjni")
  catch {
    case e: UnsatisfiedLinkError =>
      e.printStackTrace()
  }

  val ApiClient: Telegram = init()

  private def init(): Telegram = {
    val client = Client.create(new Handlers.UpdatesHandler, null, null)
    new Telegram(client)
  }
}
