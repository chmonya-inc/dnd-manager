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
import com.dnd.helper.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class CharacterCreateViewModel(
    private val repository: CharacterRepository,
    private val remoteDataSource: com.dnd.helper.data.remote.RemoteDataSource,
    private val editingRepository: com.dnd.helper.domain.repository.EditingRepository,
    private val api: com.dnd.helper.data.remote.DndApiDataSource,
    private val storage: com.dnd.helper.domain.storage.CharacterStorage,
) : ViewModel() {

    private var tempId = "temp-char-${Random.nextInt(1000000, 9999999)}"
    private var isEditing = false
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
            when (val res = api.getSpells()) {
                is Result.Success -> _state.update { it.copy(availableSpells = res.data.results) }
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

            is CharacterCreateEvent.PlayerNameChanged ->
                _state.value =
                    _state.value.copy(playerName = event.value)

            is CharacterCreateEvent.RaceChanged -> {
                _state.value = _state.value.copy(race = event.value, subrace = "")
                updateDefaultPrompt()

                val raceIndex = _state.value.availableRaces.find {
                    it.name == event.value
                }?.index ?: event.value.lowercase().replace(" ", "-")
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
                val classIndex = _state.value.availableClasses.find {
                    it.name == event.value
                }?.index ?: event.value.lowercase().replace(" ", "-")
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

            is CharacterCreateEvent.LevelChanged ->
                _state.value =
                    _state.value.copy(level = event.value)

            is CharacterCreateEvent.ExperiencePointsChanged ->
                _state.value =
                    _state.value.copy(experiencePoints = event.value)

            is CharacterCreateEvent.DescriptionChanged -> {
                _state.value = _state.value.copy(description = event.value)
                updateDefaultPrompt()
            }

            is CharacterCreateEvent.ImageUrlChanged ->
                _state.value =
                    _state.value.copy(imageUrl = event.value)

            // Appearance
            is CharacterCreateEvent.AgeChanged ->
                _state.value =
                    _state.value.copy(age = event.value)

            is CharacterCreateEvent.GenderChanged ->
                _state.value =
                    _state.value.copy(gender = event.value)

            is CharacterCreateEvent.HeightChanged ->
                _state.value =
                    _state.value.copy(height = event.value)

            is CharacterCreateEvent.WeightChanged ->
                _state.value =
                    _state.value.copy(weight = event.value)

            is CharacterCreateEvent.EyesChanged ->
                _state.value =
                    _state.value.copy(eyes = event.value)

            is CharacterCreateEvent.HairChanged ->
                _state.value =
                    _state.value.copy(hair = event.value)

            is CharacterCreateEvent.SkinChanged ->
                _state.value =
                    _state.value.copy(skin = event.value)

            // Ability Scores
            is CharacterCreateEvent.StrengthChanged ->
                _state.value =
                    _state.value.copy(strength = event.value)

            is CharacterCreateEvent.DexterityChanged ->
                _state.value =
                    _state.value.copy(dexterity = event.value)

            is CharacterCreateEvent.ConstitutionChanged ->
                _state.value =
                    _state.value.copy(constitution = event.value)

            is CharacterCreateEvent.IntelligenceChanged ->
                _state.value =
                    _state.value.copy(intelligence = event.value)

            is CharacterCreateEvent.WisdomChanged ->
                _state.value =
                    _state.value.copy(wisdom = event.value)

            is CharacterCreateEvent.CharismaChanged ->
                _state.value =
                    _state.value.copy(charisma = event.value)

            // HP & Combat
            is CharacterCreateEvent.MaxHpChanged ->
                _state.value =
                    _state.value.copy(maxHp = event.value)

            is CharacterCreateEvent.CurrentHpChanged ->
                _state.value =
                    _state.value.copy(currentHp = event.value)

            is CharacterCreateEvent.TempHpChanged ->
                _state.value =
                    _state.value.copy(tempHp = event.value)

            is CharacterCreateEvent.ArmorClassChanged ->
                _state.value =
                    _state.value.copy(armorClass = event.value)

            is CharacterCreateEvent.InitiativeChanged ->
                _state.value =
                    _state.value.copy(initiative = event.value)

            is CharacterCreateEvent.SpeedChanged ->
                _state.value =
                    _state.value.copy(speed = event.value)

            is CharacterCreateEvent.ProficiencyBonusChanged ->
                _state.value =
                    _state.value.copy(proficiencyBonus = event.value)

            is CharacterCreateEvent.HitDiceChanged ->
                _state.value =
                    _state.value.copy(hitDice = event.value)

            is CharacterCreateEvent.HitDiceCurrentChanged ->
                _state.value =
                    _state.value.copy(hitDiceCurrent = event.value)

            // Status
            is CharacterCreateEvent.InspirationChanged ->
                _state.value =
                    _state.value.copy(inspiration = event.value)

            is CharacterCreateEvent.ExhaustionChanged ->
                _state.value =
                    _state.value.copy(exhaustion = event.value)

            is CharacterCreateEvent.ConditionsChanged ->
                _state.value =
                    _state.value.copy(conditions = event.value)

            is CharacterCreateEvent.DeathSaveSuccessesChanged ->
                _state.value =
                    _state.value.copy(deathSaveSuccesses = event.value)

            is CharacterCreateEvent.DeathSaveFailuresChanged ->
                _state.value =
                    _state.value.copy(deathSaveFailures = event.value)

            // Proficiencies
            is CharacterCreateEvent.SavingThrowsChanged ->
                _state.value =
                    _state.value.copy(savingThrows = event.value)

            is CharacterCreateEvent.ArmorProficienciesChanged -> {
                _state.value = _state.value.copy(armorProficiencies = event.value)
                updateDefaultPrompt()
            }

            is CharacterCreateEvent.AddLanguage ->
                _state.value =
                    _state.value.copy(selectedLanguages = _state.value.selectedLanguages + event.value)

            is CharacterCreateEvent.RemoveLanguage ->
                _state.value =
                    _state.value.copy(selectedLanguages = _state.value.selectedLanguages - event.value)

            is CharacterCreateEvent.AddProficiencySkill ->
                _state.value =
                    _state.value.copy(selectedSkills = _state.value.selectedSkills + event.value)

            is CharacterCreateEvent.RemoveProficiencySkill ->
                _state.value =
                    _state.value.copy(selectedSkills = _state.value.selectedSkills - event.value)

            is CharacterCreateEvent.AddProficiencyWeapon -> {
                _state.value =
                    _state.value.copy(selectedWeapons = _state.value.selectedWeapons + event.value)
                updateDefaultPrompt()
            }

            is CharacterCreateEvent.RemoveProficiencyWeapon -> {
                _state.value =
                    _state.value.copy(selectedWeapons = _state.value.selectedWeapons - event.value)
                updateDefaultPrompt()
            }

            is CharacterCreateEvent.AddProficiencyTool ->
                _state.value =
                    _state.value.copy(selectedTools = _state.value.selectedTools + event.value)

            is CharacterCreateEvent.RemoveProficiencyTool ->
                _state.value =
                    _state.value.copy(selectedTools = _state.value.selectedTools - event.value)

            // Items
            CharacterCreateEvent.AddItem -> addItem()
            is CharacterCreateEvent.RemoveItem -> removeItem(event.index)
            is CharacterCreateEvent.ItemNameChanged -> updateItem(event.index) { it.copy(name = event.value) }
            is CharacterCreateEvent.ItemSlotChanged -> updateItem(event.index) { it.copy(slot = event.value) }
            is CharacterCreateEvent.ItemRarityChanged -> updateItem(event.index) { it.copy(rarity = event.value) }
            is CharacterCreateEvent.ItemDescriptionChanged -> updateItem(
                event.index
            ) { it.copy(description = event.value) }

            is CharacterCreateEvent.ItemImageUrlChanged -> updateItem(event.index) {
                it.copy(
                    imageUrl = event.value
                )
            }

            is CharacterCreateEvent.ItemEquippedChanged -> updateItem(event.index) {
                it.copy(
                    equipped = event.value
                )
            }

            // Weapons
            CharacterCreateEvent.AddWeapon -> addWeapon()
            is CharacterCreateEvent.RemoveWeapon -> removeWeapon(event.index)
            is CharacterCreateEvent.WeaponNameChanged -> updateWeapon(event.index) { it.copy(name = event.value) }
            is CharacterCreateEvent.WeaponAttackBonusChanged -> updateWeapon(
                event.index
            ) { it.copy(attackBonus = event.value) }

            is CharacterCreateEvent.WeaponDamageChanged -> updateWeapon(event.index) {
                it.copy(
                    damage = event.value
                )
            }

            is CharacterCreateEvent.WeaponDamageTypeChanged -> updateWeapon(
                event.index
            ) { it.copy(damageType = event.value) }

            is CharacterCreateEvent.WeaponNotesChanged -> updateWeapon(event.index) { it.copy(notes = event.value) }

            // Spells
            CharacterCreateEvent.AddSpell -> addSpell()
            is CharacterCreateEvent.RemoveSpell -> removeSpell(event.index)
            is CharacterCreateEvent.SpellNameChanged -> updateSpell(event.index) { it.copy(name = event.value) }
            is CharacterCreateEvent.SpellDescriptionChanged -> updateSpell(
                event.index
            ) { it.copy(description = event.value) }

            is CharacterCreateEvent.SpellLevelChanged -> updateSpell(event.index) {
                it.copy(level = event.value.toIntOrNull() ?: 0)
            }

            is CharacterCreateEvent.SpellSchoolChanged -> updateSpell(event.index) { it.copy(school = event.value) }
            is CharacterCreateEvent.SpellCastingTimeChanged -> updateSpell(
                event.index
            ) { it.copy(castingTime = event.value) }

            is CharacterCreateEvent.SpellRangeChanged -> updateSpell(event.index) { it.copy(range = event.value) }
            is CharacterCreateEvent.SpellDurationChanged -> updateSpell(event.index) {
                it.copy(
                    duration = event.value
                )
            }

            is CharacterCreateEvent.SpellDamageChanged -> updateSpell(event.index) { it.copy(damage = event.value) }
            is CharacterCreateEvent.SpellDamageTypeChanged -> updateSpell(
                event.index
            ) { it.copy(damageType = event.value) }

            // Features
            // Features
            is CharacterCreateEvent.AddClassFeature ->
                _state.value =
                    _state.value.copy(selectedClassFeatures = _state.value.selectedClassFeatures + event.value)

            is CharacterCreateEvent.RemoveClassFeature ->
                _state.value =
                    _state.value.copy(selectedClassFeatures = _state.value.selectedClassFeatures - event.value)

            is CharacterCreateEvent.AddRacialTrait ->
                _state.value =
                    _state.value.copy(selectedRacialTraits = _state.value.selectedRacialTraits + event.value)

            is CharacterCreateEvent.RemoveRacialTrait ->
                _state.value =
                    _state.value.copy(selectedRacialTraits = _state.value.selectedRacialTraits - event.value)

            is CharacterCreateEvent.AddFeat ->
                _state.value =
                    _state.value.copy(selectedFeats = _state.value.selectedFeats + event.value)

            is CharacterCreateEvent.RemoveFeat ->
                _state.value =
                    _state.value.copy(selectedFeats = _state.value.selectedFeats - event.value)

            is CharacterCreateEvent.LoadCharacter -> loadCharacter(event.character)
            is CharacterCreateEvent.StartFromTemplate -> startFromTemplate(event.character)
            CharacterCreateEvent.LoadTemplates -> loadTemplates()
            is CharacterCreateEvent.SaveCharacter -> saveCharacter()
            is CharacterCreateEvent.GenerateImage -> generateImage()
            is CharacterCreateEvent.GenerateItemImage -> generateItemImage(event.index)
            is CharacterCreateEvent.AiSizeChanged ->
                _state.value =
                    _state.value.copy(aiWidth = event.width, aiHeight = event.height)

            is CharacterCreateEvent.AiPromptChanged ->
                _state.value =
                    _state.value.copy(aiPrompt = event.value)
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

    private fun loadCharacter(character: Character) {
        _state.value = stateFromCharacter(character)
        tempId = character.id
        isEditing = true
        updateDefaultPrompt()
    }

    // Prefill the form from a saved template, but treat the result as a brand-new character
    // (fresh id, create mode) so saving produces new template + campaign char, not an overwrite.
    private fun startFromTemplate(character: Character) {
        _state.value = stateFromCharacter(character)
        tempId = "temp-char-${Random.nextInt(1000000, 9999999)}"
        isEditing = false
        updateDefaultPrompt()
    }

    private fun loadTemplates() {
        viewModelScope.launch {
            when (val res = remoteDataSource.getMyCharacters()) {
                is Result.Success ->
                    _state.value =
                        _state.value.copy(templates = res.data.templates.map { it.template })
                is Result.Error -> {}
            }
        }
    }

    private fun stateFromCharacter(character: Character): CharacterCreateState =
        CharacterCreateState(
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
            availableSpells = _state.value.availableSpells
        )

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

        _state.value = s.copy(isSaving = true, error = null)

        viewModelScope.launch {
            val result = repository.saveCharacter(character)
            // On create, also keep a reusable template in the master's personal session.
            if (result is Result.Success && !isEditing) {
                remoteDataSource.createMyCharacter(character)
            }
            when (result) {
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
