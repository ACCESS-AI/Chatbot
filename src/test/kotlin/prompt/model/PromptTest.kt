package prompt.model

import chat.model.ChatEntry
import vectorStore.model.Context
import vectorStore.model.MetaData
import kotlin.test.Test
import kotlin.test.assertFalse

class PromptTest {

    private val prompt = Prompt()

    @Test
    fun testCreatePromptWithEmptyInputs() {
        val chatEntries = emptyList<ChatEntry>()
        val context = Context("", emptySet())
        val taskInstructions = ""
        val submissions = emptyList<String>()
        val userPrompt = ""

        val result = prompt.createPrompt(chatEntries, context, taskInstructions, submissions, userPrompt)

        // Check for essential sections even in empty input scenario
        assert(result.contains("ACCESS SOFTWARE DESCRIPTION:"))
        assertFalse(result.contains("LECTURE CONTEXT:"))
        assertFalse(result.contains("LECTURE CONTEXT SOURCES:"))
        assertFalse(result.contains("STUDENT'S PREVIOUS SUBMISSIONS:"))
        assert(result.contains("STUDENT'S TASK INSTRUCTIONS:"))
        assert(result.contains("STUDENT'S PROMPT:"))
    }

    @Test
    fun testCreatePromptWithNonEmptyChatEntries() {
        val chatEntries = listOf(ChatEntry(123, "User question?", "Test LLM response."))
        val context = Context("", emptySet())
        val taskInstructions = ""
        val submissions = emptyList<String>()
        val userPrompt = ""

        val result = prompt.createPrompt(chatEntries, context, taskInstructions, submissions, userPrompt)

        assert(result.contains("Student: [User question?]"))
        assert(result.contains("Assistant: [Test LLM response.]"))
    }

    @Test
    fun testCreatePromptWithContextAndMetadata() {
        val chatEntries = emptyList<ChatEntry>()
        val context = Context("Lecture on Kotlin", setOf(MetaData()))
        val taskInstructions = "Solve the problem using loops."
        val submissions = listOf("First Attempt", "Second Attempt")
        val userPrompt = "How do I use loops?"

        val result = prompt.createPrompt(chatEntries, context, taskInstructions, submissions, userPrompt)

        assert(result.contains(context.getContext()))
        assert(result.contains(taskInstructions))
        assert(result.contains(submissions[0]))
        assert(result.contains(submissions[1]))
        assert(result.contains(userPrompt))
    }

    @Test
    fun testCreatePromptWithEmptyContextAndMetadata() {
        val chatEntries = emptyList<ChatEntry>()
        val context = Context("", emptySet())
        val taskInstructions = "Solve the problem using loops."
        val submissions = listOf("First Attempt", "Second Attempt")
        val userPrompt = "How do I use loops?"

        val result = prompt.createPrompt(chatEntries, context, taskInstructions, submissions, userPrompt)

        assertFalse(result.contains("LECTURE CONTEXT:"))
        assertFalse(result.contains("LECTURE CONTEXT SOURCES:"))
        assert(result.contains(taskInstructions))
        assert(result.contains(submissions[0]))
        assert(result.contains(submissions[1]))
        assert(result.contains(userPrompt))
    }

    @Test
    fun testCreatePromptWithEmptySubmissions() {
        val chatEntries = emptyList<ChatEntry>()
        val context = Context("", emptySet())
        val taskInstructions = "Solve the problem using loops."
        val submissions = emptyList<String>()
        val userPrompt = "How do I use loops?"

        val result = prompt.createPrompt(chatEntries, context, taskInstructions, submissions, userPrompt)

        assert(result.contains(taskInstructions))
        assertFalse(result.contains("STUDENT'S PREVIOUS SUBMISSIONS:"))
        assert(result.contains(userPrompt))
    }
}