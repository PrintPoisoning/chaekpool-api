package io.chaekpool.token.repository

import io.chaekpool.token.entity.BlacklistEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface BlacklistRepository : CrudRepository<BlacklistEntity, String>
