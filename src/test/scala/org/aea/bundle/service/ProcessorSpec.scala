package org.aea.bundle.service

import org.aea.bundle.model.{ Cart, Discount }
import org.scalatest.FunSpec
import org.scalatest.Matchers._

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class ProcessorSpec extends FunSpec {

  import org.aea.bundle.TestData._

  case class TestCase(discounts: Seq[Discount], cart: Cart, expectedCost: Int)

  val emptyDiscountTest = TestCase(List(), fullCart, 5500)
  val noDiscountsTest = TestCase(List(butterDiscount), noDisountsCart, 3900)
  val singleDiscountTest = TestCase(List(butterDiscount), fullCart, 5300)
  val skipDiscountTest = TestCase(List(butterDiscount, hamCheeseMustardDiscount, hamCheeseButterDiscount), littleButterCart, 4400)
  val allDiscountsTest = TestCase(List(butterDiscount, hamCheeseMustardDiscount, hamCheeseButterDiscount), fullCart, 4500)
  val testCases = List(emptyDiscountTest, noDiscountsTest, singleDiscountTest, skipDiscountTest, allDiscountsTest)

  val service = new CheckoutService()

  implicit val executor: (Seq[Discount], Cart) => Cart = service.inlineCheckout
  private def executeTest(testCase: TestCase)(implicit test: (Seq[Discount], Cart) => Cart): Cart = test(testCase.discounts, testCase.cart)

  describe("inlineProcessor") {
    it("should only have alacarte items when there are no discounts to apply") {
      val bestCart = executeTest(emptyDiscountTest)
      bestCart.aLaCarteItems should be(fullCart.aLaCarteItems)
      bestCart.cost shouldBe emptyDiscountTest.expectedCost
    }
    it("should have only aLaCarte items when no discounts apply") {
      noDisountsCart.appliedDiscounts.isEmpty shouldBe true
      noDisountsCart.cost shouldBe 3900
      val bestCart = executeTest(noDiscountsTest)
      bestCart.aLaCarteItems.count(item => item.id == bread.id) shouldBe 1
      bestCart.aLaCarteItems.count(item => item.id == ham.id) shouldBe 4
      bestCart.aLaCarteItems.count(item => item.id == cheese.id) shouldBe 0
      bestCart.aLaCarteItems.count(item => item.id == mustard.id) shouldBe 2
      bestCart.aLaCarteItems.count(item => item.id == butter.id) shouldBe 1
      bestCart.appliedDiscounts.size shouldBe 0
      noDisountsCart.appliedDiscounts.isEmpty shouldBe true
      noDisountsCart.cost shouldBe noDiscountsTest.expectedCost
    }
    it("should apply a single discount") {
      fullCart.appliedDiscounts.isEmpty shouldBe true
      fullCart.cost shouldBe 5500
      val bestCart = executeTest(singleDiscountTest)
      bestCart.aLaCarteItems.count(item => item.id == bread.id) shouldBe 1
      bestCart.aLaCarteItems.count(item => item.id == ham.id) shouldBe 4
      bestCart.aLaCarteItems.count(item => item.id == cheese.id) shouldBe 3
      bestCart.aLaCarteItems.count(item => item.id == mustard.id) shouldBe 2
      bestCart.aLaCarteItems.count(item => item.id == butter.id) shouldBe 1
      bestCart.appliedDiscounts.size shouldBe 2
      bestCart.appliedDiscounts should contain(butterDiscount)
      bestCart.cost shouldBe singleDiscountTest.expectedCost
      fullCart.cost shouldBe 5500
      fullCart.appliedDiscounts.isEmpty shouldBe true
    }
    it("should skip discounts that do not apply") {
      littleButterCart.appliedDiscounts.isEmpty shouldBe true
      littleButterCart.cost shouldBe 5200
      val bestCart = executeTest(skipDiscountTest)
      bestCart.aLaCarteItems.count(item => item.id == bread.id) shouldBe 1
      bestCart.aLaCarteItems.count(item => item.id == ham.id) shouldBe 1
      bestCart.aLaCarteItems.count(item => item.id == cheese.id) shouldBe 0
      bestCart.aLaCarteItems.count(item => item.id == mustard.id) shouldBe 0
      bestCart.aLaCarteItems.count(item => item.id == butter.id) shouldBe 1
      bestCart.appliedDiscounts.count(discount => discount.id == hamCheeseMustardDiscount.id) shouldBe 2
      bestCart.appliedDiscounts.count(discount => discount.id == hamCheeseButterDiscount.id) shouldBe 1
      bestCart.appliedDiscounts.count(discount => discount.id == butterDiscount.id) shouldBe 0
      bestCart.appliedDiscounts.size shouldBe 3
      bestCart.cost shouldBe skipDiscountTest.expectedCost
      littleButterCart.cost shouldBe 5200
      littleButterCart.appliedDiscounts.isEmpty shouldBe true
    }
    it("should pick all discounts") {
      fullCart.appliedDiscounts.isEmpty shouldBe true
      fullCart.cost shouldBe 5500
      val bestCart = executeTest(allDiscountsTest)
      bestCart.aLaCarteItems.count(item => item.id == bread.id) shouldBe 1
      bestCart.aLaCarteItems.count(item => item.id == ham.id) shouldBe 1
      bestCart.aLaCarteItems.count(item => item.id == cheese.id) shouldBe 0
      bestCart.aLaCarteItems.count(item => item.id == mustard.id) shouldBe 0
      bestCart.aLaCarteItems.count(item => item.id == butter.id) shouldBe 0
      bestCart.appliedDiscounts.size shouldBe 5
      bestCart.appliedDiscounts.count(discount => discount.id == hamCheeseMustardDiscount.id) shouldBe 2
      bestCart.appliedDiscounts.count(discount => discount.id == hamCheeseButterDiscount.id) shouldBe 1
      bestCart.appliedDiscounts.count(discount => discount.id == butterDiscount.id) shouldBe 2
      bestCart.cost shouldBe allDiscountsTest.expectedCost
      fullCart.cost shouldBe 5500
      fullCart.appliedDiscounts.isEmpty shouldBe true
    }
  }
  describe("checkout service") {
    it("should handle concurrent usage") {

      val testFutures: Seq[Future[Cart]] = testCases.map(tc => service.checkout(tc.discounts, tc.cart))
      val eventualResults: Future[Seq[Boolean]] = Future.sequence(testFutures)
        .map(carts => carts.map(_.cost) zip testCases.map(_.expectedCost)) // combine actual with expected
        .map(pairs => pairs.map(p => p._1 == p._2)) // compare actual with expected

      // this won't tell us which one fails, but all of these test cases were tested individually so they should still pass
      val completedResults = Await.result(eventualResults, 3.second) // wait for them all to finish
      val combinedResult = !completedResults.contains(false)
      combinedResult shouldBe true

    }
  }

}
