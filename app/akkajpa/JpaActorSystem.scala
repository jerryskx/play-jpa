package akkajpa

import akka.actor.{Props, ActorSystem}
import play.api.Logger

/**
 * Created with IntelliJ IDEA.
 * User: jerrywang
 * Date: 8/2/12
 * Time: 4:56 PM
 *
 *
 * References:
 * http://doc.akka.io/docs/akka/snapshot/scala/actors.html
 */


object JpaActorSystem {
  val DISPATCHER = "playjpa-dispatcher"
  val system = ActorSystem("JpaActorSystem")

  val logger = Logger
  logger.debug("In JpaActorSystem")

  // create JpaActorSupervisor Actor at system (top) level, using "playjpa-dispatcher" config
  val supervisor = JpaActorSystem.system.actorOf(Props[JpaActorSupervisor]
    .withDispatcher(DISPATCHER), name = "jpaActorSupervisor")
}

case class Create(obj: AnyRef)
case class Read[T](clazz: Class[T], id: Any)
case class Update(obj: AnyRef)
case class Delete(obj: AnyRef)
case class Query(query: String, params: (String, Any)*)
