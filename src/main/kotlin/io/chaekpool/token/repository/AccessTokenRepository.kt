package io.chaekpool.token.repository

import io.chaekpool.token.entity.AccessTokenEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface AccessTokenRepository : CrudRepository<AccessTokenEntity, String> {

    fun findByUserId(userId: String): List<AccessTokenEntity>
    
    fun findByToken(token: String): AccessTokenEntity?
}
