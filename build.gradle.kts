plugins {
    kotlin("jvm") version "2.2.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.http4k:http4k-connect-core:6.17.0.0")
    implementation("org.http4k:http4k-client-okhttp:6.17.0.0")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.13.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.8")
    implementation("org.mongodb:mongodb-driver-kotlin-sync:5.6.0")

    // only for spring ai
    implementation("org.springframework.ai:spring-ai-starter-mcp-server-webmvc:1.1.0-M3")
    implementation("org.springframework.ai:spring-ai-starter-model-openai:1.1.0-M3")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(23)
}