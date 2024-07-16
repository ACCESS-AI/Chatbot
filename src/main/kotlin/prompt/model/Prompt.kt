package prompt.model

import chat.model.ChatEntry
import vectorStore.model.Context
import prompt.IPrompt

class Prompt : IPrompt {
    private val chatbotInstructions: String =
            """You're an assistant guidance chatbot tasked with aiding users in solving problems independently. Your responses should offer hints, tips, and guidance based on the provided context and metadata, without providing complete solutions, including code examples that directly solve the problem. Your role is to encourage users to develop their problem-solving skills. Keep responses concise and positive, limiting each to a maximum of 5 sentences.

                Sections:
                    - Chat History: Record of the ongoing conversation for continuity and context-aware guidance.
                    - Access Software Description: Instructions on using the software to navigate, edit, and submit tasks.
                    - Lecture Context: Relevant information from the user's provided materials to tailor responses.
                    - Lecture Context Sources: References to specific filenames, page numbers, or dates to guide users in finding necessary information.
                    - Student's Previous Submissions: Review of earlier attempts to provide feedback that builds on past efforts.
                    - Student's Task Instructions: Specific requirements of the current task to understand the user's query context.
                    - Student's Prompt: The initial question or prompt from the user defining the problem needing assistance."""

    private val accessDescription =
        """- Navigation: [Top-left to switch between assignments]
            - File Tree: [All task files on the left]
            - Dashboard: [Score, attempts, and coding history are on the right]
            - Editing: [Most files can be viewed, but only some can be edited in the center]
            - Buttons: ["Test", "Run", and "Submit" are at the top-right]
            - Tabs: ["Test", "Run", "Submit" and "Chatbot" correspond to tabs at the bottom]
        For students to solve assignments they must:
            - Carefully read the instructions.
            - Edit files as instructed.
            - Click "Run" to see script output.
            - Use "Test" for basic tests.
            - Submit their solution for grading.
            - Review history for hints if needed.
        Students have limited submission attempts, but they refill daily."""

    override fun createPrompt(
        previousChatEntries: List<ChatEntry>,
        context: Context,
        taskInstructions: String,
        submissions: List<String>,
        userPrompt: String
    ): String {
        val promptBuilder = StringBuilder("${this.chatbotInstructions}\n\n\n\n")

        if (previousChatEntries.isNotEmpty()) {
            val chatHistoryBuilder = StringBuilder()
            previousChatEntries.forEach { entry ->
                chatHistoryBuilder.append("Student: [${entry.getUserPrompt()}]\n")
                chatHistoryBuilder.append("Assistant: [${entry.getLlmOutput()}]\n\n")
            }
            promptBuilder.append("CHAT HISTORY:\n$chatHistoryBuilder\n\n")
        }

        promptBuilder.append("ACCESS SOFTWARE DESCRIPTION:\n${accessDescription}\n\n\n\n")

        if (context.getContext().isNotBlank()) {
            promptBuilder.append("LECTURE CONTEXT:\n${context.getContext()}\n\n\n\n")
        }

        if (context.getMetadata().isNotEmpty()) {
            promptBuilder.append("LECTURE CONTEXT SOURCES:\n${context.metadataToString()}\n\n\n\n")
        }

        if (submissions.isNotEmpty()) {
            val submissionsBuilder = StringBuilder()
            submissions.forEachIndexed { index, submission ->
                submissionsBuilder.append("Submission ${index + 1}:\n[$submission]\n\n")
            }
            promptBuilder.append("STUDENT'S PREVIOUS SUBMISSIONS:\n$submissionsBuilder\n\n")
        }

        promptBuilder.append("STUDENT'S TASK INSTRUCTIONS:\n$taskInstructions\n\n\n\n")
        promptBuilder.append("STUDENT'S PROMPT:\n$userPrompt")

        return promptBuilder.toString()
    }
}
