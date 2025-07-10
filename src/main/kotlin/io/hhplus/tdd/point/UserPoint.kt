package io.hhplus.tdd.point

import io.hhplus.tdd.point.exception.InsufficientPointException

data class UserPoint(
    val id: Long,
    val point: Long,
    val updateMillis: Long
) {
    fun charge(amount: Long): UserPoint {
        if (this.point < amount) {
            throw InsufficientPointException()
        }
        return copy(
            point = this.point + amount,
            updateMillis = System.currentTimeMillis()
        )
    }

    fun use(amount: Long): UserPoint {
        if (this.point < amount) {
            throw InsufficientPointException()
        }
        return copy(
            point = this.point - amount,
            updateMillis = System.currentTimeMillis()
        )
    }
}
