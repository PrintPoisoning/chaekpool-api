package io.chaekpool.common.handler

import io.chaekpool.common.dto.ApiResponse
import io.micrometer.tracing.Tracer
import org.springframework.core.MethodParameter
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.http.server.ServletServerHttpResponse
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice

@RestControllerAdvice(basePackages = ["io.chaekpool"])
class ApiResponseAdvice(
    private val tracer: Tracer
) : ResponseBodyAdvice<Any> {

    override fun supports(
        returnType: MethodParameter,
        converterType: Class<out HttpMessageConverter<*>>
    ): Boolean =
        JacksonJsonHttpMessageConverter::class.java.isAssignableFrom(converterType)

    override fun beforeBodyWrite(
        body: Any?,
        returnType: MethodParameter,
        selectedContentType: MediaType,
        selectedConverterType: Class<out HttpMessageConverter<*>>,
        request: ServerHttpRequest,
        response: ServerHttpResponse
    ): Any? {
        if (body == null) return null
        if (body is ApiResponse<*>) return body

        val status = (response as? ServletServerHttpResponse)?.servletResponse?.status ?: 200

        return ApiResponse(
            traceId = tracer.currentSpan()?.context()?.traceId() ?: "",
            spanId = tracer.currentSpan()?.context()?.spanId() ?: "",
            status = status,
            data = body
        )
    }
}
