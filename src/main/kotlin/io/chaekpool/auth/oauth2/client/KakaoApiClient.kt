package io.chaekpool.auth.oauth2.client

import io.chaekpool.auth.oauth2.config.OAuth2FeignConfig
import io.chaekpool.auth.oauth2.dto.KakaoApiUserResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader

@Suppress("kotlin:S6517") // Single function interfaces should be functional interfaces
@FeignClient(
    name = "kakaoUserClient",
    url = "https://kapi.kakao.com",
    configuration = [OAuth2FeignConfig::class]
)
interface KakaoApiClient {

    @GetMapping(
        value = ["/v2/user/me"],
        consumes = [APPLICATION_JSON_VALUE],
        produces = [APPLICATION_JSON_VALUE]
    )
    fun getUser(
        @RequestHeader(AUTHORIZATION) authorization: String
    ): KakaoApiUserResponse
}
