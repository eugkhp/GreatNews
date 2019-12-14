import org.telegram.telegrambots._
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.generics.WebhookBot
import org.telegram.telegrambots.meta.api.methods.send.{SendAnimation, SendLocation, SendMessage}
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException



//под Windows:
//https://github.com/dmajkic/redis/downloads
//под Linux:
//https://redis.io/download
//разархивировать и запустить редис сервер(так на Windows)
object Main extends App {

  ApiContextInitializer.init()
  val botsApi = new TelegramBotsApi()

  val mybot = new Bot()
  try botsApi.registerBot(mybot)
  catch {
    case ex: TelegramApiRequestException => ex.printStackTrace()
  }
  print("Hello world!")

}