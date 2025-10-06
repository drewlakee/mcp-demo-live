package com.github.drewlakee.mcp.demo.live.spring.ai.services

import com.github.drewlakee.mcp.demo.live.datasource.mongo.MongoUserSubscriptionsDao
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.ai.tool.execution.ToolCallResultConverter
import org.springframework.stereotype.Service
import java.lang.reflect.Type

data class UserSubscription(
    val id: Int,
    val uid: Int,
    val subscriptionName: String,
    val features: Set<String>,
)

class HumanReadableUserSubscriptions : ToolCallResultConverter {
    override fun convert(result: Any?, returnType: Type?): String =
        buildString {
            if (result is List<*> && result.size > 0) {
                append("Активные подписки пользователя [${result.size}]: ").append("\n")
                result.forEach { element ->
                    if (element is UserSubscription) {
                        element.run {
                            append("""
                                Пользователь: $uid
                                Название подписки: $subscriptionName
                                Фичи подписки: $features
                            """.trimIndent())
                        }
                    }
                }
            } else {
                append("Нет активных подписок, скорее всего такого пользователя нет в базе подписок")
            }
        }
}

@Service
class UserSubscriptionsService {
    private val log: Logger = LoggerFactory.getLogger(UserSubscriptionsService::class.java)
    private val dao: MongoUserSubscriptionsDao = MongoUserSubscriptionsDao()

    @Tool(
        name = "get_user_active_subscriptions",
        description = "Получить активные подписки пользователя",
        resultConverter = HumanReadableUserSubscriptions::class,
    )
    fun getActiveUserSubscriptions(
        @ToolParam(required = true, description = "uid пользователя") uid: Int
    ): List<UserSubscription> {
        log.info("Calling get_user_active_subscriptions with arguments(uid=$uid)")
        return dao.getByUid(uid).map {
            UserSubscription(
                id = it.uid,
                uid = it.uid,
                subscriptionName = it.subscriptionName,
                features = it.features,
            )
        }
    }
}