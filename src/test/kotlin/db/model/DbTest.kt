package db.model

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import jakarta.persistence.EntityManager
import jakarta.persistence.EntityManagerFactory
import jakarta.persistence.Persistence
import kotlin.test.*
import io.mockk.*

class DbTest {
    private val emMock: EntityManager = mockk<EntityManager>()

    @BeforeTest
    fun setup() {
        mockkStatic(Persistence::class)
        val emfMock = mockk<EntityManagerFactory>()
        every { Persistence.createEntityManagerFactory(any<String>(), any()) } returns emfMock
        every { emfMock.createEntityManager() } returns emMock
        every { emMock.close() } returns Unit
    }

    @Test
    fun testGetEntityManager() {
        val em = Db().getEntityManager()
        assertNotNull(em)
    }

    @Test
    fun testCloseConnection() {
        val db = Db()
        db.closeConnection()
        verify { db.getEntityManager().close() }
    }

    @Test
    fun testCloseConnectionShouldNotThrowExceptionIfEntityManagerIsNull() {
        val db = Db(null)
        db.closeConnection()
    }

    @Test
    fun testCloseConnectionShouldNotThrowExceptionIfEntityManagerIsClosed() {
        every { emMock.isOpen } returns false
        val db = Db(emMock)
        db.closeConnection()
    }

    @Test
    fun testCloseConnectionShouldNotThrowExceptionIfEntityManagerIsClosedAndNull() {
        every { emMock.isOpen } returns false
        val db = Db(null)
        db.closeConnection()
    }
}