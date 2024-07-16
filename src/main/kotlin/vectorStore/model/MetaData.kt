package vectorStore.model
import chat.model.ChatEntry
import jakarta.persistence.*

@Entity
open class MetaData(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private val id: Long? = null,
        @Column(nullable = false, columnDefinition = "TEXT")
        private val source: String,
        @Column(nullable = true, columnDefinition = "TEXT")
        private var pages: String?,
        @Column
        private var score: Float,
        @ManyToOne
        @JoinColumn(name = "chat_entry_id", nullable = false)
        private var chatEntry: ChatEntry? = null
) : Comparable<MetaData> {

    constructor() : this(null, "", null, 0.toFloat(), null)

    open fun getId(): Long? {
        return id
    }

    open fun getSource(): String {
        return source
    }

    open fun getPages(): String? {
        return pages
    }

    open fun setPages(pages: String?) {
        this.pages = pages
    }

    open fun addPage(page: String) {
        this.pages += ", $page"
    }

    open fun setChatEntry(chatEntry: ChatEntry) {
        this.chatEntry = chatEntry
    }

    override fun toString(): String {
        var res = "filename: $source"
        if (!pages.isNullOrEmpty()) {
            res += " (Pages: ${pages}) -> score: $score"
        }
        return res
    }

    override fun equals(other: Any?): Boolean {
        if (other !is MetaData) return false

        if (source != other.source) return false
        if ((chatEntry == null && other.chatEntry != null)
            || (chatEntry != null && other.chatEntry == null)
            || chatEntry != null
        ) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + source.hashCode()
        result = 31 * result + (pages?.hashCode() ?: 0)
        return result
    }

    override fun compareTo(other: MetaData): Int {
        return this.source.compareTo(other.source)
    }
}
