import com.redis.RedisClient
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.{SendLocation, SendMessage, SendVideo}
import org.telegram.telegrambots.meta.api.objects.{Message, Update}


object regex {
  val addSubscribe = "/add\\shttps://t.me/(\\w+)".r
  val deleteSubscribe = "/delete\\s(\\w+)".r
  val deleteAllSubscribes = "/deleteall".r
  val showSubscribes = "/show".r
  val info = "info".r
  val slashInfo = "/info".r
}

class Bot extends TelegramLongPollingBot {
  val redis_DB = new RedisClient("localhost", 6379)

  override def onUpdateReceived(update: Update): Unit = {
//    println(update.getMessage.getFrom.getId + " " + update.getMessage.getFrom.getFirstName) //358029352

    if (update.hasMessage & update.getMessage.hasText) {
      val message = new SendMessage()
      message.setChatId(update.getMessage.getChatId)
      update.getMessage.getText match {
        case regex.deleteSubscribe(some_channel) => {
          println(update.getMessage.getFrom.getId + " " + update.getMessage.getFrom.getFirstName + " delete " + some_channel)
          deleteChannel(update.getMessage.getFrom.getId, some_channel)
          message.setText(s"You deleted $some_channel from your subscribes")
        }
        case regex.addSubscribe(some_channel) => {
          println(update.getMessage.getFrom.getId + " " + update.getMessage.getFrom.getFirstName + " add " + some_channel)
          addSubscribe(update.getMessage.getFrom.getId, some_channel)
          message.setText(s"You added $some_channel to your subscribes")
        }
        case regex.deleteAllSubscribes() => {
          println(update.getMessage.getFrom.getId + " " + update.getMessage.getFrom.getFirstName + " delete all")
          deleteAllSubscribes(update.getMessage.getFrom.getId)
          message.setText("You don't have subscribes")
        }

        case regex.showSubscribes() => {
          println(update.getMessage.getFrom.getId + " " + update.getMessage.getFrom.getFirstName + " show")
          message.setText("Your subscribes" + "\n" + showSubscribes(update.getMessage.getFrom.getId))
        }
        case regex.slashInfo() => {
          println(update.getMessage.getFrom.getId + " " + update.getMessage.getFrom.getFirstName + " info")
          message.setText(AllComands)
        }
        case _ => {
          println(update.getMessage.getFrom.getId + " " + update.getMessage.getFrom.getFirstName + " unknown command")
          message.setText("Unknown comand")
        }
      }
      sendApiMethod[Message, SendMessage](message)
    }

  }

  override def getBotUsername: String = "GreatNews"

  override def getBotToken: String = "1003839370:AAE9GDOjmlefiA4r_yE9HFyrHfnVe7GwCLg"

  def showSubscribes(ID: Int): String = {
//    println("showSubscribes")
//    println(redis_DB.get(ID.toString).getOrElse(""))
    redis_DB.get(ID.toString).getOrElse("")

  }

  def deleteChannel(ID: Int, channel: String) = {
//    println("deleteChannel")
    redis_DB.set(ID.toString, redis_DB.get(ID.toString).getOrElse("").split(" ").toList
      .filter(_ != channel)
      .foldLeft("")(_ + _ + " "))
  }

  def addSubscribe(ID: Int, channel: String) = {
//    println("addSubscribe")
//    println(redis_DB.get(ID.toString).getOrElse(""))
    if(!showSubscribes(ID).contains(channel)){
      redis_DB.set(ID.toString, redis_DB.get(ID.toString).getOrElse("") + channel + " ")
    }

  }

  def deleteAllSubscribes(ID: Int) = {
//    println("deleteAllSubscribes")
    redis_DB.set(ID.toString, "")
  }

  def AllComands: String = "/add channel_link - add channel_link to your subscribes" + "\n\n" +
    "/delete channel_link - delete channel_name from your subscribes" + "\n\n" +
    "/delete all - delete all subscribes" + "\n\n" +
    "/show - show all subscribes"
}

