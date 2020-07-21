package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.CambioComportamientoActor.Madre.MadreStart
import part2actors.CambioComportamientoActor.NinyoExigente.{NinyoAceptar, NinyoRechazar}

object CambioComportamientoActor extends App {

  /*
  Imaginemos que tenemos dos actores, una madre y un niño y la madre alimenta a su hijo y el niño cambia
  su estado depende de la comida que recibe.
  Entonces, si el niño obtiene vegetales, se pone triste.
  Y si el niño consigue un chocolate, se vuelve feliz.

  Ahora imaginemos que después de alimentar a su hijo, la madre le preguntará si quiere jugar con ella.
  El niño puede aceptar o recarzaer
  */
  object NinyoExigente{
    case object NinyoAceptar
    case object NinyoRechazar
    val FELIZ = "feliz"
    val ENFADADO = "enfadado"
  }
  class NinyoExigente extends Actor {
    import Madre._
    import NinyoExigente._
    var estado = FELIZ
    override def receive: Receive = {
      case Comida(VEGETALES) => estado = ENFADADO
      case Comida(CHOCOLATE) => estado = FELIZ
      case Pregunta(_)=>
        if (estado == FELIZ) sender() ! NinyoAceptar
        else sender() ! NinyoRechazar
    }
  }
  object Madre {
    case class MadreStart(kidRef:ActorRef)
    case class Comida(comida: String)
    case class Pregunta(msg: String) // quieres jugar
    val CHOCOLATE = "choco"
    val VEGETALES = "veggies"
  }

  class Madre extends Actor {
    import Madre._
    override def receive: Receive = {
      case MadreStart(kidRef) =>
        // test nuestra integracion
        kidRef ! Comida(VEGETALES)
        kidRef ! Pregunta("Quieres jugar")
      case NinyoAceptar => println("Bien, mi hijo esta feliz")
      case NinyoRechazar => println("Mi hijo esta enfadado")
    }
  }

  val system = ActorSystem("cambioDeEstadoActor")
  val hijo = system.actorOf(Props[NinyoExigente], "David")
  val madre = system.actorOf(Props[Madre], "Mari")
  // ya tenemos tod o, solo nos falta decir a la madre que empiece
  madre ! MadreStart(hijo)

  // dependiendo del estado tenemos que responder una cosa u otra
  // vemos ya que estamos usando variables mutables (uuffaaa ya era hora)
  class EstadoDelNinyo extends Actor {
    import Madre._
    import NinyoExigente._

    // definamos los estado como funciones
    def felizReceive: Receive = {
      case Comida(VEGETALES) => context.become(enfadadoReceive) // cabiare mi contradlor de recepcion a enfadado
      case Comida(CHOCOLATE) =>
      case Pregunta(_) => sender() ! NinyoAceptar
    }

    def enfadadoReceive: Receive = {
      case Comida(VEGETALES) => //enfadado
      case Comida(CHOCOLATE) => context.become(felizReceive)// cabiare mi contradlor de recepcion a feliz
      case Pregunta(_) => sender() ! NinyoRechazar
    }
    override def receive: Receive = felizReceive

  }

  val estadoDelNinyo = system.actorOf(Props[EstadoDelNinyo])
  madre ! MadreStart(estadoDelNinyo)





  system.terminate()
}
