package io.chaekpool.common.handler

import io.chaekpool.common.dto.ErrorResponse
import io.chaekpool.common.exception.ServiceException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler(
    private val request: HttpServletRequest
) {

    @ExceptionHandler(ServiceException::class)
    fun handleServiceException(e: ServiceException): ResponseEntity<ErrorResponse> {
        val body = ErrorResponse(
            status = e.httpStatus.value(),
            error = e.httpStatus.name,
            message = e.message,
            errorCode = e.errorCode,
            path = request.requestURI
        )

        return ResponseEntity.status(e.httpStatus)
            .body(body)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val body = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = HttpStatus.BAD_REQUEST.name,
            message = e.bindingResult.fieldErrors.joinToString { "${it.field}=${it.defaultMessage}" },
            errorCode = "VALIDATION_ERROR",
            path = request.requestURI
        )

        return ResponseEntity.badRequest()
            .body(body)
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotSupported(e: HttpRequestMethodNotSupportedException): ResponseEntity<ErrorResponse> {
        val body = ErrorResponse(
            status = HttpStatus.METHOD_NOT_ALLOWED.value(),
            error = HttpStatus.METHOD_NOT_ALLOWED.name,
            message = "지원하지 않는 HTTP 메서드입니다. supported=${e.supportedMethods?.joinToString()}",
            errorCode = "METHOD_NOT_ALLOWED",
            path = request.requestURI
        )

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
            .body(body)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(e: Exception): ResponseEntity<ErrorResponse> {
        val body = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = HttpStatus.INTERNAL_SERVER_ERROR.name,
            message = e.message ?: "서버 오류",
            errorCode = "INTERNAL_SERVER_ERROR",
            path = request.requestURI
        )

        return ResponseEntity.internalServerError()
            .body(body)
    }
}
