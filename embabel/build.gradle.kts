import java.net.URI

plugins {
    kotlin("jvm") version "2.2.0"
}

group = "com.github.drewlakee.mcp.demo.live"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()

    maven {
        url = URI.create("https://repo.embabel.com/artifactory/libs-snapshot")
    }

    maven {
        url = URI.create("https://repo.embabel.com/artifactory/libs-release")
    }
}

dependencies {
    implementation("com.embabel.agent:embabel-agent-starter:0.1.4-SNAPSHOT")
    implementation("com.embabel.agent:embabel-agent-starter-openai:0.1.4-SNAPSHOT")
}

tasks.test {
    useJUnitPlatform()
}