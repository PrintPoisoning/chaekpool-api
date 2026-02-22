package io.chaekpool.auth.token.repository

import io.chaekpool.auth.token.entity.RefreshTokenEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface RefreshTokenRepository : CrudRepository<RefreshTokenEntity, String> {

    fun findByUserId(userId: UUID): List<RefreshTokenEntity>
}
