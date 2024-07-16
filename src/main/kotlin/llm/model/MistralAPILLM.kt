package llm.model

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
import llm.ILlm

class MistralAPILLM(
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
    private val apiKey: String = System.getenv("MISTRAL_API_KEY"),
    private val model: String = System.getenv("MISTRAL_LLM_MODEL"),
    private val hostUrl: String = System.getenv("MISTRAL_LLM_HOST")
) : ILlm {

    override suspend fun query(prompt: String): String {

        val payload = buildJsonObject {
            put("model", model)
            putJsonArray("messages") {
                add(buildJsonObject {
                    put("role", "user")
                    put("content", prompt)
                })
            }
        }

        try {
            val response: HttpResponse = client.post(hostUrl) {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $apiKey")
                setBody(payload)
            }

            val jsonResponse = Json.decodeFromString(JsonObject.serializer(), response.bodyAsText())

            return jsonResponse["choices"]!!.jsonArray[0].jsonObject["message"]!!.jsonObject["content"]!!.jsonPrimitive.content

        } catch (e: Exception) {
            throw e
        } finally {
            client.close()
        }
    }
}