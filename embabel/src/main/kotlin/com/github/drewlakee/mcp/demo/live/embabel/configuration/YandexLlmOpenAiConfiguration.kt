package com.github.drewlakee.mcp.demo.live.embabel.configuration

import com.embabel.agent.api.models.OpenAiCompatibleModelFactory
import com.embabel.common.ai.model.Llm
import com.embabel.common.ai.model.PricingModel
import io.micrometer.observation.ObservationRegistry
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

data class ModelConfiguration(
    val id: String,
    val temperature: Double,
)

@ConfigurationPropertiesScan
@ConfigurationProperties("application")
open class ApplicationLlmProperties(
    val reactiveModel: ModelConfiguration,
    val thinkingModel: ModelConfiguration,
)

/**
 * 3.19.1. Adding LLMs
 * https://docs.embabel.com/embabel-agent/guide/0.1.3-SNAPSHOT/index.html#adding-llms
 */
@Configuration
open class YandexLlmOpenAiConfiguration(
    @Value("\${OPENAI_API_KEY}")
    apiKey: String,
) : OpenAiCompatibleModelFactory(
    baseUrl = "https://llm.api.cloud.yandex.net",
    apiKey = apiKey,
    observationRegistry = ObservationRegistry.NOOP,
    completionsPath = null,
    embeddingsPath = null,
) {

    @Bean
    open fun reactiveModel(properties: ApplicationLlmProperties): Llm {
        return openAiCompatibleLlm(
            model = properties.reactiveModel.id,
            provider = "Yandex Cloud",
            knowledgeCutoffDate = null,
            pricingModel = PricingModel.ALL_YOU_CAN_EAT,
        )
    }

    @Bean
    open fun thinkingModel(properties: ApplicationLlmProperties): Llm {
        return openAiCompatibleLlm(
            model = properties.thinkingModel.id,
            provider = "Yandex Cloud",
            knowledgeCutoffDate = null,
            pricingModel = PricingModel.ALL_YOU_CAN_EAT,
        )
    }
}