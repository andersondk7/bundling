package org.aea.bundle

import org.aea.bundle.model.{ Discount, Cart, CartItem, Item }

/**
 * data used in testing
 */
object TestData {

  val bread = Item(1, "bread", 200)
  val butter = Item(2, "butter", 100)
  val ham = Item(3, "ham", 800)
  val cheese = Item(4, "cheese", 400)
  val mustard = Item(5, "mustard", 200)

  val breads = CartItem(bread, 1)
  val hams = CartItem(ham, 4)
  val cheeses = CartItem(cheese, 3)
  val mustards = CartItem(mustard, 2)
  val butters = CartItem(butter, 5)

  val fullCart = Cart(Seq(breads, butters, hams, cheeses, mustards))
  val littleButterCart = Cart(Seq(breads, CartItem(butter, 2), hams, cheeses, mustards))
  val noDisountsCart = Cart(Seq(breads, hams, mustards, CartItem(butter, 1)))

  val hamCheeseMustardDiscount = Discount(1, List(ham, cheese.copy(price = cheese.price - 100), mustard.copy(price = 0)))
  val hamCheeseButterDiscount = Discount(2, List(ham, cheese.copy(price = cheese.price - 100), butter.copy(price = 0)))

  val butterDiscount = Discount(3, List(butter, butter.copy(price = 0)))

}
