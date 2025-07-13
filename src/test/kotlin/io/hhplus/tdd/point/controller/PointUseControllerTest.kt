package io.hhplus.tdd.point.controller

/**
 * @author Doha Kim
 */

import io.hhplus.tdd.point.PointTestFixture
import io.hhplus.tdd.point.exception.InsufficientPointException
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

class PointUseControllerTest : BaseControllerTest() {
    @Test
    @DisplayName("유효한 요청으로 사용 성공")
    fun useSuccess() {
        // given
        val userId = PointTestFixture.VALID_USER_ID
        val useAmount = PointTestFixture.SMALL_AMOUNT
        val remainingPoint = PointTestFixture.MEDIUM_AMOUNT - useAmount
        val expectedPoint = PointTestFixture.createUserPoint(point = remainingPoint)
        every { pointService.use(userId, useAmount) } returns expectedPoint

        // when & then
        mockMvc.perform(
            patch("/point/$userId/use")
                .contentType(MediaType.APPLICATION_JSON)
                .content(PointTestFixture.validUseRequest(useAmount))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(userId))
            .andExpect(jsonPath("$.point").value(remainingPoint))
            .andDo(print())

        verify(exactly = 1) { pointService.use(userId, useAmount) }
    }

    @Test
    @DisplayName("음수 금액으로 사용 실패")
    fun useWithNegativeAmount() {
        mockMvc.perform(
            patch("/point/${PointTestFixture.VALID_USER_ID}/use")
                .contentType(MediaType.APPLICATION_JSON)
                .content(PointTestFixture.invalidRequest())
        )
            .andExpect(status().isBadRequest)
            .andDo(print())
    }

    @Test
    @DisplayName("0원 사용 실패")
    fun useWithZeroAmount() {
        mockMvc.perform(
            patch("/point/${PointTestFixture.VALID_USER_ID}/use")
                .contentType(MediaType.APPLICATION_JSON)
                .content(PointTestFixture.ZERO_AMOUNT.toString())
        )
            .andExpect(status().isBadRequest)
            .andDo(print())
    }

    @Test
    @DisplayName("유효하지 않은 사용자 ID로 사용 실패")
    fun useWithInvalidUserId() {
        mockMvc.perform(
            patch("/point/${PointTestFixture.INVALID_USER_ID_NEGATIVE}/use")
                .contentType(MediaType.APPLICATION_JSON)
                .content(PointTestFixture.SMALL_AMOUNT.toString())
        )
            .andExpect(status().isBadRequest)
            .andDo(print())
    }

    @Test
    @DisplayName("포인트 부족으로 사용 실패")
    fun useWithInsufficientPoint() {
        // given
        val userId = PointTestFixture.VALID_USER_ID
        val excessiveAmount = PointTestFixture.LARGE_AMOUNT
        every { pointService.use(userId, excessiveAmount) } throws InsufficientPointException()

        // when & then
        mockMvc.perform(
            patch("/point/$userId/use")
                .contentType(MediaType.APPLICATION_JSON)
                .content(excessiveAmount.toString())
        )
            .andExpect(status().isInternalServerError)
            .andDo(print())
    }

    @Test
    @DisplayName("전체 포인트 사용 성공")
    fun useAllPoints() {
        // given
        val userId = PointTestFixture.VALID_USER_ID
        val allPoints = PointTestFixture.MEDIUM_AMOUNT
        val expectedPoint = PointTestFixture.createEmptyUserPoint()
        every { pointService.use(userId, allPoints) } returns expectedPoint

        // when & then
        mockMvc.perform(
            patch("/point/$userId/use")
                .contentType(MediaType.APPLICATION_JSON)
                .content(allPoints.toString())
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.point").value(0))
            .andDo(print())
    }
}
