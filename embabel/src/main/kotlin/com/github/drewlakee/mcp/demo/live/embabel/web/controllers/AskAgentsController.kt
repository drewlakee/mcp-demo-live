package com.github.drewlakee.mcp.demo.live.embabel.web.controllers

import com.embabel.agent.api.common.autonomy.AgentInvocation
import com.embabel.agent.core.AgentPlatform
import com.embabel.agent.domain.io.UserInput
import com.github.drewlakee.mcp.demo.live.embabel.agents.SubscriptionUnavailabilityExplain
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class Query(val query: String)

data class Answer(val response: String)

@RestController
@RequestMapping("/subscriptions/agents")
class AskAgentsController(
    private val agentPlatform: AgentPlatform,
) {

    // Real-World Web Application Example
    // https://docs.embabel.com/embabel-agent/guide/0.1.3-SNAPSHOT/index.html#real-world-web-application-example
    @PostMapping("/ask")
    fun askAgents(@RequestBody query: Query): Answer {
        val invocation: AgentInvocation<SubscriptionUnavailabilityExplain> =
            AgentInvocation.builder(agentPlatform)
                .options { options ->
                    options.verbosity { verbosity ->
                        verbosity
                            .showPlanning(true)
                            .showLlmResponses(true)
                            .showPrompts(true)
                            .debug(false)
                    }
                }
                .build(SubscriptionUnavailabilityExplain::class.java)

        val subscriptionUnavailabilityExplanation = invocation.invoke(
            map = mapOf(
                "userInput" to UserInput(query.query)
            )
        )
        return Answer(response = subscriptionUnavailabilityExplanation.toString())
    }
}