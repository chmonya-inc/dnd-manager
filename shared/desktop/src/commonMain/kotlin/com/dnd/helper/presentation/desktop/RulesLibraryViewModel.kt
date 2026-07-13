package com.dnd.helper.presentation.desktop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnd.helper.data.remote.DndApiDataSource
import com.dnd.helper.data.remote.dto.character.AbilityScoreDto
import com.dnd.helper.data.remote.dto.character.AlignmentDto
import com.dnd.helper.data.remote.dto.character.BackgroundDto
import com.dnd.helper.data.remote.dto.character.ClassDto
import com.dnd.helper.data.remote.dto.character.DndSkillDto
import com.dnd.helper.data.remote.dto.character.FeatDto
import com.dnd.helper.data.remote.dto.character.FeatureDto
import com.dnd.helper.data.remote.dto.character.LanguageDto
import com.dnd.helper.data.remote.dto.character.ProficiencyDto
import com.dnd.helper.data.remote.dto.character.RaceDto
import com.dnd.helper.data.remote.dto.character.SubclassDto
import com.dnd.helper.data.remote.dto.character.SubraceDto
import com.dnd.helper.data.remote.dto.character.TraitDto
import com.dnd.helper.data.remote.dto.equipment.EquipmentCategoryDto
import com.dnd.helper.data.remote.dto.equipment.MagicItemDto
import com.dnd.helper.data.remote.dto.equipment.WeaponPropertyDto
import com.dnd.helper.data.remote.dto.game.ConditionDto
import com.dnd.helper.data.remote.dto.game.DamageTypeDto
import com.dnd.helper.data.remote.dto.game.RuleDto
import com.dnd.helper.data.remote.dto.game.RuleSectionDto
import com.dnd.helper.data.remote.dto.monster.MonsterDto
import com.dnd.helper.data.remote.dto.spell.MagicSchoolDto
import com.dnd.helper.data.remote.dto.spell.SpellDto
import com.dnd.helper.domain.common.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class RuleCategory {
    CharacterData, Spells, Equipment, Monsters, Mechanics, Rules
}

data class RulesLibraryState(
    val selectedCategory: RuleCategory = RuleCategory.CharacterData,
    val searchQuery: String = "",
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
    // Spells
    val spells: List<SpellDto> = emptyList(),
    val magicSchools: List<MagicSchoolDto> = emptyList(),

    // Equipment
    val equipmentCategories: List<EquipmentCategoryDto> = emptyList(),
    val magicItems: List<MagicItemDto> = emptyList(),
    val weaponProperties: List<WeaponPropertyDto> = emptyList(),

    // Monsters
    val monsters: List<MonsterDto> = emptyList(),

    // Mechanics
    val conditions: List<ConditionDto> = emptyList(),
    val damageTypes: List<DamageTypeDto> = emptyList(),

    // Rules
    val rules: List<RuleDto> = emptyList(),
    val ruleSections: List<RuleSectionDto> = emptyList(),
)

class RulesLibraryViewModel(
    private val api: DndApiDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

    private val _state = MutableStateFlow(RulesLibraryState())
    val state = _state.asStateFlow()

    init {
        loadData()
    }

    fun setCategory(category: RuleCategory) {
        _state.update { it.copy(selectedCategory = category) }
    }

    fun setSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    fun reload() {
        api.clearCache()
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val data = withContext(ioDispatcher) {
                // Character Data
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

                // Spells
                val spellsAsync = async { fetchAll({ api.getSpells() }, { api.getSpell(it) }) }
                val magicSchoolsAsync = async { fetchAll({ api.getMagicSchools() }, { api.getMagicSchool(it) }) }

                // Equipment
                val equipmentCategoriesAsync = async {
                    fetchAll(
                        { api.getEquipmentCategories() },
                        { api.getEquipmentCategory(it) }
                    )
                }
                val magicItemsAsync = async { fetchAll({ api.getMagicItems() }, { api.getMagicItem(it) }) }
                val weaponPropertiesAsync =
                    async { fetchAll({ api.getWeaponProperties() }, { api.getWeaponProperty(it) }) }

                // Monsters
                val monstersAsync = async { fetchAll({ api.getMonsters() }, { api.getMonster(it) }) }

                // Mechanics
                val conditionsAsync = async { fetchAll({ api.getConditions() }, { api.getCondition(it) }) }
                val damageTypesAsync = async { fetchAll({ api.getDamageTypes() }, { api.getDamageType(it) }) }

                // Rules
                val rulesAsync = async { fetchAll({ api.getRules() }, { api.getRule(it) }) }
                val ruleSectionsAsync = async { fetchAll({ api.getRuleSections() }, { api.getRuleSection(it) }) }

                RulesLibraryState(
                    selectedCategory = _state.value.selectedCategory,
                    searchQuery = _state.value.searchQuery,
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
                    spells = spellsAsync.await(),
                    magicSchools = magicSchoolsAsync.await(),
                    equipmentCategories = equipmentCategoriesAsync.await(),
                    magicItems = magicItemsAsync.await(),
                    weaponProperties = weaponPropertiesAsync.await(),
                    monsters = monstersAsync.await(),
                    conditions = conditionsAsync.await(),
                    damageTypes = damageTypesAsync.await(),
                    rules = rulesAsync.await(),
                    ruleSections = ruleSectionsAsync.await(),
                    isLoading = false
                )
            }

            _state.value = data
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
