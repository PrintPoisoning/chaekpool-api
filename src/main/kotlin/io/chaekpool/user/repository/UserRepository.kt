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
                USERS.NICKNAME,
                USERS.HANDLE,
                USERS.PROFILE_IMAGE_URL,
                USERS.THUMBNAIL_IMAGE_URL,
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
            .set(USERS.NICKNAME, user.nickname)
            .set(USERS.HANDLE, user.handle)
            .set(USERS.PROFILE_IMAGE_URL, user.profileImageUrl)
            .set(USERS.THUMBNAIL_IMAGE_URL, user.thumbnailImageUrl)
            .returning(USERS.ID)
            .fetchOne()
            ?.id

        return Users(
            id = generatedId,
            email = user.email,
            nickname = user.nickname,
            handle = user.handle,
            profileImageUrl = user.profileImageUrl,
            thumbnailImageUrl = user.thumbnailImageUrl
        )
    }

    fun existsByHandle(handle: String): Boolean =
        dsl
            .selectCount()
            .from(USERS)
            .where(USERS.HANDLE.eq(handle))
            .fetchOne(0, Int::class.java)!! > 0

    fun updateLastLoginAt(userId: UUID): Int =
        dsl
            .update(USERS)
            .set(USERS.LAST_LOGIN_AT, LocalDateTime.now())
            .set(USERS.UPDATED_AT, LocalDateTime.now())
            .where(USERS.ID.eq(userId))
            .execute()
}
