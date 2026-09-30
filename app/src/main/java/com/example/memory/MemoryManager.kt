package com.example.memory

import com.example.data.model.EntityRecord
import com.example.data.repository.JarvisRepository
import com.example.nlp.NLPResult

data class MemoryItem(
    val timestamp: Long,
    val commandText: String,
    val nlpResult: NLPResult,
    val executedActions: List<String>,
    val success: Boolean
)

class MemoryManager(private val repository: JarvisRepository) {

    private val shortTermMemory = ArrayDeque<MemoryItem>(10)
    private var sessionStartTime = System.currentTimeMillis()

    fun recordShortTerm(item: MemoryItem) {
        if (shortTermMemory.size >= 10) {
            shortTermMemory.removeFirst()
        }
        shortTermMemory.addLast(item)
    }

    fun getLastCommand(): MemoryItem? = shortTermMemory.lastOrNull()

    fun getShortTermHistory(): List<MemoryItem> = shortTermMemory.toList()

    suspend fun resolveEntity(name: String): EntityRecord? {
        val exact = repository.findEntityByName(name)
        if (exact != null) {
            repository.markEntityAccessed(exact.id)
            return exact
        }
        return null
    }

    fun clearSession() {
        shortTermMemory.clear()
        sessionStartTime = System.currentTimeMillis()
    }
}
