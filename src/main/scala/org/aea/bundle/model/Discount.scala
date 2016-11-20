package org.aea.bundle.model

/**
 * Represents a bundle of items sold at a discount
 */
case class Discount(id: Long, items: Seq[Item]) {
  lazy val cost = items.foldLeft(0)((r, c) => r + c.price)

  /**
   * create an abbreviated string representation of the cart
   */
  def print: String = s"$id, ${items.map(_.description)}"
}

