package part2actors

import akka.actor.{Actor, ActorSystem, Props}

object ActorsIntro extends App {
  // 1 actor-system
  // es un sistema pesado, ya que controla una serie de hilos
  // se recomienda tener uno por aplicacion, a no ser q tenga buenas razones para tener más
  val actorSystem = ActorSystem("fistActorSystem")
  println(actorSystem.name)

  // 2 creacion de actores
  // un actor son como humanos hablamndo entre si
  // 1- un acotr se identifica de manera unica dentro de un sistema de actores
  // 2- los mensajes se pasan y procesan de manera asincrona para q pueda enviar el mensaje cuando lo necesite y responder cuando puedan
  // 3- cada actor tine un comportamiento unico, o forma de procesar el mensaje unico
  // 4- no puedes invadir a otro actor, leer su mente o forzar a dar la informacion

  // actor contarPalabas
  class WordCountActor extends Actor {
    //internal data
    var total = 0

    // behavior
    def receive: PartialFunction[Any, Unit] = {
      case message: String =>
        println(s"[wordCount] He recibido: $message")
        total += message.split(" ").length
      case msg => println(s"No entiendo ${msg.toString}")
    }
  }

  // 3 instanciar nuestro actor
  val wc = actorSystem.actorOf(Props[WordCountActor], "worctcounter")
  val wcOtro = actorSystem.actorOf(Props[WordCountActor], "worctcounterOtro")

  // 4 - comunication !
  wc ! "Estoy aprendiendo akka "
  wcOtro ! "Un mensaje diferente"

  //asincrono


  // que pasa si una clase extiende de Actor
  class Person(name: String) extends Actor {
    override def receive: Receive = {
      case "hi" => println(s"hola, mi nombre es $name")
      case _ =>
    }
  }

  // una forma no aconsejada
  val personBad = actorSystem.actorOf(Props(new Person("Bob")))
  personBad ! "hi"

  // la forma correcta es con companion object
  object Person {
    def propsMy(name: String) = Props(new Person(name))
  }

  val person = actorSystem.actorOf(Person.propsMy("David"))
  person ! "hi"

  actorSystem.terminate()
}
