import org.jooq.meta.jaxb.Logging

val flywayVersion = "12.0.1"
val jooqVersion = "3.20.11"
val kotlinLoggingJvmVersion = "7.0.3"
val lokiLogbackAppenderVersion = "2.0.3"
val springCloudVersion = "2025.1.1"
val testcontainersVersion = "1.21.3"
val uaJavaVersion = "1.6.1"

buildscript {
    dependencies {
        classpath("org.flywaydb:flyway-core:12.0.1")
        classpath("org.flywaydb:flyway-database-postgresql:12.0.1")
        classpath("org.jooq:jooq-codegen:3.20.11")
        classpath("org.postgresql:postgresql:42.7.4")
        classpath("org.testcontainers:testcontainers:1.21.3")
        classpath("org.testcontainers:postgresql:1.21.3")
    }
}

plugins {
    kotlin("jvm") version "2.3.10"
    kotlin("plugin.spring") version "2.3.10"
    id("org.springframework.boot") version "4.0.2"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.flywaydb.flyway") version "12.0.1"
}

group = "io.chaekpool"
version = "0.0.1-SNAPSHOT"
description = "Record-driven SNS for selecting books, writing reviews, and sharing."

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // === Kotlin Support ===
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.github.oshai:kotlin-logging-jvm:$kotlinLoggingJvmVersion")

    // === Spring Boot Core ===
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // === Security / OAuth2 ===
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

    // === Open Feign Client ===
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")

    // === ua-parser ===
    implementation("com.github.ua-parser:uap-java:$uaJavaVersion")

    // === Database ===
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.jooq:jooq:$jooqVersion")
    implementation("org.jooq:jooq-kotlin:$jooqVersion")
    implementation("org.postgresql:postgresql")

    // === Redis ===
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    // === Flyway ===
    implementation("org.flywaydb:flyway-core:$flywayVersion")
    implementation("org.flywaydb:flyway-database-postgresql:$flywayVersion")

    // === Observability / Monitoring ===
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-aspectj")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("org.springframework.boot:spring-boot-starter-opentelemetry")
    implementation("com.github.loki4j:loki-logback-appender:$lokiLogbackAppenderVersion")

    // === Testing ===
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage")
    }
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers:$testcontainersVersion")
    testImplementation("org.testcontainers:postgresql:$testcontainersVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testcontainersVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion")
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.named("compileKotlin") {
    dependsOn("jooqGenerate")
}

tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    environment = System.getenv()
}

// =============================================================================
// Flyway Configuration
// =============================================================================

flyway {
    url = System.getenv("POSTGRES_URL") ?: "jdbc:postgresql://localhost:5432/cp"
    user = System.getenv("POSTGRES_USER") ?: "admin"
    password = System.getenv("POSTGRES_PASSWORD") ?: "admin"
    locations = arrayOf("classpath:db/migration")
    baselineOnMigrate = true
    validateOnMigrate = true
}

tasks.withType<org.flywaydb.gradle.task.AbstractFlywayTask>().configureEach {
    notCompatibleWithConfigurationCache("Flyway plugin does not support configuration cache")
}

// =============================================================================
// jOOQ Configuration
// =============================================================================

val jooqOutputDir = layout.buildDirectory.dir("generated-src/jooq")

sourceSets {
    main {
        java {
            srcDir(jooqOutputDir)
        }
    }
}

tasks.register<Delete>("jooqClean") {
    group = "jooq"
    description = "Clean JOOQ generated code"
    delete(jooqOutputDir)
}

tasks.register("jooqGenerate") {
    group = "jooq"
    description = "Generate jOOQ code from database schema using Testcontainers"

    doLast {
        val postgres = org.testcontainers.containers.PostgreSQLContainer<Nothing>("postgres:18.2-alpine3.23")
        postgres.start()

        try {
            org.flywaydb.core.Flyway.configure()
                .dataSource(postgres.jdbcUrl, postgres.username, postgres.password)
                .locations("filesystem:src/main/resources/db/migration")
                .load()
                .migrate()

            org.jooq.codegen.GenerationTool.generate(
                org.jooq.meta.jaxb.Configuration()
                    .withLogging(Logging.INFO)
                    .withJdbc(
                        org.jooq.meta.jaxb.Jdbc()
                            .withDriver("org.postgresql.Driver")
                            .withUrl(postgres.jdbcUrl)
                            .withUser(postgres.username)
                            .withPassword(postgres.password)
                    )
                    .withGenerator(
                        org.jooq.meta.jaxb.Generator()
                            .withName("org.jooq.codegen.KotlinGenerator")
                            .withDatabase(
                                org.jooq.meta.jaxb.Database()
                                    .withName("org.jooq.meta.postgres.PostgresDatabase")
                                    .withInputSchema("public")
                                    .withIncludes(".*")
                                    .withExcludes("flyway_schema_history")
                            )
                            .withGenerate(
                                org.jooq.meta.jaxb.Generate()
                                    .withDeprecated(false)
                                    .withRecords(true)
                                    .withImmutablePojos(true)
                                    .withFluentSetters(true)
                                    .withJavaTimeTypes(true)
                                    .withKotlinNotNullPojoAttributes(true)
                                    .withKotlinNotNullRecordAttributes(true)
                            )
                            .withTarget(
                                org.jooq.meta.jaxb.Target()
                                    .withPackageName("io.chaekpool.generated.jooq")
                                    .withDirectory(jooqOutputDir.get().toString())
                                    .withClean(true)
                            )
                    )
            )
        } finally {
            postgres.stop()
        }
    }
}
