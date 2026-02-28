package io.chaekpool.common.handler

import io.chaekpool.common.exception.internal.BadRequestException
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.micrometer.tracing.Tracer
import io.mockk.every
import io.mockk.mockk
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.servlet.resource.NoResourceFoundException

class GlobalExceptionHandlerTest : BehaviorSpec({

    lateinit var tracer: Tracer
    lateinit var handler: GlobalExceptionHandler

    beforeTest {
        tracer = mockk()
        every { tracer.currentSpan() } returns null
        handler = GlobalExceptionHandler(tracer)
    }

    Given("BadRequestException이 발생했을 때") {
        val exception = BadRequestException(
            message = "잘못된 입력값입니다",
            errorCode = "INVALID_INPUT"
        )

        When("handleServiceException을 호출하면") {
            Then("status=400, code/message가 일치한다") {
                val result = handler.handleServiceException(exception)

                result.statusCode shouldBe HttpStatus.BAD_REQUEST
                result.body!!.status shouldBe 400
                result.body!!.data.code shouldBe "INVALID_INPUT"
                result.body!!.data.message shouldBe "잘못된 입력값입니다"
                result.body!!.traceId shouldBe ""
                result.body!!.spanId shouldBe ""
            }
        }
    }

    Given("MethodArgumentNotValidException이 발생했을 때") {
        val fieldError = FieldError("request", "email", "must not be blank")
        val bindingResult = mockk<BindingResult>()
        every { bindingResult.fieldErrors } returns listOf(fieldError)
        val exception = mockk<MethodArgumentNotValidException>()
        every { exception.bindingResult } returns bindingResult

        When("handleValidationException을 호출하면") {
            Then("status=400, code=VALIDATION_ERROR이다") {
                val result = handler.handleValidationException(exception)

                result.statusCode shouldBe HttpStatus.BAD_REQUEST
                result.body!!.status shouldBe 400
                result.body!!.data.code shouldBe "VALIDATION_ERROR"
                result.body!!.data.message shouldBe "email=must not be blank"
            }
        }
    }

    Given("NoResourceFoundException이 발생했을 때") {
        val exception = NoResourceFoundException(HttpMethod.GET, "/api/v1/nonexistent", "nonexistent")

        When("handleNotFound를 호출하면") {
            Then("status=404, code=NOT_FOUND이다") {
                val result = handler.handleNotFound(exception)

                result.statusCode shouldBe HttpStatus.NOT_FOUND
                result.body!!.status shouldBe 404
                result.body!!.data.code shouldBe "NOT_FOUND"
                result.body!!.data.message shouldBe "리소스를 찾을 수 없습니다"
            }
        }
    }

    Given("HttpRequestMethodNotSupportedException이 발생했을 때") {
        val exception = HttpRequestMethodNotSupportedException("DELETE", listOf("GET", "POST"))

        When("handleMethodNotSupported를 호출하면") {
            Then("status=405, code=METHOD_NOT_ALLOWED이다") {
                val result = handler.handleMethodNotSupported(exception)

                result.statusCode shouldBe HttpStatus.METHOD_NOT_ALLOWED
                result.body!!.status shouldBe 405
                result.body!!.data.code shouldBe "METHOD_NOT_ALLOWED"
                result.body!!.data.message shouldBe "지원하지 않는 HTTP 메서드입니다. supported=GET, POST"
            }
        }
    }

    Given("RuntimeException이 발생했을 때") {
        val exception = RuntimeException("Unexpected error occurred")

        When("handleGenericException을 호출하면") {
            Then("status=500, code=INTERNAL_SERVER_ERROR이다") {
                val result = handler.handleGenericException(exception)

                result.statusCode shouldBe HttpStatus.INTERNAL_SERVER_ERROR
                result.body!!.status shouldBe 500
                result.body!!.data.code shouldBe "INTERNAL_SERVER_ERROR"
                result.body!!.data.message shouldBe "Unexpected error occurred"
            }
        }
    }
})
