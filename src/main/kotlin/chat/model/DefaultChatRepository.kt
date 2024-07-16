package chat.model

import chat.IChatRepository
import db.IDb
import db.model.Db
import jakarta.persistence.EntityManager

class DefaultChatRepository(private val db: IDb = Db()) : IChatRepository {
    private val entityManager: EntityManager = db.getEntityManager()

    override fun persist(chat: Chat) {
        entityManager.transaction.begin()
        entityManager.persist(chat)
        entityManager.transaction.commit()
    }

    override fun get(
        userId: String,
        courseSlug: String,
        courseSlugHash: String,
        assignmentId: String,
        taskId: String
    ): Chat? {
        entityManager.transaction.begin()
        val jpql =
            "SELECT c FROM Chat c WHERE c.userId = :userId AND c.courseSlug = :courseSlug AND c.courseSlugHash = :courseSlugHash AND c.assignmentId = :assignmentId AND c.taskId = :taskId"
        val query = entityManager.createQuery(jpql, Chat::class.java)
        query.setParameter("userId", userId)
        query.setParameter("courseSlug", courseSlug)
        query.setParameter("courseSlugHash", courseSlugHash)
        query.setParameter("assignmentId", assignmentId)
        query.setParameter("taskId", taskId)
        val chat: Chat? = query.resultList.firstOrNull()
        entityManager.transaction.commit()

        return chat
    }

    override fun closeDbConnection() {
        db.closeConnection()
    }
}