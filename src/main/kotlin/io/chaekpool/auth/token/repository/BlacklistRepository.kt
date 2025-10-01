package io.chaekpool.auth.token.repository

import io.chaekpool.auth.token.entity.BlacklistEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface BlacklistRepository : CrudRepository<BlacklistEntity, String>
