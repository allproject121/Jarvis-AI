package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.JarvisApp
import com.example.data.model.CommandRecord
import com.example.data.model.EntityRecord
import com.example.data.model.PatternRecord
import com.example.execution.ExecutionEngine
import com.example.execution.PlanExecutionStatus
import com.example.gemini.ChatMessage
import com.example.gemini.GeminiClient
import com.example.learning.PatternRecognitionEngine
import com.example.memory.ContextBuilder
import com.example.memory.MemoryItem
import com.example.memory.MemoryManager
import com.example.memory.UserContext
import com.example.nlp.NLPEngine
import com.example.nlp.NLPResult
import com.example.planner.ActionPlan
import com.example.planner.TaskDecomposer
import com.example.prediction.ProactiveActionSuggester
import com.example.prediction.ProactiveSuggestion
import com.example.voice.SpeechRecognitionListener
import com.example.voice.VoiceInputManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class JarvisTab {
    HUD_VOICE,
    AI_CHAT,
    AUTOMATIONS,
    IMAGE_STUDIO,
    SYSTEM_DIAGNOSTICS
}

data class GeneratedImageItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val prompt: String,
    val resolution: String,
    val bitmap: Bitmap,
    val timestamp: Long = System.currentTimeMillis()
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as JarvisApp
    val repository = app.repository

    // Engines
    val voiceInputManager = VoiceInputManager(application, viewModelScope)
    val nlpEngine = NLPEngine()
    val contextBuilder = ContextBuilder(application)
    val memoryManager = MemoryManager(repository)
    val taskDecomposer = TaskDecomposer()
    val executionEngine = ExecutionEngine(application)
    val patternEngine = PatternRecognitionEngine(repository)
    val suggester = ProactiveActionSuggester()
    val geminiClient = GeminiClient(application)

    // UI States
    private val _currentTab = MutableStateFlow(JarvisTab.HUD_VOICE)
    val currentTab: StateFlow<JarvisTab> = _currentTab.asStateFlow()

    private val _userContext = MutableStateFlow(contextBuilder.getCurrentContext())
    val userContext: StateFlow<UserContext> = _userContext.asStateFlow()

    private val _currentActionPlan = MutableStateFlow<ActionPlan?>(null)
    val currentActionPlan: StateFlow<ActionPlan?> = _currentActionPlan.asStateFlow()

    private val _pendingConfirmation = MutableStateFlow<ActionPlan?>(null)
    val pendingConfirmation: StateFlow<ActionPlan?> = _pendingConfirmation.asStateFlow()

    val executionStatus: StateFlow<PlanExecutionStatus?> = executionEngine.executionStatus

    private val _proactiveSuggestions = MutableStateFlow<List<ProactiveSuggestion>>(emptyList())
    val proactiveSuggestions: StateFlow<List<ProactiveSuggestion>> = _proactiveSuggestions.asStateFlow()

    // Database reactive streams
    val recentCommands: StateFlow<List<CommandRecord>> = repository.recentCommands
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userPatterns: StateFlow<List<PatternRecord>> = repository.allPatterns
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val entityRecords: StateFlow<List<EntityRecord>> = repository.allEntities
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Chat thread
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                role = "model",
                text = "Greetings, Sir. J.A.R.V.I.S. online and all phone subsystems calibrated. What can I automate or execute for you today?"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _selectedChatModel = MutableStateFlow("gemini-3.5-flash")
    val selectedChatModel: StateFlow<String> = _selectedChatModel.asStateFlow()

    private val _isChatThinking = MutableStateFlow(false)
    val isChatThinking: StateFlow<Boolean> = _isChatThinking.asStateFlow()

    // Image Studio
    private val _generatedImages = MutableStateFlow<List<GeneratedImageItem>>(emptyList())
    val generatedImages: StateFlow<List<GeneratedImageItem>> = _generatedImages.asStateFlow()

    private val _isGeneratingImage = MutableStateFlow(false)
    val isGeneratingImage: StateFlow<Boolean> = _isGeneratingImage.asStateFlow()

    private val _selectedImageResolution = MutableStateFlow("1K") // "1K", "2K", "4K"
    val selectedImageResolution: StateFlow<String> = _selectedImageResolution.asStateFlow()

    init {
        setupVoiceListener()
        refreshContextAndSuggestions()
    }

    private fun setupVoiceListener() {
        voiceInputManager.listener = object : SpeechRecognitionListener {
            override fun onPartialResult(text: String, confidence: Float) {}

            override fun onFinalResult(text: String, confidence: Float) {
                if (text.isNotBlank()) {
                    handleVoiceCommand(text)
                }
            }

            override fun onError(errorCode: Int, message: String) {}
            override fun onAudioLevelChanged(level: Float) {}
            override fun onListeningStateChanged(isListening: Boolean) {}
        }
    }

    fun selectTab(tab: JarvisTab) {
        _currentTab.value = tab
    }

    fun refreshContextAndSuggestions() {
        viewModelScope.launch {
            val ctx = contextBuilder.getCurrentContext()
            _userContext.value = ctx
            val activePatterns = userPatterns.value
            _proactiveSuggestions.value = suggester.generateSuggestions(ctx, activePatterns)
        }
    }

    /**
     * Complete 7-layer unified pipeline execution
     */
    fun handleVoiceCommand(commandText: String) {
        viewModelScope.launch {
            val history = recentCommands.value
            val ctx = contextBuilder.getCurrentContext()

            // Layer 2: NLP Pipeline
            val nlpResult = nlpEngine.processCommand(commandText, history)

            // Layer 3: Memory & Entity Resolution
            val entityRecord = nlpResult.entities.contact?.let { memoryManager.resolveEntity(it) }
                ?: nlpResult.entities.app?.let { memoryManager.resolveEntity(it) }

            // Layer 4: Action Planning & Workflow Decomposition
            val actionPlan = taskDecomposer.planWorkflow(nlpResult, entityRecord, ctx)
            _currentActionPlan.value = actionPlan

            if (actionPlan.requiresConfirmation) {
                _pendingConfirmation.value = actionPlan
            } else {
                executeActionPlan(actionPlan, nlpResult)
            }
        }
    }

    fun confirmPendingPlan(approved: Boolean) {
        val plan = _pendingConfirmation.value
        _pendingConfirmation.value = null
        if (approved && plan != null) {
            val dummyNlp = nlpEngine.processCommand(plan.originalText, recentCommands.value)
            executeActionPlan(plan, dummyNlp)
        }
    }

    private fun executeActionPlan(actionPlan: ActionPlan, nlpResult: NLPResult) {
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()

            // Layer 5: Execution Engine
            val success = executionEngine.executePlan(actionPlan)
            val duration = System.currentTimeMillis() - startTime

            // Layer 3: Store in Memory
            val memoryItem = MemoryItem(
                timestamp = System.currentTimeMillis(),
                commandText = actionPlan.originalText,
                nlpResult = nlpResult,
                executedActions = actionPlan.tasks.map { it.name },
                success = success
            )
            memoryManager.recordShortTerm(memoryItem)

            // Database persistence
            val record = CommandRecord(
                originalText = actionPlan.originalText,
                parsedIntent = actionPlan.originalIntent.name,
                extractedEntities = "${nlpResult.entities.contact ?: ""} ${nlpResult.entities.app ?: ""} ${nlpResult.entities.settingName ?: ""}".trim(),
                actionsExecuted = actionPlan.tasks.joinToString(" -> ") { it.name },
                duration = duration,
                success = success
            )
            repository.recordCommand(record)

            // Layer 6: Pattern Learning
            val updatedHistory = recentCommands.value
            patternEngine.analyzeHistoryAndLearn(updatedHistory)

            // Provide Voice/TTS feedback
            val feedbackSpeech = if (success) {
                "${actionPlan.tasks.firstOrNull()?.name ?: "Action"} completed, Sir."
            } else {
                "Workflow encountered an interruption."
            }
            geminiClient.speakText(feedbackSpeech)

            // Refresh suggestions
            refreshContextAndSuggestions()
        }
    }

    fun togglePatternEnabled(pattern: PatternRecord) {
        viewModelScope.launch {
            repository.setPatternEnabled(pattern.id, !pattern.enabled)
        }
    }

    fun togglePatternAutoExecute(pattern: PatternRecord) {
        viewModelScope.launch {
            repository.setPatternAutoExecute(pattern.id, !pattern.autoExecute)
        }
    }

    fun deletePattern(pattern: PatternRecord) {
        viewModelScope.launch {
            repository.deletePattern(pattern)
        }
    }

    // Chatbot functionality
    fun setChatModel(model: String) {
        _selectedChatModel.value = model
    }

    fun sendChatMessage(userText: String) {
        if (userText.isBlank() || _isChatThinking.value) return

        val userMessage = ChatMessage(role = "user", text = userText)
        val updated = _chatMessages.value + userMessage
        _chatMessages.value = updated
        _isChatThinking.value = true

        viewModelScope.launch {
            val responseResult = geminiClient.generateChatResponse(
                conversationHistory = updated,
                modelName = _selectedChatModel.value
            )

            _isChatThinking.value = false
            responseResult.onSuccess { reply ->
                val assistantMsg = ChatMessage(role = "model", text = reply)
                _chatMessages.value = _chatMessages.value + assistantMsg
                // Optional voice readback of response
                geminiClient.speakText(reply.take(150))
            }.onFailure { err ->
                val errorMsg = ChatMessage(role = "model", text = "Error accessing neural node: ${err.message ?: "Network timeout"}")
                _chatMessages.value = _chatMessages.value + errorMsg
            }
        }
    }

    fun speakChatMessage(text: String) {
        viewModelScope.launch {
            geminiClient.speakText(text)
        }
    }

    // Image Studio functionality
    fun setImageResolution(resolution: String) {
        _selectedImageResolution.value = resolution
    }

    fun generateImage(prompt: String, aspectRatio: String = "1:1") {
        if (prompt.isBlank() || _isGeneratingImage.value) return
        _isGeneratingImage.value = true

        viewModelScope.launch {
            val result = geminiClient.generateImage(
                prompt = prompt,
                imageSize = _selectedImageResolution.value,
                aspectRatio = aspectRatio
            )
            _isGeneratingImage.value = false

            result.onSuccess { bmp ->
                val item = GeneratedImageItem(
                    prompt = prompt,
                    resolution = _selectedImageResolution.value,
                    bitmap = bmp
                )
                _generatedImages.value = listOf(item) + _generatedImages.value
                geminiClient.speakText("Asset rendering complete, Sir.")
            }.onFailure { err ->
                geminiClient.speakText("Image rendering encountered an issue: ${err.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceInputManager.destroy()
        geminiClient.destroy()
    }
}
