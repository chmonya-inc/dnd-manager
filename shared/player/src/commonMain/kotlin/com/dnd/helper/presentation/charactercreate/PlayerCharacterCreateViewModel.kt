package com.dnd.helper.presentation.charactercreate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnd.helper.data.remote.GenerationType
import com.dnd.helper.data.remote.PromptGenerator
import com.dnd.helper.domain.common.Result
import com.dnd.helper.domain.common.toUserMessage
import com.dnd.helper.domain.model.Character
import com.dnd.helper.domain.model.CharacterAppearance
import com.dnd.helper.domain.model.CharacterCombat
import com.dnd.helper.domain.model.CharacterFeatures
import com.dnd.helper.domain.model.CharacterProficiencies
import com.dnd.helper.domain.model.CharacterStats
import com.dnd.helper.domain.model.EquipmentSlot
import com.dnd.helper.domain.model.Item
import com.dnd.helper.domain.model.ItemRarity
import com.dnd.helper.domain.model.Spell
import com.dnd.helper.domain.model.Weapon
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class PlayerCharacterCreateViewModel(
    private val remoteDataSource: com.dnd.helper.data.remote.RemoteDataSource,
    private val editingRepository: com.dnd.helper.domain.repository.EditingRepository,
    private val api: com.dnd.helper.data.remote.DndApiDataSource,
    private val storage: com.dnd.helper.domain.storage.CharacterStorage,
    coroutineScope: kotlinx.coroutines.CoroutineScope? = null
) : ViewModel() {

    private val scope = coroutineScope ?: viewModelScope
    private var tempId = "temp-char-${Random.nextInt(1000000, 9999999)}"
    private val _state = MutableStateFlow(PlayerCharacterCreateState())
    val state: StateFlow<PlayerCharacterCreateState> = _state.asStateFlow()

    init {
        // Listen for background image generation completion
        scope.launch {
            editingRepository.activeTasks.collect { tasks ->
                val myTasks = tasks.filter {
                    it.entityId == tempId || it.entityId.startsWith("$tempId:")
                }

                myTasks.forEach { task ->
                    if ((task.status == com.dnd.helper.domain.repository.GenerationStatus.COMPLETED && task.resultUrl != null) ||
                        task.status == com.dnd.helper.domain.repository.GenerationStatus.FAILED
                    ) {

                        val resultUrl =
                            if (task.status == com.dnd.helper.domain.repository.GenerationStatus.COMPLETED) {
                                task.resultUrl
                                    ?: ""
                            } else {
                                ""
                            }

                        _state.update { currentState ->
                            if (task.entityType == "character" && currentState.imageUrl == "generating:${task.id}") {
                                currentState.copy(imageUrl = resultUrl)
                            } else if (task.entityType == "item") {
                                val itemId = task.entityId.substringAfter(":")
                                if (currentState.items.any { it.id == itemId && it.imageUrl == "generating:${task.id}" }) {
                                    val newItems = currentState.items.map {
                                        if (it.id == itemId) {
                                            it.copy(
                                                imageUrl = resultUrl
                                            )
                                        } else {
                                            it
                                        }
                                    }
                                    currentState.copy(items = newItems)
                                } else {
                                    currentState
                                }
                            } else if (task.entityType == "spell") {
                                val spellId = task.entityId.substringAfter(":")
                                if (currentState.spellList.any { it.id == spellId && it.iconUrl == "generating:${task.id}" }) {
                                    val newSpells = currentState.spellList.map {
                                        if (it.id == spellId) {
                                            it.copy(
                                                iconUrl = resultUrl
                                            )
                                        } else {
                                            it
                                        }
                                    }
                                    currentState.copy(spellList = newSpells)
                                } else {
                                    currentState
                                }
                            } else {
                                currentState
                            }
                        }
                    }
                }
            }
        }

        scope.launch {
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
            when (val res = api.getSpells()) {
                is Result.Success -> _state.update { it.copy(availableSpells = res.data.results) }
                else -> {}
            }
        }
    }

    fun onEvent(event: PlayerCharacterCreateEvent) {
        when (event) {
            // Basic info
            is PlayerCharacterCreateEvent.NameChanged -> {
                _state.update { it.copy(name = event.value) }
                updateDefaultPrompt()
            }

            is PlayerCharacterCreateEvent.PlayerNameChanged ->
                _state.update { it.copy(playerName = event.value) }

            is PlayerCharacterCreateEvent.RaceChanged -> {
                _state.update { it.copy(race = event.value, subrace = "") }
                updateDefaultPrompt()

                val raceIndex = _state.value.availableRaces.find {
                    it.name == event.value
                }?.index ?: event.value.lowercase().replace(" ", "-")
                scope.launch {
                    when (val res = api.getRace(raceIndex)) {
                        is Result.Success -> _state.update { it.copy(availableSubraces = res.data.subraces) }
                        else -> _state.update { it.copy(availableSubraces = emptyList()) }
                    }
                }
            }

            is PlayerCharacterCreateEvent.SubraceChanged -> {
                _state.update { it.copy(subrace = event.value) }
                updateDefaultPrompt()
            }

            is PlayerCharacterCreateEvent.ClassChanged -> {
                _state.update { it.copy(characterClass = event.value, subclass = "") }
                updateDefaultPrompt()

                // Fetch subclasses for this class
                val classIndex = _state.value.availableClasses.find {
                    it.name == event.value
                }?.index ?: event.value.lowercase().replace(" ", "-")
                scope.launch {
                    when (val res = api.getClass(classIndex)) {
                        is Result.Success -> _state.update { it.copy(availableSubclasses = res.data.subclasses) }
                        else -> _state.update { it.copy(availableSubclasses = emptyList()) }
                    }
                }
            }

            is PlayerCharacterCreateEvent.SubclassChanged -> {
                _state.update { it.copy(subclass = event.value) }
                updateDefaultPrompt()
            }

            is PlayerCharacterCreateEvent.BackgroundChanged -> {
                _state.update { it.copy(background = event.value) }
                updateDefaultPrompt()
            }

            is PlayerCharacterCreateEvent.AlignmentChanged -> {
                _state.update { it.copy(alignment = event.value) }
                updateDefaultPrompt()
            }

            is PlayerCharacterCreateEvent.LevelChanged ->
                _state.update { it.copy(level = event.value) }

            is PlayerCharacterCreateEvent.ExperiencePointsChanged ->
                _state.update { it.copy(experiencePoints = event.value) }

            is PlayerCharacterCreateEvent.DescriptionChanged -> {
                _state.update { it.copy(description = event.value) }
                updateDefaultPrompt()
            }

            is PlayerCharacterCreateEvent.ImageUrlChanged ->
                _state.update { it.copy(imageUrl = event.value) }

            // Appearance
            is PlayerCharacterCreateEvent.AgeChanged ->
                _state.update { it.copy(age = event.value) }

            is PlayerCharacterCreateEvent.GenderChanged ->
                _state.update { it.copy(gender = event.value) }

            is PlayerCharacterCreateEvent.HeightChanged ->
                _state.update { it.copy(height = event.value) }

            is PlayerCharacterCreateEvent.WeightChanged ->
                _state.update { it.copy(weight = event.value) }

            is PlayerCharacterCreateEvent.EyesChanged ->
                _state.update { it.copy(eyes = event.value) }

            is PlayerCharacterCreateEvent.HairChanged ->
                _state.update { it.copy(hair = event.value) }

            is PlayerCharacterCreateEvent.SkinChanged ->
                _state.update { it.copy(skin = event.value) }

            // Ability Scores
            is PlayerCharacterCreateEvent.StrengthChanged ->
                _state.update { it.copy(strength = event.value) }

            is PlayerCharacterCreateEvent.DexterityChanged ->
                _state.update { it.copy(dexterity = event.value) }

            is PlayerCharacterCreateEvent.ConstitutionChanged ->
                _state.update { it.copy(constitution = event.value) }

            is PlayerCharacterCreateEvent.IntelligenceChanged ->
                _state.update { it.copy(intelligence = event.value) }

            is PlayerCharacterCreateEvent.WisdomChanged ->
                _state.update { it.copy(wisdom = event.value) }

            is PlayerCharacterCreateEvent.CharismaChanged ->
                _state.update { it.copy(charisma = event.value) }

            // HP & Combat
            is PlayerCharacterCreateEvent.MaxHpChanged ->
                _state.update { it.copy(maxHp = event.value) }

            is PlayerCharacterCreateEvent.CurrentHpChanged ->
                _state.update { it.copy(currentHp = event.value) }

            is PlayerCharacterCreateEvent.TempHpChanged ->
                _state.update { it.copy(tempHp = event.value) }

            is PlayerCharacterCreateEvent.ArmorClassChanged ->
                _state.update { it.copy(armorClass = event.value) }

            is PlayerCharacterCreateEvent.InitiativeChanged ->
                _state.update { it.copy(initiative = event.value) }

            is PlayerCharacterCreateEvent.SpeedChanged ->
                _state.update { it.copy(speed = event.value) }

            is PlayerCharacterCreateEvent.ProficiencyBonusChanged ->
                _state.update { it.copy(proficiencyBonus = event.value) }

            is PlayerCharacterCreateEvent.HitDiceChanged ->
                _state.update { it.copy(hitDice = event.value) }

            is PlayerCharacterCreateEvent.HitDiceCurrentChanged ->
                _state.update { it.copy(hitDiceCurrent = event.value) }

            // Status
            is PlayerCharacterCreateEvent.InspirationChanged ->
                _state.update { it.copy(inspiration = event.value) }

            is PlayerCharacterCreateEvent.ExhaustionChanged ->
                _state.update { it.copy(exhaustion = event.value) }

            is PlayerCharacterCreateEvent.ConditionsChanged ->
                _state.update { it.copy(conditions = event.value) }

            is PlayerCharacterCreateEvent.DeathSaveSuccessesChanged ->
                _state.update { it.copy(deathSaveSuccesses = event.value) }

            is PlayerCharacterCreateEvent.DeathSaveFailuresChanged ->
                _state.update { it.copy(deathSaveFailures = event.value) }

            // Proficiencies
            is PlayerCharacterCreateEvent.SavingThrowsChanged ->
                _state.update { it.copy(savingThrows = event.value) }

            is PlayerCharacterCreateEvent.ArmorProficienciesChanged -> {
                _state.update { it.copy(armorProficiencies = event.value) }
                updateDefaultPrompt()
            }

            is PlayerCharacterCreateEvent.AddLanguage ->
                _state.update { it.copy(selectedLanguages = it.selectedLanguages + event.value) }

            is PlayerCharacterCreateEvent.RemoveLanguage ->
                _state.update { it.copy(selectedLanguages = it.selectedLanguages - event.value) }

            is PlayerCharacterCreateEvent.AddProficiencySkill ->
                _state.update { it.copy(selectedSkills = it.selectedSkills + event.value) }

            is PlayerCharacterCreateEvent.RemoveProficiencySkill ->
                _state.update { it.copy(selectedSkills = it.selectedSkills - event.value) }

            is PlayerCharacterCreateEvent.AddProficiencyWeapon -> {
                _state.update { it.copy(selectedWeapons = it.selectedWeapons + event.value) }
                updateDefaultPrompt()
            }

            is PlayerCharacterCreateEvent.RemoveProficiencyWeapon -> {
                _state.update { it.copy(selectedWeapons = it.selectedWeapons - event.value) }
                updateDefaultPrompt()
            }

            is PlayerCharacterCreateEvent.AddProficiencyTool ->
                _state.update { it.copy(selectedTools = it.selectedTools + event.value) }

            is PlayerCharacterCreateEvent.RemoveProficiencyTool ->
                _state.update { it.copy(selectedTools = it.selectedTools - event.value) }

            // Items
            PlayerCharacterCreateEvent.AddItem -> addItem()
            is PlayerCharacterCreateEvent.RemoveItem -> removeItem(event.index)
            is PlayerCharacterCreateEvent.ItemNameChanged -> updateItem(event.index) { it.copy(name = event.value) }
            is PlayerCharacterCreateEvent.ItemSlotChanged -> updateItem(event.index) { it.copy(slot = event.value) }
            is PlayerCharacterCreateEvent.ItemRarityChanged -> updateItem(event.index) { it.copy(rarity = event.value) }
            is PlayerCharacterCreateEvent.ItemDescriptionChanged -> updateItem(
                event.index
            ) { it.copy(description = event.value) }

            is PlayerCharacterCreateEvent.ItemImageUrlChanged -> updateItem(event.index) {
                it.copy(
                    imageUrl = event.value
                )
            }

            is PlayerCharacterCreateEvent.ItemEquippedChanged -> updateItem(event.index) {
                it.copy(
                    equipped = event.value
                )
            }

            // Weapons
            PlayerCharacterCreateEvent.AddWeapon -> addWeapon()
            is PlayerCharacterCreateEvent.RemoveWeapon -> removeWeapon(event.index)
            is PlayerCharacterCreateEvent.WeaponNameChanged -> updateWeapon(event.index) { it.copy(name = event.value) }
            is PlayerCharacterCreateEvent.WeaponAttackBonusChanged -> updateWeapon(
                event.index
            ) { it.copy(attackBonus = event.value) }

            is PlayerCharacterCreateEvent.WeaponDamageChanged -> updateWeapon(event.index) {
                it.copy(
                    damage = event.value
                )
            }

            is PlayerCharacterCreateEvent.WeaponDamageTypeChanged -> updateWeapon(
                event.index
            ) { it.copy(damageType = event.value) }

            is PlayerCharacterCreateEvent.WeaponNotesChanged -> updateWeapon(
                event.index
            ) { it.copy(notes = event.value) }

            // Spells
            PlayerCharacterCreateEvent.AddSpell -> addSpell()
            is PlayerCharacterCreateEvent.RemoveSpell -> removeSpell(event.index)
            is PlayerCharacterCreateEvent.SpellNameChanged -> updateSpell(event.index) { it.copy(name = event.value) }
            is PlayerCharacterCreateEvent.SpellDescriptionChanged -> updateSpell(
                event.index
            ) { it.copy(description = event.value) }

            is PlayerCharacterCreateEvent.SpellLevelChanged -> updateSpell(event.index) {
                it.copy(level = event.value.toIntOrNull() ?: 0)
            }

            is PlayerCharacterCreateEvent.SpellSchoolChanged -> updateSpell(
                event.index
            ) { it.copy(school = event.value) }
            is PlayerCharacterCreateEvent.SpellCastingTimeChanged -> updateSpell(
                event.index
            ) { it.copy(castingTime = event.value) }

            is PlayerCharacterCreateEvent.SpellRangeChanged -> updateSpell(event.index) { it.copy(range = event.value) }
            is PlayerCharacterCreateEvent.SpellDurationChanged -> updateSpell(event.index) {
                it.copy(
                    duration = event.value
                )
            }

            is PlayerCharacterCreateEvent.SpellDamageChanged -> updateSpell(
                event.index
            ) { it.copy(damage = event.value) }
            is PlayerCharacterCreateEvent.SpellDamageTypeChanged -> updateSpell(
                event.index
            ) { it.copy(damageType = event.value) }

            // Features
            // Features
            is PlayerCharacterCreateEvent.AddClassFeature ->
                _state.update { it.copy(selectedClassFeatures = it.selectedClassFeatures + event.value) }

            is PlayerCharacterCreateEvent.RemoveClassFeature ->
                _state.update { it.copy(selectedClassFeatures = it.selectedClassFeatures - event.value) }

            is PlayerCharacterCreateEvent.AddRacialTrait ->
                _state.update { it.copy(selectedRacialTraits = it.selectedRacialTraits + event.value) }

            is PlayerCharacterCreateEvent.RemoveRacialTrait ->
                _state.update { it.copy(selectedRacialTraits = it.selectedRacialTraits - event.value) }

            is PlayerCharacterCreateEvent.AddFeat ->
                _state.update { it.copy(selectedFeats = it.selectedFeats + event.value) }

            is PlayerCharacterCreateEvent.RemoveFeat ->
                _state.update { it.copy(selectedFeats = it.selectedFeats - event.value) }

            is PlayerCharacterCreateEvent.LoadCharacter -> loadCharacter(event.characterId)
            is PlayerCharacterCreateEvent.SaveCharacter -> saveCharacter()
            is PlayerCharacterCreateEvent.DismissError -> _state.update { it.copy(error = null) }
            is PlayerCharacterCreateEvent.GenerateImage -> generateImage()
            is PlayerCharacterCreateEvent.GenerateItemImage -> generateItemImage(event.index)
            is PlayerCharacterCreateEvent.AiSizeChanged ->
                _state.update { it.copy(aiWidth = event.width, aiHeight = event.height) }

            is PlayerCharacterCreateEvent.AiPromptChanged ->
                _state.update { it.copy(aiPrompt = event.value) }
        }
    }

    private fun updateDefaultPrompt() {
        val s = _state.value
        val promptText =
            (
                "${s.name}, ${s.alignment} ${s.subrace} ${s.race} ${s.characterClass} ${s.subclass}. " +
                    "Background: ${s.background}. " +
                    "Description: ${s.description}. " +
                    "Armor: ${s.armorProficiencies}. " +
                    "Weapons: ${s.selectedWeapons.joinToString()}."
                ).trim()
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

    // Spells
    private fun addSpell() {
        val newSpell = Spell(
            id = "spl-${getRandomId()}",
            name = "New Spell",
            description = "",
            level = 0,
            school = "",
            castingTime = "",
            range = "",
            duration = ""
        )
        _state.value = _state.value.copy(spellList = _state.value.spellList + newSpell)
    }

    private fun removeSpell(index: Int) {
        val current = _state.value.spellList
        if (index in current.indices) {
            _state.value =
                _state.value.copy(spellList = current.filterIndexed { i, _ -> i != index })
        }
    }

    private fun updateSpell(index: Int, transform: (Spell) -> Spell) {
        val current = _state.value.spellList
        if (index in current.indices) {
            _state.value = _state.value.copy(
                spellList = current.mapIndexed { i, spl -> if (i == index) transform(spl) else spl }
            )
        }
    }

    private fun loadCharacter(characterId: String) {
        _state.value = _state.value.copy(isLoading = true)
        scope.launch {
            val result = remoteDataSource.getMyCharacter(characterId)
            if (result is com.dnd.helper.domain.common.Result.Success) {
                val character = result.data
                _state.value = PlayerCharacterCreateState(
                    name = character.name,
                    playerName = character.playerName,
                    race = character.race,
                    subrace = character.subrace,
                    characterClass = character.characterClass,
                    subclass = character.subclass,
                    background = character.background,
                    alignment = character.alignment,
                    level = character.level.toString(),
                    experiencePoints = character.experiencePoints.toString(),
                    description = character.description,
                    imageUrl = character.imageUrl ?: "",
                    age = character.appearance.age.toString(),
                    gender = character.appearance.gender,
                    height = character.appearance.height,
                    weight = character.appearance.weight,
                    eyes = character.appearance.eyes,
                    hair = character.appearance.hair,
                    skin = character.appearance.skin,
                    strength = character.stats.strength.toString(),
                    dexterity = character.stats.dexterity.toString(),
                    constitution = character.stats.constitution.toString(),
                    intelligence = character.stats.intelligence.toString(),
                    wisdom = character.stats.wisdom.toString(),
                    charisma = character.stats.charisma.toString(),
                    maxHp = character.maxHp.toString(),
                    currentHp = character.currentHp.toString(),
                    tempHp = character.combat.tempHp.toString(),
                    armorClass = character.combat.armorClass.toString(),
                    initiative = character.combat.initiative.toString(),
                    speed = character.combat.speed.toString(),
                    proficiencyBonus = character.combat.proficiencyBonus.toString(),
                    hitDice = character.combat.hitDice,
                    hitDiceCurrent = character.combat.hitDiceCurrent.toString(),
                    inspiration = character.combat.inspiration,
                    exhaustion = character.combat.exhaustion.toString(),
                    conditions = character.combat.conditions.joinToString(", "),
                    deathSaveSuccesses = character.combat.deathSaveSuccesses.toString(),
                    deathSaveFailures = character.combat.deathSaveFailures.toString(),
                    savingThrows = character.proficiencies.savingThrows.joinToString(", "),
                    armorProficiencies = character.proficiencies.armor.joinToString(", "),
                    selectedSkills = character.proficiencies.skills,
                    selectedWeapons = character.proficiencies.weapons,
                    selectedTools = character.proficiencies.tools,
                    selectedLanguages = character.proficiencies.languages,
                    selectedClassFeatures = character.features.classFeatures,
                    selectedRacialTraits = character.features.racialTraits,
                    selectedFeats = character.features.feats,
                    items = character.items,
                    weapons = character.weapons,
                    spellList = character.spells,
                    notes = character.notes,
                    availableClasses = _state.value.availableClasses,
                    availableRaces = _state.value.availableRaces,
                    availableBackgrounds = _state.value.availableBackgrounds,
                    availableAlignments = _state.value.availableAlignments,
                    availableLanguages = _state.value.availableLanguages,
                    availableSkills = _state.value.availableSkills,
                    availableEquipment = _state.value.availableEquipment,
                    availableFeats = _state.value.availableFeats,
                    availableFeatures = _state.value.availableFeatures,
                    availableTraits = _state.value.availableTraits,
                    availableSpells = _state.value.availableSpells,
                    isEditMode = true,
                    isLoading = false
                )
                tempId = character.id
                updateDefaultPrompt()
            } else {
                _state.value = _state.value.copy(isLoading = false, error = "Failed to load character")
            }
        }
    }

    private fun parseCommaList(input: String): List<String> =
        input.split(",").map { it.trim() }.filter { it.isNotBlank() }

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

        // Session ID is always taken from storage (set when the campaign is selected)
        storage.saveTableId(storage.getTableId() ?: "")

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
            spells = s.spellList,
            items = s.items,
            notes = s.notes,
        )

        _state.update { it.copy(isSaving = true, error = null) }

        scope.launch {
            // Templates live in the player's personal session; createMyCharacter upserts by id,
            // so it handles both create and edit.
            val result = remoteDataSource.createMyCharacter(character)
            when (result) {
                is Result.Success -> {
                    _state.update { it.copy(isSaving = false, isSaved = true, savedCharacterId = character.id) }
                }

                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isSaving = false,
                            error = result.error.toUserMessage(),
                        )
                    }
                }
            }
        }
    }
}
