package io.chaekpool.auth.handler

import io.chaekpool.common.dto.ErrorResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import tools.jackson.databind.json.JsonMapper

@Component
class ErrorCodeAuthenticationEntryPoint(
    private val jsonMapper: JsonMapper
) : AuthenticationEntryPoint {

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException
    ) {
        val status = request.getAttribute("_httpStatus") as? HttpStatus ?: HttpStatus.UNAUTHORIZED
        val errorCode = request.getAttribute("_errorCode") as? String ?: "UNAUTHORIZED"


        val body = ErrorResponse(
            status = status.value(),
            error = status.name,
            message = exception.message,
            errorCode = errorCode,
            path = request.requestURI
        )

        response.status = status.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.writer.write(jsonMapper.writeValueAsString(body))
    }
}
