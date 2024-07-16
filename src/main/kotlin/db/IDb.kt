package db

import jakarta.persistence.EntityManager

interface IDb {
    fun getEntityManager(): EntityManager

    fun closeConnection()
}
