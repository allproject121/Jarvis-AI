package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.model.CommandRecord
import com.example.data.model.EntityRecord
import com.example.data.model.PatternRecord
import com.example.data.repository.CommandRepository
import com.example.data.repository.DefaultCommandRepository
import com.example.data.repository.DefaultEntityRepository
import com.example.data.repository.DefaultPatternRepository
import com.example.data.repository.EntityRepository
import com.example.data.repository.JarvisRepository
import com.example.data.repository.PatternRepository
import com.example.di.AppContainer
import com.example.di.AppDependencyProvider
import com.example.di.DefaultAppContainer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RepositoryAndDITest {

    private lateinit var db: AppDatabase
    private lateinit var commandRepo: CommandRepository
    private lateinit var patternRepo: PatternRepository
    private lateinit var entityRepo: EntityRepository
    private lateinit var jarvisRepo: JarvisRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        commandRepo = DefaultCommandRepository(db.commandDAO())
        patternRepo = DefaultPatternRepository(db.patternDAO())
        entityRepo = DefaultEntityRepository(db.entityDAO())
        jarvisRepo = JarvisRepository(
            commandRepository = commandRepo,
            patternRepository = patternRepo,
            entityRepository = entityRepo,
            database = db
        )
    }

    @After
    fun tearDown() {
        db.close()
        AppDependencyProvider.setContainerForTesting(null)
    }

    @Test
    fun testCommandRepository_crudOperations() = runBlocking {
        val cmd = CommandRecord(
            originalText = "Launch Camera",
            parsedIntent = "LAUNCH_APP",
            extractedEntities = "{}",
            actionsExecuted = "Opened Camera"
        )
        val id = commandRepo.recordCommand(cmd)
        assertTrue(id > 0)

        val fetched = commandRepo.getCommandById(id)
        assertNotNull(fetched)
        assertEquals("LAUNCH_APP", fetched?.parsedIntent)

        val recent = commandRepo.getRecentCommands(5).first()
        assertEquals(1, recent.size)

        commandRepo.updateFeedback(id, "CORRECT")
        val updated = commandRepo.getCommandById(id)
        assertEquals("CORRECT", updated?.userFeedback)

        commandRepo.clearHistory()
        val empty = commandRepo.allCommands.first()
        assertEquals(0, empty.size)
    }

    @Test
    fun testPatternRepository_crudOperations() = runBlocking {
        val pattern = PatternRecord(
            patternType = "LOCATION_BASED",
            description = "Home arrival",
            triggerType = "LOCATION_BASED",
            triggerParams = "Home Geofence",
            actions = "WiFi on, Light on",
            confidenceScore = 0.88f
        )
        val id = patternRepo.insertPattern(pattern)
        assertTrue(id > 0)

        val fetched = patternRepo.getPatternById(id)
        assertNotNull(fetched)
        assertEquals("Home arrival", fetched?.description)

        patternRepo.setPatternEnabled(id, false)
        val enabled = patternRepo.enabledPatterns.first()
        assertEquals(0, enabled.size)

        patternRepo.setPatternEnabled(id, true)
        val enabledAgain = patternRepo.enabledPatterns.first()
        assertEquals(1, enabledAgain.size)

        patternRepo.recordOccurrence(id)
        val recorded = patternRepo.getPatternById(id)
        assertEquals(2, recorded?.occurrenceCount)
    }

    @Test
    fun testEntityRepository_crudOperations() = runBlocking {
        val entity = EntityRecord(
            entityType = "APP",
            entityName = "Spotify",
            aliases = "Music, Tunes",
            resolvedValue = "com.spotify.music"
        )
        val id = entityRepo.insertEntity(entity)
        assertTrue(id > 0)

        val found = entityRepo.findEntityByName("Tunes")
        assertNotNull(found)
        assertEquals("Spotify", found?.entityName)

        entityRepo.markEntityAccessed(id, 12345L)
        val accessed = entityRepo.getEntityById(id)
        assertEquals(12345L, accessed?.lastAccessed)
    }

    @Test
    fun testJarvisRepository_facadeDelegation() = runBlocking {
        jarvisRepo.seedDefaultsIfEmpty()

        val patterns = jarvisRepo.allPatterns.first()
        assertTrue(patterns.isNotEmpty())

        val entities = jarvisRepo.allEntities.first()
        assertTrue(entities.isNotEmpty())

        val mom = jarvisRepo.findEntityByName("Mom")
        assertNotNull(mom)
    }

    @Test
    fun testDependencyInjection_singletonContainer() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val container: AppContainer = DefaultAppContainer(context)

        // Verify lazy singleton instances
        val db1 = container.appDatabase
        val db2 = container.appDatabase
        assertSame(db1, db2)

        val cmdRepo1 = container.commandRepository
        val cmdRepo2 = container.commandRepository
        assertSame(cmdRepo1, cmdRepo2)

        val patternRepo1 = container.patternRepository
        val patternRepo2 = container.patternRepository
        assertSame(patternRepo1, patternRepo2)

        val entityRepo1 = container.entityRepository
        val entityRepo2 = container.entityRepository
        assertSame(entityRepo1, entityRepo2)

        val jarvisRepo1 = container.jarvisRepository
        val jarvisRepo2 = container.jarvisRepository
        assertSame(jarvisRepo1, jarvisRepo2)

        // Test AppDependencyProvider
        AppDependencyProvider.setContainerForTesting(container)
        val retrieved = AppDependencyProvider.getContainer(context)
        assertSame(container, retrieved)
    }
}
