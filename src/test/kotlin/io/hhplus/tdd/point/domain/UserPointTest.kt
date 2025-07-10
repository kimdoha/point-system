package io.hhplus.tdd.point.domain

/**
 * @author Doha Kim
 */
import io.hhplus.tdd.point.PointTestFixture
import io.hhplus.tdd.point.UserPoint
import io.hhplus.tdd.point.exception.InsufficientPointException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class UserPointTest : FunSpec({

    context("기존 포인트가 있는 사용자") {
        val userPoint = PointTestFixture.createUserPoint()

        test("유효한 금액으로 충전하면 포인트가 증가하고 시간이 갱신된다") {
            val chargeAmount = PointTestFixture.SMALL_AMOUNT
            val result = userPoint.charge(chargeAmount)

            result.id shouldBe PointTestFixture.VALID_USER_ID
            result.point shouldBe (PointTestFixture.MEDIUM_AMOUNT + chargeAmount)
            result.updateMillis shouldBeGreaterThan PointTestFixture.BASE_TIME

            result.shouldBeInstanceOf<UserPoint>()
            result shouldBe UserPoint(
                PointTestFixture.VALID_USER_ID,
                PointTestFixture.MEDIUM_AMOUNT + chargeAmount,
                result.updateMillis
            )
        }

        test("충분한 포인트로 사용하면 포인트가 감소하고 시간이 갱신된다") {
            val useAmount = PointTestFixture.SMALL_AMOUNT
            val result = userPoint.use(useAmount)

            result.id shouldBe PointTestFixture.VALID_USER_ID
            result.point shouldBe (PointTestFixture.MEDIUM_AMOUNT - useAmount)
            result.updateMillis shouldBeGreaterThan PointTestFixture.BASE_TIME
        }

        test("보유 포인트보다 많은 금액을 사용하려 하면 예외가 발생한다") {
            val excessiveAmount = PointTestFixture.LARGE_AMOUNT

            shouldThrow<InsufficientPointException> {
                userPoint.use(excessiveAmount)
            }
        }

        test("보유 포인트와 같은 금액을 사용하면 포인트가 0이 된다") {
            val exactAmount = PointTestFixture.MEDIUM_AMOUNT
            val result = userPoint.use(exactAmount)

            result.point shouldBe 0L
        }
    }

    context("포인트가 0인 사용자") {
        val emptyUserPoint = PointTestFixture.createEmptyUserPoint()

        test("충전하면 충전 금액만큼 포인트가 증가한다") {
            val chargeAmount = PointTestFixture.MEDIUM_AMOUNT
            val result = emptyUserPoint.charge(chargeAmount)

            result.point shouldBe chargeAmount
        }

        test("포인트를 사용하려 하면 예외가 발생한다") {
            shouldThrow<InsufficientPointException> {
                emptyUserPoint.use(1L)
            }
        }
    }

    context("연속적인 거래를 수행할 때") {
        test("충전과 사용을 반복할 때 최종 포인트가 올바르게 계산된다") {
            val userPoint = PointTestFixture.createEmptyUserPoint()

            val result = userPoint
                .charge(PointTestFixture.MEDIUM_AMOUNT)      // +5000
                .use(PointTestFixture.SMALL_AMOUNT)          // -1000
                .charge(PointTestFixture.SMALL_AMOUNT)       // +1000
                .use(500L)                           // -500

            result.point shouldBe 4500L
        }
    }
})
