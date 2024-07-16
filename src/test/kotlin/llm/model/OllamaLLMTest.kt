package llm.model

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.mockk.*
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class OllamaLLMTest {
    private val model = "test_model"
    private val hostUrl = "http://localhost:11434/api/generate"
    private val testLLMResponse = "Test LLM response"

    private lateinit var mockClient: HttpClient

    private fun mockClient(correctResponseString: Boolean = true) {
        val mockEngine = MockEngine { _ ->
            val responseContent = when {
                correctResponseString -> """{"response":"$testLLMResponse"}"""
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
    fun testQueryReturnsExpectedResponse(): Unit = runBlocking {
        mockClient(true)

        val llm = OllamaLLM(mockClient, model, hostUrl)

        val result = llm.query("test input")

        assertEquals(testLLMResponse, result)
    }

    @Test
    fun testQueryThrowsException(): Unit = runBlocking {
        mockClient(false)

        val llm = OllamaLLM(mockClient, model, hostUrl)

        assertFailsWith<Exception> {
            llm.query("test input")
        }
    }

    @Test
    fun testCloseClient(): Unit = runBlocking {
        mockClient(true)

        val llm = OllamaLLM(mockClient, model, hostUrl)

        llm.query("test input")

        verify { mockClient.close() }
    }

    @Test
    fun testQueryWithEmptyInput(): Unit = runBlocking {
        mockClient(true)

        val llm = OllamaLLM(mockClient, model, hostUrl)

        val result = llm.query("")

        assertEquals(testLLMResponse, result)
    }

    @Test
    fun testQueryWithEmptyInputThrowsException(): Unit = runBlocking {
        mockClient(false)

        val llm = OllamaLLM(mockClient, model, hostUrl)

        assertFailsWith<Exception> {
            llm.query("")
        }
    }
}