package com.dnd.helper.presentation.charactercreate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnd.helper.domain.common.Result
import com.dnd.helper.domain.common.toUserMessage
import com.dnd.helper.domain.model.*
import com.dnd.helper.domain.repository.CharacterRepository
import com.dnd.helper.data.remote.GenerationType
import com.dnd.helper.data.remote.PromptGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.time.Clock

class CharacterCreateViewModel(
    private val repository: CharacterRepository,
    private val editingRepository: com.dnd.helper.domain.repository.EditingRepository,
    private val api: com.dnd.helper.data.remote.DndApiDataSource,
) : ViewModel() {

    private val tempId = "temp-char-${Random.nextInt(1000000, 9999999)}"
    private val _state = MutableStateFlow(CharacterCreateState())
    val state: StateFlow<CharacterCreateState> = _state.asStateFlow()

    init {
        // Listen for background image generation completion
        viewModelScope.launch {
            editingRepository.activeTasks.collect { tasks ->
                val myTasks = tasks.filter { 
                    it.entityId == tempId || it.entityId.startsWith("$tempId:") 
                }
                
                myTasks.forEach { task ->
                    if ((task.status == com.dnd.helper.domain.repository.GenerationStatus.COMPLETED && task.resultUrl != null) ||
                         task.status == com.dnd.helper.domain.repository.GenerationStatus.FAILED) {
                        
                        val resultUrl = if (task.status == com.dnd.helper.domain.repository.GenerationStatus.COMPLETED) task.resultUrl ?: "" else ""
                        
                        _state.update { currentState ->
                            if (task.entityType == "character" && currentState.imageUrl == "generating:${task.id}") {
                                currentState.copy(imageUrl = resultUrl)
                            } else if (task.entityType == "item") {
                                val itemId = task.entityId.substringAfter(":")
                                if (currentState.items.any { it.id == itemId && it.imageUrl == "generating:${task.id}" }) {
                                    val newItems = currentState.items.map { if (it.id == itemId) it.copy(imageUrl = resultUrl) else it }
                                    currentState.copy(items = newItems)
                                } else currentState
                            } else currentState
                        }
                    }
                }
            }
        }

        viewModelScope.launch {
            when (val res = api.getClasses()) {
                is Result.Success -> _state.update { it.copy(availableClasses = res.data.results) }
                else -> {}
            }
            when (val res = api.getRaces()) {
                is Result.Success -> _state.update { it.copy(availableRaces = res.data.results) }
                else -> {}
            }
            when (val res = api.getBackgrounds()) {
                is Result.Success -> _state.update { it.copy(availableBackgrounds = res.data.results) }
                else -> {}
            }
            when (val res = api.getAlignments()) {
                is Result.Success -> _state.update { it.copy(availableAlignments = res.data.results) }
                else -> {}
            }
            when (val res = api.getLanguages()) {
                is Result.Success -> _state.update { it.copy(availableLanguages = res.data.results) }
                else -> {}
            }
            when (val res = api.getSkills()) {
                is Result.Success -> _state.update { it.copy(availableSkills = res.data.results) }
                else -> {}
            }
            when (val res = api.getEquipment()) {
                is Result.Success -> _state.update { it.copy(availableEquipment = res.data.results) }
                else -> {}
            }
            when (val res = api.getFeats()) {
                is Result.Success -> _state.update { it.copy(availableFeats = res.data.results) }
                else -> {}
            }
            when (val res = api.getFeatures()) {
                is Result.Success -> _state.update { it.copy(availableFeatures = res.data.results) }
                else -> {}
            }
            when (val res = api.getTraits()) {
                is Result.Success -> _state.update { it.copy(availableTraits = res.data.results) }
                else -> {}
            }
        }
    }

    fun onEvent(event: CharacterCreateEvent) {
        when (event) {
            // Basic info
            is CharacterCreateEvent.NameChanged -> {
                _state.value = _state.value.copy(name = event.value)
                updateDefaultPrompt()
            }
            is CharacterCreateEvent.PlayerNameChanged -> _state.value = _state.value.copy(playerName = event.value)
            is CharacterCreateEvent.RaceChanged -> {
                _state.value = _state.value.copy(race = event.value, subrace = "")
                updateDefaultPrompt()

                val raceIndex = _state.value.availableRaces.find { it.name == event.value }?.index ?: event.value.lowercase().replace(" ", "-")
                viewModelScope.launch {
                    when (val res = api.getRace(raceIndex)) {
                        is Result.Success -> _state.update { it.copy(availableSubraces = res.data.subraces) }
                        else -> _state.update { it.copy(availableSubraces = emptyList()) }
                    }
                }
            }
            is CharacterCreateEvent.SubraceChanged -> {
                _state.value = _state.value.copy(subrace = event.value)
                updateDefaultPrompt()
            }
            is CharacterCreateEvent.ClassChanged -> {
                _state.value = _state.value.copy(characterClass = event.value, subclass = "")
                updateDefaultPrompt()
                
                // Fetch subclasses for this class
                val classIndex = _state.value.availableClasses.find { it.name == event.value }?.index ?: event.value.lowercase().replace(" ", "-")
                viewModelScope.launch {
                    when (val res = api.getClass(classIndex)) {
                        is Result.Success -> _state.update { it.copy(availableSubclasses = res.data.subclasses) }
                        else -> _state.update { it.copy(availableSubclasses = emptyList()) }
                    }
                }
            }
            is CharacterCreateEvent.SubclassChanged -> {
                _state.value = _state.value.copy(subclass = event.value)
                updateDefaultPrompt()
            }
            is CharacterCreateEvent.BackgroundChanged -> {
                _state.value = _state.value.copy(background = event.value)
                updateDefaultPrompt()
            }
            is CharacterCreateEvent.AlignmentChanged -> {
                _state.value = _state.value.copy(alignment = event.value)
                updateDefaultPrompt()
            }
            is CharacterCreateEvent.LevelChanged -> _state.value = _state.value.copy(level = event.value)
            is CharacterCreateEvent.ExperiencePointsChanged -> _state.value = _state.value.copy(experiencePoints = event.value)
            is CharacterCreateEvent.DescriptionChanged -> {
                _state.value = _state.value.copy(description = event.value)
                updateDefaultPrompt()
            }
            is CharacterCreateEvent.ImageUrlChanged -> _state.value = _state.value.copy(imageUrl = event.value)

            // Appearance
            is CharacterCreateEvent.AgeChanged -> _state.value = _state.value.copy(age = event.value)
            is CharacterCreateEvent.GenderChanged -> _state.value = _state.value.copy(gender = event.value)
            is CharacterCreateEvent.HeightChanged -> _state.value = _state.value.copy(height = event.value)
            is CharacterCreateEvent.WeightChanged -> _state.value = _state.value.copy(weight = event.value)
            is CharacterCreateEvent.EyesChanged -> _state.value = _state.value.copy(eyes = event.value)
            is CharacterCreateEvent.HairChanged -> _state.value = _state.value.copy(hair = event.value)
            is CharacterCreateEvent.SkinChanged -> _state.value = _state.value.copy(skin = event.value)

            // Ability Scores
            is CharacterCreateEvent.StrengthChanged -> _state.value = _state.value.copy(strength = event.value)
            is CharacterCreateEvent.DexterityChanged -> _state.value = _state.value.copy(dexterity = event.value)
            is CharacterCreateEvent.ConstitutionChanged -> _state.value = _state.value.copy(constitution = event.value)
            is CharacterCreateEvent.IntelligenceChanged -> _state.value = _state.value.copy(intelligence = event.value)
            is CharacterCreateEvent.WisdomChanged -> _state.value = _state.value.copy(wisdom = event.value)
            is CharacterCreateEvent.CharismaChanged -> _state.value = _state.value.copy(charisma = event.value)

            // HP & Combat
            is CharacterCreateEvent.MaxHpChanged -> _state.value = _state.value.copy(maxHp = event.value)
            is CharacterCreateEvent.CurrentHpChanged -> _state.value = _state.value.copy(currentHp = event.value)
            is CharacterCreateEvent.TempHpChanged -> _state.value = _state.value.copy(tempHp = event.value)
            is CharacterCreateEvent.ArmorClassChanged -> _state.value = _state.value.copy(armorClass = event.value)
            is CharacterCreateEvent.InitiativeChanged -> _state.value = _state.value.copy(initiative = event.value)
            is CharacterCreateEvent.SpeedChanged -> _state.value = _state.value.copy(speed = event.value)
            is CharacterCreateEvent.ProficiencyBonusChanged -> _state.value = _state.value.copy(proficiencyBonus = event.value)
            is CharacterCreateEvent.HitDiceChanged -> _state.value = _state.value.copy(hitDice = event.value)
            is CharacterCreateEvent.HitDiceCurrentChanged -> _state.value = _state.value.copy(hitDiceCurrent = event.value)

            // Status
            is CharacterCreateEvent.InspirationChanged -> _state.value = _state.value.copy(inspiration = event.value)
            is CharacterCreateEvent.ExhaustionChanged -> _state.value = _state.value.copy(exhaustion = event.value)
            is CharacterCreateEvent.ConditionsChanged -> _state.value = _state.value.copy(conditions = event.value)
            is CharacterCreateEvent.DeathSaveSuccessesChanged -> _state.value = _state.value.copy(deathSaveSuccesses = event.value)
            is CharacterCreateEvent.DeathSaveFailuresChanged -> _state.value = _state.value.copy(deathSaveFailures = event.value)

            // Proficiencies
            is CharacterCreateEvent.SavingThrowsChanged -> _state.value = _state.value.copy(savingThrows = event.value)
            is CharacterCreateEvent.ArmorProficienciesChanged -> {
                _state.value = _state.value.copy(armorProficiencies = event.value)
                updateDefaultPrompt()
            }
            is CharacterCreateEvent.AddLanguage -> _state.value = _state.value.copy(selectedLanguages = _state.value.selectedLanguages + event.value)
            is CharacterCreateEvent.RemoveLanguage -> _state.value = _state.value.copy(selectedLanguages = _state.value.selectedLanguages - event.value)
            is CharacterCreateEvent.AddProficiencySkill -> _state.value = _state.value.copy(selectedSkills = _state.value.selectedSkills + event.value)
            is CharacterCreateEvent.RemoveProficiencySkill -> _state.value = _state.value.copy(selectedSkills = _state.value.selectedSkills - event.value)
            is CharacterCreateEvent.AddProficiencyWeapon -> {
                _state.value = _state.value.copy(selectedWeapons = _state.value.selectedWeapons + event.value)
                updateDefaultPrompt()
            }
            is CharacterCreateEvent.RemoveProficiencyWeapon -> {
                _state.value = _state.value.copy(selectedWeapons = _state.value.selectedWeapons - event.value)
                updateDefaultPrompt()
            }
            is CharacterCreateEvent.AddProficiencyTool -> _state.value = _state.value.copy(selectedTools = _state.value.selectedTools + event.value)
            is CharacterCreateEvent.RemoveProficiencyTool -> _state.value = _state.value.copy(selectedTools = _state.value.selectedTools - event.value)

            // Items
            CharacterCreateEvent.AddItem -> addItem()
            is CharacterCreateEvent.RemoveItem -> removeItem(event.index)
            is CharacterCreateEvent.ItemNameChanged -> updateItem(event.index) { it.copy(name = event.value) }
            is CharacterCreateEvent.ItemSlotChanged -> updateItem(event.index) { it.copy(slot = event.value) }
            is CharacterCreateEvent.ItemRarityChanged -> updateItem(event.index) { it.copy(rarity = event.value) }
            is CharacterCreateEvent.ItemDescriptionChanged -> updateItem(event.index) { it.copy(description = event.value) }
            is CharacterCreateEvent.ItemImageUrlChanged -> updateItem(event.index) { it.copy(imageUrl = event.value) }
            is CharacterCreateEvent.ItemEquippedChanged -> updateItem(event.index) { it.copy(equipped = event.value) }

            // Weapons
            CharacterCreateEvent.AddWeapon -> addWeapon()
            is CharacterCreateEvent.RemoveWeapon -> removeWeapon(event.index)
            is CharacterCreateEvent.WeaponNameChanged -> updateWeapon(event.index) { it.copy(name = event.value) }
            is CharacterCreateEvent.WeaponAttackBonusChanged -> updateWeapon(event.index) { it.copy(attackBonus = event.value) }
            is CharacterCreateEvent.WeaponDamageChanged -> updateWeapon(event.index) { it.copy(damage = event.value) }
            is CharacterCreateEvent.WeaponDamageTypeChanged -> updateWeapon(event.index) { it.copy(damageType = event.value) }
            is CharacterCreateEvent.WeaponNotesChanged -> updateWeapon(event.index) { it.copy(notes = event.value) }

            // Skills
            CharacterCreateEvent.AddSkill -> addSkill()
            is CharacterCreateEvent.RemoveSkill -> removeSkill(event.index)
            is CharacterCreateEvent.SkillNameChanged -> updateSkill(event.index) { it.copy(name = event.value) }
            is CharacterCreateEvent.SkillDescriptionChanged -> updateSkill(event.index) { it.copy(description = event.value) }
            is CharacterCreateEvent.SkillIconNameChanged -> updateSkill(event.index) { it.copy(iconUrl = event.value.ifBlank { null }) }
            is CharacterCreateEvent.SkillDamageChanged -> updateSkill(event.index) { it.copy(damage = event.value) }
            is CharacterCreateEvent.SkillDamageTypeChanged -> updateSkill(event.index) { it.copy(damageType = event.value) }
            is CharacterCreateEvent.SkillResourceCostChanged -> updateSkill(event.index) { it.copy(resourceCost = event.value) }
            is CharacterCreateEvent.SkillRangeChanged -> updateSkill(event.index) { it.copy(range = event.value) }
            is CharacterCreateEvent.SkillCastingTimeChanged -> updateSkill(event.index) { it.copy(castingTime = event.value) }
            is CharacterCreateEvent.SkillDurationChanged -> updateSkill(event.index) { it.copy(duration = event.value) }
            is CharacterCreateEvent.SkillLevelChanged -> updateSkill(event.index) { it.copy(level = event.value.toIntOrNull() ?: 0) }
            is CharacterCreateEvent.SkillSchoolChanged -> updateSkill(event.index) { it.copy(school = event.value) }
            is CharacterCreateEvent.SkillIsPassiveChanged -> updateSkill(event.index) { it.copy(isPassive = event.value) }

            // Features
            // Features
            is CharacterCreateEvent.AddClassFeature -> _state.value = _state.value.copy(selectedClassFeatures = _state.value.selectedClassFeatures + event.value)
            is CharacterCreateEvent.RemoveClassFeature -> _state.value = _state.value.copy(selectedClassFeatures = _state.value.selectedClassFeatures - event.value)
            is CharacterCreateEvent.AddRacialTrait -> _state.value = _state.value.copy(selectedRacialTraits = _state.value.selectedRacialTraits + event.value)
            is CharacterCreateEvent.RemoveRacialTrait -> _state.value = _state.value.copy(selectedRacialTraits = _state.value.selectedRacialTraits - event.value)
            is CharacterCreateEvent.AddFeat -> _state.value = _state.value.copy(selectedFeats = _state.value.selectedFeats + event.value)
            is CharacterCreateEvent.RemoveFeat -> _state.value = _state.value.copy(selectedFeats = _state.value.selectedFeats - event.value)

            is CharacterCreateEvent.SaveCharacter -> saveCharacter()
            is CharacterCreateEvent.GenerateImage -> generateImage()
            is CharacterCreateEvent.GenerateItemImage -> generateItemImage(event.index)
            is CharacterCreateEvent.AiSizeChanged -> _state.value = _state.value.copy(aiWidth = event.width, aiHeight = event.height)
            is CharacterCreateEvent.AiPromptChanged -> _state.value = _state.value.copy(aiPrompt = event.value)
        }
    }

    private fun updateDefaultPrompt() {
        val s = _state.value
        val promptText = "${s.name}, ${s.alignment} ${s.subrace} ${s.race} ${s.characterClass} ${s.subclass}. Background: ${s.background}. Description: ${s.description}. Armor: ${s.armorProficiencies}. Weapons: ${s.selectedWeapons.joinToString()}.".trim()
        if (promptText.isNotBlank()) {
            _state.value = _state.value.copy(
                aiPrompt = PromptGenerator.getFullPrompt(promptText, GenerationType.CHARACTER)
            )
        }
    }

    private fun generateImage() {
        val s = _state.value
        val prompt = s.aiPrompt.ifBlank {
            val text = "${s.name}, ${s.race} ${s.characterClass}. ${s.description}"
            PromptGenerator.getFullPrompt(text, GenerationType.CHARACTER)
        }
        if (s.name.isBlank() && s.aiPrompt.isBlank()) return

        val mockUrl = editingRepository.startGeneration(
            entityId = tempId,
            entityType = "character",
            prompt = prompt,
            genType = GenerationType.CHARACTER,
            width = s.aiWidth,
            height = s.aiHeight
        )
        _state.value = _state.value.copy(imageUrl = mockUrl)
    }

    private fun generateItemImage(index: Int) {
        val s = _state.value
        val items = s.items
        if (index !in items.indices) return
        val item = items[index]
        val promptText = "${item.name}, ${item.rarity}. ${item.description}"
        if (item.name.isBlank()) return

        val fullPrompt = PromptGenerator.getFullPrompt(promptText, GenerationType.ITEM)
        
        val mockUrl = editingRepository.startGeneration(
            entityId = "$tempId:${item.id}",
            entityType = "item",
            prompt = fullPrompt,
            genType = GenerationType.ITEM,
            width = s.aiWidth,
            height = s.aiHeight
        )
        updateItem(index) { it.copy(imageUrl = mockUrl) }
    }

    // Items
    private fun addItem() {
        val newItem = Item(
            id = "item-${getRandomId()}",
            name = "New Item",
            slot = EquipmentSlot.MAIN_HAND,
            rarity = ItemRarity.COMMON,
            description = "",
            equipped = false,
        )
        _state.value = _state.value.copy(items = _state.value.items + newItem)
    }

    private fun removeItem(index: Int) {
        val current = _state.value.items
        if (index in current.indices) {
            _state.value = _state.value.copy(items = current.filterIndexed { i, _ -> i != index })
        }
    }

    private fun updateItem(index: Int, transform: (Item) -> Item) {
        val current = _state.value.items
        if (index in current.indices) {
            _state.value = _state.value.copy(
                items = current.mapIndexed { i, item -> if (i == index) transform(item) else item }
            )
        }
    }

    // Weapons
    private fun addWeapon() {
        val newWeapon = Weapon(
            id = "wpn-${getRandomId()}",
            name = "New Weapon",
            attackBonus = "",
            damage = "",
            damageType = "",
            notes = "",
        )
        _state.value = _state.value.copy(weapons = _state.value.weapons + newWeapon)
    }

    private fun removeWeapon(index: Int) {
        val current = _state.value.weapons
        if (index in current.indices) {
            _state.value = _state.value.copy(weapons = current.filterIndexed { i, _ -> i != index })
        }
    }

    private fun updateWeapon(index: Int, transform: (Weapon) -> Weapon) {
        val current = _state.value.weapons
        if (index in current.indices) {
            _state.value = _state.value.copy(
                weapons = current.mapIndexed { i, wpn -> if (i == index) transform(wpn) else wpn }
            )
        }
    }

    // Skills
    private fun addSkill() {
        val newSkill = Skill(
            id = "skl-${getRandomId()}",
            name = "New Skill",
            description = "",
            iconUrl = null,
            damage = "",
            damageType = "",
            resourceCost = "",
            range = "",
            castingTime = "",
            duration = "",
            level = 0,
            school = "",
            isPassive = false,
        )
        _state.value = _state.value.copy(skillList = _state.value.skillList + newSkill)
    }

    private fun removeSkill(index: Int) {
        val current = _state.value.skillList
        if (index in current.indices) {
            _state.value = _state.value.copy(skillList = current.filterIndexed { i, _ -> i != index })
        }
    }

    private fun updateSkill(index: Int, transform: (Skill) -> Skill) {
        val current = _state.value.skillList
        if (index in current.indices) {
            _state.value = _state.value.copy(
                skillList = current.mapIndexed { i, skl -> if (i == index) transform(skl) else skl }
            )
        }
    }

    private fun parseCommaList(input: String): List<String> =
        input.split(",").map { it.trim() }.filter { it.isNotBlank() }

    private fun parseLineList(input: String): List<String> =
        input.lines().map { it.trim() }.filter { it.isNotBlank() }

    private fun getRandomId(): String {
        return Random.nextInt(100000, 999999).toString()
    }

    private fun saveCharacter() {
        val s = _state.value

        // Parse numbers
        val level = s.level.toIntOrNull() ?: 1
        val maxHp = s.maxHp.toIntOrNull() ?: 10
        val currentHp = s.currentHp.toIntOrNull() ?: maxHp
        val strength = s.strength.toIntOrNull() ?: 10
        val dexterity = s.dexterity.toIntOrNull() ?: 10
        val constitution = s.constitution.toIntOrNull() ?: 10
        val intelligence = s.intelligence.toIntOrNull() ?: 10
        val wisdom = s.wisdom.toIntOrNull() ?: 10
        val charisma = s.charisma.toIntOrNull() ?: 10
        val experiencePoints = s.experiencePoints.toIntOrNull() ?: 0
        val age = s.age.toIntOrNull() ?: 0
        val armorClass = s.armorClass.toIntOrNull() ?: 10
        val initiative = s.initiative.toIntOrNull() ?: 0
        val speed = s.speed.toIntOrNull() ?: 30
        val proficiencyBonus = s.proficiencyBonus.toIntOrNull() ?: 2
        val tempHp = s.tempHp.toIntOrNull() ?: 0
        val hitDiceCurrent = s.hitDiceCurrent.toIntOrNull() ?: 1
        val exhaustion = s.exhaustion.toIntOrNull()?.coerceIn(0, 6) ?: 0
        val deathSaveSuccesses = s.deathSaveSuccesses.toIntOrNull()?.coerceIn(0, 3) ?: 0
        val deathSaveFailures = s.deathSaveFailures.toIntOrNull()?.coerceIn(0, 3) ?: 0

        if (s.name.isBlank()) {
            _state.value = s.copy(error = "Name is required")
            return
        }

        val character = Character(
            id = tempId,
            name = s.name.trim(),
            playerName = s.playerName.trim(),
            race = s.race.trim(),
            subrace = s.subrace.trim(),
            characterClass = s.characterClass.trim(),
            subclass = s.subclass.trim(),
            background = s.background.trim(),
            alignment = s.alignment.trim(),
            level = level,
            experiencePoints = experiencePoints,
            description = s.description.trim(),
            imageUrl = s.imageUrl.trim().ifBlank { null },
            appearance = CharacterAppearance(
                age = age,
                gender = s.gender.trim(),
                height = s.height.trim(),
                weight = s.weight.trim(),
                eyes = s.eyes.trim(),
                hair = s.hair.trim(),
                skin = s.skin.trim(),
            ),
            stats = CharacterStats(
                strength = strength,
                dexterity = dexterity,
                constitution = constitution,
                intelligence = intelligence,
                wisdom = wisdom,
                charisma = charisma,
            ),
            maxHp = maxHp,
            currentHp = currentHp.coerceIn(0, maxHp),
            combat = CharacterCombat(
                armorClass = armorClass,
                initiative = initiative,
                speed = speed,
                proficiencyBonus = proficiencyBonus,
                tempHp = tempHp,
                hitDice = s.hitDice.trim().ifBlank { "1d8" },
                hitDiceCurrent = hitDiceCurrent,
                inspiration = s.inspiration,
                exhaustion = exhaustion,
                conditions = parseCommaList(s.conditions),
                deathSaveSuccesses = deathSaveSuccesses,
                deathSaveFailures = deathSaveFailures,
            ),
            proficiencies = CharacterProficiencies(
                savingThrows = parseCommaList(s.savingThrows),
                skills = s.selectedSkills,
                armor = parseCommaList(s.armorProficiencies),
                weapons = s.selectedWeapons,
                tools = s.selectedTools,
                languages = s.selectedLanguages,
            ),
            weapons = s.weapons,
            features = CharacterFeatures(
                classFeatures = s.selectedClassFeatures,
                racialTraits = s.selectedRacialTraits,
                feats = s.selectedFeats,
            ),
            skills = s.skillList,
            items = s.items,
            notes = s.notes,
        )

        _state.value = s.copy(isSaving = true, error = null)

        viewModelScope.launch {
            when (val result = repository.saveCharacter(character)) {
                is Result.Success -> {
                    _state.value = _state.value.copy(isSaving = false, isSaved = true)
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(
                        isSaving = false,
                        error = result.error.toUserMessage(),
                    )
                }
            }
        }
    }
}
