package utils


/**
 * Created with IntelliJ IDEA.
 * User: jerrywang
 * Date: 8/3/12
 * Time: 12:01 PM
 * To change this template use File | Settings | File Templates.
 */

trait Logger {
  val logger = play.api.Logger

  def info(msg:String):Unit = logger.info(msg)
  def info(msg:String, error:Throwable) : Unit = logger.info(msg,error)

  def debug(msg:String):Unit = logger.debug(msg)
  def debug(msg:String, error:Throwable):Unit = logger.debug(msg,error)

  def trace(msg:String) : Unit = logger.trace(msg)
  def trace(msg:String, error:Throwable) : Unit = logger.trace(msg,error)

  def warn(msg:String) : Unit = logger.warn(msg)
  def warn(msg:String, error:Throwable) : Unit = logger.warn(msg,error)

  def error(msg:String) : Unit = logger.error(msg)
  def error(msg:String, error:Throwable) : Unit = logger.error(msg, error)
}
