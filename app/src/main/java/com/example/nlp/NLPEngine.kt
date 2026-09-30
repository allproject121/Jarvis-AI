package com.example.nlp

import com.example.data.model.CommandRecord

data class NLPResult(
    val originalText: String,
    val normalizedText: String,
    val intent: JarvisIntent,
    val confidence: Float,
    val complexity: CommandComplexity,
    val entities: ExtractedEntities,
    val implicitRoutine: String? = null,
    val isValid: Boolean = true
)

class NLPEngine {
    private val preprocessor = TextPreprocessor()
    private val entityExtractor = EntityExtractor()
    private val intentClassifier = IntentClassifier()
    private val contextEngine = ContextualUnderstandingEngine()

    fun processCommand(
        commandText: String,
        recentHistory: List<CommandRecord> = emptyList()
    ): NLPResult {
        val preprocessed = preprocessor.preprocess(commandText)
        val rawEntities = entityExtractor.extract(preprocessed)
        val resolvedEntities = contextEngine.resolvePronouns(commandText, rawEntities, recentHistory)
        val classification = intentClassifier.classify(preprocessed, resolvedEntities)
        val implicit = contextEngine.inferImplicitRoutine(commandText)

        return NLPResult(
            originalText = commandText,
            normalizedText = preprocessed.normalized,
            intent = classification.intent,
            confidence = classification.confidence,
            complexity = classification.complexity,
            entities = resolvedEntities,
            implicitRoutine = implicit,
            isValid = classification.confidence > 0.75f && classification.intent != JarvisIntent.UNKNOWN
        )
    }
}
