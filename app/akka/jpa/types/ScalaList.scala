package akka.jpa.types

import org.hibernate.usertype.UserCollectionType
import org.hibernate.persister.collection.CollectionPersister
import org.hibernate.engine.spi.SessionImplementor
import org.hibernate.collection.internal.PersistentList

import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._

/**
 * Created with IntelliJ IDEA.
 * User: jerrywang
 * Date: 8/6/12
 * Time: 12:00 PM
 *
 * http://www.javalobby.org/java/forums/m91832311.html
 */

class ScalaList extends UserCollectionType {

  def contains(collection: Object, obj: Object) = collection.asInstanceOf[ListBuffer[AnyRef]].contains(obj)

  def getElementsIterator(collection: Object) = collection.asInstanceOf[ListBuffer[AnyRef]].iterator

  def indexOf(collection: Object, obj: Object) = collection.asInstanceOf[ListBuffer[AnyRef]].indexOf(obj).asInstanceOf[java.lang.Integer]

  def instantiate() = new ListBuffer[AnyRef]

  def instantiate(size: Int) = new ListBuffer[AnyRef]

  def instantiate(session: SessionImplementor, persister: CollectionPersister) = new PersistentList(session)

  def replaceElements(collectionA: Object, collectionB: Object, persister: CollectionPersister,
                      owner: Object, copyCache: java.util.Map[_, _], implementor: SessionImplementor) = {
    collectionB.asInstanceOf[ListBuffer[AnyRef]].clear()
    collectionB.asInstanceOf[ListBuffer[AnyRef]].appendAll(collectionA.asInstanceOf[ListBuffer[AnyRef]])
    collectionB
  }

  def wrap(session: SessionImplementor, collection: Object) = new PersistentList(session, collection.asInstanceOf[ListBuffer[AnyRef]])
}