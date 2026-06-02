package com.dnd.helper.domain.repository

import kotlinx.coroutines.flow.StateFlow
import com.dnd.helper.data.remote.GenerationType

data class GenerationTask(
    val id: String,
    val remoteId: String? = null, // ComfyUI prompt_id
    val entityId: String,
    val entityType: String,
    val prompt: String,
    val status: GenerationStatus,
    val resultUrl: String? = null
)

enum class GenerationStatus {
    PENDING,
    GENERATING,
    COMPLETED,
    FAILED
}

interface EditingRepository {
    val activeTasks: StateFlow<List<GenerationTask>>

    /**
     * Starts background image generation.
     * @return a mock URL like "generating:<task_id>"
     */
    fun startGeneration(
        entityId: String,
        entityType: String,
        prompt: String,
        genType: GenerationType,
        width: Int = 1024,
        height: Int = 1024
    ): String

    /**
     * Cancels a specific generation task by its id.
     */
    fun cancelGeneration(taskId: String)

    /**
     * Cancels all generation tasks for a given entity id.
     * Also matches nested entities (e.g. "charId:itemId" when entityId is "charId").
     */
    fun cancelGenerationForEntity(entityId: String)
}
