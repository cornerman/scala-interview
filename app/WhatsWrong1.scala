package com.particeep.test

object WhatsWrong1 {

  trait Interface {
    val city: String
    val support: String = s"Ici c'est $city !"
  }

  case object Supporter extends Interface {

    override val city = "Paris"
  }

  Supporter.city //What does this print ?
  // It prints "Paris".

  Supporter.support //What does this print and why ? How to fix it ?
  // It prints "Ici c'est null !". This is because of the initialization order
  // of vals in scala classes/traits. A `val` is initialized with a default
  // value during construction. At the time of assigning `support`, the value
  // of `city` is therefore `null`. And only after the trait is initialized,
  // the value of `city` becomes "Paris". There are multiple possibilities to
  // fix this:
  // 1) Make `Interface` an abstract class taking a constructor argument `city` (In scala 2.14 or scala 3, there will be trait parameters).
  // 2) Make `support` a `lazy val` or a `def`, thereby `city` is only used when `support` is called - after `city` has been initialized.
  // 3) Make `city` in trait a `def` and make it a `def` or `lazy val` inside the object. A `def` is handled differently during construction.
}
