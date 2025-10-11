plugins {
    kotlin("jvm") version "2.2.0"
}

group = "com.github.drewlakee.mcp.demo.live"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        name = "embabel-releases"
        url = uri("https://repo.embabel.com/artifactory/libs-release")
        mavenContent {
            releasesOnly()
        }
    }
    maven {
        name = "embabel-snapshots"
        url = uri("https://repo.embabel.com/artifactory/libs-snapshot")
        mavenContent {
            snapshotsOnly()
        }
    }
    maven {
        name = "Spring Milestones"
        url = uri("https://repo.spring.io/milestone")
    }
}

dependencies {
    implementation("org.springframework.ai:spring-ai-starter-mcp-server-webmvc:1.1.0-M3")
    implementation("com.embabel.agent:embabel-agent-starter:0.1.4-SNAPSHOT")
}

tasks.test {
    useJUnitPlatform()
}