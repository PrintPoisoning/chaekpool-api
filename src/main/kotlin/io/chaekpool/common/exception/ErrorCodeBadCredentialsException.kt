package io.chaekpool.common.exception

import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.authentication.BadCredentialsException

class ErrorCodeBadCredentialsException(
    message: String,
    cause: Throwable
) : BadCredentialsException(message, cause) {
    constructor(e: ServiceException, request: HttpServletRequest) : this(e.message, e) {
        request.setAttribute("_errorCode", e.errorCode)
        request.setAttribute("_httpStatus", e.httpStatus)
    }
}
