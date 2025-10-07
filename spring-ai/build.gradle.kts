plugins {
    kotlin("jvm") version "2.2.0"
}

group = "com.github.drewlakee.mcp.demo.live"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.ai:spring-ai-starter-mcp-server-webmvc:1.1.0-M3")
    implementation("org.springframework.ai:spring-ai-starter-model-openai:1.1.0-M3")

    implementation(project(":demo-commons"))
}

tasks.test {
    useJUnitPlatform()
}