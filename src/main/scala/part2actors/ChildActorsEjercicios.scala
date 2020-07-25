package part2actors

import akka.actor.Actor

object ChildActorsEjercicios extends App {
  // contar palabas distribuido
  object WordCounterMaster{
    case class Initialize(nChilden:Int)
    case class WordCountTask(text:String)
    case class WordCountReply(count:Int)
  }
  /*
  Entonces, la forma en que esto funcionara es que el maestro de conteo de palabras recibira un fragmento de texto
  y creara un mensaje de tipo de recuento de palabras con colmillo que enviara a uno de sus hijos de trabajadores
  de guerra que a su vez, después de la computación, responderá al contador maestro con una respuesta de conteo
  de palabras y un mensaje de contador de palabras responderá al centro original
   */
  class WordCounterMaster extends Actor{
    override def receive: Receive = ???
  }
  class WordCounterWorker extends Actor{
    override def receive: Receive = ???
  }

  /*
  Crear   WordCounterMaster
  Enviar  Initialize(10) to WordCounterMaster
  Enviar "Akka es divertido" to WordCounterMaster
    wcm enviarara WordCountTask a  uno de los hijos
      el hijo respondera con WordCountReply(3) al maestro
    master replies con 3 al sender

  requester -> wcm -> wcw
         r <- wcm <-
   */
}
