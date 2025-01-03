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

    // Spring Web
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-tomcat")

    // MongoDB and Spring Data
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.25")

    // Spring Security for Web (without WebFlux)
    implementation("org.springframework.boot:spring-boot-starter-security")

    // dotenv for environment variables
    implementation("io.github.cdimascio:dotenv-kotlin:6.2.2")

    // JWT dependencies
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    implementation("io.jsonwebtoken:jjwt-impl:0.11.5")
    implementation("io.jsonwebtoken:jjwt-jackson:0.11.5")

    // AOP for logging
    implementation("org.springframework.boot:spring-boot-starter-aop")

    // Swagger (OpenAPI) for Spring Web
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.1.0")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-api:2.1.0")

    // Lombok to reduce boilerplate
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok:1.18.24")

    // Caching
    implementation("org.springframework.boot:spring-boot-starter-cache")

    // Testing dependencies for Web
    testImplementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Additional dependencies for testing
    testImplementation("org.springframework.boot:spring-boot-starter-data-mongodb")  // For MongoDB testing
    testImplementation("org.springframework.boot:spring-boot-starter-aop")  // For AOP testing
    testImplementation("org.mockito:mockito-core:5.3.0")  // For additional mock support
    testImplementation("org.springframework.security:spring-security-test:6.4.1") // To test Spring Security
}