package com.github.drewlakee.mcp.demo.live.spring.ai

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(
    scanBasePackages = ["com.github.drewlakee.mcp.demo.live.spring.ai"]
)
open class SpringAiWebApplication

fun main(args: Array<String>) {
    runApplication<SpringAiWebApplication>(*args)
}