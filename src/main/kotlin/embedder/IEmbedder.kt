package embedder;

interface IEmbedder {
    suspend fun embed(input: String): List<Float>
}
