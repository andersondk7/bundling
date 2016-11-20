package org.aea.bundle.service

import org.aea.bundle.model._

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Represents the service used to complete purchases
 */
class CheckoutService {
  /**
   * Apply the discounts to the cart and return the optimum cart/discount configuration
   * <p>
   *   this is a long running (potentially blocking) call, so it is wrapped in a future
   * </p>
   * @param discounts discounts to apply
   * @param cart initial cart of items
   * @return optimum cart/discount configuration
   */
  def checkout(discounts: Seq[Discount], cart: Cart): Future[Cart] = Future {
    inlineCheckout(discounts, cart)
  }

  /**
   * extracted checkout functionality for ease of testing
   */
  protected[service] def inlineCheckout(discounts: Seq[Discount], cart: Cart): Cart = {
    if (discounts.isEmpty) cart
    else calcBest(discounts, cart)
  }

  /*
  calculate the best combination of discounts to a cart
   */
  private def calcBest(discounts: Seq[Discount], cart: Cart): Cart = {
    // apply all of the discounts to the cart
    // if there are savings (the cart costs less)
    //         then apply the discount to the cart and try again with the new cart
    // otherwise return the cart without any further discounts
    @tailrec
    def best(originalCart: Cart): Cart = {
      // for every available discount, create a new cart
      val possibleCarts = discounts.map(Cart(_, originalCart))
      val cheapestCart = possibleCarts.sorted.head
      if (cheapestCart.cost < originalCart.cost) best(cheapestCart) // try again with updated cart
      else originalCart // break out of loop, no savings possible
    }

    best(cart)
  }
}
