package io.hhplus.tdd.point.exception

import io.hhplus.tdd.common.ErrorCode

/**
 * @author Doha Kim
 */
abstract class PointSystemException(
    errorCode: ErrorCode,
    message: String? = null
) : RuntimeException(message ?: errorCode.message)
