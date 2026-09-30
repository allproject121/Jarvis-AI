package com.example.data.repository

import com.example.nlp.JarvisIntent
import com.example.planner.RiskLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FirebaseWorkflowRepositoryTest {

    private lateinit var repository: FirebaseWorkflowRepository

    @Before
    fun setUp() {
        // Instantiate without real network call for parsing unit tests
        repository = FirebaseWorkflowRepository()
    }

    @Test
    fun testParseWorkflowJson_MultiStepOfficeMode() {
        val sampleJson = """
        {
          "workflowId": "wf_office_123",
          "workflowName": "Office Mode Automation",
          "summary": "Setup office connectivity and audio modes",
          "intent": "TURN_ON_MODE",
          "riskLevel": "LOW",
          "requiresConfirmation": false,
          "steps": [
            {
              "id": "step_1",
              "name": "Connect Office WiFi",
              "description": "Switch network to Office WiFi",
              "actionType": "SYSTEM_SETTING",
              "parameters": {
                "setting": "WIFI",
                "value": "1"
              },
              "dependsOn": [],
              "canRunParallel": false,
              "riskLevel": "LOW",
              "estimatedMs": 500
            },
            {
              "id": "step_2",
              "name": "Engage Vibrate Shield",
              "description": "Silence ringer for office focus",
              "actionType": "SYSTEM_SETTING",
              "parameters": {
                "setting": "VIBRATE_MODE",
                "value": "1"
              },
              "dependsOn": ["step_1"],
              "canRunParallel": false,
              "riskLevel": "LOW",
              "estimatedMs": 400
            }
          ]
        }
        """.trimIndent()

        val plan = repository.parseWorkflowJson(sampleJson, "Office mode on")

        assertEquals("wf_office_123", plan.id)
        assertEquals(JarvisIntent.TURN_ON_MODE, plan.originalIntent)
        assertEquals("Office mode on", plan.originalText)
        assertEquals(RiskLevel.LOW, plan.riskLevel)
        assertFalse(plan.requiresConfirmation)
        assertEquals(2, plan.tasks.size)

        val task1 = plan.tasks[0]
        assertEquals("step_1", task1.id)
        assertEquals("Connect Office WiFi", task1.name)
        assertEquals("SYSTEM_SETTING", task1.actionType)
        assertEquals("WIFI", task1.parameters["setting"])
        assertEquals("1", task1.parameters["value"])

        val task2 = plan.tasks[1]
        assertEquals("step_2", task2.id)
        assertEquals(listOf("step_1"), task2.dependsOn)
        assertEquals(900L, plan.estimatedDurationMs)
    }

    @Test
    fun testParseWorkflowJson_WithMarkdownCodeBlock() {
        val markdownWrapped = """
        ```json
        {
          "workflowId": "wf_backup_99",
          "workflowName": "Instagram Cloud Backup",
          "summary": "Download media and upload to Google Drive",
          "intent": "BACKUP_MEDIA",
          "riskLevel": "MEDIUM",
          "requiresConfirmation": false,
          "steps": [
            {
              "id": "step_1",
              "name": "Launch Instagram",
              "description": "Open Instagram app",
              "actionType": "LAUNCH_APP",
              "parameters": {
                "packageName": "com.instagram.android"
              },
              "dependsOn": [],
              "estimatedMs": 800
            }
          ]
        }
        ```
        """.trimIndent()

        val plan = repository.parseWorkflowJson(markdownWrapped, "Backup Instagram to drive")

        assertEquals("wf_backup_99", plan.id)
        assertEquals(JarvisIntent.BACKUP_MEDIA, plan.originalIntent)
        assertEquals(RiskLevel.MEDIUM, plan.riskLevel)
        assertEquals(1, plan.tasks.size)
        assertEquals("com.instagram.android", plan.tasks[0].parameters["packageName"])
    }

    @Test
    fun testParseWorkflowJson_HighRiskRequiresConfirmation() {
        val criticalJson = """
        {
          "workflowId": "wf_danger_1",
          "workflowName": "Remote Wipe",
          "summary": "High risk system operation",
          "intent": "CHANGE_SETTING",
          "riskLevel": "CRITICAL",
          "requiresConfirmation": true,
          "confirmationMessage": "Are you sure you want to proceed with this high-risk automation?",
          "steps": [
            {
              "id": "step_1",
              "name": "Lock Screen",
              "description": "Engage immediate lock",
              "actionType": "LOCK_SCREEN",
              "parameters": {
                "action": "LOCK"
              },
              "riskLevel": "CRITICAL"
            }
          ]
        }
        """.trimIndent()

        val plan = repository.parseWorkflowJson(criticalJson, "Lock screen immediately")

        assertEquals(RiskLevel.CRITICAL, plan.riskLevel)
        assertTrue(plan.requiresConfirmation)
        assertNotNull(plan.confirmationMessage)
        assertEquals(1, plan.tasks.size)
        assertEquals("LOCK_SCREEN", plan.tasks[0].actionType)
    }
}
