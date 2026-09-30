package com.example.nlp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class Layers1And2Test {

    private lateinit var nlpEngine: NLPEngine
    private lateinit var entityExtractor: EntityExtractor
    private lateinit var intentClassifier: IntentClassifier

    @Before
    fun setUp() {
        nlpEngine = NLPEngine()
        entityExtractor = EntityExtractor()
        intentClassifier = IntentClassifier()
    }

    @Test
    fun testSendMessageToMom() {
        val result = nlpEngine.processCommand("Send message to Mom")
        assertTrue("Expected command to be valid", result.isValid)
        assertTrue("Confidence should be >= 0.85, got ${result.confidence}", result.confidence >= 0.85f)
        assertEquals(JarvisIntent.SEND_MESSAGE, result.intent)
        assertEquals("Mom", result.entities.contact)
    }

    @Test
    fun testCallRaj() {
        val result = nlpEngine.processCommand("Call Raj")
        assertTrue("Expected command to be valid", result.isValid)
        assertTrue("Confidence should be >= 0.85, got ${result.confidence}", result.confidence >= 0.85f)
        assertEquals(JarvisIntent.MAKE_CALL, result.intent)
        assertEquals("Raj", result.entities.contact)
    }

    @Test
    fun testOpenGmail() {
        val result = nlpEngine.processCommand("Open Gmail")
        assertTrue("Expected command to be valid", result.isValid)
        assertTrue("Confidence should be >= 0.85, got ${result.confidence}", result.confidence >= 0.85f)
        assertEquals(JarvisIntent.OPEN_APP, result.intent)
        assertEquals("Gmail", result.entities.app)
    }

    @Test
    fun testSetBrightnessTo80() {
        val result = nlpEngine.processCommand("Set brightness to 80")
        assertTrue("Expected command to be valid", result.isValid)
        assertTrue("Confidence should be >= 0.85, got ${result.confidence}", result.confidence >= 0.85f)
        assertEquals(JarvisIntent.CHANGE_SETTING, result.intent)
        assertEquals("BRIGHTNESS", result.entities.settingName)
        assertEquals(80, result.entities.settingValue)
    }

    @Test
    fun testTurnOffWiFi() {
        val result = nlpEngine.processCommand("Turn off WiFi")
        assertTrue("Expected command to be valid", result.isValid)
        assertTrue("Confidence should be >= 0.85, got ${result.confidence}", result.confidence >= 0.85f)
        assertEquals(JarvisIntent.CHANGE_SETTING, result.intent)
        assertEquals("WIFI", result.entities.settingName)
    }

    @Test
    fun testLockScreen() {
        val result = nlpEngine.processCommand("Lock screen")
        assertTrue("Expected command to be valid", result.isValid)
        assertTrue("Confidence should be >= 0.85, got ${result.confidence}", result.confidence >= 0.85f)
        assertEquals(JarvisIntent.LOCK_UNLOCK_SCREEN, result.intent)
        assertEquals("LOCK_SCREEN", result.entities.settingName)
    }

    @Test
    fun testUnlockScreen() {
        val result = nlpEngine.processCommand("Unlock screen")
        assertTrue("Expected command to be valid", result.isValid)
        assertTrue("Confidence should be >= 0.85, got ${result.confidence}", result.confidence >= 0.85f)
        assertEquals(JarvisIntent.LOCK_UNLOCK_SCREEN, result.intent)
        assertEquals("UNLOCK_SCREEN", result.entities.settingName)
    }

    @Test
    fun testFuzzyMatching() {
        assertTrue(entityExtractor.fuzzyMatch("brightness", "set briteness to 50"))
        assertTrue(entityExtractor.fuzzyMatch("instagram", "open instgram"))
        assertTrue(entityExtractor.fuzzyMatch("whatsapp", "send watsapp message"))
    }
}
