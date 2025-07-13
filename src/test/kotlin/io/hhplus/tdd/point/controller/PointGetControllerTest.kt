package io.hhplus.tdd.point.controller

import io.hhplus.tdd.point.PointTestFixture
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * @author Doha Kim
 */
class PointGetControllerTest: BaseControllerTest() {
    @Nested
    @DisplayName("포인트 조회 API 테스트")
    inner class GetPointTest {

        @Test
        @DisplayName("유효한 ID로 포인트 조회 성공")
        fun getPointSuccess() {
            // given
            val userId = PointTestFixture.VALID_USER_ID
            val expectedPoint = PointTestFixture.createUserPoint()
            every { pointService.getPoint(userId) } returns expectedPoint

            // when & then
            mockMvc.perform(
                get("/point/$userId")
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.point").value(PointTestFixture.MEDIUM_AMOUNT))
                .andExpect(jsonPath("$.updateMillis").exists())
                .andDo(print())

            verify(exactly = 1) { pointService.getPoint(userId) }
        }

        @Test
        @DisplayName("유효하지 않은 ID로 포인트 조회 실패 - 0")
        fun getPointWithZeroId() {
            mockMvc.perform(
                get("/point/${PointTestFixture.INVALID_USER_ID_ZERO}")
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isBadRequest)
                .andDo(print())
        }

        @Test
        @DisplayName("유효하지 않은 ID로 포인트 조회 실패 - 음수")
        fun getPointWithNegativeId() {
            mockMvc.perform(
                get("/point/${PointTestFixture.INVALID_USER_ID_NEGATIVE}")
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isBadRequest)
                .andDo(print())
        }

        @Test
        @DisplayName("포인트가 0인 사용자 조회")
        fun getPointWithZeroBalance() {
            // given
            val userId = PointTestFixture.VALID_USER_ID
            val emptyPoint = PointTestFixture.createEmptyUserPoint()
            every { pointService.getPoint(userId) } returns emptyPoint

            // when & then
            mockMvc.perform(
                get("/point/$userId")
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.point").value(0))
                .andDo(print())
        }
    }
}
