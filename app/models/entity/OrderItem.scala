package models.entity

import javax.persistence._
import java.util.Calendar
import utils.DatabaseConstants._


// todo
import scala.collection.JavaConversions._
import java.{util => ju, lang => jl}

/**
 * Created with IntelliJ IDEA.
 * User: jerrywang
 * Date: 8/2/12
 * Time: 3:39 PM
 * To change this template use File | Settings | File Templates.
 */

@Entity
@Table(name = TABLE_ORDER_ITEMS)
class OrderItem extends Serializable  {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = COL_ID)
  var id: Int = _

  @Column(name = COL_SKU)
  var sku:String = _

  @Column(name = COL_PRICE)
  var price: jl.Float= _

  @Column(name = COL_STATUS)
  var status:String = _

  @ManyToOne
  @JoinColumn(name = COL_ORDER_ID)
  var orderLog: OrderLog =  _
}
