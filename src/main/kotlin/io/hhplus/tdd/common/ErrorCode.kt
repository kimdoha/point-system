package io.hhplus.tdd.common

/**
 * @author Doha Kim
 */
enum class ErrorCode (
    val code: String,
    val message: String,
) {
    // 사용자 관련 에러
    USER_NOT_FOUND("P001", "존재하지 않는 사용자입니다."),

    // 포인트 충전 관련 에러
    MAX_POINT_EXCEEDED("P003", "최대 포인트를 초과할 수 없습니다."),

    // 포인트 사용 관련 에러
    INSUFFICIENT_POINT("P005", "포인트가 부족합니다."),
}
