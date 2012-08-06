package akkajpa

import akka.util.duration._
import akka.actor._
import akka.routing.RoundRobinRouter
import akka.actor.SupervisorStrategy.{Restart, Stop}
//import play.api.Logger
import utils.Logger

/**
 * Created with IntelliJ IDEA.
 * User: jerrywang
 * Date: 8/2/12
 * Time: 4:57 PM
 * To change this template use File | Settings | File Templates.
 */

class JpaActorSupervisor extends Actor with Logger {
  var jpaActor: Option[ActorRef] = None
//  val logger = Logger

  // Try to restart the actor 3 times within a minute, otherwise stop it.
  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 3,
    withinTimeRange = 1 minute) {

    case aie: ActorInitializationException => {
      logger.debug("Exception starting JpaActor: " + aie.toString)
      Stop
    }
    case e => {
      logger.debug("Exception in JpaActor: " + e.toString)
      Restart
    }
  }


  override def preStart() {
    logger debug "Starting JpaActorSupervisor"
    initActor()
  }

  override def postStop() {
    logger warn "Stopping JpaActorSupervisor"
    jpaActor.foreach(_ ! "Attack ships on fire off the shoulder of Orion." )
    // TYRELL: The light that burns twice as bright burns for half as long -
    //         and you have burned so very, very brightly, Roy.
  }

  def initActor() {
    logger debug "Initializing JpaActor"
    jpaActor = Some(context.watch(context.actorOf(Props[JpaActor]
      .withRouter(RoundRobinRouter(5, supervisorStrategy = supervisorStrategy))
      .withDispatcher(JpaActorSystem.DISPATCHER), name = "JpaActor")))
  }

  def receive = {
    case Terminated(actorRef) if Some(actorRef) == jpaActor => {
      logger debug "JpaActor ended"
      jpaActor = None
      context.system.scheduler.scheduleOnce(1 minute, self, "Don't you die on me!")
      logger debug "Scheduled restart of JpaActor"
    }

    // http://tvtropes.org/pmwiki/pmwiki.php/Main/HowDareYouDieOnMe
    case "Don't you die on me!" => {
      logger debug "Restarting JpaActor"
      initActor()
    }

    // This is obfuscated. You really want the variable "together" to be "message"
    // and the variable "moving" to be "act".
    case together => {
      debug("that's forward the message")
      jpaActor match {
        case Some(moving) => moving forward together // All that for this LOC?
        case None => context.system.scheduler.scheduleOnce(1 minute, self, together)
      }
    }
  }

}
