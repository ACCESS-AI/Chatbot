package llm.model

import llm.model.MistralAPILLM
import io.ktor.client.*
import io.ktor.http.*
import io.mockk.*
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.*
import kotlin.test.assertFailsWith

class MistralAPILLMTest {
    private val apiKey = "test_api_key"
    private val model = "test_model"
    private val hostUrl = "https://test.api.mistral.ai/v1/chat/completions"
    private val testLLMResponse = "Test LLM response"

    private lateinit var mockClient: HttpClient

    private fun mockClient(correctResponseString: Boolean = true) {
        val mockEngine = MockEngine { _ ->
            val responseContent = when {
                correctResponseString -> """{"choices":[{"message":{"content":"$testLLMResponse"}}]}"""
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
    fun testQueryReturnsExpectedResponse() = runBlocking {
        mockClient(true)

        val llm = MistralAPILLM(mockClient, apiKey, model, hostUrl)

        val result = llm.query("test input")

        assertEquals(testLLMResponse, result)
    }

    @Test
    fun testQueryThrowsException(): Unit = runBlocking {
        mockClient(false)

        val llm = MistralAPILLM(mockClient, apiKey, model, hostUrl)

        assertFailsWith<Exception> {
            llm.query("test input")
        }
    }

    @Test
    fun testCloseClient() = runBlocking {
        mockClient(true)

        val llm = MistralAPILLM(mockClient, apiKey, model, hostUrl)

        llm.query("test input")

        verify { mockClient.close() }
    }

    @Test
    fun testQueryWithEmptyInputReturnsExpectedResponse() = runBlocking {
        mockClient(true)

        val llm = MistralAPILLM(mockClient, apiKey, model, hostUrl)

        val result = llm.query("")

        assertEquals(testLLMResponse, result)
    }

    @Test
    fun testQueryWithEmptyInputThrowsException() = runBlocking {
        mockClient(false)

        val llm = MistralAPILLM(mockClient, apiKey, model, hostUrl)

        assertFailsWith<Exception> {
            llm.query("")
        }
    }
}