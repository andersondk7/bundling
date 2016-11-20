package org.aea.bundle.model

/**
 * Represents a line item in the cart
 */
case class CartItem(item: Item, quantity: Int) {
  lazy val items: Seq[Item] = (1 to quantity).map(i => item)
}
