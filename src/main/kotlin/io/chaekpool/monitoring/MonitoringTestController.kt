package io.chaekpool.monitoring

import io.github.oshai.kotlinlogging.KotlinLogging
import io.micrometer.core.annotation.Timed
import io.micrometer.observation.annotation.Observed
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import kotlin.random.Random

@RestController
@RequestMapping("/api/v1/monitoring")
class MonitoringTestController {

    private val logger = KotlinLogging.logger {}

    @GetMapping("/ping")
    @Timed(value = "monitoring.ping", description = "Ping endpoint response time")
    fun ping(): Map<String, Any> {
        logger.info { "Ping endpoint called" }
        return mapOf(
            "status" to "OK",
            "timestamp" to LocalDateTime.now(),
            "message" to "Monitoring stack is working!"
        )
    }

    @GetMapping("/logs/{level}")
    @Observed(name = "monitoring.logs")
    fun testLogs(@PathVariable level: String): Map<String, String> {
        when (level.uppercase()) {
            "INFO" -> logger.info { "This is an INFO level log message" }
            "DEBUG" -> logger.debug { "This is a DEBUG level log message" }
            "WARN" -> logger.warn { "This is a WARN level log message" }
            "ERROR" -> logger.error { "This is an ERROR level log message" }
            else -> logger.info { "Unknown log level: $level" }
        }
        return mapOf("logLevel" to level, "message" to "Log message sent")
    }

    @GetMapping("/delay")
    @Timed(
        value = "monitoring.delay",
        description = "Endpoint with random delay",
        histogram = true,
        percentiles = [0.5, 0.95, 0.99]
    )
    fun randomDelay(@RequestParam(defaultValue = "1000") maxDelayMs: Long): Map<String, Any> {
        val delay = Random.nextLong(0, maxDelayMs)
        logger.info { "Simulating delay of $delay ms" }

        Thread.sleep(delay)

        return mapOf(
            "delayMs" to delay,
            "maxDelayMs" to maxDelayMs,
            "message" to "Delayed response"
        )
    }

    @GetMapping("/error")
    @Observed(name = "monitoring.error")
    fun simulateError(@RequestParam(defaultValue = "false") throwError: Boolean): Map<String, Any> {
        logger.warn { "Error simulation endpoint called with throwError=$throwError" }

        if (throwError) {
            logger.error { "Simulating an error!" }
            throw RuntimeException("Simulated error for monitoring test")
        }

        return mapOf(
            "status" to "OK",
            "message" to "No error thrown. Use ?throwError=true to simulate an error"
        )
    }

    @GetMapping("/trace")
    @Timed(value = "monitoring.trace", description = "Multi-step trace simulation")
    fun traceTest(): Map<String, Any> {
        logger.info { "Starting multi-step trace" }

        simulateOperation("database", 50)
        simulateOperation("external-api", 100)
        simulateOperation("cache", 20)

        logger.info { "Completed multi-step trace" }

        return mapOf(
            "status" to "completed",
            "steps" to listOf("database", "external-api", "cache"),
            "message" to "Check Jaeger for distributed trace"
        )
    }

    private fun simulateOperation(operationName: String, delayMs: Long) {
        logger.debug { "Executing operation: $operationName" }
        Thread.sleep(delayMs)
        logger.debug { "Completed operation: $operationName in ${delayMs}ms" }
    }
}
