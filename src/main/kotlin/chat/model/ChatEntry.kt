package chat.model

import jakarta.persistence.*
import vectorStore.model.MetaData
import java.time.LocalDateTime

@Entity
open class ChatEntry(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long? = null,

    @Column(nullable = false, columnDefinition = "TEXT")
    private val userPrompt: String,

    @Column(nullable = true, columnDefinition = "TEXT")
    private var llmOutput: String? = null,

    @ManyToOne
    @JoinColumn(name = "chat_id", nullable = false)
    private var chat: Chat? = null,

    @Column(nullable = true)
    private var llmTimestamp: LocalDateTime? = null,

    @Column(nullable = false)
    private val userTimestamp: LocalDateTime = LocalDateTime.now(),

    @OneToMany(mappedBy = "chatEntry", targetEntity = MetaData::class, cascade = [CascadeType.ALL], orphanRemoval = true)
    private var metadata: Set<MetaData> = setOf(),

    @Column(nullable = true, columnDefinition = "TEXT")
    private var finalPrompt: String? = null
) {
    constructor() : this(null, "", null, null, null, LocalDateTime.now(), setOf(), null)

    open fun getId(): Long? {
        return id
    }

    open fun getUserPrompt(): String {
        return userPrompt
    }

    open fun getLlmOutput(): String? {
        return llmOutput
    }

    open fun setLlmOutput(llmOutput: String) {
        this.llmOutput = llmOutput
    }

    open fun getLlmTimestamp(): LocalDateTime? {
        return llmTimestamp
    }

    open fun setLlmTimestamp(llmTimestamp: LocalDateTime) {
        this.llmTimestamp = llmTimestamp
    }

    open fun getUserTimestamp(): LocalDateTime {
        return userTimestamp
    }

    open fun getMetadata(): Set<MetaData> {
        return metadata
    }

    open fun setMetadata(metadata: Set<MetaData>) {
        this.metadata = metadata
        this.metadata.forEach { it.setChatEntry(this) }
    }

    open fun addMetaData(metadata: MetaData) {
        metadata.setChatEntry(this)
        this.metadata.plus(metadata)
    }

    open fun removeMetaData(metadata: MetaData) {
        this.metadata.minus(metadata)
    }

    open fun getChat(): Chat? {
        return this.chat
    }

    open fun setChat(chat: Chat){
        this.chat = chat
    }

    open fun getFinalPrompt(): String? {
        return this.finalPrompt
    }

    open fun setFinalPrompt(finalPrompt: String) {
        this.finalPrompt = finalPrompt
    }
}
