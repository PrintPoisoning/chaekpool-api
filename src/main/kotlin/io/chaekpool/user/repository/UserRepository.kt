package io.chaekpool.user.repository

import io.chaekpool.generated.jooq.tables.pojos.Users
import io.chaekpool.generated.jooq.tables.references.USERS
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

@Repository
class UserRepository(private val dsl: DSLContext) {

    fun findById(userId: UUID): Users? =
        dsl
            .select(
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

    fun save(user: Users): Users {
        val generatedId = dsl
            .insertInto(USERS)
            .set(USERS.EMAIL, user.email)
            .set(USERS.USERNAME, user.username)
            .set(USERS.PROFILE_IMAGE_URL, user.profileImageUrl)
            .returning(USERS.ID)
            .fetchOne()
            ?.id

        return Users(
            id = generatedId,
            email = user.email,
            username = user.username,
            profileImageUrl = user.profileImageUrl
        )
    }

    fun updateLastLoginAt(userId: UUID): Int =
        dsl
            .update(USERS)
            .set(USERS.LAST_LOGIN_AT, LocalDateTime.now())
            .set(USERS.UPDATED_AT, LocalDateTime.now())
            .where(USERS.ID.eq(userId))
            .execute()
}
