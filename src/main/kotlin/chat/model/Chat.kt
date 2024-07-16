package chat.model

import chat.IChatRepository
import db.model.Db
import jakarta.persistence.*

@Entity
open class Chat(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long? = null,
    @Column(nullable = false)
    private val userId: String,
    @Column(nullable = false)
    private val courseSlug: String,
    @Column(nullable = false)
    private val courseSlugHash: String,
    @Column(nullable = false)
    private val assignmentId: String?,
    @Column(nullable = false)
    private val taskId: String?,
    @OneToMany(mappedBy = "chat", cascade = [CascadeType.ALL], orphanRemoval = true)
    private val chatEntries: MutableList<ChatEntry> = mutableListOf()
) {
    constructor() : this(null, "", "", "", null, null)

    open fun getCourseSlugHash(): String {
        return courseSlugHash
    }

    open fun getChatEntries(): List<ChatEntry> {
        return chatEntries
    }

    open fun addChatEntry(chatEntry: ChatEntry) {
        chatEntry.setChat(this)
        chatEntries.add(chatEntry)
    }
}
