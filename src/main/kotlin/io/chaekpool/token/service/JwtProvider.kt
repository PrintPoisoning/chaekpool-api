package io.chaekpool.token.service

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jose.crypto.MACVerifier
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.chaekpool.common.util.LoggerDelegate
import io.chaekpool.token.config.JwtProperties
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.Date

@Component
class JwtProvider(
    private val props: JwtProperties
) {

    private val log by LoggerDelegate()
    private val secretKey: ByteArray = props.secret.toByteArray()

    fun createAccessToken(userId: String): String = createToken(userId, props.accessTokenValiditySeconds)

    fun createRefreshToken(userId: String): String = createToken(userId, props.refreshTokenValiditySeconds)

    private fun createToken(userId: String, validitySeconds: Long): String {
        val now = Instant.now()
        val exp = now.plusSeconds(validitySeconds)

        val claims = JWTClaimsSet.Builder()
            .subject(userId)
            .issueTime(Date.from(now))
            .expirationTime(Date.from(exp))
            .build()

        val signedJWT = SignedJWT(
            JWSHeader(JWSAlgorithm.HS256),
            claims
        )

        signedJWT.sign(MACSigner(secretKey))

        return signedJWT.serialize()
    }

    fun validateToken(token: String): Boolean =
        try {
            val signedJWT = SignedJWT.parse(token)
            val verifier = MACVerifier(secretKey)

            signedJWT.verify(verifier) && signedJWT.jwtClaimsSet.expirationTime.after(Date())
        } catch (e: Exception) {
            log.error("Token validation error", e)
            false
        }

    fun getUserId(token: String): String {
        val signedJWT = SignedJWT.parse(token)

        return signedJWT.jwtClaimsSet.subject
    }

    fun getExpirationTime(token: String): Long {
        val jwt = SignedJWT.parse(token)
        val exp = jwt.jwtClaimsSet.expirationTime.toInstant()
        val now = Date().toInstant()

        return exp.epochSecond - now.epochSecond
    }
}
