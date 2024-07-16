package embedder.model

import embedder.IEmbedder
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.kotlinx.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.HttpHeaders.ContentEncoding
import io.ktor.serialization.*
import kotlinx.serialization.json.*
import io.ktor.serialization.kotlinx.json.*

class MistralAPIEmbedder(
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
    private val model: String = System.getenv("MISTRAL_EMBEDDING_MODEL"),
    private val hostUrl: String = System.getenv("MISTRAL_EMBEDDING_HOST")
) : IEmbedder {

    override suspend fun embed(input: String): List<Float> {
        val payload = buildJsonObject {
            put("model", model)
            put("encoding_format", "float")
            put("input", "[$input]")
        }

        try {
            val response: HttpResponse = client.post(hostUrl) {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $apiKey")
                setBody(payload)
            }

            val jsonResponse = Json.decodeFromString(JsonObject.serializer(), response.bodyAsText())
            val embeddingStrings = jsonResponse["data"]!!.jsonArray[0].jsonObject["embedding"]!!.jsonArray.toList()
            return embeddingStrings.map { it.jsonPrimitive.float }
        } catch (e: Exception) {
            println("Error in Mistral Embedder: ${e.message}")
            throw e
        } finally {
            client.close()
        }
    }
}