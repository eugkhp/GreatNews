import com.typesafe.scalalogging.Logger
import org.telegram.telegrambots._
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException

object Main extends App {

  val logger = Logger("Main")
  ApiContextInitializer.init()
  val botsApi = new TelegramBotsApi()

  val bot = new Bot()
  try botsApi.registerBot(bot)
  catch {
    case ex: TelegramApiRequestException =>
      logger.error(s"Error $ex")
      ex.printStackTrace()
  }
  logger.info("Bot Started")

}
