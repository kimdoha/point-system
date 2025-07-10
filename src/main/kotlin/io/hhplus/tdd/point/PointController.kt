package io.hhplus.tdd.point

import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/point")
class PointController(
    private val pointService: PointService,
) {
    @GetMapping("{id}")
    fun point(
        @PathVariable id: Long,
    ): UserPoint {
        return pointService.getPoint(id.validateUserId())
    }

    @GetMapping("{id}/histories")
    fun history(
        @PathVariable id: Long,
    ): List<PointHistory> {
        return pointService.getHistories(id.validateUserId())
    }

    @PatchMapping("{id}/charge")
    fun charge(
        @PathVariable id: Long,
        @RequestBody amount: Long,
    ): UserPoint {
        return pointService.charge(id.validateUserId(), amount.validateChargeAmount())
    }

    @PatchMapping("{id}/use")
    fun use(
        @PathVariable id: Long,
        @RequestBody amount: Long,
    ): UserPoint {
        if(amount <= 0) {
            throw IllegalArgumentException("사용 금액은 0보다 커야 합니다.")
        }
        return pointService.use(id.validateUserId(), amount.validateUseAmount())
    }

    private fun Long.validateChargeAmount(): Long {
        if (this <= 0) {
            throw IllegalArgumentException("충전 금액은 0보다 커야 합니다.")
        }
        return this
    }

    private fun Long.validateUseAmount(): Long {
        if (this <= 0) {
            throw IllegalArgumentException("사용 금액은 0보다 커야 합니다.")
        }
        return this
    }

    private fun Long.validateUserId(): Long {
        if (this <= 0) {
            throw IllegalArgumentException("사용자 ID는 0보다 커야 합니다.")
        }
        return this
    }
}
