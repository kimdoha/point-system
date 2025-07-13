package io.hhplus.tdd.point.service

import io.hhplus.tdd.common.Constants.Companion.MAX_POINT
import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import io.hhplus.tdd.point.PointService
import io.hhplus.tdd.point.PointTestFixture
import io.hhplus.tdd.point.PointTestFixture.OVER_MAX_AMOUNT
import io.hhplus.tdd.point.TransactionType
import io.hhplus.tdd.point.exception.InsufficientPointException
import io.hhplus.tdd.point.exception.MaxPointExceededException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldBeSortedBy
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.*
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.extension.ExtendWith

/**
 * @author Doha Kim
 */
@ExtendWith(MockKExtension::class)
class PointServiceTest : DescribeSpec({

    lateinit var pointHistoryTable: PointHistoryTable
    lateinit var userPointTable: UserPointTable
    lateinit var pointService: PointService

    beforeEach {
        pointHistoryTable = mockk()
        userPointTable = mockk()
        pointService = PointService(pointHistoryTable, userPointTable)
    }

    afterEach {
        clearAllMocks()
    }

    describe("getPoint") {
        context("사용자 포인트 조회") {
            it("사용자의 현재 포인트를 정확히 반환한다") {
                // given
                val userId = PointTestFixture.VALID_USER_ID
                val expectedPoint = PointTestFixture.createUserPoint(point = 5000L)
                every { userPointTable.selectById(userId) } returns expectedPoint

                // when
                val result = pointService.getPoint(userId)

                // then
                result shouldBe expectedPoint
                verify(exactly = 1) { userPointTable.selectById(userId) }
            }
        }

        context("신규 사용자의 경우") {
            it("초기 포인트 0을 반환한다") {
                // given
                val newUserId = PointTestFixture.NEW_USER_ID
                val initialPoint = PointTestFixture.createUserPoint(point = 0L)
                every { userPointTable.selectById(newUserId) } returns initialPoint

                // when
                val result = pointService.getPoint(newUserId)

                // then
                result.point shouldBe 0L
                verify(exactly = 1) { userPointTable.selectById(newUserId) }
            }
        }
    }

    describe("getHistories") {
        context("포인트 히스토리가 존재하는 경우") {
            it("시간 순으로 정렬된 히스토리를 반환한다") {
                // given
                val userId = PointTestFixture.VALID_USER_ID
                val history1 = PointTestFixture.createChargeHistory(timeMillis = 1000L)
                val history2 = PointTestFixture.createUseHistory(timeMillis = 2000L)
                val history3 = PointTestFixture.createChargeHistory(id = 3L, timeMillis = 3000L)
                val unsortedHistories = listOf(history2, history3, history1)
                every { pointHistoryTable.selectAllByUserId(userId) } returns unsortedHistories

                // when
                val result = pointService.getHistories(userId)

                // then
                result shouldHaveSize 3
                result.shouldBeSortedBy { it.timeMillis }
                result[0].timeMillis shouldBe 1000L
                result[1].timeMillis shouldBe 2000L
                result[2].timeMillis shouldBe 3000L
                verify(exactly = 1) { pointHistoryTable.selectAllByUserId(userId) }
            }
        }

        context("포인트 히스토리가 없는 경우") {
            it("빈 리스트를 반환한다") {
                // given
                val userId = PointTestFixture.VALID_USER_ID
                every { pointHistoryTable.selectAllByUserId(userId) } returns emptyList()

                // when
                val result = pointService.getHistories(userId)

                // then
                result.shouldBeEmpty()
                verify(exactly = 1) { pointHistoryTable.selectAllByUserId(userId) }
            }
        }

        context("다양한 거래 유형이 섞여있는 경우") {
            it("모든 거래 유형을 포함하여 시간순으로 반환한다") {
                // given - 충전과 사용이 섞인 히스토리
                val userId = PointTestFixture.VALID_USER_ID
                val chargeHistory = PointTestFixture.createChargeHistory(timeMillis = 1000L)
                val useHistory = PointTestFixture.createUseHistory(timeMillis = 2000L)
                val mixedHistories = listOf(useHistory, chargeHistory) // 역순
                every { pointHistoryTable.selectAllByUserId(userId) } returns mixedHistories

                // when
                val result = pointService.getHistories(userId)

                // then
                result shouldHaveSize 2
                result.shouldBeSortedBy { it.timeMillis }
                result[0] shouldBe chargeHistory
                result[1] shouldBe useHistory
                verify(exactly = 1) { pointHistoryTable.selectAllByUserId(userId) }
            }
        }
    }


    describe("charge") {
        context("정상적인 포인트 충전") {
            it("포인트를 정확히 충전하고 히스토리를 기록한다") {
                // given
                val userId = PointTestFixture.VALID_USER_ID
                val chargeAmount = 3000L
                val currentPoint = PointTestFixture.createUserPoint(point = 2000L)
                val expectedChargedPoint = PointTestFixture.createUserPoint(point = 5000L)
                val expectedHistory = PointTestFixture.createChargeHistory(
                    userId = userId,
                    amount = chargeAmount,
                    timeMillis = expectedChargedPoint.updateMillis
                )

                every { userPointTable.selectById(userId) } returns currentPoint
                every { userPointTable.insertOrUpdate(userId, 5000L) } returns expectedChargedPoint
                every {
                    pointHistoryTable.insert(
                        userId,
                        chargeAmount,
                        TransactionType.CHARGE,
                        expectedChargedPoint.updateMillis
                    )
                } returns expectedHistory

                mockkObject(currentPoint)
                every { currentPoint.charge(chargeAmount) } returns expectedChargedPoint

                // when
                val result = pointService.charge(userId, chargeAmount)

                // then
                result shouldBe expectedChargedPoint
                verify(exactly = 1) { userPointTable.selectById(userId) }
                verify(exactly = 1) { userPointTable.insertOrUpdate(userId, 5000L) }
                verify(exactly = 1) {
                    pointHistoryTable.insert(
                        userId,
                        chargeAmount,
                        TransactionType.CHARGE,
                        expectedChargedPoint.updateMillis
                    )
                }
            }
        }

        context("최대 포인트 한도 초과 충전 시도") {
            it("MaxPointExceededException을 발생시키고 데이터 변경을 방지한다") {
                // given - 최대 한도 근처의 포인트를 가진 사용자 (1,000,000 - 50,000 = 950,000)
                val userId = PointTestFixture.VALID_USER_ID
                val chargeAmount = 100000L
                val currentPoint = PointTestFixture.createUserPoint(point = 950000L)

                every { userPointTable.selectById(userId) } returns currentPoint

                // when & then
                shouldThrow<MaxPointExceededException> {
                    pointService.charge(userId, chargeAmount)
                }

                verify(exactly = 1) { userPointTable.selectById(userId) }
                verify(exactly = 0) { userPointTable.insertOrUpdate(userId, 1050000L) }
                verify(exactly = 0) {
                    pointHistoryTable.insert(userId, chargeAmount, TransactionType.CHARGE, any<Long>())
                }
            }

            context("기존 포인트가 있는 상태에서 추가 충전") {
                it("기존 포인트에 충전 금액이 정확히 누적된다") {
                    // given
                    val userId = PointTestFixture.VALID_USER_ID
                    val existingPoint = 15000L
                    val chargeAmount = 5000L
                    val expectedTotalPoint = 20000L

                    val currentPoint = PointTestFixture.createUserPoint(point = existingPoint)
                    val chargedPoint = PointTestFixture.createUserPoint(point = expectedTotalPoint)
                    val expectedHistory = PointTestFixture.createChargeHistory(
                        userId = userId,
                        amount = chargeAmount,
                        timeMillis = chargedPoint.updateMillis,
                    )

                    every { userPointTable.selectById(userId) } returns currentPoint
                    every { userPointTable.insertOrUpdate(userId, expectedTotalPoint) } returns chargedPoint
                    every {
                        pointHistoryTable.insert(
                            userId,
                            chargeAmount,
                            TransactionType.CHARGE,
                            chargedPoint.updateMillis
                        )
                    } returns expectedHistory

                    // when
                    val result = pointService.charge(userId, chargeAmount)

                    // then
                    result.point shouldBe expectedTotalPoint
                    verify(exactly = 1) { userPointTable.insertOrUpdate(userId, expectedTotalPoint) }
                    verify(exactly = 1) {
                        pointHistoryTable.insert(
                            userId,
                            chargeAmount,
                            TransactionType.CHARGE,
                            chargedPoint.updateMillis
                        )
                    }
                }
            }

            context("최대 포인트 한도 경계값 테스트") {
                it("최대 포인트까지 정확히 충전할 수 있다") {
                    // given - 최대 포인트에서 1000원 적은 상태
                    val userId = PointTestFixture.VALID_USER_ID
                    val currentPoint = PointTestFixture.createUserPoint(point = 999000L)
                    val chargeAmount = 1000L
                    val expectedChargedPoint = PointTestFixture.createUserPoint(MAX_POINT)
                    val expectedHistory = PointTestFixture.createChargeHistory(
                        userId = userId,
                        amount = chargeAmount,
                        timeMillis = expectedChargedPoint.updateMillis
                    )

                    every { userPointTable.selectById(userId) } returns currentPoint
                    every { userPointTable.insertOrUpdate(userId, MAX_POINT) } returns expectedChargedPoint
                    every {
                        pointHistoryTable.insert(
                            userId,
                            chargeAmount,
                            TransactionType.CHARGE,
                            expectedChargedPoint.updateMillis
                        )
                    } returns expectedHistory

                    // when
                    val result = pointService.charge(userId, chargeAmount)

                    // then
                    result.point shouldBe MAX_POINT
                    verify(exactly = 1) { userPointTable.insertOrUpdate(userId, MAX_POINT) }
                    verify(exactly = 1) {
                        pointHistoryTable.insert(
                            userId,
                            chargeAmount,
                            TransactionType.CHARGE,
                            expectedChargedPoint.updateMillis
                        )
                    }
                }
            }

            context("최대 포인트 한도를 1원만 초과하는 경우") {
                it("MaxPointExceededException을 발생시킨다") {
                    // given - 최대 포인트에서 1원 초과하는 충전 시도
                    val userId = PointTestFixture.VALID_USER_ID
                    val currentPoint = PointTestFixture.createUserPoint(point = MAX_POINT)
                    val chargeAmount = 1L

                    every { userPointTable.selectById(userId) } returns currentPoint

                    // when & then
                    shouldThrow<MaxPointExceededException> {
                        pointService.charge(userId, chargeAmount)
                    }

                    verify(exactly = 1) { userPointTable.selectById(userId) }
                    verify(exactly = 0) { userPointTable.insertOrUpdate(userId, OVER_MAX_AMOUNT) }
                    verify(exactly = 0) {
                        pointHistoryTable.insert(userId, chargeAmount, TransactionType.CHARGE, any<Long>())
                    }
                }
            }
        }
    }
    describe("use") {
        context("정상적인 포인트 사용") {
            it("포인트를 정확히 차감하고 히스토리를 기록한다") {
                // given
                val userId = PointTestFixture.VALID_USER_ID
                val useAmount = 2000L
                val currentPoint = PointTestFixture.createUserPoint(point = 5000L)
                val expectedUsedPoint = PointTestFixture.createUserPoint(point = 3000L)
                val expectedHistory = PointTestFixture.createUseHistory(
                    userId = userId,
                    amount = -useAmount,
                    timeMillis = System.currentTimeMillis()
                )

                every { userPointTable.selectById(userId) } returns currentPoint
                every { userPointTable.insertOrUpdate(userId, -useAmount) } returns expectedUsedPoint
                every {
                    pointHistoryTable.insert(
                        userId,
                        -useAmount,
                        TransactionType.USE,
                        any()
                    )
                } returns expectedHistory

                // when
                val result = pointService.use(userId, useAmount)

                // then
                result shouldBe expectedUsedPoint
                verify(exactly = 1) { userPointTable.selectById(userId) }
                verify(exactly = 1) { userPointTable.insertOrUpdate(userId, -useAmount) }
                verify(exactly = 1) {
                    pointHistoryTable.insert(userId, -useAmount, TransactionType.USE, any())
                }
            }
        }

        context("잔고 부족 시 포인트 사용 시도") {
            it("InsufficientPointException을 발생시키고 데이터 변경을 방지한다") {
                // given - 사용하려는 금액보다 적은 잔고
                val userId = PointTestFixture.VALID_USER_ID
                val useAmount = 10000L
                val currentPoint = PointTestFixture.createUserPoint(point = 5000L)

                every { userPointTable.selectById(userId) } returns currentPoint

                // when & then
                shouldThrow<InsufficientPointException> {
                    pointService.use(userId, useAmount)
                }

                verify(exactly = 1) { userPointTable.selectById(userId) }
                verify(exactly = 0) { userPointTable.insertOrUpdate(userId, -useAmount) }
                verify(exactly = 0) {
                    pointHistoryTable.insert(
                        userId,
                        -useAmount,
                        TransactionType.USE,
                        any<Long>() // updateMillis는 예외로 인해 실행되지 않음
                    )
                }
            }
        }

        context("잔고가 정확히 사용 금액과 같은 경우") {
            it("InsufficientPointException이 발생하지 않고 정상 처리된다") {
                // given - 사용 금액과 정확히 같은 잔고
                val userId = PointTestFixture.VALID_USER_ID
                val useAmount = 5000L
                val currentPoint = PointTestFixture.createUserPoint(point = 5000L)
                val expectedUsedPoint = PointTestFixture.createUserPoint(point = 0L)
                val expectedHistory = PointTestFixture.createUseHistory(
                    userId = userId,
                    amount = -useAmount,
                    timeMillis = System.currentTimeMillis()
                )

                every { userPointTable.selectById(userId) } returns currentPoint
                every { userPointTable.insertOrUpdate(userId, -useAmount) } returns expectedUsedPoint
                every {
                    pointHistoryTable.insert(
                        userId,
                        -useAmount,
                        TransactionType.USE,
                        any()
                    )
                } returns expectedHistory

                // when
                val result = pointService.use(userId, useAmount)

                // then
                result.point shouldBe 0L
                verify(exactly = 1) { userPointTable.selectById(userId) }
                verify(exactly = 1) { userPointTable.insertOrUpdate(userId, -useAmount) }
                verify(exactly = 1) {
                    pointHistoryTable.insert(userId, -useAmount, TransactionType.USE, any<Long>())
                }
            }
        }

        context("잔고가 사용 금액보다 1원 적은 경우") {
            it("InsufficientPointException을 발생시킨다") {
                // given - 사용 금액보다 1원 적은 잔고
                val userId = PointTestFixture.VALID_USER_ID
                val useAmount = 5000L
                val currentPoint = PointTestFixture.createUserPoint(point = 4999L)

                every { userPointTable.selectById(userId) } returns currentPoint

                // when & then
                shouldThrow<InsufficientPointException> {
                    pointService.use(userId, useAmount)
                }

                verify(exactly = 1) { userPointTable.selectById(userId) }
                verify(exactly = 0) { userPointTable.insertOrUpdate(userId, -useAmount) }
                verify(exactly = 0) {
                    pointHistoryTable.insert(userId, -useAmount, TransactionType.USE, any<Long>())
                }
            }
        }
    }
})

