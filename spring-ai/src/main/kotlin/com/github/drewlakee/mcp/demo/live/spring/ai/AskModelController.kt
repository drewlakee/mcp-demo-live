package com.github.drewlakee.mcp.demo.live.spring.ai

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.ai.openai.api.ResponseFormat
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class Query(val query: String)

data class Answer(val response: String)

@RestController
@RequestMapping("/subscriptions/ai")
class AskModelController(
    private val chatClient: ChatClient,
) {

    @PostMapping("/ask")
    fun askModel(@RequestBody query: Query): Answer {
        val responseFormat = ResponseFormat.builder()
            .type(ResponseFormat.Type.TEXT)
            .build()

        val chatOptions = OpenAiChatOptions.builder()
            .model("gpt://b1gioucfterb2rnsqb1q/gpt-oss-20b")
            .temperature(0.5)
            .responseFormat(responseFormat)
            .build()

        return Answer(
            response = chatClient.prompt(
                Prompt(

                    buildList {
                        add(SystemMessage(
                            """
        Ты - дежурный поддержки разработки очень крутой команды подписочной тарифной сетки.
        Твоя задача по известным источникам данных объяснить причину недоступности той или иной подписки пользователю.
        Отвечай максимально просто и понятно, так как в поддержку обращаются люди, которые ничего не знает про доменную область очень крутой команды подписочной тарифной сетки.
        Ничего не придумывай, используй только ту информацию, которая тебе доступна.
        """
                        ))

                        add(SystemMessage(
                            """
        Правила перехода с одной подписки на другую обусловлены подписочными механиками - апгрейд и даунгрейд.
        Двигаться по лестнице с апгрейдом можно только по принципу, если в новой подписке больше фичей, чем в текущей, либо в новой подписке есть фичи, которых нет в текущей.
        Двигаться по лестнице с даунгрейдом можно только по принциу, если в новой подписке такое же количество фичей, но их меньше, чем в текущей.
        Фича - это доступ к контенту, который дает сама подписка.
        Если у пользователя нет никаких подписок, а подписка ему недоступна, то попроси уточнить более коректные данные.
        """
                        ))

                        add(SystemMessage(
                            """
        В случае если вопрос скорее всего не связан с предметной областью подписок, скажи, что ты не можешь помочь, либо направь по этому вопросу к дежурному разработчику.
        """
                        ))
                    },

                    chatOptions
                )
            ).user(query.query).call().content() ?: "Не удалось получить ответ"
        )
    }
}