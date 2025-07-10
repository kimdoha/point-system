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
    INVALID_CHARGE_AMOUNT("P002", "충전 금액은 0보다 커야 합니다."),
    MAX_POINT_EXCEEDED("P003", "최대 포인트를 초과할 수 없습니다."),

    // 포인트 사용 관련 에러
    INVALID_USE_AMOUNT("P004", "사용 금액은 0보다 커야 합니다."),
    INSUFFICIENT_POINT("P005", "포인트가 부족합니다."),

    VALIDATION_FAILED("P006", "입력 값 검증에 실패했습니다."),
    INTERNAL_SERVER_ERROR("P999", "서버 내부 오류가 발생했습니다."),
}
