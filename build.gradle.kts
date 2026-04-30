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

// ─── Source Sets ────────────────────────────────────────────────────────────

sourceSets {
    create("integrationTest") {
        java.srcDir("src/integrationTest/java")
        resources.srcDir("src/integrationTest/resources")
        compileClasspath += sourceSets["main"].output + sourceSets["test"].output
        runtimeClasspath += sourceSets["main"].output + sourceSets["test"].output
    }
}

configurations["integrationTestImplementation"]
    .extendsFrom(configurations["testImplementation"])
configurations["integrationTestRuntimeOnly"]
    .extendsFrom(configurations["testRuntimeOnly"])

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

    // Test (unit)
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.modulith:spring-modulith-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation(platform("org.testcontainers:testcontainers-bom:1.20.4"))
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Test (integration) — Testcontainers
    "integrationTestImplementation"("org.springframework.boot:spring-boot-starter-test")
    "integrationTestImplementation"("org.springframework.security:spring-security-test")
    "integrationTestImplementation"("org.springframework.boot:spring-boot-testcontainers")
    "integrationTestImplementation"(platform("org.testcontainers:testcontainers-bom:1.20.4"))
    "integrationTestImplementation"("org.testcontainers:postgresql")
    "integrationTestImplementation"("org.testcontainers:junit-jupiter")
    "integrationTestRuntimeOnly"("org.junit.platform:junit-platform-launcher")
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

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
    }
}

val integrationTest = tasks.register<Test>("integrationTest") {
    description = "Roda testes de integração com Testcontainers + PostgreSQL"
    group = "verification"
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    useJUnitPlatform()
    shouldRunAfter(tasks.test)
}

// ─── JaCoCo ──────────────────────────────────────────────────────────────

val jacocoExclusions = listOf(
    "**/*DTO*",
    "**/*MapperImpl*",
    "**/*Config*",
    "**/*Properties*",
    "**/QronisApplication*",
    "**/QronisModuleDetectionStrategy*"
)

tasks.named<JacocoReport>("jacocoTestReport") {
    dependsOn(tasks.test)
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
    dependsOn(tasks.named("jacocoTestReport"))
    classDirectories.setFrom(
        files(sourceSets["main"].output.classesDirs.map {
            fileTree(it) { exclude(jacocoExclusions) }
        })
    )
    executionData.setFrom(tasks.named<JacocoReport>("jacocoTestReport").get().executionData)
    violationRules {
        rule {
            limit {
                minimum = "0.90".toBigDecimal()
            }
        }
    }
}
