package io.chaekpool.user.service

import io.chaekpool.common.exception.internal.NotFoundException
import io.chaekpool.common.util.notNullOrThrow
import io.chaekpool.generated.jooq.tables.references.USERS
import io.chaekpool.user.dto.UserResponse
import io.chaekpool.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class UserService(private val userRepository: UserRepository) {

    @Transactional
    fun save(
        providerName: String,
        providerUserId: String,
        email: String?,
        profileImageUrl: String?,
        kakaoAccessToken: String?,
        kakaoRefreshToken: String?,
        tokenExpiry: LocalDateTime?
    ): UUID {
        val providerId = userRepository.findProviderIdByName(providerName)
            .notNullOrThrow { NotFoundException("인증 제공자를 찾을 수 없습니다: $providerName") }

        userRepository.findUserIdByProvider(providerName, providerUserId)?.let { userId ->
            userRepository.updateLastLoginAt(userId)
            userRepository.updateAuthAccountTokens(
                providerId, providerUserId, kakaoAccessToken, kakaoRefreshToken, tokenExpiry
            )
            return userId
        }

        val userId = userRepository.createUser(email, profileImageUrl)
        userRepository.createAuthAccount(
            userId, providerId, providerUserId, kakaoAccessToken, kakaoRefreshToken, tokenExpiry
        )
        return userId
    }

    fun getUser(userId: UUID): UserResponse {
        val record = userRepository.findById(userId)
            .notNullOrThrow { NotFoundException("사용자를 찾을 수 없습니다.") }

        return UserResponse(
            id = record.get(USERS.ID)!!,
            email = record.get(USERS.EMAIL),
            username = record.get(USERS.USERNAME),
            profileImageUrl = record.get(USERS.PROFILE_IMAGE_URL),
            status = record.get(USERS.STATUS)!!.literal,
            visibility = record.get(USERS.VISIBILITY)!!.literal
        )
    }
}
