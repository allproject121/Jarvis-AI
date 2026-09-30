package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.CommandDAO
import com.example.data.EntityDAO
import com.example.data.JarvisDatabase
import com.example.data.PatternDAO
import com.example.data.model.CommandRecord
import com.example.data.model.EntityRecord
import com.example.data.model.PatternRecord
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DaoTest {

    private lateinit var db: JarvisDatabase
    private lateinit var commandDao: CommandDAO
    private lateinit var patternDao: PatternDAO
    private lateinit var entityDao: EntityDAO

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, JarvisDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        commandDao = db.commandDAO()
        patternDao = db.patternDAO()
        entityDao = db.entityDAO()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testCommandDAO_insertAndQuery() = runBlocking {
        val record = CommandRecord(
            originalText = "Turn on lights",
            parsedIntent = "LIGHTS_ON",
            extractedEntities = "{}",
            actionsExecuted = "Done"
        )
        val id = commandDao.insertCommand(record)
        assertTrue(id > 0)

        val fetched = commandDao.getCommandById(id)
        assertNotNull(fetched)
        assertEquals("LIGHTS_ON", fetched?.parsedIntent)

        val list = commandDao.getAllCommands().first()
        assertEquals(1, list.size)

        commandDao.updateFeedback(id, "CORRECT")
        val updated = commandDao.getCommandById(id)
        assertEquals("CORRECT", updated?.userFeedback)

        commandDao.deleteById(id)
        val afterDelete = commandDao.getCommandById(id)
        assertNull(afterDelete)
    }

    @Test
    fun testPatternDAO_insertAndQuery() = runBlocking {
        val pattern = PatternRecord(
            patternType = "DAILY_ROUTINE",
            description = "Morning routine",
            triggerType = "TIME_BASED",
            triggerParams = "08:00 AM",
            actions = "Unmute, Calendar",
            confidenceScore = 0.9f
        )
        val id = patternDao.insertPattern(pattern)
        assertTrue(id > 0)

        val fetched = patternDao.getPatternById(id)
        assertNotNull(fetched)
        assertEquals("Morning routine", fetched?.description)

        val patterns = patternDao.getEnabledPatterns().first()
        assertEquals(1, patterns.size)

        patternDao.setEnabled(id, false)
        val enabledPatterns = patternDao.getEnabledPatterns().first()
        assertEquals(0, enabledPatterns.size)
    }

    @Test
    fun testEntityDAO_insertAndResolve() = runBlocking {
        val entity = EntityRecord(
            entityType = "CONTACT",
            entityName = "Alice",
            aliases = "Al, Ali",
            resolvedValue = "+1234567890"
        )
        val id = entityDao.insertEntity(entity)
        assertTrue(id > 0)

        val found = entityDao.findEntityByName("ali")
        assertNotNull(found)
        assertEquals("Alice", found?.entityName)

        val foundExact = entityDao.getEntityByName("Alice")
        assertNotNull(foundExact)
        assertEquals("+1234567890", foundExact?.resolvedValue)

        entityDao.markAccessed(id, 99999L)
        val updated = entityDao.getEntityById(id)
        assertEquals(99999L, updated?.lastAccessed)
        assertEquals(2, updated?.accessCount)
    }
}
