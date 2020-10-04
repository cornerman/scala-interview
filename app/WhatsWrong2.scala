package com.particeep.test

import scala.concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global

case class CEO(id: String, first_name: String, last_name: String)
case class Enterprise(id: String, name: String, ceo_id: String)

object CEODao {
  val ceos = List(
    CEO("1", "Mark", "Zuckerberg"),
    CEO("2", "Sundar", "Pichai")
  )

  def byId(id: String): Future[Option[CEO]] = Future { ceos.find(_.id == id) }
}

object EnterpriseDao {
  val enterprises = List(
    Enterprise("1", "Google", "1"),
    Enterprise("2", "Facebook", "2")
  )

  def byId(id: String): Future[Option[Enterprise]] = Future { enterprises.find(_.id == id) }
  def byCEOId(ceo_id: String): Future[Option[Enterprise]] = Future { enterprises.find(_.ceo_id == ceo_id) }
}

object WhatsWrong2 {

  //Review this code. What could be done better ? How would you do it ?
  def getCEOAndEnterprise(ceo_id: Option[String]): Future[(Option[CEO], Option[Enterprise])] = {
    for {
      ceo <- CEODao.byId(ceo_id.get)
      enterprise <- EnterpriseDao.byCEOId(ceo_id.get)
    } yield {
      (ceo, enterprise)
    }
  }

  // There are multiple problems with this code:
  // 1) Calling `get` on an option is not recommended as it will fail with a
  //    runtime error, we should handle the case where `ceo_id` is `None` - or
  //    even better we could change the parameter to be `ceo_id: String` and
  //    map over the `Option` on the call side.
  // 2) Doing a for comprehension with futures like above, will make the two
  //    futures run in succession, we should run the two futures in parallel,
  //    as they do not depend on each other. Also using the `zip` method on
  //    futures is more concise in this case.
  // 3) The EnterpriseDao is wrong, because Mark Zuckerberg is the CEO of
  //    Facebook and Sundar Pichai is the CEO of google (not the other way
  //    around).
  def getCEOAndEnterpriseBetter(ceo_id: Option[String]): Future[(Option[CEO], Option[Enterprise])] =
    ceo_id.fold(
      Future.successful((Option.empty[CEO], Option.empty[Enterprise]))
    ) { ceo_id =>
      CEODao.byId(ceo_id) zip EnterpriseDao.byCEOId(ceo_id)
    }
}
