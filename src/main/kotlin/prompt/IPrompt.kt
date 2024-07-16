package prompt

import chat.model.ChatEntry
import vectorStore.model.Context

interface IPrompt {
    fun createPrompt(previousChatEntries: List<ChatEntry>, context: Context, taskInstructions: String, submissions: List<String>, userPrompt: String): String
}