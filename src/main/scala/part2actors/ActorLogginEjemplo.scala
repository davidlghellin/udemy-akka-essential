package part2actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.event.Logging

object ActorLogginEjemplo extends App {

  class SimpleActorWithExplictLogger extends Actor {
    val logger = Logging(context.system, this)

    /* 4 niveles de log
      1 - DEBUG
      2 - INFO
      3 - WARNING/WARN
      4 - ERROR
     */
    override def receive: Receive = {
      case msg => logger.info(msg.toString) // log it
    }
  }

  val system = ActorSystem("LoggingDemo")
  val actor = system.actorOf(Props[SimpleActorWithExplictLogger])

  actor ! "Logging a simple message"

  // #2 - ActorLogging indicando en la clase
  class ActorWithLogging extends Actor with ActorLogging {
    override def receive: Receive = {
      case (a, b) => log.info("Two things: {} and {}", a, b) // Two things: 2 and 3
      case message => log.info(message.toString)
    }
  }

  val simplerActor = system.actorOf(Props[ActorWithLogging])
  simplerActor ! "Logging a simple message by extending a trait"

  simplerActor ! (42, 65)
  /*
  El primer registro completo se realiza de forma asincrónica para minimizar el impacto en el rendimiento.
  En particular, el sistema de registro que utilizamos en este video se implementa utilizando los propios actores.
  En segundo lugar, el registro no depende de una implementación particular del registrador.
   */
}
