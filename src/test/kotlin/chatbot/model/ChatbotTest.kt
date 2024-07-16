package chatbot.model

import chat.IChatRepository
import chat.model.Chat
import chat.model.ChatEntry
import chat.model.DefaultChatRepository
import db.IDb
import embedder.IEmbedder
import io.mockk.*
import kotlinx.coroutines.runBlocking
import llm.ILlm
import org.junit.Assert.assertNotNull
import prompt.IPrompt
import vectorStore.IVectorStore
import vectorStore.model.Context
import java.time.LocalDateTime
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ChatbotTest {

    private val userId = "testUserId"
    private val courseSlug = "testCourseSlug"
    private val courseSlugHash = "testCourseSlugHash"
    private val assignmentId = "testAssignmentId"
    private val taskId = "testTaskId"

    private val taskInstructions = "testTaskInstructions"
    private val submissions = listOf("testSubmission1", "testSubmission2")
    private val userPrompt = "testUserPrompt"

    private lateinit var mockEmbedder: IEmbedder
    private lateinit var mockVectorStore: IVectorStore
    private lateinit var mockPromptModel: IPrompt
    private lateinit var mockLlm: ILlm
    private lateinit var mockDb: IDb
    private lateinit var mockChatRepository: IChatRepository

    @BeforeTest
    fun setup() {
        mockkStatic(Chatbot::class)

        mockEmbedder = mockk()
        coEvery { mockEmbedder.embed(any()) } returns listOf(1.0F, 2.0F, 3.0F, 4.0F, 5.0F, 6.0F, 7.0F, 8.0F)

        mockVectorStore = mockk()
        coEvery { mockVectorStore.getContext(any(), any()) } returns Context()

        mockPromptModel = mockk()
        every { mockPromptModel.createPrompt(any(), any(), any(), any(), any()) } returns "A test meta prompt."

        mockLlm = mockk()
        coEvery { mockLlm.query(any()) } returns "A test LLM response."

        mockDb = mockk()
        every { mockDb.getEntityManager() } returns mockk()

        mockChatRepository = spyk(DefaultChatRepository(mockDb))
        every { mockChatRepository.persist(any()) } just Runs
        every { mockChatRepository.closeDbConnection() } just Runs
    }

    private fun mockChatRepositoryGet(returnChat: Boolean, withChatHistory: Boolean = true): Chat? {
        val mockChat: Chat? = if (returnChat) {
            mockk<Chat>().apply {
                every { getCourseSlugHash() } returns "testCourseSlugHash"
                every { getChatEntries() } returns (if(withChatHistory) listOf(ChatEntry(), ChatEntry(123, userPrompt, "testLLMOutput", this, LocalDateTime.now(), LocalDateTime.now().minusMinutes(2), setOf(), "Test final meta prompt")) else listOf())
                every { addChatEntry(any()) } just Runs
            }
        } else {
            null
        }
        every { mockChatRepository.get(userId, courseSlug, courseSlugHash, assignmentId, taskId) } returns mockChat

        return mockChat
    }

    @Test
    fun testNewChatbotInstanceWithExistingChat() {
        mockChatRepositoryGet(true)

        val chatbot = Chatbot(
            userId, courseSlug, courseSlugHash, assignmentId, taskId,
            mockEmbedder, mockVectorStore, mockPromptModel, mockLlm, mockChatRepository
        )

        assertNotNull(chatbot)
        verify { mockChatRepository.get(userId, courseSlug, courseSlugHash, assignmentId, taskId) }
    }

    @Test
    fun testNewChatbotInstanceWithNewChat() {
        mockChatRepositoryGet(false)

        val chatbot = Chatbot(
            userId, courseSlug, courseSlugHash, assignmentId, taskId,
            mockEmbedder, mockVectorStore, mockPromptModel, mockLlm, mockChatRepository
        )

        assertNotNull(chatbot)
        verify { mockChatRepository.get(userId, courseSlug, courseSlugHash, assignmentId, taskId) }
        assertNull(mockChatRepository.get(userId, courseSlug, courseSlugHash, assignmentId, taskId) )
    }

    @Test
    fun testRunWithNewChat() {
        mockChatRepositoryGet(false)

        val chatbot = Chatbot(
            userId, courseSlug, courseSlugHash, assignmentId, taskId,
            mockEmbedder, mockVectorStore, mockPromptModel, mockLlm, mockChatRepository
        )

        runBlocking {
            val chatbotResponse = chatbot.run(taskInstructions, submissions, userPrompt)

            assertNotNull(chatbotResponse)
            coVerify { mockEmbedder.embed(any()) }
            coVerify { mockVectorStore.getContext(any(), any()) }
            coVerify { mockPromptModel.createPrompt(any(), any(), any(), any(), any()) }
            coVerify { mockLlm.query(any()) }
            verify { mockChatRepository.persist(any()) }
        }
    }

    @Test
    fun testRunWithExistingChat() {
        mockChatRepositoryGet(true)

        val chatbot = Chatbot(
            userId, courseSlug, courseSlugHash, assignmentId, taskId,
            mockEmbedder, mockVectorStore, mockPromptModel, mockLlm, mockChatRepository
        )

        runBlocking {
            val chatbotResponse = chatbot.run(taskInstructions, submissions, userPrompt)

            assertNotNull(chatbotResponse)
            coVerify { mockEmbedder.embed(any()) }
            coVerify { mockVectorStore.getContext(any(), any()) }
            coVerify { mockPromptModel.createPrompt(any(), any(), any(), any(), any()) }
            coVerify { mockLlm.query(any()) }
            verify { mockChatRepository.persist(any()) }
        }
    }

    @Test
    fun testRunWithExceptionInEmbed() {
        mockChatRepositoryGet(false)

        val chatbot = Chatbot(
            userId, courseSlug, courseSlugHash, assignmentId, taskId,
            mockEmbedder, mockVectorStore, mockPromptModel, mockLlm, mockChatRepository
        )

        runBlocking {
            coEvery { mockEmbedder.embed(any()) } throws Exception()

            val chatbotResponse = chatbot.run(taskInstructions, submissions, userPrompt)

            assertNotNull(chatbotResponse)
            coVerify { mockEmbedder.embed(any()) }
        }
    }

    @Test
    fun testRunWithExceptionInGetContext() {
        mockChatRepositoryGet(false)

        val chatbot = Chatbot(
            userId, courseSlug, courseSlugHash, assignmentId, taskId,
            mockEmbedder, mockVectorStore, mockPromptModel, mockLlm, mockChatRepository
        )

        runBlocking {
            coEvery { mockVectorStore.getContext(any(), any()) } throws Exception()

            val chatbotResponse = chatbot.run(taskInstructions, submissions, userPrompt)

            assertNotNull(chatbotResponse)
            coVerify { mockVectorStore.getContext(any(), any()) }
        }
    }

    @Test
    fun testRunWithExceptionInCreatePrompt() {
        mockChatRepositoryGet(false)

        val chatbot = Chatbot(
            userId, courseSlug, courseSlugHash, assignmentId, taskId,
            mockEmbedder, mockVectorStore, mockPromptModel, mockLlm, mockChatRepository
        )

        runBlocking {
            every { mockPromptModel.createPrompt(any(), any(), any(), any(), any()) } throws Exception()

            val chatbotResponse = chatbot.run(taskInstructions, submissions, userPrompt)

            assertNotNull(chatbotResponse)
            verify { mockPromptModel.createPrompt(any(), any(), any(), any(), any()) }
        }
    }

    @Test
    fun testRunWithExceptionInLlmQuery() {
        mockChatRepositoryGet(false)

        val chatbot = Chatbot(
            userId, courseSlug, courseSlugHash, assignmentId, taskId,
            mockEmbedder, mockVectorStore, mockPromptModel, mockLlm, mockChatRepository
        )

        runBlocking {
            coEvery { mockLlm.query(any()) } throws Exception()

            val chatbotResponse = chatbot.run(taskInstructions, submissions, userPrompt)

            assertNotNull(chatbotResponse)
            coVerify { mockLlm.query(any()) }
        }
    }

    @Test
    fun testRunWithExceptionInChatRepositoryPersist() {
        mockChatRepositoryGet(false)

        val chatbot = Chatbot(
            userId, courseSlug, courseSlugHash, assignmentId, taskId,
            mockEmbedder, mockVectorStore, mockPromptModel, mockLlm, mockChatRepository
        )

        runBlocking {
            every { mockChatRepository.persist(any()) } throws Exception()

            val chatbotResponse = chatbot.run(taskInstructions, submissions, userPrompt)

            assertNotNull(chatbotResponse)
            verify { mockChatRepository.persist(any()) }
        }
    }

    @Test
    fun testRunWithEmptySubmissions() {
        mockChatRepositoryGet(false)

        val chatbot = Chatbot(
            userId, courseSlug, courseSlugHash, assignmentId, taskId,
            mockEmbedder, mockVectorStore, mockPromptModel, mockLlm, mockChatRepository
        )

        val chatHistory = chatbot.getHistory()

        assertNotNull(chatHistory)
        assertTrue { chatHistory.isEmpty() }
    }

    @Test
    fun testGetHistory() {
        val mockChat: Chat? = mockChatRepositoryGet(true)

        val chatbot = Chatbot(
            userId, courseSlug, courseSlugHash, assignmentId, taskId,
            mockEmbedder, mockVectorStore, mockPromptModel, mockLlm, mockChatRepository
        )

        val chatHistory = chatbot.getHistory()

        assertNotNull(chatHistory)
        assertTrue { chatHistory.isNotEmpty() }
        verify { mockChat?.getChatEntries() }
    }

    @Test
    fun testGetHistoryWithEmptyChat() {
        val mockChat: Chat? = mockChatRepositoryGet(true, false)

        val chatbot = Chatbot(
            userId, courseSlug, courseSlugHash, assignmentId, taskId,
            mockEmbedder, mockVectorStore, mockPromptModel, mockLlm, mockChatRepository
        )

        val chatHistory = chatbot.getHistory()

        assertNotNull(chatHistory)
        assertTrue { chatHistory.isEmpty() }
        verify { mockChat?.getChatEntries() }
    }    
}