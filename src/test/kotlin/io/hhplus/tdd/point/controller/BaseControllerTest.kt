package io.hhplus.tdd.point.controller

import com.ninjasquad.springmockk.MockkBean
import io.hhplus.tdd.point.PointController
import io.hhplus.tdd.point.PointService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc

/**
 * @author Doha Kim
 */
@WebMvcTest(PointController::class)
abstract class BaseControllerTest {
    @Autowired
    protected lateinit var mockMvc: MockMvc

    @MockkBean
    protected lateinit var pointService: PointService
}
