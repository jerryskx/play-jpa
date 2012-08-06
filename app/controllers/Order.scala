package controllers

import play.api.mvc.{Controller, Action}
import utils.Logger
import services.JPA
import models.entity.OrderLog
import scala.collection.JavaConversions._

/**
 * Created with IntelliJ IDEA.
 * User: jerrywang
 * Date: 8/3/12
 * Time: 12:20 PM
 * To change this template use File | Settings | File Templates.
 */

object Order  extends Controller with Logger {
  def list() = Action { request =>
//    Ok(views.html.index("Your new application is ready."))
//    JPA.query(classOf[OrderLog],"from OrderLog", ("id"->"123"))
    JPA.query(classOf[OrderLog],"from OrderLog order by order_id asc ").map(ord => {
      debug(" Order Id: " + ord.id)
      debug(" addr 2 " + ord.shiptoAddress2.getOrElse(""));
//      debug(" order timestamp " + ord.orderTimestamp.map(_.getTime).getOrElse(""))
      ord.orderItems.foreach(oi=>debug("   " + oi.sku + " - $" + oi.price))
//      ord.scalaItems.foreach(oi=>debug("   " + oi.sku + " - $" + oi.price))
    })
    Ok(views.html.playjpa("Test Listing","In listing page"))
  }
}
