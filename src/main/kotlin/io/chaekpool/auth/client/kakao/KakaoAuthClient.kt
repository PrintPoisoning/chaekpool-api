package io.chaekpool.auth.client.kakao

import io.chaekpool.auth.config.KakaoFeignConfig
import io.chaekpool.auth.dto.kakao.KakaoTokenResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "kakaoAuthClient",
    url = "https://kauth.kakao.com",
    configuration = [KakaoFeignConfig::class]
)
interface KakaoAuthClient {

    @PostMapping(
        value = ["/oauth/token"],
        consumes = ["application/json"],
        produces = ["application/json"]
    )
    fun getAccessToken(
        @RequestParam("grant_type") grantType: String = "authorization_code",
        @RequestParam("client_id") clientId: String,
        @RequestParam("client_secret") clientSecret: String,
        @RequestParam("redirect_uri") redirectUri: String,
        @RequestParam("code") code: String
    ): KakaoTokenResponse
}
