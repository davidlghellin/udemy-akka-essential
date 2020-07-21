package playground

import akka.actor.ActorSystem

object Playgorund extends App {
  val actorSystem = ActorSystem("HolaAkka")
  println(actorSystem.name)
}
