plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.4.0"
    id("io.spring.dependency-management") version "1.1.6"
}

group = "hampusborg"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // WebFlux dependencies
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-reactor-netty")

    // MongoDB and Spring Data
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.25")

    // Spring Security for WebFlux (version will be managed by Spring Boot BOM)
    implementation("org.springframework.boot:spring-boot-starter-security")

    // dotenv for environment variables
    implementation("io.github.cdimascio:dotenv-kotlin:6.2.2")

    // QRCode dependencies
    implementation("com.google.zxing:core:3.5.1")
    implementation("com.google.zxing:javase:3.5.1")

    // JWT dependencies
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    implementation("io.jsonwebtoken:jjwt-impl:0.11.5")
    implementation("io.jsonwebtoken:jjwt-jackson:0.11.5")

    // Resilience4j for fault tolerance
    implementation("io.github.resilience4j:resilience4j-kotlin:1.7.1")

    // AOP for logging
    implementation("org.springframework.boot:spring-boot-starter-aop")

    // Swagger (OpenAPI) for WebFlux
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.1.0")
    implementation("org.springdoc:springdoc-openapi-starter-webflux-api:2.1.0")

    // Lombok to reduce boilerplate
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok:1.18.24")

    // Caching (if needed)
    implementation("org.springframework.boot:spring-boot-starter-cache")

    // Testing dependencies for WebFlux
    testImplementation("org.springframework.boot:spring-boot-starter-webflux")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}