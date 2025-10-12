package com.github.drewlakee.mcp.demo.live.embabel.agents

import com.embabel.agent.api.annotation.AchievesGoal
import com.embabel.agent.api.annotation.Action
import com.embabel.agent.api.annotation.Agent
import com.embabel.agent.api.annotation.Planner
import com.embabel.agent.api.common.OperationContext
import com.embabel.agent.api.common.createObject
import com.embabel.agent.domain.io.UserInput
import com.embabel.common.ai.model.LlmOptions
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import com.github.drewlakee.mcp.demo.live.datasource.mongo.MongoSubscriptionsCatalogueDao
import com.github.drewlakee.mcp.demo.live.datasource.mongo.MongoUserSubscriptionsDao

data class Uid(
    @get:JsonPropertyDescription("Идентификатор пользователя, обычно означает uid или puid") val uid: Int?,
) {
    override fun toString() = buildString {
        if (uid != null) {
            append("uid=$uid")
        } else {
            append("Пользователь не был найден в сообщении")
        }
    }
}

data class TargetSubscription(
    @get:JsonPropertyDescription("Идентификатор подписки, о которой хотят понять причину недоступности пользователю. Обычно формат такой подписки похож на basic.subscription.tariff") val subscriptionName: String?,
) {
    override fun toString() = buildString {
        if (subscriptionName != null) {
            append(subscriptionName)
        }
    }
}

data class UserActiveSubscriptions(
    var subscriptions: List<ActiveSubscription>,
) {
    data class ActiveSubscription(
        val subscriptionName: String,
        val features: Set<String>,
    )

    override fun toString() = if (subscriptions.isEmpty()) {
        "Пользователь не имеет активных подписок, скорее всего его нет в нашей базе"
    } else {
        subscriptions.joinToString(separator = ", ", prefix = "[", postfix = "]") {
            buildString {
                append("Подписка: ${it.subscriptionName}, ")
                append("Фичи: ${it.features}")
            }
        }
    }
}

data class CatalogueTargetSubscription(
    val subscriptionName: String?,
    val features: Set<String>?,
    val isInDatabaseExisting: Boolean = false,
    val isTargetSubscriptionNameMentioned: Boolean,
) {
    override fun toString() = buildString {
        if (isInDatabaseExisting) {
            append("Подписка: $subscriptionName, ")
            append("Фичи: $features")
        } else {
            append("Подписка в каталоге $subscriptionName не существует")
        }
    }
}

data class SubscriptionUnavailabilityExplain(
    @get:JsonPropertyDescription("Объяснение причины недоступности подписки") val explanation: String,
) {
    override fun toString() = explanation
}

// 2.5.2. Example: WriteAndReviewAgent
// https://docs.embabel.com/embabel-agent/guide/0.1.3-SNAPSHOT/index.html#example-writeandreviewagent
@Agent(
    name = "Subscriptions Agent",
    description = """
       Агент помогает разобрать случаи недоступности подписок на 
       пользователях по их активным подписочным состояниям
    """,
    // Планировщик не делает вызовы к LLM, путь достижения цели заппрограммирован
    // алгоритмом для искуственного интеллекта. Подобный алгоритм
    // используется в разных играх, например, написанных на Unity
    // для различных мобов/противников в окружении с открытым миром.
    planner = Planner.GOAP,
)
class SubscriptionsAgent {

    private val activeSubscriptionsDao: MongoUserSubscriptionsDao = MongoUserSubscriptionsDao()
    private val catalogueSubscriptionsDao: MongoSubscriptionsCatalogueDao = MongoSubscriptionsCatalogueDao()

    @Action(description = "Вычленить из сообщения информацию об пользователе")
    fun extractUid(userInput: UserInput, context: OperationContext): Uid =
        context
            .ai()
            .withDefaultLlm()
            .createObject<Uid>(
                """
                    На основе сообщения вычлени информацию о пользователе для которого нужно выяснить недоступность подписки:
                    ${userInput.content}
                """.trimIndent(),
            )

    @Action(description = "Получить по пользователю его активные подписки")
    fun getActiveSubscriptions(uid: Uid): UserActiveSubscriptions = uid.uid?.let {
        activeSubscriptionsDao.getByUid(it).let {
            UserActiveSubscriptions(
                subscriptions = it.map {
                    UserActiveSubscriptions.ActiveSubscription(
                        subscriptionName = it.subscriptionName,
                        features = it.features,
                    )
                }
            )
        }
    } ?: UserActiveSubscriptions(listOf())

    @Action(description = "Вычленить из сообщения информацию об подписке на которую пользователь не может перейти")
    fun getTargetSubscription(userInput: UserInput, context: OperationContext): CatalogueTargetSubscription =
        context
            .ai()
            .withDefaultLlm()
            .createObject<TargetSubscription>(
                """
                    Вычлени из сообщения название подписки, по которой хотят получить объяснения о ее недоступности пользователю:
                    ${userInput.content}
                """.trimIndent()
            ).let {
               it.subscriptionName?.let { targetSubscription ->
                   catalogueSubscriptionsDao.getSubscriptionByName(targetSubscription)?.let {
                       CatalogueTargetSubscription(
                           subscriptionName = it.subscriptionName,
                           features = it.features,
                           isInDatabaseExisting = true,
                           isTargetSubscriptionNameMentioned = true,
                       )
                   } ?: CatalogueTargetSubscription(
                       subscriptionName = targetSubscription,
                       features = null,
                       isInDatabaseExisting = false,
                       isTargetSubscriptionNameMentioned = true,
                   )
               } ?: CatalogueTargetSubscription(
                   subscriptionName = null,
                   features = null,
                   isInDatabaseExisting = false,
                   isTargetSubscriptionNameMentioned = false,
               )
            }

    @AchievesGoal(
        description = "Объясни на основе запроса от пользователя причину недоступности подписки",
    )
    @Action
    fun explainSubscriptionUnavailability(
        userInput: UserInput,
        uid: Uid,
        activeSubscriptions: UserActiveSubscriptions,
        targetSubscription: CatalogueTargetSubscription,
        context: OperationContext,
    ): SubscriptionUnavailabilityExplain =
        context
            .ai()
            .withLlm(
                LlmOptions
                    .withDefaultLlm()
                    .withTemperature(0.9)
            )
            .createObject<SubscriptionUnavailabilityExplain>(
                """
                    Ты - дежурный поддержки разработки очень крутой команды подписочной тарифной сетки.
                    Твоя задача по известным источникам данных объяснить причину недоступности той или иной подписки пользователю.
                    Отвечай максимально просто и понятно, так как в поддержку обращаются люди, которые ничего не знает про доменную область очень крутой команды подписочной тарифной сетки.
                    Ничего не придумывай, используй только ту информацию, которая тебе доступна.
                    
                    Правила перехода с одной подписки на другую обусловлены подписочными механиками - апгрейд и даунгрейд.
                    Двигаться по лестнице с апгрейдом можно только по принципу, если в новой подписке больше фичей, чем в текущей, либо в новой подписке есть фичи, которых нет в текущей.
                    Двигаться по лестнице с даунгрейдом можно только по принциу, если в новой подписке такое же количество фичей, но их меньше, чем в текущей.
                    Фича - это доступ к контенту, который дает сама подписка.    
                    
                    Не обращайся к пользователю напрямую, формулируй ответ обобщенно, как будто ты общаешься сам с собой.
                                        
                    Пользователь: $uid
                                        
                    Активные подписки этого пользователя: $activeSubscriptions
                    
                    ${if (targetSubscription.isTargetSubscriptionNameMentioned) "Пользователь хочет перейти на подписку: $targetSubscription" else ""}
                    
                    Постарайся помочь пользователю, объяснив причину недоступности подписки по его запросу: ${userInput.content}
                    
                    В случае если вопрос скорее всего не связан с предметной областью подписок, скажи, что ты не можешь помочь, либо направь по этому вопросу к дежурному разработчику.
                """.trimIndent()
            )
}
