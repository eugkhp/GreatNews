name := "GreatNews"

version := "0.1"

scalaVersion := "2.13.1"

libraryDependencies += "org.telegram" % "telegrambots" % "4.4.0.2"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.3" % Runtime
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
libraryDependencies += "org.typelevel" %% "cats-effect" % "2.0.0"
libraryDependencies += "dev.zio" %% "zio" % "1.0.0-RC17"
libraryDependencies += "net.debasishg" %% "redisclient" % "3.20"
