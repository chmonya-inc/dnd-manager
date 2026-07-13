package com.dnd.helper.fakes

import com.dnd.helper.data.remote.GenerationType
import com.dnd.helper.domain.repository.EditingRepository
import com.dnd.helper.domain.repository.GenerationStatus
import com.dnd.helper.domain.repository.GenerationTask
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeEditingRepository : EditingRepository {
    private val _activeTasks = MutableStateFlow<List<GenerationTask>>(emptyList())
    override val activeTasks: StateFlow<List<GenerationTask>> = _activeTasks.asStateFlow()

    var lastStartedEntityType: String? = null
    var lastPrompt: String? = null
    var cancelGenerationCalled = false
    var cancelledTaskIds = mutableListOf<String>()
    var cancelledEntityIds = mutableListOf<String>()

    override fun startGeneration(
        entityId: String,
        entityType: String,
        prompt: String,
        genType: GenerationType,
        width: Int,
        height: Int
    ): String {
        lastStartedEntityType = entityType
        lastPrompt = prompt
        val taskId = "task-${kotlin.random.Random.nextInt()}"
        val task = GenerationTask(
            id = taskId,
            remoteId = null,
            entityId = entityId,
            entityType = entityType,
            prompt = prompt,
            status = GenerationStatus.PENDING,
            resultUrl = null
        )
        _activeTasks.value = _activeTasks.value + task
        return "generating:$taskId"
    }

    override fun cancelGeneration(taskId: String) {
        cancelGenerationCalled = true
        cancelledTaskIds.add(taskId)
        _activeTasks.value = _activeTasks.value.filter { it.id != taskId }
    }

    override fun cancelGenerationForEntity(entityId: String) {
        cancelledEntityIds.add(entityId)
        _activeTasks.value = _activeTasks.value.filter { !it.entityId.startsWith(entityId) }
    }

    // Helper method for testing - simulate task completion
    fun completeTask(taskId: String, resultUrl: String) {
        _activeTasks.value = _activeTasks.value.map { task ->
            if (task.id == taskId) {
                task.copy(status = GenerationStatus.COMPLETED, resultUrl = resultUrl)
            } else {
                task
            }
        }
    }

    // Helper method for testing - add a task directly
    fun addTask(task: GenerationTask) {
        _activeTasks.value = _activeTasks.value + task
    }

    fun reset() {
        _activeTasks.value = emptyList()
        lastStartedEntityType = null
        lastPrompt = null
        cancelGenerationCalled = false
        cancelledTaskIds.clear()
        cancelledEntityIds.clear()
    }
}
