package org.obrafamily.akkaseed

import akka.actor.{Actor, ActorLogging}

/**
  * Created by bobra on 1/18/16.
  */
class TestActor extends Actor with ActorLogging{

  def receive = {
    case _ => log.info("halo govnuh")

  }

}
