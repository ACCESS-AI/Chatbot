package embedder

import dev.langchain4j.data.embedding.Embedding
import dev.langchain4j.model.ollama.OllamaEmbeddingModel
import dev.langchain4j.model.output.Response
import embedder.model.OllamaEmbedder
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertThrows
import org.junit.jupiter.api.assertThrows
import kotlin.test.BeforeTest
import kotlin.test.Test

class OllamaEmbedderTest {
    private var mockEmbeddingModel: OllamaEmbeddingModel = mockk<OllamaEmbeddingModel>()
    private val mockEmbeddingResult = mockk<Response<Embedding>>(relaxed = true)
    private val mockContentResult = mockk<Embedding>(relaxed = true)

    private lateinit var ollamaEmbedder: IEmbedder

    @BeforeTest
    fun startup() {
        val builder = mockk<OllamaEmbeddingModel.OllamaEmbeddingModelBuilder>()
        every { builder.baseUrl(any()) } returns builder
        every { builder.modelName(any()) } returns builder
        every { builder.build() } returns mockEmbeddingModel

        mockkStatic(OllamaEmbeddingModel::class)
        every { OllamaEmbeddingModel.builder() } returns builder

        ollamaEmbedder = OllamaEmbedder()
    }

    @Test
    fun testEmbed() {
        val testInput = "test input"
        val expectedOutput = listOf(1.0f, 2.0f, 3.0f)

        coEvery { mockEmbeddingModel.embed(testInput) } returns mockEmbeddingResult
        every { mockEmbeddingResult.content() } returns mockContentResult
        every { mockContentResult.vector() } returns expectedOutput.toFloatArray()

        runBlocking {
            val result = ollamaEmbedder.embed(testInput)

            assert(result == expectedOutput)
            coVerify { mockEmbeddingModel.embed(testInput) }
        }
    }

    @Test
    fun testEmbedEmptyInput() {
        val testInput = ""
        val expectedOutput = listOf<Float>()

        coEvery { mockEmbeddingModel.embed(testInput) } returns mockEmbeddingResult
        every { mockEmbeddingResult.content() } returns mockContentResult
        every { mockContentResult.vector() } returns expectedOutput.toFloatArray()

        runBlocking {
            val result = ollamaEmbedder.embed(testInput)

            assert(result == expectedOutput)
            coVerify { mockEmbeddingModel.embed(testInput) }
        }
    }

    @Test
    fun testEmbedThrowsException() {
        val testInput = "test input"

        coEvery { mockEmbeddingModel.embed(testInput) } throws Exception()

        assertThrows<Exception> {
            runBlocking {
                ollamaEmbedder.embed(testInput)
            }
        }
        coVerify { mockEmbeddingModel.embed(testInput) }
    }

    @Test
    fun testEmbedEmptyInputThrowsException() {
        val testInput = ""

        coEvery { mockEmbeddingModel.embed(testInput) } throws Exception()

        assertThrows<Exception> {
            runBlocking {
                ollamaEmbedder.embed(testInput)
            }
        }
        coVerify { mockEmbeddingModel.embed(testInput) }
    }
}