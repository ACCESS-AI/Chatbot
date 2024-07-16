package chatbot.model

import org.junit.Assert.assertTrue
import kotlin.test.Test

class ChatbotResponseTest {
    @Test
    fun testSomethingWentWrong() {
        val response = ChatbotResponse.somethingWentWrong()
        assertTrue(response.llmOutput is String)
    }
}