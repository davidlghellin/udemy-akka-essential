package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.ChidActors.CreditCard.{AttachToAccount, CheckStatus}

object ChidActors extends App {

  // Los actores pueden crear actores

  object Padre {
    case class CrearHijo(nombre: String)
    case class DecirHijo(msg: String)
  }

  class Padre extends Actor {
    import Padre._
    var hijo: ActorRef = null
    override def receive: Receive = {
      case CrearHijo(name) =>
        println(s"${self.path}: creando hijo")
        // creamos el hijo
        val hijoRef = context.actorOf(Props[Hijo], name)
        context.become(withHijo(hijoRef))

    }

    def withHijo(hijoRef: ActorRef): Receive = {
      case DecirHijo(msg) => hijoRef forward msg
    }
  }

  class Hijo extends Actor {
    override def receive: Receive = {
      case msg => println(s"${self.path}: tengo el $msg")
    }
  }

  import Padre._

  val system = ActorSystem("CreacionHijoDemos")
  val padre = system.actorOf(Props[Padre], "padree")
  padre ! CrearHijo("hijo")
  padre ! DecirHijo("Hola chico")
  // los actores pueden crear actores con:
  // context.actorOf...

  // esto le da a Akka la habilidad de crear de crear jeraquias de actores

  // Los actores tienen unos padres que no podemos tratar, actores guardas (top-level).
  // /system = system guardian
  // /user = user-level guardian
  // / = root guardian, si a este la pasa algo, el sistemas de actores se ve afectado

  /**
   * Actor selection
   */
  val hijoSelection = system.actorSelection("/user/padree/hijo")
  // esto es un wraper para selecionar
  hijoSelection ! "He encontrado al hijo"

  /**
   * Peligro!!
   * NUNCA PASES EL ESTADO MUTABLE DEL ACTOR O LA REFERENCIA A LOS HIJOS ACTORES
   *
   * NUNCA EN TU VIDA
   */

  // TODO nunca ver ejemplo
  object NavieBankAccount {
    case class Deposit(cantidad: Int)
    case class Retirar(cantidad: Int)
    case object InitializeAccount
  }

  class NavieBankAccount extends Actor {

    import CreditCard._
    import NavieBankAccount._

    var amount = 0

    override def receive: Receive = {
      case InitializeAccount =>
        val creditCardRef = context.actorOf(Props[CreditCard], "card")
        creditCardRef ! AttachToAccount(this) // !!!!
      case Deposit(founds) => deposit(founds)
      case Retirar(founds) => retirar(founds)
    }

    def deposit(n: Int) = {
      println(s"${self.path} depositando $n en $amount")
      amount += n
    }

    def retirar(n: Int) = {
      println(s"${self.path} retirando $n en $amount")
      amount -= n
    }
  }

  object CreditCard {
    case class AttachToAccount(bankAccount: NavieBankAccount) //// TODO maaaaal
    case object CheckStatus
  }

  class CreditCard extends Actor {

    override def receive: Receive = {
      case AttachToAccount(account) => context.become(attachedTo(account))
    }

    def attachedTo(account: NavieBankAccount): Receive = {
      case CheckStatus =>
        println(s"${self.path} tu mensaje ha sidop  procesado")

        account.retirar(1) // esto se esta accediendo directamente a la cuenta y se salta toda la logica
    }
  }

  import NavieBankAccount._

  val bankAccountRef = system.actorOf(Props[NavieBankAccount], "account")
  bankAccountRef ! InitializeAccount
  bankAccountRef ! Deposit(100)

  Thread.sleep(500)
  val ccSelection = system.actorSelection("/user/account/card")
  ccSelection ! CheckStatus
  // nos hemos saltado toda la logica y el sistema de seguridad de los actores
  // Este es una retirada ilegitima porque estoiy enviadno un mensaje de estado de texto sin formato a mi tarjeta de credito

  // !!MAAAAL !!!!!!
  // este error es muy muy dicifil de debugear
  // llamar a un metodo directamente a un actor omite toda la logica y todos los posibles controles de seguridad
  // que normalmente hariamos

  // el problema se orgino en:
  // case class AttachToAccount(bankAccount: NavieBankAccount)
  // lo que normalemente hariamos
  // case class AttachToAccount(bankAccount: ActorRef)
  // tendriamos que arreglar el 80% del codigo
  // la interaccion con los actores debe ser a traves de mensajes y nunca nunca a atraves de metodos de llamada

  // esto se llama "colsing over" cieer sobre estado mutable, nuestro trabajo es mantener la encapsulacion
  // del actor.

  // repasando
  // never close over mutable state or `this`!

  system.terminate()
}
