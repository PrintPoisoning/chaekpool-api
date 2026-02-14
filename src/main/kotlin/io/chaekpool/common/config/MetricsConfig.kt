package io.chaekpool.common.config

import io.micrometer.core.aop.TimedAspect
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.observation.ObservationPredicate
import io.micrometer.observation.ObservationRegistry
import io.micrometer.observation.aop.ObservedAspect
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.server.observation.ServerRequestObservationContext
import org.springframework.security.config.observation.SecurityObservationSettings

@Configuration
class MetricsConfig {

    @Bean
    fun timedAspect(registry: MeterRegistry): TimedAspect {
        return TimedAspect(registry)
    }

    @Bean
    fun observedAspect(observationRegistry: ObservationRegistry): ObservedAspect {
        return ObservedAspect(observationRegistry)
    }

    @Bean
    fun noActuatorObservationPredicate(): ObservationPredicate {
        return ObservationPredicate { _, context ->
            if (context is ServerRequestObservationContext) {
                context.carrier?.requestURI?.startsWith("/actuator") != true
            } else {
                true
            }
        }
    }

    @Bean
    fun securityObservationSettings(): SecurityObservationSettings {
        return SecurityObservationSettings.noObservations()
    }
}
