package db.model

import db.IDb
import jakarta.persistence.EntityManager
import jakarta.persistence.Persistence

class Db(private var entityManager: EntityManager? = null) : IDb {
    init {
        if (entityManager == null || !entityManager!!.isOpen) {
            val env: Map<String, String> = System.getenv()
            val configOverrides: MutableMap<String, Any> = HashMap()
            for ((envName, value) in env) {
                when {
                    envName.contains("CHATBOT_DB_URL") -> configOverrides["javax.persistence.jdbc.url"] = value
                    envName.contains("CHATBOT_DB_USER") -> configOverrides["javax.persistence.jdbc.user"] = value
                    envName.contains("CHATBOT_DB_PASSWORD") -> configOverrides["javax.persistence.jdbc.password"] = value
                }
            }
            entityManager = createEntityManager(configOverrides)
        }
    }

    private fun createEntityManager(configOverrides: Map<String, Any>): EntityManager {
        val entityManagerFactory = Persistence.createEntityManagerFactory("chatbot", configOverrides)
        return entityManagerFactory.createEntityManager()
    }

    override fun getEntityManager(): EntityManager = entityManager!!

    override fun closeConnection() {
        entityManager?.close()
    }
}
