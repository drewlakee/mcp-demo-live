package com.github.drewlakee.mcp.demo.live.embabel

import com.github.drewlakee.mcp.demo.live.embabel.configuration.ApplicationLlmProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(ApplicationLlmProperties::class)
open class EmbabelWebApplication

fun main(args: Array<String>) {
    runApplication<EmbabelWebApplication>(*args)
}