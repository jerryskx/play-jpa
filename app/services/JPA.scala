package services

import akka.util.Timeout
import akka.util.duration._
import akka.pattern.ask
import akka.dispatch.Await
import akka.jpa._
import javax.persistence.EntityManager

/**
 * Created with IntelliJ IDEA.
 * User: jerrywang
 * Date: 8/2/12
 * Time: 3:54 PM
 *
 * References:
 * JPA
 *  http://osdir.com/ml/play-framework/2012-03/msg02464.html
 *  https://groups.google.com/forum/#!topic/play-framework/dYDxijsv4QM

 * Play JPA (ThreadLocal Approach)
 *  https://github.com/playframework/Play20/blob/master/framework/src/play/src/main/java/play/db/jpa/JPA.java#L20
 *  https://github.com/playframework/Play20/blob/master/framework/src/play/src/main/java/play/db/jpa/JPAPlugin.java
 */

object JPA {
  implicit lazy val dur = 30 seconds
  implicit lazy val timeout = Timeout(dur)

  def find[T <: AnyRef](id:Any)(implicit manifest: Manifest[T]): Option[T] =
    Option(Await.result(JpaActorSystem.supervisor ? Read(manifest.erasure,id), dur).asInstanceOf[T])

  def persist(entity:AnyRef):Unit = Await.result(JpaActorSystem.supervisor ? Create(entity), dur)

  def merge[T <: AnyRef](entity:T):T = Await.result(JpaActorSystem.supervisor ? Update(entity), dur).asInstanceOf[T]

  def remove[T <: AnyRef](entity:T):Unit = Await.result(JpaActorSystem.supervisor ? Delete(entity), dur)

  def query[T](hql:String, params: (String, Any)*): List[T] =
    query(hql, QueryConfig(), params: _*)

  def query[T](hql:String, config: QueryConfig, params: (String, Any)*): List[T] =
    Await.result(JpaActorSystem.supervisor ? Query(hql, config, params: _*), dur).asInstanceOf[List[T]]

  def querySingleResult[T](hql:String, params: (String, Any)*): Option[T] =
    query[T](hql, QueryConfig(maxResults=Some(1)), params: _*).headOption

  def transaction(f:EntityManager => Any): AnyRef =
    Await.result(JpaActorSystem.supervisor ? Transaction(f), dur).asInstanceOf[AnyRef]

}