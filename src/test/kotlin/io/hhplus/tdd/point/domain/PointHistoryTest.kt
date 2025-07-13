package io.hhplus.tdd.point.domain

import io.hhplus.tdd.point.PointTestFixture
import io.hhplus.tdd.point.TransactionType
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

/**
 * @author Doha Kim
 */
class PointHistoryTest : FunSpec({

    test("충전 히스토리를 생성할 때 모든 필드가 올바르게 설정된다") {
        val history = PointTestFixture.createChargeHistory()

        history.id shouldBe 1L
        history.userId shouldBe PointTestFixture.VALID_USER_ID
        history.type shouldBe TransactionType.CHARGE
        history.amount shouldBe PointTestFixture.MEDIUM_AMOUNT
        history.timeMillis shouldBe PointTestFixture.BASE_TIME
    }

    test("사용 히스토리를 생성할 때 사용 금액이 음수로 저장된다") {
        val history = PointTestFixture.createUseHistory()

        history.type shouldBe TransactionType.USE
        history.amount shouldBe -PointTestFixture.SMALL_AMOUNT
    }

    test("여러 히스토리를 생성할 때 올바른 개수와 순서로 생성된다") {
        val histories = PointTestFixture.createHistoryList()

        histories shouldHaveSize 3
        histories[0].type shouldBe TransactionType.CHARGE
        histories[1].type shouldBe TransactionType.USE
        histories[2].type shouldBe TransactionType.CHARGE
    }
})
