plugins {
    java
    jacoco
    id("org.springframework.boot") version "3.5.14"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.qronis"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

// ─── Dependency Management ────────────────────────────────────────────────

dependencyManagement {
    imports {
        mavenBom("org.springframework.modulith:spring-modulith-bom:1.4.1")
    }
}

val mapstructVersion = "1.6.3"

dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.modulith:spring-modulith-starter-core")
    implementation("org.springframework.modulith:spring-modulith-core")

    // Database
    runtimeOnly("org.postgresql:postgresql")
    implementation("org.flywaydb:flyway-core")
    runtimeOnly("org.flywaydb:flyway-database-postgresql")

    // MapStruct & Lombok
    implementation("org.mapstruct:mapstruct:$mapstructVersion")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")
    annotationProcessor("org.mapstruct:mapstruct-processor:$mapstructVersion")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.modulith:spring-modulith-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation(platform("org.testcontainers:testcontainers-bom:1.20.4"))
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// ─── Compile Options ─────────────────────────────────────────────────────

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(listOf(
        "-parameters",
        "-Amapstruct.defaultComponentModel=spring",
        "-Amapstruct.unmappedTargetPolicy=IGNORE"
    ))
}

// ─── Test Tasks ──────────────────────────────────────────────────────────

tasks.test {
    useJUnitPlatform { excludeTags("integration") }
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
    }
}

tasks.register<Test>("integrationTest") {
    description = "Roda testes de integração com Testcontainers + PostgreSQL"
    group = "verification"
    useJUnitPlatform { includeTags("integration") }
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
    }
    shouldRunAfter(tasks.test)
    classpath = sourceSets["test"].runtimeClasspath
    testClassesDirs = sourceSets["test"].output.classesDirs
}

// ─── JaCoCo ──────────────────────────────────────────────────────────────

val jacocoExclusions = listOf(
    "**/*DTO*",
    "**/*MapperImpl*",
    "**/*Config*",
    "**/*Properties*",
    "**/QronisApplication*",
    "**/QronisModuleDetectionStrategy*",
    // JPA domain entities — no-arg constructors called reflectively by Hibernate
    "**/domain/entity/**",
    // Domain and API exception classes — trivial constructors, tested indirectly
    "**/domain/exception/**",
    "**/api/exception/**",
    // Shared global exception handler — covered by integration tests (excluded from unit threshold)
    "**/shared/exception/**",
    // Security filter — covered by integration tests
    "**/security/**"
)

tasks.jacocoTestReport {
    executionData.setFrom(
        fileTree(layout.buildDirectory).include("jacoco/test.exec", "jacoco/integrationTest.exec")
    )
    reports {
        xml.required = true
        html.required = true
    }
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) { exclude(jacocoExclusions) }
        })
    )
}

tasks.register<JacocoCoverageVerification>("jacocoCoverageVerification") {
    dependsOn(tasks.jacocoTestReport)
    executionData.setFrom(
        fileTree(layout.buildDirectory).include("jacoco/test.exec", "jacoco/integrationTest.exec")
    )
    classDirectories.setFrom(
        files(sourceSets["main"].output.classesDirs.map {
            fileTree(it) { exclude(jacocoExclusions) }
        })
    )
    violationRules {
        rule {
            limit {
                counter = "LINE"
                minimum = "0.93".toBigDecimal()
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.named("jacocoCoverageVerification"))
}
