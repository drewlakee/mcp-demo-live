package com.github.drewlakee.mcp.demo.live.embabel.configuration

import com.embabel.agent.config.models.OpenAiCompatibleModelFactory
import com.embabel.common.ai.model.Llm
import com.embabel.common.ai.model.PricingModel
import io.micrometer.observation.ObservationRegistry
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


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
    open fun yandexCloudGptOss120b(): Llm {
        return openAiCompatibleLlm(
            model = "gpt://b1gioucfterb2rnsqb1q/gpt-oss-120b",
            provider = "Yandex Cloud",
            knowledgeCutoffDate = null,
            pricingModel = PricingModel.ALL_YOU_CAN_EAT,
        )
    }
}