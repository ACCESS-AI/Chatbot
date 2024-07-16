package vectorStore.model

import io.milvus.param.highlevel.dml.response.SearchResponse
import io.milvus.v2.client.MilvusClientV2
import io.milvus.v2.service.vector.response.SearchResp
import io.mockk.*
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MilvusTest {
    private val milvusClient: MilvusClientV2 = mockk()
    private lateinit var milvus: Milvus

    private fun setup(returnContext: Boolean = true, distanceBelowThreshold: Boolean = false, throwException: Boolean = false) {
        val mockSearchResult1 = mockk<SearchResp.SearchResult>()
        every { mockSearchResult1.distance } returns if(distanceBelowThreshold) 0f else 1f
        every { mockSearchResult1.entity } returns mapOf("text" to "testContext", "metadata" to "{\"file_identifier\": \"test.pdf\", \"page\": \"1\"}")

        val mockSearchResult2 = mockk<SearchResp.SearchResult>()
        every { mockSearchResult2.distance } returns if(distanceBelowThreshold) 0f else 1f
        every { mockSearchResult2.entity } returns mapOf("text" to "testContext", "metadata" to "{\"file_identifier\": \"test.txt\"}")

        val searchResults = if(returnContext) listOf(listOf(mockSearchResult1, mockSearchResult1, mockSearchResult2, mockSearchResult2)) else listOf()

        val mockSearchResp = mockk<SearchResp>()

        every { mockSearchResp.searchResults } returns searchResults

        if(throwException) {
            every { milvusClient.search(any()) } throws Exception("Test exception")
        } else {
            every { milvusClient.search(any()) } returns mockSearchResp
        }
        every { milvusClient.loadCollection(any()) } just Runs
        every { milvusClient.releaseCollection(any()) } just Runs

        milvus = Milvus(milvusClient)
    }

    @Test
    fun testGetContextWithSuccessfulSearch() {
        setup()

        runBlocking {
            val context = milvus.getContext(listOf(0.1f, 0.2f, 0.3f), "testCollection")

            assertTrue(context.getContext().isNotEmpty())
        }
    }

    @Test
    fun testGetContextWithEmptySearch() {
        setup(false)

        runBlocking {
            val context = milvus.getContext(listOf(0.1f, 0.2f, 0.3f), "testCollection")

            assertTrue(context.getContext().isEmpty())
        }
    }

    @Test
    fun testGetContextWithException() {
        setup(throwException = true)

        runBlocking {
            val context = milvus.getContext(listOf(0.1f, 0.2f, 0.3f), "testCollection")

            assertTrue(context.getContext().isEmpty())
        }
    }

    @Test
    fun testDistanceBelowThreshold() {
        setup(distanceBelowThreshold = true)

        runBlocking {
            val context = milvus.getContext(listOf(0.1f, 0.2f, 0.3f), "testCollection")

            assertTrue(context.getContext().isEmpty())
        }
    }
}