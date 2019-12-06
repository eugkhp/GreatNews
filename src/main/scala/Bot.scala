import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.{BotApiMethod, GetMe}
import org.telegram.telegrambots.meta.api.methods.send.{SendLocation, SendMessage, SendVideo}
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import org.telegram.telegrambots.meta.generics.BotOptions
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
class Bot extends TelegramLongPollingBot {


  override def onUpdateReceived(update: Update): Unit = {
    println(update.getMessage.getText + " by " + update.getMessage.getFrom.getFirstName)

    val message = new SendMessage(update.getMessage.getChatId, "hello, friend")

    try execute(message) // Sending our message object to user
    catch {
      case e: TelegramApiException =>
        e.printStackTrace()
    }

  }

  override def getBotUsername: String = "GreatNews"

  override def getBotToken: String = "1003839370:AAE9GDOjmlefiA4r_yE9HFyrHfnVe7GwCLg"


}
