package com.github.drewlakee.mcp.demo.live.toolcalling

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

private val JSON = ObjectMapper()
    .registerKotlinModule()
    .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

data class CustomCallingTools(
    val tools: List<Tool>,
) {
    data class Tool(
        @field:JsonProperty("tool_name") val toolName: String,
        val arguments: List<Argument>,
    ) {
        data class Argument(
            @field:JsonProperty("argument_name")
            val name: String,
            val value: String,
            val type: String,
        )
    }
}

fun String.toCustomCallingTools(): CustomCallingTools? =
    runCatching<CustomCallingTools> { JSON.readValue(this) }.getOrNull()