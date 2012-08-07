package akkajpa

import akka.actor.Actor
import javax.persistence.{EntityTransaction, EntityManager}

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

    case Create(entity) => {
      db.map (em => {
        var tx:EntityTransaction = null
        try {
          tx = em.getTransaction
          tx.begin()
          em.persist(entity)
          tx.commit()

          sender ! entity
        } catch {
          case e: Exception => {
            try { tx.rollback() } catch { case e: Exception => e.printStackTrace()}
            error("Failed to persist entity", e)
            throw e
          }
        }
      })
    }
    case Read(clazz,id) => {
      db.map(em => sender ! em.find(clazz,id))
    }

    case Update(entity) => {
      db.map (em => {
        var tx:EntityTransaction = null
        try {
          tx = em.getTransaction
          tx.begin()
          val res = em.merge(entity)
          tx.commit()

          sender ! res
        } catch {
          case e: Exception => {
            try { tx.rollback() } catch { case e: Exception => e.printStackTrace()}
            error("Failed to merge entity", e)
            throw e
          }
        }
      })
    }
    case Delete(entity) => {
      db.map(em=>{
        var tx:EntityTransaction = null
        try {
          tx = em.getTransaction
          tx.begin()
          em.remove(em.merge(entity)) // reattached entity to session before delete
          tx.commit()

          sender ! entity
        } catch {
          case e: Exception => {
            try { tx.rollback() } catch { case e: Exception => e.printStackTrace()}
            error("Failed to merge entity", e)
            throw e
          }
        }
      })
    }
    case Query(hql, params @ _*) => {
      db.map (em => {
        val query = em.createQuery(hql)
        params.foreach(_ match {
          case (key, value) => query.setParameter(key,value)
        })
        sender ! toList(query.getResultList)
      })
    }

    // Add any other messages you see fit here. Compaction, multi-get, etc.
    case _ => debug("Received unknown message")
  }

  private def toList(jlist: java.util.List[_]) = new JListWrapper[AnyRef](jlist.asInstanceOf[java.util.List[AnyRef]]).toList
}
