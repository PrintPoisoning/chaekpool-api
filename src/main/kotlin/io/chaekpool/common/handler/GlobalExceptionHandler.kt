package io.chaekpool.common.handler

import io.chaekpool.common.dto.ApiResponse
import io.chaekpool.common.dto.ErrorData
import io.chaekpool.common.exception.ServiceException
import io.github.oshai.kotlinlogging.KotlinLogging
import io.micrometer.tracing.Tracer
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.resource.NoResourceFoundException

@RestControllerAdvice
class GlobalExceptionHandler(
    private val tracer: Tracer
) {

    private val log = KotlinLogging.logger {}

    @ExceptionHandler(ServiceException::class)
    fun handleServiceException(e: ServiceException): ResponseEntity<ApiResponse<ErrorData>> {
        log.warn { "[EXCEPTION] code=${e.errorCode} message=${e.message}" }

        return buildErrorResponse(
            status = e.httpStatus,
            code = e.errorCode,
            message = e.message
        )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<ApiResponse<ErrorData>> {
        val message = e.bindingResult.fieldErrors.joinToString { "${it.field}=${it.defaultMessage}" }
        log.warn { "[EXCEPTION] code=VALIDATION_ERROR message=$message" }

        return buildErrorResponse(
            status = HttpStatus.BAD_REQUEST,
            code = "VALIDATION_ERROR",
            message = message
        )
    }

    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNotFound(e: NoResourceFoundException): ResponseEntity<ApiResponse<ErrorData>> {
        log.warn { "[EXCEPTION] code=NOT_FOUND message=리소스를 찾을 수 없습니다" }

        return buildErrorResponse(
            status = HttpStatus.NOT_FOUND,
            code = "NOT_FOUND",
            message = "리소스를 찾을 수 없습니다"
        )
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotSupported(e: HttpRequestMethodNotSupportedException): ResponseEntity<ApiResponse<ErrorData>> {
        val message = "지원하지 않는 HTTP 메서드입니다. supported=${e.supportedMethods?.joinToString()}"
        log.warn { "[EXCEPTION] code=METHOD_NOT_ALLOWED message=$message" }

        return buildErrorResponse(
            status = HttpStatus.METHOD_NOT_ALLOWED,
            code = "METHOD_NOT_ALLOWED",
            message = message
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(e: Exception): ResponseEntity<ApiResponse<ErrorData>> {
        log.error(e) { "[EXCEPTION] code=INTERNAL_SERVER_ERROR message=${e.message}" }

        return buildErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR,
            code = "INTERNAL_SERVER_ERROR",
            message = e.message ?: "서버 오류"
        )
    }

    private fun buildErrorResponse(
        status: HttpStatus,
        code: String,
        message: String?
    ): ResponseEntity<ApiResponse<ErrorData>> {
        val body = ApiResponse(
            traceId = tracer.currentSpan()?.context()?.traceId() ?: "",
            spanId = tracer.currentSpan()?.context()?.spanId() ?: "",
            status = status.value(),
            data = ErrorData(code = code, message = message)
        )
        return ResponseEntity.status(status).body(body)
    }
}
