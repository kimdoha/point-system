package io.hhplus.tdd.point.controller

import io.hhplus.tdd.point.PointTestFixture
import io.hhplus.tdd.point.TransactionType
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
class PointHistoryControllerTest : BaseControllerTest() {
    @Test
    @DisplayName("유효한 ID로 히스토리 조회 성공")
    fun getHistoriesSuccess() {
        // given
        val userId = PointTestFixture.VALID_USER_ID
        val expectedHistories = PointTestFixture.createHistoryList(userId)
        every { pointService.getHistories(userId) } returns expectedHistories

        // when & then
        mockMvc.perform(
            get("/point/$userId/histories")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$.length()").value(3))
            .andExpect(jsonPath("$[0].userId").value(userId))
            .andExpect(jsonPath("$[0].type").value(TransactionType.CHARGE.name))
            .andExpect(jsonPath("$[1].type").value(TransactionType.USE.name))
            .andExpect(jsonPath("$[2].type").value(TransactionType.CHARGE.name))
            .andDo(print())

        verify(exactly = 1) { pointService.getHistories(userId) }
    }

    @Test
    @DisplayName("빈 히스토리 조회 성공")
    fun getEmptyHistories() {
        // given
        val userId = PointTestFixture.VALID_USER_ID
        every { pointService.getHistories(userId) } returns emptyList()

        // when & then
        mockMvc.perform(
            get("/point/$userId/histories")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$.length()").value(0))
            .andDo(print())
    }

    @Test
    @DisplayName("유효하지 않은 ID로 히스토리 조회 실패")
    fun getHistoriesWithInvalidId() {
        mockMvc.perform(
            get("/point/${PointTestFixture.INVALID_USER_ID_ZERO}/histories")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
            .andDo(print())
    }

    @Test
    @DisplayName("시간순으로 정렬된 히스토리 조회")
    fun getHistoriesSortedByTime() {
        // given
        val userId = PointTestFixture.VALID_USER_ID
        val sortedHistories = PointTestFixture.createHistoryList(userId)
        every { pointService.getHistories(userId) } returns sortedHistories

        // when & then
        mockMvc.perform(
            get("/point/$userId/histories")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].timeMillis").value(1000))
            .andExpect(jsonPath("$[1].timeMillis").value(2000))
            .andExpect(jsonPath("$[2].timeMillis").value(3000))
            .andDo(print())
    }
}
