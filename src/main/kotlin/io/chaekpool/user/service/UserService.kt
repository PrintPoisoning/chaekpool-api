package io.chaekpool.user.service

import io.chaekpool.common.exception.internal.NotFoundException
import io.chaekpool.common.util.notNullOrThrow
import io.chaekpool.generated.jooq.tables.pojos.Users
import io.chaekpool.user.dto.UserResponse
import io.chaekpool.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UserService(private val userRepository: UserRepository) {

    @Transactional(readOnly = true)
    fun getUser(userId: UUID): UserResponse {
        val user = userRepository.findById(userId)
            .notNullOrThrow { NotFoundException("사용자를 찾을 수 없습니다.") }

        return UserResponse(
            email = user.email,
            username = user.username,
            profileImageUrl = user.profileImageUrl,
            visibility = user.visibility!!.name,
            status = user.status!!.name
        )
    }

    @Transactional
    fun createUser(
        email: String?,
        username: String?,
        profileImageUrl: String?
    ): Users {
        val user = Users(
            email = email,
            username = username,
            profileImageUrl = profileImageUrl
        )

        return userRepository.save(user)
    }

    @Transactional
    fun updateLastLoginAt(userId: UUID) {
        userRepository.updateLastLoginAt(userId)
    }
}
