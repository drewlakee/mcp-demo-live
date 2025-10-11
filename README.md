# Презентация [demo-live.ipynb](demo-live.ipynb)

## demo-commons

- пример реализации tool calling и взаимодействия с LLM
- общие модели
- доступ к mongodb
- кастомный клиент к Yandex Cloud

```kotlin
// Используемые перменные окружения
MONGO_CONNECTION_STRING // пример mongodb://localhost:27017/test
MONGO_DATABASE // пример test
YANDEX_CLOUD_API_KEY // см. документацию yandex cloud Api-Key для service-account 
```

## spring-ai

- пример реализации mcp-сервера, mcp-клиента
- spring ai + webmvc

```kotlin
// Используемые перменные окружения
OPEN_AI_API_KEY // см. документацию yandex cloud Api-Key для service-account 
```

## embabel-agent поверх spring-ai

- пример реализации агентов с алгоритмом Goal-Oriented Action Planning

```kotlin
// Используемые перменные окружения
OPEN_AI_API_KEY // см. документацию yandex cloud Api-Key для service-account 
```