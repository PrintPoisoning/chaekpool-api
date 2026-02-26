package io.chaekpool.auth.oauth2.repository

import io.chaekpool.generated.jooq.tables.pojos.AuthProviders
import io.chaekpool.generated.jooq.tables.references.AUTH_PROVIDERS
import org.jooq.DSLContext
import org.springframework.stereotype.Repository

@Repository
class AuthProviderRepository(private val dsl: DSLContext) {

    fun findByProviderName(providerName: String): AuthProviders? =
        dsl
            .select(
                AUTH_PROVIDERS.ID,
                AUTH_PROVIDERS.PROVIDER_NAME,
                AUTH_PROVIDERS.DESCRIPTION
            )
            .from(AUTH_PROVIDERS)
            .where(AUTH_PROVIDERS.PROVIDER_NAME.eq(providerName))
            .fetchOneInto(AuthProviders::class.java)
}
