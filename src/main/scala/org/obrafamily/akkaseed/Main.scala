package org.obrafamily.akkaseed

import akka.actor.{Props, ActorSystem}

import com.typesafe.config.{Config, ConfigFactory}

/**
  * Created by bobra on 1/18/16.
  */
object Main extends App  {


  val role = { System.getProperty("role","seed1")}
  println(s"starting with role: $role")
  val config = ConfigFactory.parseResources(s"$role.conf").resolve()
  println(s"${config.isResolved}, ${config.root().values()}")
  val system = ActorSystem(config.getString("seed.system.name"),config = config)
  system.actorOf(Props[TestActor]) ! "yo"
  system.awaitTermination()

}
