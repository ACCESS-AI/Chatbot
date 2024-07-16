package vectorStore.model

class Context(
        private val context: String = "",
        private val metadata: Set<MetaData> = setOf()
) {
    fun getContext(): String {
        return context
    }

    fun getMetadata(): Set<MetaData> {
        return metadata
    }

    fun metadataToString(): String {
        var res = ""
        for (data in metadata) {
            res += "${data}\n"
        }
        return res
    }
}
