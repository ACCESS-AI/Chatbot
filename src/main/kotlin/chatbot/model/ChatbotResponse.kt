package chatbot.model

import vectorStore.model.MetaData
import java.time.LocalDateTime

data class ChatbotResponse(
    val llmOutput: String?,
    val metadata: Set<MetaData> = setOf(),
    val llmTimestamp: LocalDateTime = LocalDateTime.now(),
    val finalPrompt: String? = null
) {
    companion object {
        fun somethingWentWrong(): ChatbotResponse{
            return ChatbotResponse(llmOutput = "I'm sorry to say, but something went wrong on my side. Please try again.")
        }
    }
}
