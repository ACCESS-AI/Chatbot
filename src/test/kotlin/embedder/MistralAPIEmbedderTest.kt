package embedder

import embedder.model.MistralAPIEmbedder
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.mockk.*
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.*
import kotlinx.serialization.json.*
import kotlin.test.assertFailsWith

class MistralAPIEmbedderTest {
    private val apiKey = "test_api_key"
    private val model = "test_model"
    private val hostUrl = "https://test.api.mistral.ai/v1/embeddings"
    private val testEmbeddings = listOf(0.1f, 0.2f, 0.3f)

    private lateinit var mockClient: HttpClient

    private fun mockClient(correctResponseString: Boolean = true) {
        val mockEngine = MockEngine { req ->
            val requestBodyAsString = req.body.toByteArray().decodeToString()
            val jsonElement = Json.parseToJsonElement(requestBodyAsString)
            val inputKeyValue = jsonElement.jsonObject["input"]?.jsonPrimitive?.contentOrNull
            val isEmptyRequestBody = inputKeyValue == "[]"
            val responseContent = when {
                correctResponseString -> when {
                    isEmptyRequestBody -> """{"data":[{"embedding":[]}]}"""
                    else -> """{"data":[{"embedding":${testEmbeddings}}]}"""
                }
                else -> """{"error":"test error"}"""
            }
            respond(
                content = responseContent,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        mockClient = spyk(HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json()
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 30000
                connectTimeoutMillis = 15000
                socketTimeoutMillis = 30000
            }
        })
    }

    @Test
    fun testEmbedReturnsExpectedResponse(): Unit = runBlocking {
        mockClient(true)

        val embedder = MistralAPIEmbedder(mockClient, apiKey, model, hostUrl)

        val result = embedder.embed("test input")

        assertEquals(testEmbeddings, result)
    }

    @Test
    fun testEmbedThrowsException(): Unit = runBlocking {
        mockClient(false)

        val embedder = MistralAPIEmbedder(mockClient, apiKey, model, hostUrl)

        assertFailsWith<Exception> {
            embedder.embed("test input")
        }
    }

    @Test
    fun testCloseClient(): Unit = runBlocking {
        mockClient(true)

        val embedder = MistralAPIEmbedder(mockClient, apiKey, model, hostUrl)

        embedder.embed("test input")

        verify { mockClient.close() }
    }

    @Test
    fun testEmbedWithEmptyInputReturnsExpectedResponse(): Unit = runBlocking {
        mockClient(true)

        val embedder = MistralAPIEmbedder(mockClient, apiKey, model, hostUrl)

        val result = embedder.embed("")

        assertEquals(listOf<Float>(), result)
    }

    @Test
    fun testEmbedWithEmptyInputThrowsException(): Unit = runBlocking {
        mockClient(false)

        val embedder = MistralAPIEmbedder(mockClient, apiKey, model, hostUrl)

        assertFailsWith<Exception> {
            embedder.embed("")
        }
    }
}