package com.dnd.helper.data.repository

import com.dnd.helper.data.remote.AiImageService
import com.dnd.helper.data.remote.GenerationType
import com.dnd.helper.domain.common.Result
import com.dnd.helper.domain.repository.CharacterRepository
import com.dnd.helper.domain.repository.EditingRepository
import com.dnd.helper.domain.repository.GenerationStatus
import com.dnd.helper.domain.repository.GenerationTask
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class EditingRepositoryImpl(
    private val aiService: AiImageService,
    private val characterRepository: CharacterRepository
) : EditingRepository {

    private val scope =
        CoroutineScope(
            SupervisorJob() + Dispatchers.Default + CoroutineExceptionHandler { context, throwable ->
                println(throwable)
            }
        )

    private val _activeTasks = MutableStateFlow<List<GenerationTask>>(emptyList())
    override val activeTasks: StateFlow<List<GenerationTask>> = _activeTasks.asStateFlow()

    private val activeJobs = mutableMapOf<String, Job>()

    override fun startGeneration(
        entityId: String,
        entityType: String,
        prompt: String,
        genType: GenerationType,
        width: Int,
        height: Int
    ): String {
        val taskId = Random.nextLong(1000000, 9999999).toString()
        val mockUrl = "generating:$taskId"

        val task = GenerationTask(
            id = taskId,
            entityId = entityId,
            entityType = entityType,
            prompt = prompt,
            status = GenerationStatus.PENDING
        )

        _activeTasks.value = _activeTasks.value + task

        val job = scope.launch {
            println("[EditingRepo] Starting generation for $entityType $entityId (Task: $taskId)")
            updateTaskStatus(taskId, GenerationStatus.GENERATING)

            val url = try {
                aiService.generateImage(
                    promptText = prompt,
                    type = genType,
                    customWidth = width,
                    customHeight = height,
                    onRemoteIdGenerated = { remoteId ->
                        println("[EditingRepo] Received remote ID for task $taskId: $remoteId")
                        _activeTasks.value = _activeTasks.value.map {
                            if (it.id == taskId) it.copy(remoteId = remoteId) else it
                        }
                    }
                )
            } catch (e: CancellationException) {
                println("[EditingRepo] Generation cancelled for task $taskId")
                throw e
            } catch (e: Exception) {
                println("[EditingRepo] AI Service failed: ${e.message}")
                null
            }

            if (url != null) {
                println("[EditingRepo] Generation successful: $url")
                updateTaskStatus(taskId, GenerationStatus.COMPLETED, url)
                // Retry saving a few times in case character is being created
                var success = false
                repeat(5) { attempt ->
                    if (!success) {
                        println(
                            "[EditingRepo] Attempting to save URL to $entityType $entityId (attempt ${attempt + 1})"
                        )
                        success = try {
                            saveGeneratedUrl(entityId, entityType, url)
                        } catch (e: Exception) {
                            println("[EditingRepo] Save failed with exception: ${e.message}")
                            false
                        }
                        if (!success) {
                            delay(2000)
                        }
                    }
                }
            } else {
                println("[EditingRepo] Generation failed.")
                updateTaskStatus(taskId, GenerationStatus.FAILED)
                // Clear mock URL from entity so it doesn't show loading forever
                saveGeneratedUrl(entityId, entityType, "")
            }

            // Auto-cleanup task after 30 seconds to keep flow clean
//            delay(30_000)
//            _activeTasks.value = _activeTasks.value.filter { it.id != taskId }
        }

        activeJobs[taskId] = job
        job.invokeOnCompletion {
            activeJobs.remove(taskId)
        }

        return mockUrl
    }

    override fun cancelGeneration(taskId: String) {
        activeJobs[taskId]?.cancel()
        activeJobs.remove(taskId)
        _activeTasks.value = _activeTasks.value.filter { it.id != taskId }
    }

    override fun cancelGenerationForEntity(entityId: String) {
        val tasks = _activeTasks.value.filter {
            it.entityId == entityId || it.entityId.startsWith("$entityId:")
        }
        tasks.forEach { cancelGeneration(it.id) }
    }

    private fun updateTaskStatus(
        taskId: String,
        status: GenerationStatus,
        resultUrl: String? = null
    ) {
        _activeTasks.value = _activeTasks.value.map {
            if (it.id == taskId) it.copy(status = status, resultUrl = resultUrl) else it
        }
    }

    private suspend fun saveGeneratedUrl(
        entityId: String,
        entityType: String,
        url: String
    ): Boolean {
        return when (entityType.lowercase()) {
            "character" -> {
                val result = characterRepository.getCharacter(entityId)
                if (result is Result.Success) {
                    val saveResult =
                        characterRepository.saveCharacter(result.data.copy(imageUrl = url))
                    saveResult is Result.Success
                } else {
                    false
                }
            }

            "npc" -> {
                val result = characterRepository.getNpcs()
                if (result is Result.Success) {
                    val entity = result.data.find { it.id == entityId }
                    if (entity != null) {
                        val saveResult = characterRepository.saveNpc(entity.copy(imageUrl = url))
                        saveResult is Result.Success
                    } else {
                        false
                    }
                } else {
                    false
                }
            }

            "monster" -> {
                val result = characterRepository.getMonsters()
                if (result is Result.Success) {
                    val entity = result.data.find { it.id == entityId }
                    if (entity != null) {
                        val saveResult =
                            characterRepository.saveMonster(entity.copy(imageUrl = url))
                        saveResult is Result.Success
                    } else {
                        false
                    }
                } else {
                    false
                }
            }

            "location" -> {
                val result = characterRepository.getLocations()
                if (result is Result.Success) {
                    val entity = result.data.find { it.id == entityId }
                    if (entity != null) {
                        val saveResult =
                            characterRepository.saveLocation(entity.copy(imageUrl = url))
                        saveResult is Result.Success
                    } else {
                        false
                    }
                } else {
                    false
                }
            }

            "battlefield" -> {
                val result = characterRepository.getBattlefields()
                if (result is Result.Success) {
                    val entity = result.data.find { it.id == entityId }
                    if (entity != null) {
                        val saveResult =
                            characterRepository.saveBattlefield(entity.copy(imageUrl = url))
                        saveResult is Result.Success
                    } else {
                        false
                    }
                } else {
                    false
                }
            }

            "item" -> {
                // Item is nested in Character. entityId should be "charId:itemId"
                val parts = entityId.split(":")
                if (parts.size == 2) {
                    val charId = parts[0]
                    val itemId = parts[1]
                    val result = characterRepository.getCharacter(charId)
                    if (result is Result.Success) {
                        val char = result.data
                        if (char.items.any { it.id == itemId }) {
                            val updatedItems = char.items.map {
                                if (it.id == itemId) it.copy(imageUrl = url) else it
                            }
                            val saveResult =
                                characterRepository.saveCharacter(char.copy(items = updatedItems))
                            saveResult is Result.Success
                        } else {
                            false
                        }
                    } else {
                        false
                    }
                } else {
                    false
                }
            }

            "spell" -> {
                // Spell is nested in Character. entityId should be "charId:spellId"
                val parts = entityId.split(":")
                if (parts.size == 2) {
                    val charId = parts[0]
                    val spellId = parts[1]
                    val result = characterRepository.getCharacter(charId)
                    if (result is Result.Success) {
                        val char = result.data
                        if (char.spells.any { it.id == spellId }) {
                            val updatedSpells = char.spells.map {
                                if (it.id == spellId) it.copy(iconUrl = url) else it
                            }
                            val saveResult =
                                characterRepository.saveCharacter(char.copy(spells = updatedSpells))
                            saveResult is Result.Success
                        } else {
                            false
                        }
                    } else {
                        false
                    }
                } else {
                    false
                }
            }

            else -> false
        }
    }
}
