package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ChildActorsEjercicios extends App {
  // contar palabas distribuido
  object WordCounterMaster{
    case class Initialize(nChilden:Int)
    case class WordCountTask(id:Int,text:String)
    case class WordCountReply(id:Int,count:Int)
  }
  /*
  Entonces, la forma en que esto funcionara es que el maestro de conteo de palabras recibira un fragmento de texto
  y creara un mensaje de tipo de recuento de palabras con colmillo que enviara a uno de sus hijos de trabajadores
  de guerra que a su vez, después de la computación, responderá al contador maestro con una respuesta de conteo
  de palabras y un mensaje de contador de palabras responderá al centro original
   */
  class WordCounterMaster extends Actor {
    import WordCounterMaster._

    override def receive: Receive = {
      case Initialize(n) =>
        println("[master] iniciado")
        val childrenRefs = for (i <- 1 to n) yield context.actorOf(Props[WordCounterWorker], s"wcw_$i")
        context.become(withChildren(childrenRefs, 0, 0, Map()))
    }

    def withChildren(childrenRefs: Seq[ActorRef], currentChildIndex: Int, currentTaskId: Int, requestMap: Map[Int, ActorRef]): Receive = {
      case text: String =>
        println(s"[master] He recibido $text - envio al hijo $currentChildIndex")
        val originalSender = sender()
        val task = WordCountTask(currentTaskId, text)
        val childRef = childrenRefs(currentChildIndex)
        childRef ! task
        val nextChildIndex = (currentChildIndex + 1) % childrenRefs.length
        val newTaskId = currentTaskId + 1
        val newRequesMap = requestMap + (currentTaskId -> originalSender)
        context.become(withChildren(childrenRefs, nextChildIndex, newTaskId, newRequesMap))
      case WordCountReply(id, count) =>
        println(s"[master] Tengo que devolver el id $id con $count")
        val originalSender = requestMap(id)
        originalSender ! count
        context.become(withChildren(childrenRefs, currentChildIndex, currentTaskId, requestMap - id))
    }
  }

  class WordCounterWorker extends Actor {
    import WordCounterMaster._
    override def receive: Receive = {
      // sender Remitente de referencia Actor del último mensaje recibido, generalmente utilizado como se describe en Actor
      case WordCountTask(id, text) =>
        println(s"${self.path} Tengo auna tarea con el $text")
        sender() ! WordCountReply(id, text.split(" ").length)
    }
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
  class TestActor extends Actor {

    import WordCounterMaster._

    override def receive: Receive = {
      case "go" =>
        val master = context.actorOf(Props[WordCounterMaster], "master")
        master ! Initialize(3)
        val text = List("Yo amo Akka", "scala es super ", "si", "yo tb")
        text.foreach(txt => master ! txt)
      case count: Int =>
        println(s"[Test Actor] = $count")
    }
  }

  val system = ActorSystem("roundRobinWordCount")
  val testActor = system.actorOf(Props[TestActor], "testactor")
  testActor ! "go"

  //system.terminate()
}
