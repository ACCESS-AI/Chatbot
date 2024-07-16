package chatbot.model

import chat.IChatRepository
import chat.model.Chat
import chat.model.ChatEntry
import chat.model.DefaultChatRepository
import db.model.Db
import embedder.IEmbedder
import java.time.LocalDateTime
import llm.ILlm
import embedder.model.MistralAPIEmbedder
import llm.model.MistralAPILLM
import prompt.IPrompt
import prompt.model.Prompt
import vectorStore.IVectorStore
import vectorStore.model.Context
import vectorStore.model.Milvus

class Chatbot(
        userId: String,
        courseSlug: String,
        courseSlugHash: String,
        assignmentId: String,
        taskId: String,
        private val embedder: IEmbedder = MistralAPIEmbedder(),
        private val vectorStore: IVectorStore = Milvus(),
        private val promptModel: IPrompt = Prompt(),
        private val llm: ILlm = MistralAPILLM(),
        private val chatRepository: IChatRepository = DefaultChatRepository(Db())
) {
        private var chat: Chat

        init {
                val chat: Chat? = chatRepository.get(userId, courseSlug, courseSlugHash, assignmentId, taskId)
                if (chat != null) {
                        this.chat = chat
                }
                else {
                        this.chat = Chat(
                                userId = userId,
                                courseSlug = courseSlug,
                                courseSlugHash = courseSlugHash,
                                assignmentId = assignmentId,
                                taskId = taskId
                        )
                }
        }

        suspend fun run(
                taskInstructions: String,
                submissions: List<String>,
                userPrompt: String
        ): ChatbotResponse {
                val res: ChatbotResponse
                val context: Context
                val llmOutput: String?
                try {
                        val chatEntry = ChatEntry(userPrompt = userPrompt)

                        val embeddings = embedder.embed(userPrompt)

                        // get context from vector database by doing a similarity search between the user's prompt and the lecture context
                        context = vectorStore.getContext(embeddings, chat.getCourseSlugHash())

                        chatEntry.setMetadata(context.getMetadata())

                        val previousChatEntries = chat.getChatEntries().takeLast(3)

                        // creates the final meta prompt which will be the input for the LLM
                        val finalPrompt =
                                promptModel.createPrompt(
                                        previousChatEntries,
                                        context,
                                        taskInstructions,
                                        submissions,
                                        userPrompt
                                )

                        chatEntry.setFinalPrompt(finalPrompt)

                        llmOutput = llm.query(finalPrompt)

                        chatEntry.setLlmOutput(llmOutput)
                        chatEntry.setLlmTimestamp(LocalDateTime.now())

                        chat.addChatEntry(chatEntry)

                        res = ChatbotResponse(
                                llmOutput = llmOutput,
                                metadata = context.getMetadata(),
                                llmTimestamp = chat.getChatEntries().lastOrNull()!!.getLlmTimestamp()!!,
                                finalPrompt = finalPrompt
                        )

                        chatRepository.persist(chat)

                        chatRepository.closeDbConnection()

                        return res
                } catch (e: Exception) {
                        println("Failed to execute Chatbot.run: ${e.message}")
                        return ChatbotResponse.somethingWentWrong()
                }
        }

        fun getHistory(): List<Message> {
                val history = mutableListOf<Message>()
                chat.getChatEntries().forEach { entry ->
                        history.add(
                                Message(
                                        message = entry.getUserPrompt(),
                                        timestamp = entry.getUserTimestamp(),
                                        metadata = entry.getMetadata()
                                )
                        )
                        history.add(
                                Message(
                                        message = entry.getLlmOutput() ?: "",
                                        timestamp = entry.getLlmTimestamp() ?: LocalDateTime.now(),
                                        metadata = entry.getMetadata(),
                                        finalPrompt = entry.getFinalPrompt()
                                )
                        )
                }

                chatRepository.closeDbConnection()

                return history
        }
}
