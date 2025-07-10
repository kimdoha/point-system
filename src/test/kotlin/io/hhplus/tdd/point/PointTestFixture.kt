package io.hhplus.tdd.point

/**
 * @author Doha Kim
 */
object PointTestFixture {
    // User IDs
    const val VALID_USER_ID = 1L
    const val NEW_USER_ID = 2L
    const val INVALID_USER_ID_ZERO = 0L
    const val INVALID_USER_ID_NEGATIVE = -1L

    // Amounts
    const val SMALL_AMOUNT = 1000L
    const val MEDIUM_AMOUNT = 5000L
    const val LARGE_AMOUNT = 50000L
    const val MAX_AMOUNT = 1000000L
    const val OVER_MAX_AMOUNT = 1000001L
    const val ZERO_AMOUNT = 0L
    const val NEGATIVE_AMOUNT = -1000L

    // Time
    val BASE_TIME = System.currentTimeMillis()
    const val OLD_TIME = 1000L

    // User Points
    fun createUserPoint(
        id: Long = VALID_USER_ID,
        point: Long = MEDIUM_AMOUNT,
        updateMillis: Long = BASE_TIME
    ) = UserPoint(id, point, updateMillis)

    fun createEmptyUserPoint(id: Long = VALID_USER_ID) =
        UserPoint(id, 0L, BASE_TIME)

    fun createUserPointWithSufficientBalance(id: Long = VALID_USER_ID) =
        UserPoint(id, LARGE_AMOUNT, BASE_TIME)

    // Point Histories
    fun createChargeHistory(
        id: Long = 1L,
        userId: Long = VALID_USER_ID,
        amount: Long = MEDIUM_AMOUNT,
        timeMillis: Long = BASE_TIME
    ) = PointHistory(id, userId, TransactionType.CHARGE, amount, timeMillis)

    fun createUseHistory(
        id: Long = 2L,
        userId: Long = VALID_USER_ID,
        amount: Long = -SMALL_AMOUNT,
        timeMillis: Long = BASE_TIME + 1000L
    ) = PointHistory(id, userId, TransactionType.USE, amount, timeMillis)

    fun createHistoryList(userId: Long = VALID_USER_ID): List<PointHistory> = listOf(
        createChargeHistory(1L, userId, MEDIUM_AMOUNT, OLD_TIME),
        createUseHistory(2L, userId, -SMALL_AMOUNT, OLD_TIME + 1000L),
        createChargeHistory(3L, userId, SMALL_AMOUNT, OLD_TIME + 2000L)
    )

    // JSON Requests
    fun validChargeRequest(amount: Long = MEDIUM_AMOUNT): String = amount.toString()
    fun validUseRequest(amount: Long = SMALL_AMOUNT): String = amount.toString()
    fun invalidRequest(): String = NEGATIVE_AMOUNT.toString()
    fun maxAmountRequest(): String = MAX_AMOUNT.toString()
}
