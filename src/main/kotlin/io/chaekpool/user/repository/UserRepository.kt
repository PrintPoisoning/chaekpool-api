package io.chaekpool.user.repository

import io.chaekpool.generated.jooq.enums.UserStatusType
import io.chaekpool.generated.jooq.tables.pojos.Users
import io.chaekpool.generated.jooq.tables.references.USERS
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.Objects
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
        var step = dsl
            .insertInto(USERS)
            .set(USERS.EMAIL, user.email)
            .set(USERS.NICKNAME, user.nickname)
            .set(USERS.HANDLE, user.handle)
            .set(USERS.PROFILE_IMAGE_URL, user.profileImageUrl)
            .set(USERS.THUMBNAIL_IMAGE_URL, user.thumbnailImageUrl)

        if (Objects.nonNull(user.id)) {
            step = step.set(USERS.ID, user.id)
        }

        return step
            .onConflict(USERS.ID)
            .doUpdate()
            .set(USERS.NICKNAME, user.nickname)
            .set(USERS.PROFILE_IMAGE_URL, user.profileImageUrl)
            .set(USERS.THUMBNAIL_IMAGE_URL, user.thumbnailImageUrl)
            .set(USERS.UPDATED_AT, LocalDateTime.now())
            .returning()
            .fetchOneInto(Users::class.java)!!
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

    fun leaveById(userId: UUID): Int =
        dsl
            .update(USERS)
            .set(USERS.STATUS, UserStatusType.LEAVED)
            .set(USERS.UPDATED_AT, LocalDateTime.now())
            .where(USERS.ID.eq(userId))
            .and(USERS.STATUS.ne(UserStatusType.LEAVED))
            .execute()

    fun restoreById(
        userId: UUID,
        nickname: String?,
        profileImageUrl: String?,
        thumbnailImageUrl: String?
    ): Int =
        dsl
            .update(USERS)
            .set(USERS.STATUS, UserStatusType.ACTIVE)
            .set(USERS.NICKNAME, nickname)
            .set(USERS.PROFILE_IMAGE_URL, profileImageUrl)
            .set(USERS.THUMBNAIL_IMAGE_URL, thumbnailImageUrl)
            .set(USERS.UPDATED_AT, LocalDateTime.now())
            .where(USERS.ID.eq(userId))
            .and(USERS.STATUS.eq(UserStatusType.LEAVED))
            .execute()

    fun anonymizeById(userId: UUID): Int =
        dsl
            .update(USERS)
            .setNull(USERS.EMAIL)
            .setNull(USERS.NICKNAME)
            .setNull(USERS.PROFILE_IMAGE_URL)
            .setNull(USERS.THUMBNAIL_IMAGE_URL)
            .set(USERS.UPDATED_AT, LocalDateTime.now())
            .where(USERS.ID.eq(userId))
            .and(USERS.STATUS.eq(UserStatusType.LEAVED))
            .execute()
}
