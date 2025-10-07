package com.github.drewlakee.mcp.demo.live.spring.ai

import com.github.drewlakee.mcp.demo.live.datasource.mongo.MongoSubscriptionsCatalogueDao
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.ai.tool.execution.ToolCallResultConverter
import org.springframework.stereotype.Service
import java.lang.reflect.Type

data class CatalogueSubscription(
    val subscriptionName: String,
    val features: Set<String>,
)

class HumanReadableCatalogueSubscriptions : ToolCallResultConverter {
    override fun convert(result: Any?, returnType: Type?): String =
        buildString {
            if (result is List<*> && result.size > 0) {
                append("Подписки каталога: ").append("\n")
                result.forEach { element ->
                    if (element is CatalogueSubscription) {
                        element.run {
                            append(
                                """
                                Название подписки: $subscriptionName
                                Фичи подписки: $features
                            """.trimIndent()
                            )
                        }
                    }
                }
            } else {
                append("В каталоге нет таких подписок")
            }
        }
}

@Service
class CatalogueSubscriptionsService {
    private val log: Logger = LoggerFactory.getLogger(CatalogueSubscriptionsService::class.java)
    private val dao: MongoSubscriptionsCatalogueDao = MongoSubscriptionsCatalogueDao()

    @Tool(
        name = "get_all_catalogue_subscriptions",
        description = "Получить все подписки каталога тарифной сетки",
        resultConverter = HumanReadableCatalogueSubscriptions::class,
    )
    fun getCatalogueSubscriptions(): List<CatalogueSubscription> {
        log.info("Calling get_all_catalogue_subscriptions")
        return dao.getCatalogueSubscriptions()
            .map {
                CatalogueSubscription(
                    subscriptionName = it.subscriptionName,
                    features = it.features,
                )
            }
    }

    @Tool(
        name = "get_specific_catalogue_subscriptions",
        description = "Получить информацию о конкретных подписках",
        resultConverter = HumanReadableCatalogueSubscriptions::class,
    )
    fun getCatalogueSubscriptions(
        @ToolParam(required = true, description = "Список названий подписок") subscriptionNames: List<String>
    ): List<CatalogueSubscription> {
        log.info("Calling get_specific_catalogue_subscriptions with arguments(subscriptionNames=$subscriptionNames)")
        return subscriptionNames.mapNotNull {
            dao.getSubscriptionByName(it)?.let {
                CatalogueSubscription(
                    subscriptionName = it.subscriptionName,
                    features = it.features,
                )
            }
        }
    }
}