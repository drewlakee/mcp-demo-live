package com.github.drewlakee.mcp.demo.live.spring.ai

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor
import org.springframework.ai.tool.ToolCallbackProvider
import org.springframework.ai.tool.method.MethodToolCallbackProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class AiConfiguration {
    @Bean
    open fun toolsProvider(
        userSubscriptionsTools: UserSubscriptionsService,
        catalogueSubscriptionsService: CatalogueSubscriptionsService,
    ): ToolCallbackProvider =
        MethodToolCallbackProvider
            .builder()
            .toolObjects(
                userSubscriptionsTools,
                catalogueSubscriptionsService,
            ).build()

    @Bean
    open fun chatClient(
        builder: ChatClient.Builder,
        toolCallbackProvider: ToolCallbackProvider,
    ): ChatClient =
        builder
            .defaultAdvisors(SimpleLoggerAdvisor())
            .defaultToolCallbacks(toolCallbackProvider)
            .build()
}