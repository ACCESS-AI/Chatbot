package embedder.model

import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.model.ollama.OllamaEmbeddingModel
import embedder.IEmbedder

class OllamaEmbedder() : IEmbedder {
    private val EMBEDDER_MODEL = System.getenv("OLLAMA_EMBEDDER_MODEL")
    private val EMBEDDER_HOST = System.getenv("OLLAMA_EMBEDDER_HOST")

    private val embeddingModel: EmbeddingModel

    init {
        this.embeddingModel = this.getEmbeddingModel()
    }

    private fun getEmbeddingModel(): EmbeddingModel {
        return OllamaEmbeddingModel.builder()
            .baseUrl(EMBEDDER_HOST)
            .modelName(EMBEDDER_MODEL)
            .build()
    }

    override suspend fun embed(input: String): List<Float> {
        return this.embeddingModel.embed(input).content().vector().toList()
    }
}