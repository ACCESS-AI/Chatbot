package llm.model

import llm.ILlm
import io.ktor.client.HttpClient
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

class OllamaLLM(
    private val client: HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30000
            connectTimeoutMillis = 15000
            socketTimeoutMillis = 30000
        }
    },
    private val model: String = System.getenv("OLLAMA_LLM_MODEL"),
    private val hostUrl: String = System.getenv("OLLAMA_LLM_HOST")
) : ILlm {

    override suspend fun query(prompt: String): String {
        val payload = buildJsonObject {
            put("model", model)
            put("stream", false)
            put("json", true)
            put("prompt", prompt)
        }

        try {
            val response: HttpResponse = client.post(hostUrl) {
                contentType(ContentType.Application.Json)
                setBody(payload)
            }

            val jsonResponse = Json.decodeFromString(JsonObject.serializer(), response.bodyAsText())

            return jsonResponse["response"]!!.jsonPrimitive.content
        } catch (e: Exception) {
            println("Failed to query Ollama: $e.message")
            throw e
        } finally {
            client.close()
        }
    }
}