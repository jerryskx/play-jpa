package models.entity

import javax.persistence._
import java.util.Calendar
import utils.DatabaseConstants._
import org.hibernate.annotations.{CollectionType, Type}


//todo
import scala.collection.JavaConversions._
import java.{util => ju, lang => jl}

/**
 * Created with IntelliJ IDEA.
 * User: jerrywang
 * Date: 8/2/12
 * Time: 2:51 PM
 * To change this template use File | Settings | File Templates.
 */

object OrderLog {
  def apply(firstName:String, lastName:String, addr1: String, addr2:Option[String],
             city:String, state:String, zip:String) = {
    val order = new OrderLog()
    order.shiptoFirstName = firstName
    order.shiptoLastName = lastName
    order.shiptoAddress1 = addr1
    order.shiptoAddress2 = addr2
    order.shiptoCity = city
    order.shiptoState = state
    order.shiptoZip = zip
    order.orderTimestamp = Calendar.getInstance()
    order
  }
}

@Entity
@Cacheable(false)     // disable 2nd level cache
@Table(name = TABLE_ORDER_LOG)
class OrderLog extends Serializable {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = COL_ORDER_ID)
  var id: Int = _

  @Column(name = COL_SHIPTO_FIRST_NAME)
  var shiptoFirstName: String = _

  @Column(name = COL_SHIPTO_LAST_NAME)
  var shiptoLastName: String = _

  @Column(name = COL_SHIPTO_ADDRESS1)
  var shiptoAddress1: String = _

  // http://dzone.com/snippets/mapping-scalas-option
  @Column(name = COL_SHIPTO_ADDRESS2)
  @Type (`type` = "akka.jpa.types.OptionString" )
  var shiptoAddress2: Option[String] = _

  @Column(name = COL_SHIPTO_CITY)
  var shiptoCity: String = _

  @Column(name = COL_SHIPTO_STATE)
  var shiptoState: String = _

  @Column(name = COL_SHIPTO_ZIP)
  var shiptoZip: String = _

  @Column(name = COL_ORDERTIMESTAMP)
  var orderTimestamp: Calendar = _
//  @Type (`type` = "akka.jpa.types.OptionCalendar" )
//  var orderTimestamp: Option[Calendar] = _

  // Not Scala List; it's unfortunate that even with @CollectionType annotation, at this point, Hibernate only accept Java Collections (List, Set, Map)  (may be with custom PersistenceCollection it will be doable)
//  @OneToMany(cascade = Array(CascadeType.ALL), fetch = FetchType.LAZY, mappedBy = "orderLog", orphanRemoval = true)
  @OneToMany(cascade = Array(CascadeType.ALL), fetch = FetchType.EAGER, mappedBy = "orderLog", orphanRemoval = true)
  var orderItems: ju.List[OrderItem] = _

  @Transient
  def addItem(item:OrderItem) = {
    if(orderItems == null)
      orderItems = new ju.ArrayList[OrderItem]()
    orderItems.add(item)
    item.orderLog = this
  }

//  @Transient
//  def scalaItems: List[OrderItem] = orderItems

  /*
   bitmap$init$0 will be generated by Scala; if not @Transient this variable, JPA/HB will have problem matching col
   bitmap$init$0 variable

   private[this] means private to the instance (not class)

   "bitmap$init$0" is where the initialization states for lazy vals are stored. When you access a lazy val (or a nested
   object, which is equivalent), the compiler uses the bitmap field to determine whether it's already been evaluated.
   When lazy vals are initialized, this field is also used for synchronization when the value is initialized.
   (http://stackoverflow.com/questions/6877040/in-compiled-scala-what-is-the-bitmap0-field)
    */
  @Transient private[this] var bitmap$init$0: Int = 0;

}

