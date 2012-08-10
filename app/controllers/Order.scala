package controllers

import play.api.mvc.{Controller, Action}
import utils.Logger
import services.JPA
import models.entity.{OrderItem, OrderLog}
import scala.collection.JavaConversions._
import org.hibernate.Hibernate

/**
 * Created with IntelliJ IDEA.
 * User: jerrywang
 * Date: 8/3/12
 * Time: 12:20 PM
 * To change this template use File | Settings | File Templates.
 */

object Order extends Controller with Logger {

  val TEST_USER_FIRST_NAME = "Tom"

  def test() = Action { request =>

    println("----- " + new java.util.Date())

//    testTnx

    testQuery()

    testPersist()

    testMerge()

    testRemove()

    Ok(views.html.playjpa("Test","Test Complete"))
  }

  private def printOrder(ord:OrderLog, printItems:Boolean = false):Unit = {
    debug("--------------------------------------------------")
    debug(" Order Id: " + ord.id)
    debug(" Order Time: " + ord.orderTimestamp.getTime)
    debug(" addr 1 " + ord.shiptoAddress1);
    ord.shiptoAddress2.map(adr2 => debug(" addr 2 " + adr2));
    if (printItems)
      ord.orderItems.foreach(oi=>debug("   " + oi.sku + " - $" + oi.price))
  }



  // Test transaction
  // Warning: do not reference any methods belong to this object; Akka (on a separate thread) probably won't be able to reference it
  //   minimize transaction logic as transaction with long computation will block Akka actor.
  private def testTnx():Unit = {
    // (this test focusing on testing initializing lazy collections within session; to test it make sure change
    //   OrderLog fetch strategy to LAZY on orderitems)
    val ord = JPA.transaction {
      em =>
        val entity = em.find(classOf[OrderLog], 1000000)
        Hibernate.initialize(entity.orderItems)
        entity
    }

    printOrder(ord.asInstanceOf[OrderLog],true)
  }

  // Test query
  private def testQuery():Unit = {
    // test search by ID
    JPA.find(classOf[OrderLog],1000000).map(printOrder(_)).getOrElse(debug("not found"))

    // test query with 1 result
    JPA.querySingleResult(classOf[OrderLog],"from OrderLog order by order_id desc ").map(printOrder(_))

    // test query with order by
    JPA.query(classOf[OrderLog],"from OrderLog order by order_id asc ").map(printOrder(_))

    // test OptionString
    JPA.query(classOf[OrderLog],"from OrderLog as o where o.shiptoAddress2 is not null").map(printOrder(_))

    // test query with param
    JPA.query(classOf[OrderLog],"from OrderLog as o where o.shiptoFirstName =:firstname and o.shiptoLastName = :lastname",
      ("firstname" -> "Jerry"),("lastname" -> "Wang"))
      .map(printOrder(_))
  }

  // Test write / persist
  private def testPersist():Unit = {
    val ord1 = OrderLog(TEST_USER_FIRST_NAME, "Hanks", "Hollywood 1", None, "Hollywood", "CA", "91234")
    ord1.addItem(OrderItem("123123123", 20.99f))
    JPA.persist(ord1)
    printOrder(ord1)
  }

  // Test update / merge
  private def testMerge():Unit = {
    JPA.query(classOf[OrderLog],"from OrderLog as o where o.shiptoFirstName = :firstname", ("firstname" -> TEST_USER_FIRST_NAME)).map(ord => {
      ord.shiptoAddress1 = "TEST UPDATE addr1 2"
      ord.orderItems.foreach (oi => oi.price = oi.price + 1)
      JPA.merge(ord)
      printOrder(ord)
    })
  }

  // Test delete / remove
  private def testRemove():Unit = {
    JPA.query(classOf[OrderLog],"from OrderLog as o where o.shiptoFirstName = :firstname", ("firstname" -> TEST_USER_FIRST_NAME)).map(ord => {
      JPA.remove(ord)
    })
  }
}

