package com.github.drewlakee.mcp.demo.live.embabel

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class EmbabelWebApplication

fun main(args: Array<String>) {
    runApplication<EmbabelWebApplication>(*args)
}