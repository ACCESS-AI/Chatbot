package chatbot.model

import vectorStore.model.MetaData
import java.time.LocalDateTime

data class Message(val message: String, val timestamp: LocalDateTime, val metadata: Set<MetaData>, val finalPrompt: String? = null)