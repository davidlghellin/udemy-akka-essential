package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.ActorCapacidades.BankAccount.{Ingresar, Retirar, Saldo}
import part2actors.ActorCapacidades.Person.ViveLaVida

object ActorCapacidades extends App {

  class SimpleActor extends Actor {
    override def receive: Receive = {
      case "hi!" => context.sender() ! "Hello, there!!" // responde el mensaje
      case message: String => println(s"[${context.self}] He recibido: $message")
      case n: Int => println(s"[actor simple] He recibido un NUMERO: $n")
      case SpecialMensaje(msg) => println(s"[actor simple] He recibido un SpecialMensaje: $msg")
      case EnviaMensajesAmiMismo(content) =>
        self ! content // nos enviamos a nosotros el mensaje y entrará como String
      case SayHiTo(ref) => ref ! "hi!"
      case TelefonoInalambricoMensaje(content, ref) => ref forward (content + "s") // mantengo el que envio el mensaje al telefono
      //responder
      //mensaje distorsionado
    }
  }

  val system = ActorSystem("actorCapacidadesDemo")
  val simpleActor = system.actorOf(Props[SimpleActor], "simpleActor")
  simpleActor ! "hola, actor"

  // 1 - los mensajes pueden ser de cualquier tipo
  // a) los mensajes tienen que ser INMUTABLES
  // b) los mensajes deber  de ser SERIALIZABLES
  // en la practica el 99.99% se puede usar case class y case object
  simpleActor ! 3432

  case class SpecialMensaje(message: String)

  simpleActor ! SpecialMensaje("algo especial")

  // 2 - los actores tiene informacion sobre ellos mismos y sobre su contexto
  //  context.self === 'this'
  // podriamos enviarmnos mensajes a nosotros mismos
  case class EnviaMensajesAmiMismo(content: String)

  simpleActor ! EnviaMensajesAmiMismo("Soy un actor y ")
  // Todos los actores trabajan de manera asincrona y no bloqueante

  // 3 - actores puede responder mensanes
  val alice = system.actorOf(Props[SimpleActor], "Alice")
  val bob = system.actorOf(Props[SimpleActor], "Bob")

  // vamos a escribir una case class para que responda
  case class SayHiTo(ref: ActorRef)

  alice ! SayHiTo(bob)

  // 4 - cont5esta a mi
  alice ! "hi!"

  // 5 - reenvio de mensajes
  // daniel -> alice -> bob
  // forwarding = enviar un mensaje al que envio el mensaje original
  case class TelefonoInalambricoMensaje(conten: String, ref: ActorRef)

  alice ! TelefonoInalambricoMensaje("hola", bob)

  /**
   * Ejercicios
   *
   * 1. Acotr que cuente
   *  - Incrementar
   *  - Disminuir
   */

  class Ejercicio1 extends Actor {
    var n = 0

    override def receive: Receive = {
      case "Inc" => n += 1
        println(s"El valor de n es: ${n}")
      case "Dec" => n -= 1
        println(s"El valor de n es: ${n}")
    }
  }

  val ejer1Actor = system.actorOf(Props[Ejercicio1], "Ejercicio1")
  (1 to 5) foreach (_=>ejer1Actor ! "Inc")
  ejer1Actor ! "Dec"

  /**
   * Solucion 1
   */
  object Counter {
    case object Inc
    case object Dec
    case object Print
  }

  class Counter extends Actor {
    import Counter._
    var n = 0

    override def receive: Receive = {
      case Inc => n += 1
      case Dec => n -= 1
      case Print => println(s"[counter] My actual count es: $n")
    }
  }
  /**
   * 2.  Cuenta de un banco como actor
   * receive
   * - Ingresar una cantidad
   * - Retirar una cantidad
   * - Saldo
   * Devolver
   * - exito
   * - fracaso
   */
  object BankAccount{
    case object Saldo
    case class Ingresar(cantidad:Int)
    case class Retirar(cantidad:Int)

    case class TransaccionExito(msg:String)
    case class TransaccionFallida(msg:String)
  }

  class BankAccount extends Actor {
    import BankAccount._
    var dinero = 0
    override def receive: Receive = {
      case Ingresar(n) => {
        if (n < 0) sender ! TransaccionFallida(s"Invalido incorrecto")
        else {
          dinero += n
          sender() ! TransaccionExito(s"Transaccion ok $dinero")
        }
      }
      case Retirar(n) => {
        if (n < 0) sender ! TransaccionFallida(s"Invalido incorrecto")
        else if (n > dinero) sender ! TransaccionFallida(s"Invalido incorrecto importe mayor")
        else {
          dinero -= n
          sender() ! TransaccionExito(s"Transaccion ok $dinero")
        }
      }
      case Saldo =>  sender() ! TransaccionExito(s"Saldo total de $dinero")
    }
  }

  // Crearemos una persona
  object Person{
    case class ViveLaVida(account:ActorRef)
  }
  class Person extends Actor{
    import Person._
    override def receive: Receive = {
      case ViveLaVida(account)=>
        account ! Ingresar(100)
        account ! Ingresar(100)
        account ! Retirar(99100)
        account ! Retirar(50)
        account ! Saldo
      case mensaje=> println(mensaje.toString)
    }
  }
  // ahora solo tenemos que instanciar el banco y la perssoona
  val account = system.actorOf(Props[BankAccount],"bankacoount")
  val person = system.actorOf(Props[Person],"milllonaria")

  person ! ViveLaVida(account)
  system.terminate()
}
/*
Aqui podemos tener ciertas dudas, como el orden es el correcto y otras preguntas

- Un actor tiene un dispacher de mensajes y una cola, un actor siempre tiene un unico hilo
 */