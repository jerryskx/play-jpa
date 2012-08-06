package akkajpa

import akka.actor.Actor
import javax.persistence.EntityManager

//import play.api.Logger
import utils.Logger
import scala.collection.JavaConversions._

/**
 * Created with IntelliJ IDEA.
 * User: jerrywang
 * Date: 8/2/12
 * Time: 5:01 PM
 * To change this template use File | Settings | File Templates.
 */

class JpaActor extends Actor with Logger {
//  val logger = Logger


  lazy val conf = com.typesafe.config.ConfigFactory.load()
  lazy val persistenceUnit = try { conf.getString("playjpa.name") } catch { case e => "default"}
  var db: Option[EntityManager] = None

  override def preStart() {
    logger debug "PreStart in JpaActor"
//    initEntityManager()
    debug("persistenceUnit -> " + persistenceUnit)
    val em = play.db.jpa.JPA.em(persistenceUnit)
    db = Some(em)
  }

  override def postStop() {
    logger warn "postStop JpaActor"
    db.map(_.close())
    db = None
  }

  def receive = {

    case "Attack ships on fire off the shoulder of Orion." => {
      debug("Received stop message")
      context stop self
    }

    case Query(hql, params @ _*) => {
      debug ("Query: " + hql)
      params.foreach(_ match {
        case (key, value) => println(key + " -> " + value.toString)
      })
      var result:List[AnyRef] = Nil
      db.map (em => {
        sender ! toList(em.createQuery(hql).getResultList)
      })
      result
    }

    // Add any other messages you see fit here. Compaction, multi-get, etc.
    case _ => debug("Received unknown message")
  }


  private def toList(jlist: java.util.List[_]) = new JListWrapper[AnyRef](jlist.asInstanceOf[java.util.List[AnyRef]]).toList
}
