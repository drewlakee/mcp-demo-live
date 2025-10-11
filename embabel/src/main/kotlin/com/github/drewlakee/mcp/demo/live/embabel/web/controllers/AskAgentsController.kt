package com.github.drewlakee.mcp.demo.live.embabel.web.controllers

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class Query(val query: String)

data class Answer(val response: String)

@RestController
@RequestMapping("/subscriptions/agents")
class AskAgentsController {

    @PostMapping("/ask")
    fun askAgents(@RequestBody query: Query): Answer {
        return Answer(
            response = ""
        )
    }
}