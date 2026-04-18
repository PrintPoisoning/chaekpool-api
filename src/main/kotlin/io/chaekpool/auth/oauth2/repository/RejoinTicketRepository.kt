package io.chaekpool.auth.oauth2.repository

import io.chaekpool.auth.oauth2.entity.RejoinTicketEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RejoinTicketRepository : CrudRepository<RejoinTicketEntity, String>
