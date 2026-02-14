package io.chaekpool.auth.token.service

import com.nimbusds.jose.JOSEException
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jose.crypto.MACVerifier
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.chaekpool.auth.token.config.JwtProperties
import io.chaekpool.auth.token.exception.InvalidTokenException
import io.chaekpool.auth.token.exception.MissingClaimException
import io.chaekpool.auth.token.exception.TokenExpiredException
import io.chaekpool.common.exception.ServiceException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import java.text.ParseException
import java.time.Instant
import java.util.*

@Component
class JwtProvider(
    private val props: JwtProperties
) {

    private val log = KotlinLogging.logger {}

    private val secretKey: ByteArray = props.secret.toByteArray()

    fun createAccessToken(userId: Long): String = createToken(userId.toString(), props.accessTokenValiditySeconds)

    fun createRefreshToken(userId: Long): String = createToken(userId.toString(), props.refreshTokenValiditySeconds)

    private fun createToken(subject: String, validitySeconds: Long): String {
        val now = Instant.now()
        val exp = now.plusSeconds(validitySeconds)

        val claims = JWTClaimsSet.Builder()
            .subject(subject)
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

    fun validateToken(token: String): Boolean {
        return try {
            assertToken(token)
            true
        } catch (e: Exception) {
            log.error(e) { "JWT validation error" }
            false
        }
    }

    fun assertToken(token: String) {
        try {
            val jwt = SignedJWT.parse(token) // ParseException 가능
            val verifier = MACVerifier(secretKey) // JOSEException / IAE 가능

            if (!jwt.verify(verifier)) {
                throw InvalidTokenException("JWT signature verification failed")
            }

            val claims = jwt.jwtClaimsSet // ParseException 가능
            val exp = claims.expirationTime ?: run {
                throw MissingClaimException("JWT has no exp")
            }

            if (exp.before(Date())) {
                throw TokenExpiredException()
            }
        } catch (e: ServiceException) {
            throw e
        } catch (e: ParseException) {
            throw InvalidTokenException("Malformed JWT: ${e.message}") // JWT parse error
        } catch (e: JOSEException) {
            throw InvalidTokenException("JWT verification error: ${e.message}") // JOSE error (key/alg/crypto)
        } catch (e: IllegalArgumentException) {
            throw InvalidTokenException("Illegal JWT argument: ${e.message}") // Illegal argument during JWT handling
        } catch (e: Exception) {
            throw InvalidTokenException("Unexpected JWT error: ${e.message}") // Unexpected token validation error
        }
    }

    fun getUserId(token: String): Long {
        val signedJWT = SignedJWT.parse(token)

        return signedJWT.jwtClaimsSet.subject.toLong()
    }

    fun getExpirationTime(token: String): Long {
        val jwt = SignedJWT.parse(token)
        val exp = jwt.jwtClaimsSet.expirationTime.toInstant()
        val now = Date().toInstant()

        return exp.epochSecond - now.epochSecond
    }
}
