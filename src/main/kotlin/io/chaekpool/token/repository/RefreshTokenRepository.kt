package io.chaekpool.token.repository

import io.chaekpool.token.entity.RefreshTokenEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RefreshTokenRepository : CrudRepository<RefreshTokenEntity, String> {

    fun findByUserId(userId: String): List<RefreshTokenEntity>
    
    fun findByToken(token: String): RefreshTokenEntity?
}
