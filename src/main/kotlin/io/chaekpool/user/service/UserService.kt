package io.chaekpool.user.service

import io.chaekpool.common.util.notNullOrThrow
import io.chaekpool.user.dto.UserResponse
import io.chaekpool.user.exception.UserNotFoundException
import io.chaekpool.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UserService(private val userRepository: UserRepository) {

    @Transactional(readOnly = true)
    fun getUser(userId: UUID): UserResponse {
        val user = userRepository.findById(userId)
            .notNullOrThrow { UserNotFoundException() }

        return UserResponse(
            email = user.email,
            username = user.username,
            profileImageUrl = user.profileImageUrl,
            visibility = user.visibility!!.name,
            status = user.status!!.name
        )
    }
}
