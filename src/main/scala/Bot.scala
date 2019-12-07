import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.{Message, Update}
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

class Bot extends TelegramLongPollingBot {

  override def onUpdateReceived(update: Update): Unit = {
    println(
      update.getMessage.getText + " by " + update.getMessage.getFrom.getFirstName
    )

    val message = new SendMessage(update.getMessage.getChatId, "hello, friend ")

    try execute[Message, SendMessage](message)
    catch {
      case e: TelegramApiException =>
        e.printStackTrace()
    }

  }

  override def getBotUsername: String = "GreatNewsBot"

  override def getBotToken: String =
    "823708273:AAEsJrfv8U8kgw3zrM8izOCal_ybaMjGfNw"

}
