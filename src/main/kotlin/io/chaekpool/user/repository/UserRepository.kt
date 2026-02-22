package io.chaekpool.user.repository

import io.chaekpool.generated.jooq.tables.references.AUTH_PROVIDERS
import io.chaekpool.generated.jooq.tables.references.USERS
import io.chaekpool.generated.jooq.tables.references.USER_AUTH_ACCOUNTS
import org.jooq.DSLContext
import org.jooq.Record
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

@Repository
class UserRepository(private val dsl: DSLContext) {

    fun findProviderIdByName(providerName: String): UUID? =
        dsl.select(AUTH_PROVIDERS.ID)
            .from(AUTH_PROVIDERS)
            .where(AUTH_PROVIDERS.PROVIDER_NAME.eq(providerName))
            .fetchOne(AUTH_PROVIDERS.ID)

    fun findUserIdByProvider(providerName: String, providerUserId: String): UUID? =
        dsl.select(USER_AUTH_ACCOUNTS.USER_ID)
            .from(USER_AUTH_ACCOUNTS)
            .join(AUTH_PROVIDERS).on(USER_AUTH_ACCOUNTS.PROVIDER_ID.eq(AUTH_PROVIDERS.ID))
            .where(
                AUTH_PROVIDERS.PROVIDER_NAME.eq(providerName),
                USER_AUTH_ACCOUNTS.PROVIDER_USER_ID.eq(providerUserId)
            )
            .fetchOne(USER_AUTH_ACCOUNTS.USER_ID)

    fun createUser(email: String?, profileImageUrl: String?): UUID =
        dsl.insertInto(USERS)
            .set(USERS.EMAIL, email)
            .set(USERS.PROFILE_IMAGE_URL, profileImageUrl)
            .set(USERS.LAST_LOGIN_AT, LocalDateTime.now())
            .returning(USERS.ID)
            .fetchOne()!!
            .id!!

    fun updateLastLoginAt(userId: UUID) {
        dsl.update(USERS)
            .set(USERS.LAST_LOGIN_AT, LocalDateTime.now())
            .set(USERS.UPDATED_AT, LocalDateTime.now())
            .where(USERS.ID.eq(userId))
            .execute()
    }

    fun createAuthAccount(
        userId: UUID,
        providerId: UUID,
        providerUserId: String,
        accessToken: String?,
        refreshToken: String?,
        tokenExpiry: LocalDateTime?
    ) {
        dsl.insertInto(USER_AUTH_ACCOUNTS)
            .set(USER_AUTH_ACCOUNTS.USER_ID, userId)
            .set(USER_AUTH_ACCOUNTS.PROVIDER_ID, providerId)
            .set(USER_AUTH_ACCOUNTS.PROVIDER_USER_ID, providerUserId)
            .set(USER_AUTH_ACCOUNTS.ACCESS_TOKEN, accessToken)
            .set(USER_AUTH_ACCOUNTS.REFRESH_TOKEN, refreshToken)
            .set(USER_AUTH_ACCOUNTS.TOKEN_EXPIRY, tokenExpiry)
            .execute()
    }

    fun updateAuthAccountTokens(
        providerId: UUID,
        providerUserId: String,
        accessToken: String?,
        refreshToken: String?,
        tokenExpiry: LocalDateTime?
    ) {
        dsl.update(USER_AUTH_ACCOUNTS)
            .set(USER_AUTH_ACCOUNTS.ACCESS_TOKEN, accessToken)
            .set(USER_AUTH_ACCOUNTS.REFRESH_TOKEN, refreshToken)
            .set(USER_AUTH_ACCOUNTS.TOKEN_EXPIRY, tokenExpiry)
            .set(USER_AUTH_ACCOUNTS.UPDATED_AT, LocalDateTime.now())
            .where(
                USER_AUTH_ACCOUNTS.PROVIDER_ID.eq(providerId),
                USER_AUTH_ACCOUNTS.PROVIDER_USER_ID.eq(providerUserId)
            )
            .execute()
    }

    fun findById(userId: UUID): Record? =
        dsl.select(
            USERS.ID,
            USERS.EMAIL,
            USERS.USERNAME,
            USERS.PROFILE_IMAGE_URL,
            USERS.STATUS,
            USERS.VISIBILITY
        )
            .from(USERS)
            .where(USERS.ID.eq(userId))
            .fetchOne()
}
