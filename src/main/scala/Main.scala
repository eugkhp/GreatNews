import org.telegram.telegrambots._
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.generics.WebhookBot
import org.telegram.telegrambots.meta.api.methods.send.{SendAnimation, SendLocation, SendMessage}
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException
import org.telegram.telegrambots.bots.DefaultBotOptions
import org.telegram.telegrambots.meta.ApiContext
import org.telegram.telegrambots.meta.bots.AbsSender




object Main extends App {

  ApiContextInitializer.init()
  val botsApi = new TelegramBotsApi()
//  val botOptions = ApiContext.getInstance(classOf[DefaultBotOptions])
//  botOptions.setProxyHost("127.0.0.1")
//  botOptions.setProxyPort(9150)
//   Select proxy type: [HTTP|SOCKS4|SOCKS5] (default: NO_PROXY)
//  botOptions.setProxyType(DefaultBotOptions.ProxyType.SOCKS5)

//  val mybot = new MyBot.MyBot("1003839370:AAE9GDOjmlefiA4r_yE9HFyrHfnVe7GwCLg", "GreatNews", botOptions)
  val mybot = new Bot()
  try botsApi.registerBot(mybot)
  catch {
    case ex: TelegramApiRequestException => ex.printStackTrace()
  }
//mybot.execute(new SendAnimation())


  print("Hello world!")


}
