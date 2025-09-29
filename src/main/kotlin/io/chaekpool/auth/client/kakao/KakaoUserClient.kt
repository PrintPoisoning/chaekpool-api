package io.chaekpool.auth.client.kakao

import io.chaekpool.auth.config.KakaoFeignConfig
import io.chaekpool.auth.dto.kakao.KakaoUserResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader

@Suppress("kotlin:S6517") // Single function interfaces should be functional interfaces
@FeignClient(
    name = "kakaoUserClient",
    url = "https://kapi.kakao.com",
    configuration = [KakaoFeignConfig::class]
)
interface KakaoUserClient {

    @GetMapping(
        value = ["/v2/user/me"],
        consumes = ["application/json"],
        produces = ["application/json"]
    )
    fun getUserInfo(
        @RequestHeader("Authorization") authorization: String
    ): KakaoUserResponse
}
