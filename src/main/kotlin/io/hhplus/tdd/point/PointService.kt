package io.hhplus.tdd.point

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import org.springframework.stereotype.Service

/**
 * @author Doha Kim
 */
@Service
class PointService(
    private val pointHistoryTable: PointHistoryTable,
    private val userPointTable: UserPointTable,
) {
    fun getPoint(id: Long): UserPoint {
        return userPointTable.selectById(id)
    }

    fun getHistories(id: Long): List<PointHistory> {
        return pointHistoryTable.selectAllByUserId(id)
            .sortedBy { it.timeMillis }
    }

    fun charge(id: Long, amount: Long): UserPoint {
        val currentPoint = userPointTable.selectById(id)
        val chargedPoint = currentPoint.charge(amount)

        userPointTable.insertOrUpdate(id, chargedPoint.point)
        pointHistoryTable.insert(id, amount, TransactionType.CHARGE, chargedPoint.updateMillis)

        return chargedPoint
    }

    fun use(id: Long, amount: Long): UserPoint {
        val currentPoint = userPointTable.selectById(id)

        val usedPoint = currentPoint.use(amount)
        userPointTable.insertOrUpdate(id, -amount)

        pointHistoryTable.insert(id, -amount, TransactionType.USE, System.currentTimeMillis())
        return usedPoint
    }
}
