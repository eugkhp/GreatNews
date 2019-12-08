name := "GreatNews"

version := "0.1"

scalaVersion := "2.13.1"



libraryDependencies += "org.telegram" % "telegrambots" % "4.4.0.2"


libraryDependencies ++= Seq(
  "net.debasishg" %% "redisclient" % "3.20"
)

// https://mvnrepository.com/artifact/org.slf4j/slf4j-api
libraryDependencies += "org.slf4j" % "slf4j-api" % "2.0.0-alpha1"
