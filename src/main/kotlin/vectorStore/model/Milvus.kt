package vectorStore.model

import com.alibaba.fastjson.JSONObject
import io.milvus.v2.client.ConnectConfig
import io.milvus.v2.client.MilvusClientV2
import io.milvus.v2.common.IndexParam.MetricType
import io.milvus.v2.service.collection.request.LoadCollectionReq
import io.milvus.v2.service.collection.request.ReleaseCollectionReq
import io.milvus.v2.service.vector.request.SearchReq
import io.milvus.v2.service.vector.response.SearchResp
import io.milvus.v2.service.vector.response.SearchResp.SearchResult
import vectorStore.IVectorStore

class Milvus(
    private var vectorStore: MilvusClientV2? = null
) : IVectorStore {
    private val VECTOR_STORE_HOST = System.getenv("VECTOR_STORE_HOST")
    private val TEXT_FIELD: String = "text"
    private val METADATA_FIELD: String = "metadata"
    private val SIMILARITY_SCORE_THRESHOLD: Float =
            (System.getenv("SIMILARITY_SCORE_THRESHOLD") ?: "0.7").toFloat()
    private val TOP_K_RESULTS: Int = (System.getenv("TOP_K_RESULTS") ?: "3").toInt()

    init {
        if (this.vectorStore == null) {
            vectorStore = getVectorStore()
        }
    }

    private fun getVectorStore(): MilvusClientV2 {
        val connectConfig = ConnectConfig.builder().uri(VECTOR_STORE_HOST).build()
        return MilvusClientV2(connectConfig)
    }

    override suspend fun getContext(embeddings: List<Float>, collectionName: String): Context {
        try {
            this.vectorStore!!.loadCollection(
                    LoadCollectionReq.builder().collectionName(collectionName).build()
            )

            val searchReq =
                    SearchReq.builder()
                            .collectionName(collectionName)
                            .data(listOf(embeddings))
                            .searchParams(mapOf<String, Any>("metric_type" to MetricType.COSINE))
                            .filter("metadata[\"start\"] <= ${System.currentTimeMillis()}")
                            .topK(TOP_K_RESULTS)
                            .outputFields(listOf(TEXT_FIELD, METADATA_FIELD))
                            .build()

            val searchResp: SearchResp = vectorStore!!.search(searchReq)

            // Release the collection loaded in Milvus to reduce memory consumption when the search is completed.
            vectorStore!!.releaseCollection(ReleaseCollectionReq.builder().collectionName(collectionName).build())

            val searchResults = searchResp.searchResults

            if (searchResults.isEmpty()) return Context()

            return this.buildContext(searchResults)
        } catch (e: Exception) {
            println("Failed to get context: ${e.message}")
            return Context()
        }
    }

    private fun buildContext(searchResults: MutableList<MutableList<SearchResult>>): Context {
        var context = ""
        var metadata: Set<MetaData> = sortedSetOf()

        searchResults.forEach { searchResult ->
            searchResult.forEach { result ->
                val score = result.distance
                if (score > SIMILARITY_SCORE_THRESHOLD) {

                    val metadataJson =
                            JSONObject.parseObject(result.entity[METADATA_FIELD].toString())
                    context += "${result.entity[TEXT_FIELD]} "

                    val page: String? = metadataJson["page"]?.toString()
                    val metadataObject =
                            MetaData(
                                    source = metadataJson["file_identifier"].toString(),
                                    pages = page,
                                    score = score
                            )
                    val otherMetadataObject = metadata.find { it == metadataObject }
                    if (otherMetadataObject != null) {
                        if (page != null) {
                            otherMetadataObject.addPage(page)
                        }
                    } else {
                        metadata = metadata.plus(metadataObject)
                    }
                }
            }
        }

        if (context.isEmpty()) return Context()

        return Context(context, metadata)
    }
}
