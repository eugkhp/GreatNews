name := "GreatNews"

version := "0.1"

libraryDependencies ++= Seq(
  "org.telegram" % "telegrambots" % "4.4.0.2",
  "com.iheart" %% "ficus" % "1.4.0",
  "ch.qos.logback" % "logback-classic" % "1.1.3" % Runtime,
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "org.typelevel" %% "cats-effect" % "2.0.0",
  "net.debasishg" %% "redisclient" % "3.20"
)
