package io.hhplus.tdd.point.controller

import io.hhplus.tdd.point.PointTestFixture
import io.hhplus.tdd.point.exception.MaxPointExceededException
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

/**
 * @author Doha Kim
 */
class PointChargeControllerTest : BaseControllerTest() {
    @Test
    @DisplayName("유효한 요청으로 충전 성공")
    fun chargeSuccess() {
        // given
        val userId = PointTestFixture.VALID_USER_ID
        val chargeAmount = PointTestFixture.MEDIUM_AMOUNT
        val expectedPoint = PointTestFixture.createUserPoint(point = chargeAmount)
        every { pointService.charge(userId, chargeAmount) } returns expectedPoint

        // when & then
        mockMvc.perform(
            patch("/point/$userId/charge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(PointTestFixture.validChargeRequest(chargeAmount))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(userId))
            .andExpect(jsonPath("$.point").value(chargeAmount))
            .andDo(print())

        verify(exactly = 1) { pointService.charge(userId, chargeAmount) }
    }

    @Test
    @DisplayName("음수 금액으로 충전 실패")
    fun chargeWithNegativeAmount() {
        mockMvc.perform(
            patch("/point/${PointTestFixture.VALID_USER_ID}/charge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(PointTestFixture.invalidRequest())
        )
            .andExpect(status().isBadRequest)
            .andDo(print())
    }

    @Test
    @DisplayName("0원 충전 실패")
    fun chargeWithZeroAmount() {
        mockMvc.perform(
            patch("/point/${PointTestFixture.VALID_USER_ID}/charge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(PointTestFixture.ZERO_AMOUNT.toString())
        )
            .andExpect(status().isBadRequest)
            .andDo(print())
    }

    @Test
    @DisplayName("유효하지 않은 사용자 ID로 충전 실패")
    fun chargeWithInvalidUserId() {
        mockMvc.perform(
            patch("/point/${PointTestFixture.INVALID_USER_ID_ZERO}/charge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(PointTestFixture.MEDIUM_AMOUNT.toString())
        )
            .andExpect(status().isBadRequest)
            .andDo(print())
    }

    @Test
    @DisplayName("최대 포인트 정확히 충전 성공")
    fun chargeExactMaxAmount() {
        // given
        val userId = PointTestFixture.VALID_USER_ID
        val maxAmount = PointTestFixture.MAX_AMOUNT
        val expectedPoint = PointTestFixture.createUserPoint(point = maxAmount)
        every { pointService.charge(userId, maxAmount) } returns expectedPoint

        // when & then
        mockMvc.perform(
            patch("/point/$userId/charge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(PointTestFixture.maxAmountRequest())
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.point").value(maxAmount))
            .andDo(print())

        verify(exactly = 1) { pointService.charge(userId, maxAmount) }
    }

    @Test
    @DisplayName("최대 포인트 1원 초과 충전 실패")
    fun chargeOneOverMaxAmount() {
        // given
        val userId = PointTestFixture.VALID_USER_ID
        val overMaxByOne = PointTestFixture.MAX_AMOUNT + 1
        every { pointService.charge(userId, overMaxByOne) } throws MaxPointExceededException()

        // when & then
        mockMvc.perform(
            patch("/point/$userId/charge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(overMaxByOne.toString())
        )
            .andExpect(status().isInternalServerError)
            .andDo(print())
    }
}
