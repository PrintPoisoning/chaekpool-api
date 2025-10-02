package io.chaekpool.auth.exception

import io.chaekpool.common.exception.ServiceException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.authentication.BadCredentialsException

class ErrorCodeBadCredentialsException(
    message: String,
    cause: Throwable? = null
) : BadCredentialsException(message, cause) {
    constructor(e: ServiceException, request: HttpServletRequest) : this(e.message, e) {
        request.setAttribute("_errorCode", e.errorCode)
        request.setAttribute("_httpStatus", e.httpStatus)
    }
}
