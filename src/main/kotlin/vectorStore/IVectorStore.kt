package vectorStore

import vectorStore.model.Context

interface IVectorStore {
    suspend fun getContext(embeddings: List<Float>, collectionName: String): Context
}
