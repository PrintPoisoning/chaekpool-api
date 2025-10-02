package io.chaekpool.auth.token.repository

import io.chaekpool.auth.token.entity.RefreshTokenEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RefreshTokenRepository : CrudRepository<RefreshTokenEntity, String> {

    fun findByUserId(userId: Long): List<RefreshTokenEntity>
}
