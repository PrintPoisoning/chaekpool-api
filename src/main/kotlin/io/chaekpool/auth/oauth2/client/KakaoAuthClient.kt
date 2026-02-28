package io.chaekpool.auth.oauth2.client

import io.chaekpool.auth.oauth2.config.OAuth2FeignConfig
import io.chaekpool.auth.oauth2.dto.KakaoAuthRefreshTokenResponse
import io.chaekpool.auth.oauth2.dto.KakaoAuthTokenResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.CLIENT_ID
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.CLIENT_SECRET
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.CODE
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.GRANT_TYPE
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.REDIRECT_URI
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.REFRESH_TOKEN
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "kakaoAuthClient",
    url = "https://kauth.kakao.com",
    configuration = [OAuth2FeignConfig::class]
)
interface KakaoAuthClient {

    @PostMapping(
        value = ["/oauth/token"],
        consumes = [APPLICATION_JSON_VALUE],
        produces = [APPLICATION_JSON_VALUE]
    )
    fun postOAuthToken(
        @RequestParam(GRANT_TYPE) grantType: String = AuthorizationGrantType.AUTHORIZATION_CODE.value,
        @RequestParam(CLIENT_ID) clientId: String,
        @RequestParam(CLIENT_SECRET) clientSecret: String,
        @RequestParam(REDIRECT_URI) redirectUri: String,
        @RequestParam(CODE) code: String
    ): KakaoAuthTokenResponse

    @PostMapping(
        value = ["/oauth/token"],
        consumes = [APPLICATION_JSON_VALUE],
        produces = [APPLICATION_JSON_VALUE]
    )
    fun postRefreshToken(
        @RequestParam(GRANT_TYPE) grantType: String = AuthorizationGrantType.REFRESH_TOKEN.value,
        @RequestParam(CLIENT_ID) clientId: String,
        @RequestParam(CLIENT_SECRET) clientSecret: String,
        @RequestParam(REFRESH_TOKEN) refreshToken: String
    ): KakaoAuthRefreshTokenResponse
}
