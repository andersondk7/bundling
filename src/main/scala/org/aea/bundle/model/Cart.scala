package org.aea.bundle.model

import scala.annotation.tailrec

/**
 * Represents the user's shopping cart
 */
case class Cart(aLaCarteItems: Seq[Item], appliedDiscounts: Seq[Discount] = List()) extends Ordered[Cart] {

  /**
   * cost of a cart is the aLaCarte times plus all of the discounts
   */
  val cost = aLaCarteItems.map(_.price).sum + appliedDiscounts.map(_.cost).sum

  /**
   * Basis for comparing carts is the cost of the cart (which includes discounts)
   */
  def compare(that: Cart): Int = this.cost compare that.cost

  /**
   * create an abbreviated string representation of the cart
   */
  def print = s"cost: $cost, items: ${aLaCarteItems.map(_.description)}, discounts: ${appliedDiscounts.map(_.print)}}"
}

object Cart {

  def apply(items: Seq[CartItem]): Cart = Cart(items.flatMap(_.items))

  def apply(discount: Discount, cart: Cart): Cart = {
    @tailrec
    def extract(mergedDiscount: Discount, cart: Cart): (Discount, Cart) = {
      // remove the next discounted item from the cart
      // continue until no more discount items
      //                or cart does not the discount item

      if (cart.aLaCarteItems.isEmpty) (mergedDiscount, cart) // break out of loop, no more cart times
      else {
        mergedDiscount.items.headOption match {
          case None => // break out of loop, no more discount items to apply
            (mergedDiscount, cart)

          case Some(nextDiscountItem) => // more discounts items to apply
            // find the discounted item in the cart...
            cart.aLaCarteItems.indexWhere(ci => ci.id == nextDiscountItem.id) match {
              case x if x < 0 => (mergedDiscount, cart) // break out of loop, cart does not have discount item

              case x if x == 0 => // first item in cart, remove item from cart and discount and try to remove the next
                extract(
                  mergedDiscount.copy(items = mergedDiscount.items.tail), Cart(cart.aLaCarteItems.tail, cart.appliedDiscounts)
                )

              case x => // some other item in cart, remove item from cart and discount and try to remove the next
                val (top, bottom) = cart.aLaCarteItems.splitAt(x)
                extract(
                  mergedDiscount.copy(items = mergedDiscount.items.tail), Cart(top ++ bottom.tail, cart.appliedDiscounts)
                )
            }
        }
      }
    }

    val (mergedDiscount, mergedCart) = extract(discount, cart)
    if (mergedDiscount.items.isEmpty) Cart(mergedCart.aLaCarteItems, cart.appliedDiscounts :+ discount)
    else cart
  }
}
