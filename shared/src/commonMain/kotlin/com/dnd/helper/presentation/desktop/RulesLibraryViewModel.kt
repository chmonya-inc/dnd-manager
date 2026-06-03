package com.dnd.helper.presentation.desktop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnd.helper.data.remote.DndApiDataSource
import com.dnd.helper.data.remote.dto.character.*
import com.dnd.helper.domain.common.Result
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class RuleCategory {
    CharacterData, Spells, Equipment, Monsters, Mechanics, Rules
}

data class RulesLibraryState(
    val selectedCategory: RuleCategory = RuleCategory.CharacterData,
    val isLoading: Boolean = false,
    
    // Character Data
    val abilityScores: List<AbilityScoreDto> = emptyList(),
    val alignments: List<AlignmentDto> = emptyList(),
    val backgrounds: List<BackgroundDto> = emptyList(),
    val classes: List<ClassDto> = emptyList(),
    val races: List<RaceDto> = emptyList(),
    val subraces: List<SubraceDto> = emptyList(),
    val subclasses: List<SubclassDto> = emptyList(),
    val traits: List<TraitDto> = emptyList(),
    val features: List<FeatureDto> = emptyList(),
    val feats: List<FeatDto> = emptyList(),
    val skills: List<DndSkillDto> = emptyList(),
    val proficiencies: List<ProficiencyDto> = emptyList(),
    val languages: List<LanguageDto> = emptyList(),
)

class RulesLibraryViewModel(
    private val api: DndApiDataSource
) : ViewModel() {

    private val _state = MutableStateFlow(RulesLibraryState())
    val state = _state.asStateFlow()

    init {
        loadCharacterData()
    }

    fun setCategory(category: RuleCategory) {
        _state.update { it.copy(selectedCategory = category) }
    }

    private fun loadCharacterData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            // Launch all categories concurrently
            val abilityScoresAsync = async { fetchAll({ api.getAbilityScores() }, { api.getAbilityScore(it) }) }
            val alignmentsAsync = async { fetchAll({ api.getAlignments() }, { api.getAlignment(it) }) }
            val backgroundsAsync = async { fetchAll({ api.getBackgrounds() }, { api.getBackground(it) }) }
            val classesAsync = async { fetchAll({ api.getClasses() }, { api.getClass(it) }) }
            val racesAsync = async { fetchAll({ api.getRaces() }, { api.getRace(it) }) }
            val subracesAsync = async { fetchAll({ api.getSubraces() }, { api.getSubrace(it) }) }
            val subclassesAsync = async { fetchAll({ api.getSubclasses() }, { api.getSubclass(it) }) }
            val traitsAsync = async { fetchAll({ api.getTraits() }, { api.getTrait(it) }) }
            val featuresAsync = async { fetchAll({ api.getFeatures() }, { api.getFeature(it) }) }
            val featsAsync = async { fetchAll({ api.getFeats() }, { api.getFeat(it) }) }
            val skillsAsync = async { fetchAll({ api.getSkills() }, { api.getSkill(it) }) }
            val proficienciesAsync = async { fetchAll({ api.getProficiencies() }, { api.getProficiency(it) }) }
            val languagesAsync = async { fetchAll({ api.getLanguages() }, { api.getLanguage(it) }) }

            _state.update { it.copy(
                abilityScores = abilityScoresAsync.await(),
                alignments = alignmentsAsync.await(),
                backgrounds = backgroundsAsync.await(),
                classes = classesAsync.await(),
                races = racesAsync.await(),
                subraces = subracesAsync.await(),
                subclasses = subclassesAsync.await(),
                traits = traitsAsync.await(),
                features = featuresAsync.await(),
                feats = featsAsync.await(),
                skills = skillsAsync.await(),
                proficiencies = proficienciesAsync.await(),
                languages = languagesAsync.await(),
                isLoading = false
            ) }
        }
    }

    private suspend fun <T> fetchAll(
        getList: suspend () -> Result<com.dnd.helper.data.remote.dto.common.ApiReferenceListDto>,
        getDetail: suspend (String) -> Result<T>
    ): List<T> = kotlinx.coroutines.coroutineScope {
        val listResult = getList()
        if (listResult is Result.Success) {
            val deferreds = listResult.data.results.map { ref ->
                async { getDetail(ref.index) }
            }
            deferreds.awaitAll().mapNotNull { if (it is Result.Success) it.data else null }
        } else {
            emptyList()
        }
    }
}
