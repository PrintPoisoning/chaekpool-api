package io.chaekpool.common.util

import feign.FeignException
import feign.Response
import feign.codec.ErrorDecoder
import java.nio.charset.StandardCharsets

class FeignErrorDecoder : ErrorDecoder {

    private val log by LoggerDelegate()

    override fun decode(methodKey: String?, response: Response): RuntimeException {
        val body = response.body()
            ?.asInputStream()
            ?.use { String(it.readAllBytes(), StandardCharsets.UTF_8) }
            ?: "No response body"

        log.error("[Feign-Error] methodKey={}, status={}, body={}", methodKey, response.status(), body)

        return when (response.status()) {
            400 -> IllegalArgumentException("잘못된 요청 (400)")
            401 -> RuntimeException("인증 실패 (401)")
            403 -> RuntimeException("권한 없음 (403)")
            else -> FeignException.errorStatus(methodKey, response)
        }
    }
}
