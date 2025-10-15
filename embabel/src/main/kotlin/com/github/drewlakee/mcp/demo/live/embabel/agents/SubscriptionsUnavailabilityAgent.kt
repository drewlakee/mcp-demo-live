package com.github.drewlakee.mcp.demo.live.embabel.agents

import com.embabel.agent.api.annotation.AchievesGoal
import com.embabel.agent.api.annotation.Action
import com.embabel.agent.api.annotation.Agent
import com.embabel.agent.api.annotation.Planner
import com.embabel.agent.api.common.OperationContext
import com.embabel.agent.api.common.SomeOf
import com.embabel.agent.api.common.createObject
import com.embabel.agent.domain.io.UserInput
import com.embabel.common.ai.model.LlmOptions
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import com.github.drewlakee.mcp.demo.live.datasource.mongo.MongoSubscriptionsCatalogueDao
import com.github.drewlakee.mcp.demo.live.datasource.mongo.MongoUserSubscriptionsDao

data class SomeOfUserMessagesExtract(
    val provided: UidProvided? = null,
    val notProvided: UidNotProvided? = null,
    val mentionedSubscriptions: MentionedSubscriptions? = null,
) : SomeOf

data class UserMessagesExtract(
    @get:JsonPropertyDescription("Идентификаторы подписок, которые указаны. Обычно формат подписок похож на basic.subscription.tariff") val subscriptionNames: List<String>?,
    @get:JsonPropertyDescription("Идентификатор пользователя, недоступность подписки, по которому хотят получить причину. Обычно означает uid или puid") val uid: Int?,
)

data class UidProvided(val uid: Int) {
    override fun toString() = "uid $uid"
}

object UidNotProvided {
    override fun toString() = "Пользователь не был найден в сообщении"
}

data class MentionedSubscriptions(val subscriptions: List<MentionedSubscription>)

data class MentionedSubscription(val subscriptionName: String) {
    override fun toString() = subscriptionName
}

data class UserActiveSubscriptions(
    var subscriptions: List<ActiveSubscription>,
) {
    data class ActiveSubscription(
        val subscriptionName: String,
        val features: Set<String>,
    )

    override fun toString() = subscriptions.joinToString(separator = ", ", prefix = "[", postfix = "]") {
        buildString {
            append("Подписка: ${it.subscriptionName}, ")
            append("Фичи: ${it.features}")
        }
    }
}

data class CatalogueSubscriptions(
    val catalogueSubscriptions: List<CatalogueSubscription>,
) {
    override fun toString() = catalogueSubscriptions.joinToString(separator = ", ", prefix = "[", postfix = "]") { it.toString() }
}

data class CatalogueSubscription(
    val subscriptionName: String?,
    val features: Set<String>? = setOf(),
    val isInDatabaseExisting: Boolean = false,
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

    @Action(description = "Вычленить из сообщения информацию для объяснения недоступности подписки")
    fun userMessagesExtract(userInput: UserInput, context: OperationContext): SomeOfUserMessagesExtract {
        val extract = context
            .ai()
            .withDefaultLlm()
            .createObject<UserMessagesExtract>(
                """
                    На основе сообщения вычлени информацию о пользователе, для которого нужно выяснить недоступность подписки, а также
                    вычлени из сообщения название подписки, по которой хотят получить объяснения о ее недоступности пользователю.
                    
                    ${userInput.content}
                """.trimIndent(),
            )

        return SomeOfUserMessagesExtract(
            provided = extract.uid?.let { UidProvided(extract.uid) },
            notProvided = if (extract.uid == null) UidNotProvided else null,
            mentionedSubscriptions = if (extract.subscriptionNames != null) {
                MentionedSubscriptions(extract.subscriptionNames.map(::MentionedSubscription))
            } else null
        )
    }

    @Action(description = "Получить по пользователю его активные подписки")
    fun getActiveSubscriptions(uid: UidProvided): UserActiveSubscriptions = UserActiveSubscriptions(
        subscriptions = activeSubscriptionsDao.getByUid(uid.uid).map {
            UserActiveSubscriptions.ActiveSubscription(
                subscriptionName = it.subscriptionName,
                features = it.features,
            )
        }
    )

    @Action(description = "Получить информацию об подписках из каталога, которые были упомянуты в сообщении пользователя")
    fun getMentionedSubscriptionsFromCatalogue(mentionedSubscription: MentionedSubscriptions): CatalogueSubscriptions {
        val catalogueMentionedSubscriptions = mentionedSubscription.subscriptions.map {
            catalogueSubscriptionsDao.getSubscriptionByName(it.subscriptionName)?.let {
                CatalogueSubscription(
                    subscriptionName = it.subscriptionName,
                    features = it.features,
                    isInDatabaseExisting = true,
                )
            } ?: CatalogueSubscription(subscriptionName = it.subscriptionName, isInDatabaseExisting = false)
        }

        return CatalogueSubscriptions(catalogueMentionedSubscriptions)
    }

    @AchievesGoal(description = "Объясни, что недостаточное кол-во информации было предоставлено для поиска причины недоступности")
    fun complainAboutMissingDetailsAboutUser(notProvided: UidNotProvided): SubscriptionUnavailabilityExplain =
        SubscriptionUnavailabilityExplain(explanation = "Без пользователя тяжело определить причину недоступности подписки. Укажите пользователя")

    private val commonSystemPromt = """
        Ты - дежурный поддержки разработки очень крутой команды подписочной тарифной сетки.
        Твоя задача по известным источникам данных объяснить причину недоступности той или иной подписки пользователю.
        Отвечай максимально просто и понятно, так как в поддержку обращаются люди, которые ничего не знает про доменную область очень крутой команды подписочной тарифной сетки.
        Ничего не придумывай, используй только ту информацию, которая тебе доступна.
        
        Правила перехода с одной подписки на другую обусловлены подписочными механиками - апгрейд и даунгрейд.
        Двигаться по лестнице с апгрейдом можно только по принципу, если в новой подписке больше фичей, чем в текущей, либо в новой подписке есть фичи, которых нет в текущей.
        Двигаться по лестнице с даунгрейдом можно только по принциу, если в новой подписке такое же количество фичей, но их меньше, чем в текущей.
        Фича - это доступ к контенту, который дает сама подписка.    
        
        Не обращайся к пользователю напрямую, формулируй ответ обобщенно, как будто ты общаешься сам с собой.
        
        В случае если вопрос скорее всего не связан с предметной областью подписок, скажи, что ты не можешь помочь, либо направь по этому вопросу к дежурному разработчику.
    """.trimIndent()

    @AchievesGoal(description = "Объясни на основе запроса от пользователя причину недоступности подписки")
    @Action(value = 0.4, cost = 1.0)
    fun explainSubscriptionUnavailability0(
        userInput: UserInput,
        uid: UidProvided,
        activeSubscriptions: UserActiveSubscriptions,
        context: OperationContext,
    ): SubscriptionUnavailabilityExplain =
        context
            .ai()
            .withLlm(
                LlmOptions
                    .withDefaultLlm()
                    .withTemperature(0.9)
            )
            .createObject(
                """
                    $commonSystemPromt
                                        
                    Пользователь: $uid   
                    Активные подписки этого пользователя: $activeSubscriptions  
                    Постарайся помочь пользователю, объяснив причину недоступности подписки именно по апгрейду. 
                    Если у тебя нет достаточной информации, уточни идентификаторы конкретных подписок в каталоге. 
                    
                    Сообщение пользователя: ${userInput.content}
                """.trimIndent()
            )

    @AchievesGoal(description = "Объясни на основе запроса от пользователя причину недоступности подписки")
    @Action(value = 1.0, cost = 0.5)
    fun explainSubscriptionUnavailability1(
        userInput: UserInput,
        uid: UidProvided,
        activeSubscriptions: UserActiveSubscriptions,
        catalogueMentionedSubscriptions: CatalogueSubscriptions,
        context: OperationContext,
    ): SubscriptionUnavailabilityExplain =
        context
            .ai()
            .withLlm(
                LlmOptions
                    .withDefaultLlm()
                    .withTemperature(0.9)
            )
            .createObject(
                """
                    $commonSystemPromt
                                        
                    Пользователь: $uid     
                    Активные подписки этого пользователя: $activeSubscriptions
                    Информация о подписках, которые упоминались пользователем: $catalogueMentionedSubscriptions
                    Постарайся помочь пользователю, объяснив причину недоступности подписки именно по апгрейду.
                    
                    Сообщение пользователя: ${userInput.content}
                """.trimIndent()
            )
}
