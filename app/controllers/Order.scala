package controllers

import play.api.mvc.{Controller, Action}
import utils.Logger
import services.JPA
import models.entity.{OrderItem, OrderLog}
import scala.collection.JavaConversions._

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

    testQuery()

    testPersist()

    testMerge()

    testRemove()

    Ok(views.html.playjpa("Test","Test Complete"))
  }

  private def printOrder(ord:OrderLog):Unit = {
    debug("--------------------------------------------------")
    debug(" Order Id: " + ord.id)
    debug(" Order Time: " + ord.orderTimestamp.getTime)
    debug(" addr 1 " + ord.shiptoAddress1);
    ord.shiptoAddress2.map(adr2 => debug(" addr 2 " + adr2));
    ord.orderItems.foreach(oi=>debug("   " + oi.sku + " - $" + oi.price))
  }



  // Test query
  private def testQuery():Unit = {
    // test search by ID
    JPA.find(classOf[OrderLog],1000000).map(printOrder(_)).getOrElse(debug("not found"))

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

