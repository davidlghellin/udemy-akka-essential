package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object EjerciciosPart2Actor extends App {

  /**
   * Recrear el Counter Actor in estado mutable, tomar la base de ejercicios anteriores
   */
  object Counter {

    case object Inc
    case object Dec
    case object Print

  }

  class Counter extends Actor {

    import Counter._

    def cuentaRecibe(actualContador: Int): Receive = {
      case Inc =>
        println(s"[actualContador[$actualContador]] Incrementar")
        // esta es la forma de tener un actor sin estado
        context.become(cuentaRecibe(actualContador + 1))
      case Dec =>
        println(s"[actualContador[$actualContador]] Decrementar")
        context.become(cuentaRecibe(actualContador - 1))
      case Print => println(s"[Contador] my actual valor es  $actualContador")
    }

    override def receive: Receive = cuentaRecibe(0)

  }

  import Counter._

  val system = ActorSystem("actorCapacidadesDemo")
  val ejer1Actor = system.actorOf(Props[Counter], "Ejercicio1CounterInmutable")
  (1 to 5) foreach (_ => ejer1Actor ! Inc)
  ejer1Actor ! Dec
  ejer1Actor ! Print


  /**
   * Ejercicio 2 un sistema de votaciones
   */
  // tenemos ciudadnos
  class Ciudadano extends Actor {
    /*
    -------
    mutable
    -------
    */
    // var candidato: Option[String] = None
    // override def receive: Receive = {
    //  case Voto(c) =>  candidato =  Some(c)
    //  case SolicitudEstadoVoto => sender() ! VotosStatusReply(candidato)
    //}

    def voted(candi: String): Receive = {
      case SolicitudEstadoVoto => sender() ! VotosStatusReply(Some(candi))
    }

    override def receive: Receive = {
      case Voto(c) => context.become(voted(c))
      case SolicitudEstadoVoto => sender() ! VotosStatusReply(None)
    }

  }

  // el ciudadno maneajra unos votos
  case class Voto(candidato: String)

  // el ciudadno recibira el voto solamente una vez y este se entregara
  // el agregador de votos preguntara al ciudadano pregunmtado por quien votaron
  // tendremos una clase que seran los votos agregados, este preguntara a los ciudadnos pòr su voto en turno
  // un mensaje llamamdo SolicitudEstadoVoto
  case class AggregateVotes(cudadatos: Set[ActorRef])

  case object SolicitudEstadoVoto

  // cada ciudadano respondera con el voto del candidato
  case class VotosStatusReply(candidato: Option[String])

  // agragador de votos
  class VoteAggregators extends Actor {
    // Mutable
    // para saber que modifciar , quitamos la parte mutable
    var esperandoVoto: Set[ActorRef] = Set()
    var actualEstados: Map[String, Int] = Map()

    override def receive: Receive = {
      case AggregateVotes(ciudadnos) =>
        esperandoVoto = ciudadnos
        ciudadnos.foreach(ciudadaonosRef => ciudadaonosRef ! SolicitudEstadoVoto)
      case VotosStatusReply(None) =>
        // ciudadno que no ha votado
        sender() ! SolicitudEstadoVoto // puede ser bucle infinito sino han votado, nosotros los pondremos qiue voten a todos
      case VotosStatusReply(Some(candidato)) =>
        val newStillWatieng = esperandoVoto - sender()
        val currentVitesDelosCAndidatos = actualEstados.getOrElse(candidato, 0)
        actualEstados = actualEstados + (candidato -> (currentVitesDelosCAndidatos + 1))
        if (newStillWatieng.isEmpty) {
          println(s"[agregador] poll stasts $actualEstados")
        } else {
          esperandoVoto = newStillWatieng
        }
    }

  }

  class VoteAggregatorsInmutable extends Actor {
    override def receive: Receive = awaitingComand

    def awaitingComand: Receive = {
      case AggregateVotes(ciudadnos) =>
        ciudadnos.foreach(ciudadaonosRef => ciudadaonosRef ! SolicitudEstadoVoto)
        context.become(awaitingStatuses(ciudadnos, Map()))
    }

    def awaitingStatuses(stillwaitng: Set[ActorRef], currentStats: Map[String, Int]): Receive = {

      case VotosStatusReply(None) =>
        // ciudadno que no ha votado
        sender() ! SolicitudEstadoVoto // puede ser bucle infinito sino han votado, nosotros los pondremos qiue voten a todos
      case VotosStatusReply(Some(candidato)) =>
        val newStillWatieng = stillwaitng - sender()
        val currentVitesDelosCAndidatos = currentStats.getOrElse(candidato, 0)
        val newactualEstados = currentStats + (candidato -> (currentVitesDelosCAndidatos + 1))
        if (newStillWatieng.isEmpty) {
          println(s"[agregador] poll stasts $newactualEstados")
        } else {
          context.become(awaitingStatuses(newStillWatieng, newactualEstados))
        }
    }

  }

  // tendremos 4 votoantes
  val alice = system.actorOf(Props[Ciudadano])
  val bob = system.actorOf(Props[Ciudadano])
  val charlie = system.actorOf(Props[Ciudadano])
  val daniel = system.actorOf(Props[Ciudadano])

  alice ! Voto("Martin")
  bob ! Voto("David")
  charlie ! Voto("Martin")
  daniel ! Voto("Donal")

  val votoAgregado = system.actorOf(Props[VoteAggregators])
  votoAgregado ! AggregateVotes(Set(alice, bob, charlie, daniel))
  val votoAgregadoInmutable = system.actorOf(Props[VoteAggregatorsInmutable])
  votoAgregadoInmutable ! AggregateVotes(Set(alice, bob, charlie, daniel))

  /*
  Imprimir el estado de los votos, mapa de candidato y votos recibidios
   */

  system.terminate()
}
