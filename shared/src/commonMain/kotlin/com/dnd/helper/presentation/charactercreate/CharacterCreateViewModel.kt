package com.dnd.helper.presentation.charactercreate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import com.dnd.helper.domain.model.Skill
import com.dnd.helper.domain.model.Weapon
import com.dnd.helper.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.time.Clock

class CharacterCreateViewModel(
    private val repository: CharacterRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CharacterCreateState())
    val state: StateFlow<CharacterCreateState> = _state.asStateFlow()

    fun onEvent(event: CharacterCreateEvent) {
        when (event) {
            // Basic info
            is CharacterCreateEvent.NameChanged -> _state.value = _state.value.copy(name = event.value)
            is CharacterCreateEvent.PlayerNameChanged -> _state.value = _state.value.copy(playerName = event.value)
            is CharacterCreateEvent.RaceChanged -> _state.value = _state.value.copy(race = event.value)
            is CharacterCreateEvent.ClassChanged -> _state.value = _state.value.copy(characterClass = event.value)
            is CharacterCreateEvent.SubclassChanged -> _state.value = _state.value.copy(subclass = event.value)
            is CharacterCreateEvent.BackgroundChanged -> _state.value = _state.value.copy(background = event.value)
            is CharacterCreateEvent.LevelChanged -> _state.value = _state.value.copy(level = event.value)
            is CharacterCreateEvent.ExperiencePointsChanged -> _state.value = _state.value.copy(experiencePoints = event.value)
            is CharacterCreateEvent.DescriptionChanged -> _state.value = _state.value.copy(description = event.value)
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
            is CharacterCreateEvent.SkillsChanged -> _state.value = _state.value.copy(skills = event.value)
            is CharacterCreateEvent.ArmorProficienciesChanged -> _state.value = _state.value.copy(armorProficiencies = event.value)
            is CharacterCreateEvent.WeaponProficienciesChanged -> _state.value = _state.value.copy(weaponProficiencies = event.value)
            is CharacterCreateEvent.ToolProficienciesChanged -> _state.value = _state.value.copy(toolProficiencies = event.value)
            is CharacterCreateEvent.LanguagesChanged -> _state.value = _state.value.copy(languages = event.value)

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
            is CharacterCreateEvent.ClassFeaturesChanged -> _state.value = _state.value.copy(classFeatures = event.value)
            is CharacterCreateEvent.RacialTraitsChanged -> _state.value = _state.value.copy(racialTraits = event.value)
            is CharacterCreateEvent.FeatsChanged -> _state.value = _state.value.copy(feats = event.value)

            CharacterCreateEvent.SaveCharacter -> saveCharacter()
        }
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

    private fun getRandomId(): Int {
        return Random.nextInt()
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
            id = "char-${getRandomId()}",
            name = s.name.trim(),
            playerName = s.playerName.trim(),
            race = s.race.trim(),
            characterClass = s.characterClass.trim(),
            subclass = s.subclass.trim(),
            background = s.background.trim(),
            level = level,
            experiencePoints = experiencePoints,
            description = s.description.trim(),
            _imageUrl = s.imageUrl.trim().ifBlank { null },
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
                skills = parseCommaList(s.skills),
                armor = parseCommaList(s.armorProficiencies),
                weapons = parseCommaList(s.weaponProficiencies),
                tools = parseCommaList(s.toolProficiencies),
                languages = parseCommaList(s.languages),
            ),
            weapons = s.weapons,
            features = CharacterFeatures(
                classFeatures = parseLineList(s.classFeatures),
                racialTraits = parseLineList(s.racialTraits),
                feats = parseLineList(s.feats),
            ),
            skills = s.skillList,
            items = s.items,
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
