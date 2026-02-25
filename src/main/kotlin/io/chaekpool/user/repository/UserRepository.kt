package io.chaekpool.user.repository

import io.chaekpool.generated.jooq.tables.pojos.Users
import io.chaekpool.generated.jooq.tables.references.USERS
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class UserRepository(private val dsl: DSLContext) {

    fun findById(userId: UUID): Users? =
        dsl.select(
            USERS.ID,
            USERS.EMAIL,
            USERS.USERNAME,
            USERS.PROFILE_IMAGE_URL,
            USERS.VISIBILITY,
            USERS.STATUS
        )
            .from(USERS)
            .where(USERS.ID.eq(userId))
            .fetchOneInto(Users::class.java)
}
