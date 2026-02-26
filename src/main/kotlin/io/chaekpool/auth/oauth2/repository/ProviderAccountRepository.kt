package io.chaekpool.auth.oauth2.repository

import io.chaekpool.generated.jooq.tables.pojos.ProviderAccounts
import io.chaekpool.generated.jooq.tables.references.PROVIDER_ACCOUNTS
import org.jooq.DSLContext
import org.jooq.JSONB
import org.springframework.stereotype.Repository
import tools.jackson.databind.json.JsonMapper
import java.time.LocalDateTime
import java.util.UUID

@Repository
class ProviderAccountRepository(
    private val dsl: DSLContext,
    private val jsonMapper: JsonMapper
) {

    fun findByProviderAndAccountId(providerId: UUID, accountId: String): ProviderAccounts? =
        dsl
            .select(
                PROVIDER_ACCOUNTS.USER_ID,
                PROVIDER_ACCOUNTS.PROVIDER_ID,
                PROVIDER_ACCOUNTS.ACCOUNT_ID,
                PROVIDER_ACCOUNTS.ACCOUNT_REGISTRY,
                PROVIDER_ACCOUNTS.AUTH_REGISTRY,
                PROVIDER_ACCOUNTS.CREATED_AT,
                PROVIDER_ACCOUNTS.UPDATED_AT
            )
            .from(PROVIDER_ACCOUNTS)
            .where(PROVIDER_ACCOUNTS.PROVIDER_ID.eq(providerId))
            .and(PROVIDER_ACCOUNTS.ACCOUNT_ID.eq(accountId))
            .fetchOneInto(ProviderAccounts::class.java)

    fun saveProviderAccount(
        userId: UUID,
        providerId: UUID,
        accountId: String,
        accountRegistry: Any,
        authRegistry: Any
    ) {
        val accountRegistryJson = JSONB.jsonb(jsonMapper.writeValueAsString(accountRegistry))
        val authRegistryJson = JSONB.jsonb(jsonMapper.writeValueAsString(authRegistry))

        dsl
            .insertInto(PROVIDER_ACCOUNTS)
            .set(PROVIDER_ACCOUNTS.USER_ID, userId)
            .set(PROVIDER_ACCOUNTS.PROVIDER_ID, providerId)
            .set(PROVIDER_ACCOUNTS.ACCOUNT_ID, accountId)
            .set(PROVIDER_ACCOUNTS.ACCOUNT_REGISTRY, accountRegistryJson)
            .set(PROVIDER_ACCOUNTS.AUTH_REGISTRY, authRegistryJson)
            .execute()
    }

    fun updateAuthRegistry(userId: UUID, providerId: UUID, authRegistry: Any): Int {
        val authRegistryJson = JSONB.jsonb(jsonMapper.writeValueAsString(authRegistry))
        return dsl
            .update(PROVIDER_ACCOUNTS)
            .set(PROVIDER_ACCOUNTS.AUTH_REGISTRY, authRegistryJson)
            .set(PROVIDER_ACCOUNTS.UPDATED_AT, LocalDateTime.now())
            .where(PROVIDER_ACCOUNTS.USER_ID.eq(userId))
            .and(PROVIDER_ACCOUNTS.PROVIDER_ID.eq(providerId))
            .execute()
    }

    fun updateAccountRegistry(userId: UUID, providerId: UUID, accountRegistry: Any): Int {
        val accountRegistryJson = JSONB.jsonb(jsonMapper.writeValueAsString(accountRegistry))
        return dsl
            .update(PROVIDER_ACCOUNTS)
            .set(PROVIDER_ACCOUNTS.ACCOUNT_REGISTRY, accountRegistryJson)
            .set(PROVIDER_ACCOUNTS.UPDATED_AT, LocalDateTime.now())
            .where(PROVIDER_ACCOUNTS.USER_ID.eq(userId))
            .and(PROVIDER_ACCOUNTS.PROVIDER_ID.eq(providerId))
            .execute()
    }

    fun findByUserIdAndProviderId(userId: UUID, providerId: UUID): ProviderAccounts? =
        dsl
            .select(
                PROVIDER_ACCOUNTS.USER_ID,
                PROVIDER_ACCOUNTS.PROVIDER_ID,
                PROVIDER_ACCOUNTS.ACCOUNT_ID,
                PROVIDER_ACCOUNTS.ACCOUNT_REGISTRY,
                PROVIDER_ACCOUNTS.AUTH_REGISTRY,
                PROVIDER_ACCOUNTS.CREATED_AT,
                PROVIDER_ACCOUNTS.UPDATED_AT
            )
            .from(PROVIDER_ACCOUNTS)
            .where(PROVIDER_ACCOUNTS.USER_ID.eq(userId))
            .and(PROVIDER_ACCOUNTS.PROVIDER_ID.eq(providerId))
            .fetchOneInto(ProviderAccounts::class.java)
}
