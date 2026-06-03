package com.dnd.helper.presentation.monstercreate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnd.helper.domain.common.Result
import com.dnd.helper.domain.model.Monster
import com.dnd.helper.domain.model.CharacterStats
import com.dnd.helper.domain.repository.CharacterRepository
import com.dnd.helper.domain.repository.EditingRepository
import com.dnd.helper.domain.repository.GenerationStatus
import com.dnd.helper.data.remote.DndApiDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class MonsterCreateViewModel(
    private val repository: CharacterRepository,
    private val editingRepository: EditingRepository,
    private val api: DndApiDataSource,
) : ViewModel() {

    private val tempId = "temp-monster-${Random.nextInt(1000000, 9999999)}"
    private val _state = MutableStateFlow(MonsterCreateState())
    val state: StateFlow<MonsterCreateState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            editingRepository.activeTasks.collect { tasks ->
                val myTasks = tasks.filter { it.entityId == tempId }
                myTasks.forEach { task ->
                    if (task.status == GenerationStatus.COMPLETED && task.resultUrl != null) {
                        _state.update { currentState ->
                            if (task.entityType == "monster" && currentState.imageUrl == "generating:${task.id}") {
                                currentState.copy(imageUrl = task.resultUrl)
                            } else currentState
                        }
                    } else if (task.status == GenerationStatus.FAILED) {
                        _state.update { currentState ->
                            if (task.entityType == "monster" && currentState.imageUrl == "generating:${task.id}") {
                                currentState.copy(imageUrl = "")
                            } else currentState
                        }
                    }
                }
            }
        }

        viewModelScope.launch {
            when (val res = api.getAlignments()) {
                is Result.Success -> _state.update { it.copy(availableAlignments = res.data.results) }
                else -> {}
            }
            when (val res = api.getLanguages()) {
                is Result.Success -> _state.update { it.copy(availableLanguages = res.data.results) }
                else -> {}
            }
            when (val res = api.getConditions()) {
                is Result.Success -> _state.update { it.copy(availableConditions = res.data.results) }
                else -> {}
            }
            when (val res = api.getDamageTypes()) {
                is Result.Success -> _state.update { it.copy(availableDamageTypes = res.data.results) }
                else -> {}
            }
        }
    }

    fun onEvent(event: MonsterCreateEvent) {
        when (event) {
            is MonsterCreateEvent.NameChanged -> {
                _state.value = _state.value.copy(name = event.value)
                updateDefaultPrompt()
            }
            is MonsterCreateEvent.DescriptionChanged -> {
                _state.value = _state.value.copy(description = event.value)
                updateDefaultPrompt()
            }
            is MonsterCreateEvent.ChallengeRatingChanged -> _state.value = _state.value.copy(challengeRating = event.value)
            is MonsterCreateEvent.TypeChanged -> {
                _state.value = _state.value.copy(type = event.value)
                updateDefaultPrompt()
            }
            is MonsterCreateEvent.AlignmentChanged -> _state.value = _state.value.copy(alignment = event.value)
            is MonsterCreateEvent.SizeChanged -> _state.value = _state.value.copy(size = event.value)
            is MonsterCreateEvent.MaxHpChanged -> _state.value = _state.value.copy(maxHp = event.value)
            is MonsterCreateEvent.ArmorClassChanged -> _state.value = _state.value.copy(armorClass = event.value)
            is MonsterCreateEvent.SpeedChanged -> _state.value = _state.value.copy(speed = event.value)
            
            is MonsterCreateEvent.StrengthChanged -> _state.value = _state.value.copy(strength = event.value)
            is MonsterCreateEvent.DexterityChanged -> _state.value = _state.value.copy(dexterity = event.value)
            is MonsterCreateEvent.ConstitutionChanged -> _state.value = _state.value.copy(constitution = event.value)
            is MonsterCreateEvent.IntelligenceChanged -> _state.value = _state.value.copy(intelligence = event.value)
            is MonsterCreateEvent.WisdomChanged -> _state.value = _state.value.copy(wisdom = event.value)
            is MonsterCreateEvent.CharismaChanged -> _state.value = _state.value.copy(charisma = event.value)

            is MonsterCreateEvent.AddLanguage -> _state.update { it.copy(selectedLanguages = it.selectedLanguages + event.item) }
            is MonsterCreateEvent.RemoveLanguage -> _state.update { it.copy(selectedLanguages = it.selectedLanguages - event.item) }
            
            is MonsterCreateEvent.AddSpecialAbility -> _state.update { it.copy(specialAbilities = it.specialAbilities + event.action) }
            is MonsterCreateEvent.RemoveSpecialAbility -> _state.update { it.copy(specialAbilities = it.specialAbilities - event.action) }
            
            is MonsterCreateEvent.AddAction -> _state.update { it.copy(actions = it.actions + event.action) }
            is MonsterCreateEvent.RemoveAction -> _state.update { it.copy(actions = it.actions - event.action) }
            
            is MonsterCreateEvent.AddLegendaryAction -> _state.update { it.copy(legendaryActions = it.legendaryActions + event.action) }
            is MonsterCreateEvent.RemoveLegendaryAction -> _state.update { it.copy(legendaryActions = it.legendaryActions - event.action) }
            
            is MonsterCreateEvent.AddReaction -> _state.update { it.copy(reactions = it.reactions + event.action) }
            is MonsterCreateEvent.RemoveReaction -> _state.update { it.copy(reactions = it.reactions - event.action) }
            
            is MonsterCreateEvent.AddConditionImmunity -> _state.value = _state.value.copy(selectedConditionImmunities = _state.value.selectedConditionImmunities + event.value)
            is MonsterCreateEvent.RemoveConditionImmunity -> _state.value = _state.value.copy(selectedConditionImmunities = _state.value.selectedConditionImmunities - event.value)
            
            is MonsterCreateEvent.AddDamageImmunity -> _state.value = _state.value.copy(selectedDamageImmunities = _state.value.selectedDamageImmunities + event.value)
            is MonsterCreateEvent.RemoveDamageImmunity -> _state.value = _state.value.copy(selectedDamageImmunities = _state.value.selectedDamageImmunities - event.value)
            
            is MonsterCreateEvent.AddDamageResistance -> _state.value = _state.value.copy(selectedDamageResistances = _state.value.selectedDamageResistances + event.value)
            is MonsterCreateEvent.RemoveDamageResistance -> _state.value = _state.value.copy(selectedDamageResistances = _state.value.selectedDamageResistances - event.value)
            
            is MonsterCreateEvent.AddDamageVulnerability -> _state.value = _state.value.copy(selectedDamageVulnerabilities = _state.value.selectedDamageVulnerabilities + event.value)
            is MonsterCreateEvent.RemoveDamageVulnerability -> _state.value = _state.value.copy(selectedDamageVulnerabilities = _state.value.selectedDamageVulnerabilities - event.value)

            is MonsterCreateEvent.ImageUrlChanged -> _state.value = _state.value.copy(imageUrl = event.value)
            is MonsterCreateEvent.AiSizeChanged -> _state.value = _state.value.copy(aiWidth = event.width, aiHeight = event.height)
            is MonsterCreateEvent.AiPromptChanged -> _state.value = _state.value.copy(aiPrompt = event.value)
            
            is MonsterCreateEvent.GenerateImage -> generateImage()
            is MonsterCreateEvent.SaveMonster -> saveMonster()
        }
    }

    private fun updateDefaultPrompt() {
        val s = _state.value
        val promptText = "${s.name}, ${s.size} ${s.type}, ${s.alignment}. ${s.description}".trim()
        if (promptText.isNotBlank()) {
            _state.value = _state.value.copy(aiPrompt = promptText)
        }
    }

    private fun generateImage() {
        if (_state.value.aiPrompt.isBlank()) return
        
        viewModelScope.launch {
            val taskId = editingRepository.startGeneration(
                entityId = tempId,
                entityType = "monster",
                prompt = _state.value.aiPrompt,
                genType = com.dnd.helper.data.remote.GenerationType.MONSTER,
                width = _state.value.aiWidth,
                height = _state.value.aiHeight
            )
            _state.value = _state.value.copy(imageUrl = taskId)
        }
    }

    private fun saveMonster() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, error = null)
            val s = _state.value
            
            val monster = Monster(
                id = tempId,
                name = s.name.ifBlank { "Unknown Monster" },
                description = s.description,
                imageUrl = s.imageUrl.takeIf { !it.startsWith("generating:") && it.isNotBlank() },
                stats = CharacterStats(
                    strength = s.strength.toIntOrNull() ?: 10,
                    dexterity = s.dexterity.toIntOrNull() ?: 10,
                    constitution = s.constitution.toIntOrNull() ?: 10,
                    intelligence = s.intelligence.toIntOrNull() ?: 10,
                    wisdom = s.wisdom.toIntOrNull() ?: 10,
                    charisma = s.charisma.toIntOrNull() ?: 10
                ),
                maxHp = s.maxHp.toIntOrNull() ?: 10,
                currentHp = s.maxHp.toIntOrNull() ?: 10,
                armorClass = s.armorClass.toIntOrNull() ?: 10,
                speed = s.speed.toIntOrNull() ?: 30,
                challengeRating = s.challengeRating.ifBlank { "1" },
                type = s.type.ifBlank { "Humanoid" },
                alignment = s.alignment.ifBlank { "Neutral" },
                size = s.size.ifBlank { "Medium" },
                conditionImmunities = s.selectedConditionImmunities,
                damageImmunities = s.selectedDamageImmunities,
                damageResistances = s.selectedDamageResistances,
                damageVulnerabilities = s.selectedDamageVulnerabilities,
                languages = s.selectedLanguages,
                specialAbilities = s.specialAbilities,
                actions = s.actions,
                legendaryActions = s.legendaryActions,
                reactions = s.reactions
            )

            when (val res = repository.saveMonster(monster)) {
                is Result.Success -> _state.value = _state.value.copy(isSaving = false, isSaved = true)
                is Result.Error -> _state.value = _state.value.copy(isSaving = false, error = "Failed to save monster")
            }
        }
    }
}
