package services

import akka.util.Timeout
import akka.util.duration._
import akka.pattern.ask
import akka.dispatch.Await
import akkajpa._

/**
 * Created with IntelliJ IDEA.
 * User: jerrywang
 * Date: 8/2/12
 * Time: 3:54 PM
 * To change this template use File | Settings | File Templates.
 */

object JPA {
  //  val logger = Logger

  // JPA reference todo
  //  http://osdir.com/ml/play-framework/2012-03/msg02464.html
  //  https://groups.google.com/forum/#!topic/play-framework/dYDxijsv4QM

  // Play JPA (ThreadLocal Approach)
  //  https://github.com/playframework/Play20/blob/master/framework/src/play/src/main/java/play/db/jpa/JPA.java#L20
  //  https://github.com/playframework/Play20/blob/master/framework/src/play/src/main/java/play/db/jpa/JPAPlugin.java


  implicit lazy val dur = 15 seconds
  implicit lazy val timeout = Timeout(dur)

  def find[T <: AnyRef](clazz:Class[T] , id:Any): Option[T] =
    Option(Await.result(JpaActorSystem.supervisor ? Read(clazz,id), dur).asInstanceOf[T])

  def persist(entity:AnyRef):Unit = Await.result(JpaActorSystem.supervisor ? Create(entity), dur)

  def merge[T <: AnyRef](entity:T):T = Await.result(JpaActorSystem.supervisor ? Update(entity), dur).asInstanceOf[T]

  def remove[T <: AnyRef](entity:T):Unit = Await.result(JpaActorSystem.supervisor ? Delete(entity), dur)

  def query[T](clazz:Class[T] ,hql:String, params: (String, Any)*): List[T] =
    Await.result(JpaActorSystem.supervisor ? Query(hql, params: _*), dur).asInstanceOf[List[T]]

}