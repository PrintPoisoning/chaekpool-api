package io.chaekpool.user.repository

import io.chaekpool.generated.jooq.tables.pojos.UserAccounts
import io.chaekpool.generated.jooq.tables.references.USER_ACCOUNTS
import org.jooq.DSLContext
import org.jooq.JSONB
import org.springframework.stereotype.Repository
import tools.jackson.databind.json.JsonMapper
import java.time.LocalDateTime
import java.util.UUID

@Repository
class UserAccountRepository(
    private val dsl: DSLContext,
    private val jsonMapper: JsonMapper
) {

    fun findByProviderAndAccountId(providerId: UUID, accountId: String): UserAccounts? =
        dsl
            .select(
                USER_ACCOUNTS.USER_ID,
                USER_ACCOUNTS.PROVIDER_ID,
                USER_ACCOUNTS.ACCOUNT_ID,
                USER_ACCOUNTS.ACCOUNT_REGISTRY,
                USER_ACCOUNTS.AUTH_REGISTRY,
                USER_ACCOUNTS.CREATED_AT,
                USER_ACCOUNTS.UPDATED_AT
            )
            .from(USER_ACCOUNTS)
            .where(USER_ACCOUNTS.PROVIDER_ID.eq(providerId))
            .and(USER_ACCOUNTS.ACCOUNT_ID.eq(accountId))
            .fetchOneInto(UserAccounts::class.java)

    fun save(
        userId: UUID,
        providerId: UUID,
        accountId: String,
        accountRegistry: Any,
        authRegistry: Any
    ) {
        val accountRegistryJson = JSONB.jsonb(jsonMapper.writeValueAsString(accountRegistry))
        val authRegistryJson = JSONB.jsonb(jsonMapper.writeValueAsString(authRegistry))

        dsl
            .insertInto(USER_ACCOUNTS)
            .set(USER_ACCOUNTS.USER_ID, userId)
            .set(USER_ACCOUNTS.PROVIDER_ID, providerId)
            .set(USER_ACCOUNTS.ACCOUNT_ID, accountId)
            .set(USER_ACCOUNTS.ACCOUNT_REGISTRY, accountRegistryJson)
            .set(USER_ACCOUNTS.AUTH_REGISTRY, authRegistryJson)
            .execute()
    }

    fun updateAuthRegistry(userId: UUID, providerId: UUID, authRegistry: Any): Int {
        val authRegistryJson = JSONB.jsonb(jsonMapper.writeValueAsString(authRegistry))
        return dsl
            .update(USER_ACCOUNTS)
            .set(USER_ACCOUNTS.AUTH_REGISTRY, authRegistryJson)
            .set(USER_ACCOUNTS.UPDATED_AT, LocalDateTime.now())
            .where(USER_ACCOUNTS.USER_ID.eq(userId))
            .and(USER_ACCOUNTS.PROVIDER_ID.eq(providerId))
            .execute()
    }

    fun updateAccountRegistry(userId: UUID, providerId: UUID, accountRegistry: Any): Int {
        val accountRegistryJson = JSONB.jsonb(jsonMapper.writeValueAsString(accountRegistry))
        return dsl
            .update(USER_ACCOUNTS)
            .set(USER_ACCOUNTS.ACCOUNT_REGISTRY, accountRegistryJson)
            .set(USER_ACCOUNTS.UPDATED_AT, LocalDateTime.now())
            .where(USER_ACCOUNTS.USER_ID.eq(userId))
            .and(USER_ACCOUNTS.PROVIDER_ID.eq(providerId))
            .execute()
    }

    fun findByUserId(userId: UUID, providerId: UUID): UserAccounts? =
        dsl
            .select(
                USER_ACCOUNTS.USER_ID,
                USER_ACCOUNTS.PROVIDER_ID,
                USER_ACCOUNTS.ACCOUNT_ID,
                USER_ACCOUNTS.ACCOUNT_REGISTRY,
                USER_ACCOUNTS.AUTH_REGISTRY,
                USER_ACCOUNTS.CREATED_AT,
                USER_ACCOUNTS.UPDATED_AT
            )
            .from(USER_ACCOUNTS)
            .where(USER_ACCOUNTS.USER_ID.eq(userId))
            .and(USER_ACCOUNTS.PROVIDER_ID.eq(providerId))
            .fetchOneInto(UserAccounts::class.java)
}
