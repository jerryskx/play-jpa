package akka.jpa

import akka.actor.Actor
import javax.persistence.{EntityTransaction, EntityManager}
import org.hibernate.ejb.QueryHints

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

  lazy val conf = com.typesafe.config.ConfigFactory.load()
  lazy val persistenceUnit = try { conf.getString("akkajpa.name") } catch { case e => "default"}
  var db: Option[EntityManager] = None

  override def preStart() {
    debug("PreStart in JpaActor: initialize EntityManager")
    debug("persistenceUnit -> " + persistenceUnit)
    db = Some(play.db.jpa.JPA.em(persistenceUnit))
  }

  override def postStop() {
    warn("PostStop JpaActor: close EntityManager")
    db.map(em => {if (em.isOpen) em.close()})
    db = None
  }

  def receive = {
    case JpaActorSystem.STOP_JPA_ACTOR => {
      debug("Received stop message")
      context stop self
    }

    case JpaActorSystem.REFRESH_EM => {
      println("reload em")
      postStop()
      preStart()
    }

    case Create(entity) => {
      execute (em => {
        var tx:EntityTransaction = null
        try {
          tx = em.getTransaction
          tx.begin()
          em.persist(entity)
          tx.commit()

          sender ! entity
        } catch {
          case e: Exception => {
            if (tx != null) try { tx.rollback() } catch { case e: Exception => e.printStackTrace()}
            error("Failed to persist entity", e)
            throw e
          }
        }
      })
    }
    case Read(clazz,id) => {
      execute (em => sender ! em.find(clazz,id))
    }

    case Update(entity) => {
      execute (em => {
        var tx:EntityTransaction = null
        try {
          tx = em.getTransaction
          tx.begin()
          val res = em.merge(entity)
          tx.commit()

          sender ! res
        } catch {
          case e: Exception => {
            if (tx != null) try { tx.rollback() } catch { case e: Exception => e.printStackTrace()}
            error("Failed to merge entity", e)
            throw e
          }
        }
      })
    }
    case Delete(entity) => {
      execute (em=>{
        var tx:EntityTransaction = null
        try {
          tx = em.getTransaction
          tx.begin()
          em.remove(em.merge(entity)) // reattached entity to session before delete
          tx.commit()

          sender ! entity
        } catch {
          case e: Exception => {
            if (tx != null) try { tx.rollback() } catch { case e: Exception => e.printStackTrace()}
            error("Failed to merge entity", e)
            throw e
          }
        }
      })
    }
    case Query(hql, hints, params @ _*) => {
      execute (em => {
        val query = em.createQuery(hql)

        hints.cacheMode.map(query.setHint(QueryHints.HINT_CACHE_MODE, _))
        hints.fetchSize.map(query.setHint(QueryHints.HINT_FETCH_SIZE, _))
        hints.readOnly.map(query.setHint(QueryHints.HINT_READONLY, _))
        hints.maxResults.map(query.setMaxResults(_))

        params.foreach(_ match {
          case (key, value) => query.setParameter(key,value)
        })
        sender ! toList(query.getResultList)
      })
    }

    case Transaction(f) => {
      execute (em => {
        var tx:EntityTransaction = null
        try {
          tx = em.getTransaction
          tx.begin()
          val result = f(em)
          tx.commit()

          sender ! result
        } catch {
          case e: Exception => {
            if (tx != null) try { tx.rollback() } catch { case e: Exception => e.printStackTrace()}
            error("Failed to run transaction", e)
            throw e
          }
        }
      })
    }

    // Add any other messages you see fit here. Compaction, multi-get, etc.
    case _ => warn("Received unknown message")
  }

  private def toList(jlist: java.util.List[_]) = new JListWrapper[AnyRef](jlist.asInstanceOf[java.util.List[AnyRef]]).toList

  private def execute(f:EntityManager => Unit):Unit = {
    db.map (em => synchronized {
      f(em)
      em.clear()  // Clear the persistence context, causing all managed entities to become detached.
    })

  }
}
