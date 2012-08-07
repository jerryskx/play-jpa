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

object JpaActorSupervisor {
  val DISPATCHER = "akkajpa-dispatcher"
}
class JpaActorSupervisor extends Actor with Logger {
  var jpaActor: Option[ActorRef] = None
//  val logger = Logger

  // Try to restart the actor 3 times within a minute, otherwise stop it.
  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 3,
    withinTimeRange = 1 minute) {

    case aie: ActorInitializationException => {
      debug("Exception starting JpaActor: " + aie.toString)
      Stop
    }
    case e => {
      debug("Exception in JpaActor: " + e.toString)
      Restart
    }
  }


  override def preStart() {
    debug("Starting JpaActorSupervisor")
    initActor()
  }

  override def postStop() {
    warn("Stopping JpaActorSupervisor")
    jpaActor.foreach(_ ! JpaActorSystem.STOP_JPA_ACTOR )
    // TYRELL: The light that burns twice as bright burns for half as long -
    //         and you have burned so very, very brightly, Roy.
  }

  def initActor() {
    debug("Initializing JpaActor")
    // Chapter 5.10 Routing Scala (Akka Documentation PDF Release 2.1 Page 232)

    // create 5 routees with "supervisorStrategy" and use RR default dispatcher
    val router = RoundRobinRouter(nrOfInstances = 5, supervisorStrategy = supervisorStrategy)

    // create JpaActor with RoundRobinRouter and playjpa-dispatcher
    val children = context.actorOf(
      Props[JpaActor].withRouter(router).withDispatcher(JpaActorSupervisor.DISPATCHER),
      name = "JpaActor")

    context.watch(children)  // watch over my children
    jpaActor = Some(children)
  }

  def receive = {
    case Terminated(actorRef) if Some(actorRef) == jpaActor => {    // children are gone (JPA Actors are all dead); re-init them
      debug("JpaActor ended")
      jpaActor = None
      context.system.scheduler.scheduleOnce(1 minute, self, JpaActorSystem.RESTART_JPA_ACTOR)
      debug("Scheduled restart of JpaActor")
    }

    case JpaActorSystem.RESTART_JPA_ACTOR => {
      debug("Restarting JpaActor")
      initActor()
    }

    case it => {
      jpaActor match {
        case Some(child) => child forward it      // "it" will be the "message"
        case None => context.system.scheduler.scheduleOnce(1 minute, self, it)   // no child (actor) yet; wait for a min; hopefully, child will be back in a min
      }
    }
  }

}
