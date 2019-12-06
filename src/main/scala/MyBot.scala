//import org.telegram.abilitybots.api.bot.AbilityBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.bots.DefaultBotOptions
import org.telegram.telegrambots.meta.generics.{BotOptions, LongPollingBot}
object MyBot {

  class MyBot(val botToken: String, val botUsername: String, val options: DefaultBotOptions) extends LongPollingBot {



    def onUpdateReceived(update: Update): Unit = {
      if (update.hasMessage) print(update.getMessage.getText)
      else print("No message")

    }

    override def getBotUsername: String = "GreatNews"

    override def getBotToken: String = "1003839370:AAE9GDOjmlefiA4r_yE9HFyrHfnVe7GwCLg"

    override def getOptions: BotOptions = options

    override def clearWebhook(): Unit = print("clearing")
  }

}
