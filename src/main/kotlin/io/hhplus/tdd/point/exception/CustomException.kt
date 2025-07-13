package io.hhplus.tdd.point.exception

import io.hhplus.tdd.common.ErrorCode

/**
 * @author Doha Kim
 */
class UserNotFoundException(message: String? = null) : PointSystemException(
    ErrorCode.USER_NOT_FOUND,
    message
)

class InsufficientPointException(message: String? = null) : PointSystemException(
    ErrorCode.INSUFFICIENT_POINT,
    message
)

class MaxPointExceededException(message: String? = null) : PointSystemException(
    ErrorCode.MAX_POINT_EXCEEDED,
    message
)
