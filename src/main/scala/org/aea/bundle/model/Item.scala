package org.aea.bundle.model

/**
 * Represents something to be purchased
 * <p>
 *   just to make this simple, treat item prices as an integer number of pennies.
 *   while this won't work for anything beyond simple addition/subtraction,
 *   for this project it should be sufficient
 * </p>
 */
case class Item(id: Long, description: String, price: Int) extends Ordered[Item] {
  override def compare(that: Item): Int = this.id compare that.id
}

