package chat

import chat.model.Chat

interface IChatRepository {
    fun persist(chat: Chat)
    
    fun get(
        userId: String,
        courseSlug: String,
        courseSlugHash: String,
        assignmentId: String,
        taskId: String
    ): Chat?

    fun closeDbConnection()
}