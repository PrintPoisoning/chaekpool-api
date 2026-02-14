package io.chaekpool.auth.exception

import io.chaekpool.common.exception.ServiceException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.access.AccessDeniedException

class ErrorCodeAccessDeniedException(
    message: String,
    cause: Throwable
) : AccessDeniedException(message, cause) {
    constructor(e: ServiceException, request: HttpServletRequest) : this(e.message, e) {
        request.setAttribute("_errorCode", e.errorCode)
        request.setAttribute("_httpStatus", e.httpStatus)
    }
}
