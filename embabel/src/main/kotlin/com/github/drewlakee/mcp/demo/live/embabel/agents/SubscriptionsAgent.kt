package com.github.drewlakee.mcp.demo.live.embabel.agents

import com.embabel.agent.api.annotation.Agent
import com.embabel.agent.api.annotation.Planner

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
    // используется в разных играх, например, на написанных на Unity
    // для различных мобов/противников в окружении с открытым миром.
    planner = Planner.GOAP,
)
class SubscriptionsAgent {
}