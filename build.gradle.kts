extra["kotlin-logging-version"] = "7.0.3"
extra["spring-cloud-version"] = "2025.1.1"
extra["loki-logback-appender"] = "2.0.3"
extra["ua-parser-version"] = "1.6.1"

plugins {
    kotlin("jvm") version "2.3.10"
    kotlin("plugin.spring") version "2.3.10"
    id("org.springframework.boot") version "4.0.2"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "io.chaekpool"
version = "0.0.1-SNAPSHOT"
description = "Record-driven SNS for selecting books, writing reviews, and sharing (책풀)."

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

// 버전은 Spring Boot BOM이 관리
dependencies {
    // === Spring Boot Core ===
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // === Observability / Monitoring ===
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-aspectj")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("org.springframework.boot:spring-boot-starter-opentelemetry")
    implementation("com.github.loki4j:loki-logback-appender:${property("loki-logback-appender")}")

    // === Database ===
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.postgresql:postgresql")

    // === Redis ===
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    // === Security / OAuth2 ===
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

    // === Flyway ===
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")

    // === Kotlin Support ===
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.github.oshai:kotlin-logging-jvm:${property("kotlin-logging-version")}")

    // === Open Feign Client ===
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")

    // === ua-parser ===
    implementation("com.github.ua-parser:uap-java:${property("ua-parser-version")}")

    // === Testing ===
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage")
    }
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("spring-cloud-version")}")
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

tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    environment = System.getenv()
}
