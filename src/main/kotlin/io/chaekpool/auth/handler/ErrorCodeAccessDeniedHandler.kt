package io.chaekpool.auth.handler

import io.chaekpool.common.dto.ApiResponse
import io.chaekpool.common.dto.ErrorData
import io.micrometer.tracing.Tracer
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component
import tools.jackson.databind.json.JsonMapper

@Component
class ErrorCodeAccessDeniedHandler(
    private val jsonMapper: JsonMapper,
    private val tracer: Tracer
) : AccessDeniedHandler {

    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AccessDeniedException
    ) {
        val status = request.getAttribute("_httpStatus") as? HttpStatus ?: HttpStatus.FORBIDDEN
        val errorCode = request.getAttribute("_errorCode") as? String ?: "FORBIDDEN"

        val body = ApiResponse(
            traceId = tracer.currentSpan()?.context()?.traceId() ?: "",
            spanId = tracer.currentSpan()?.context()?.spanId() ?: "",
            status = status.value(),
            data = ErrorData(code = errorCode, message = exception.message)
        )

        response.status = status.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.writer.write(jsonMapper.writeValueAsString(body))
    }
}
