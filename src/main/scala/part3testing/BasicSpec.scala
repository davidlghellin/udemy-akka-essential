package part3testing

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

import scala.concurrent.duration._
import scala.util.Random

class BasicSpec
  extends TestKit(ActorSystem("BasicSpec"))
    with ImplicitSender
    with WordSpecLike
    with BeforeAndAfterAll {

  // setup
  def aflterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "The thing beijng teste" should {
    "do this" in {
      // testing sceniraio
    }
  }

  import BasicSpec._

  "A simple actor" should {
    "send back the same message" in {
      val echoActor = system.actorOf(Props[SimpleActor])
      val mensaje = "Hola test"
      echoActor ! mensaje
      expectMsg(mensaje)
    }
  }

  "A blackhhole actor" should {
    "send back the same message blasckhole" in {
      val black = system.actorOf(Props[BlackHole])
      val mensaje = "Hola test"
      black ! mensaje
      expectNoMessage(1 second)
    }
  }
  // assertions
  "A lab test actor" should {
    val labActorTest = system.actorOf(Props[LabActor])
    "convierte en mayusculas" in {
      labActorTest ! "yo amo Akka"
      expectMsg("YO AMO AKKA")
    }
    "tipo string" in {
      labActorTest ! "yo amo Akka"
      val reply = expectMsgType[String]
      assert(reply == "YO AMO AKKA")
    }
    "contestar al saludo" in {
      labActorTest ! "saludar"
      expectMsgAnyOf("hi", "hola")
    }
    "contestar fav tech" in {
      labActorTest ! "favTech"
      expectMsgAllOf("Scala", "Akka")
    }
    "contestar fav tech numero" in {
      labActorTest ! "favTech"
      val mensjaes = receiveN(2) // Seq[Any]

    }
    "contestar fav tech de una camino cool" in {
      labActorTest ! "favTech"
      // no lo veo
      expectMsgPF() {
        case "Scala" => // solo toma que estos valores son definidos
        case "Akka" =>
      }
    }
  }
}

object BasicSpec {

  class SimpleActor extends Actor {
    override def receive: Receive = {
      case msg => sender() ! msg
    }
  }

  class BlackHole extends Actor {
    override def receive: Receive = Actor.emptyBehavior
  }

  class LabActor extends Actor {
    val random = new Random()

    override def receive: Receive = {
      case "saludar" =>
        if (random.nextBoolean()) sender() ! "hi" else sender() ! "hola"
      case "favTech" =>
        sender() ! "Scala"
        sender() ! "Akka"
      case msg: String => sender() ! msg.toUpperCase()
    }
  }

}