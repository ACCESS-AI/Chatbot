package llm

interface ILlm {
    suspend fun query(prompt: String): String
}
