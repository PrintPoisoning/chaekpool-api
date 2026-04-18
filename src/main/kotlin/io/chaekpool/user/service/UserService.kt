package io.chaekpool.user.service

import io.chaekpool.auth.token.service.TokenService
import io.chaekpool.common.util.isTrueOrThrow
import io.chaekpool.common.util.notNullOrThrow
import io.chaekpool.generated.jooq.enums.UserStatusType
import io.chaekpool.user.dto.UserResponse
import io.chaekpool.user.exception.UserLeavedException
import io.chaekpool.user.exception.UserNotFoundException
import io.chaekpool.user.repository.UserRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    private val tokenService: TokenService
) {

    private val log = KotlinLogging.logger {}

    @Transactional(readOnly = true)
    fun getUser(userId: UUID): UserResponse {
        val user = userRepository.findById(userId)
            .notNullOrThrow { UserNotFoundException() }

        (user.status != UserStatusType.LEAVED).isTrueOrThrow { UserLeavedException() }

        return UserResponse(
            email = user.email,
            nickname = user.nickname,
            handle = user.handle,
            profileImageUrl = user.profileImageUrl,
            thumbnailImageUrl = user.thumbnailImageUrl,
            visibility = user.visibility!!.name,
            status = user.status!!.name
        )
    }

    @Transactional
    fun leave(userId: UUID, accessToken: String, refreshToken: String) {
        val user = userRepository.findById(userId)
            .notNullOrThrow { UserNotFoundException() }

        (user.status != UserStatusType.LEAVED).isTrueOrThrow { UserLeavedException() }

        val affected = userRepository.leaveById(userId)
        (affected > 0).isTrueOrThrow { UserLeavedException() }

        tokenService.deactivate(userId, accessToken, refreshToken)
        tokenService.deactivateAll(userId)

        log.info { "[USER_LEAVE] userId=$userId" }
    }
}
