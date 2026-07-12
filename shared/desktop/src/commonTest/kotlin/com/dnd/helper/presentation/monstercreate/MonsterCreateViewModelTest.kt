package com.dnd.helper.presentation.monstercreate

import com.dnd.helper.data.remote.dto.common.ApiReferenceDto
import com.dnd.helper.data.remote.dto.common.ApiReferenceListDto
import com.dnd.helper.data.remote.dto.monster.MonsterActionDto
import com.dnd.helper.data.remote.dto.monster.MonsterProficiencyDto
import com.dnd.helper.data.remote.dto.monster.MonsterSpecialAbilityDto
import com.dnd.helper.domain.common.Result
import com.dnd.helper.domain.model.Monster
import com.dnd.helper.fakes.FakeCharacterRepository
import com.dnd.helper.fakes.FakeDndApiDataSource
import com.dnd.helper.fakes.FakeEditingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MonsterCreateViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: MonsterCreateViewModel
    private lateinit var fakeRepository: FakeCharacterRepository
    private lateinit var fakeEditingRepository: FakeEditingRepository
    private lateinit var fakeApiDataSource: FakeDndApiDataSource

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeCharacterRepository()
        fakeEditingRepository = FakeEditingRepository()
        fakeApiDataSource = FakeDndApiDataSource()

        // Setup some default API data
        fakeApiDataSource.getAlignmentsResult = Result.Success(ApiReferenceListDto(
            count = 1,
            results = listOf(ApiReferenceDto(name = "Lawful Good", index = "lawful-good"))
        ))
        fakeApiDataSource.getLanguagesResult = Result.Success(ApiReferenceListDto(
            count = 1,
            results = listOf(ApiReferenceDto(name = "Common", index = "common"))
        ))

        viewModel = MonsterCreateViewModel(
            repository = fakeRepository,
            editingRepository = fakeEditingRepository,
            api = fakeApiDataSource
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // === Initial State Tests ===

    @Test
    fun `initial state is empty`() = runTest(testDispatcher) {
        assertEquals("", viewModel.state.value.name)
        assertEquals("", viewModel.state.value.type)
        assertEquals("", viewModel.state.value.alignment)
        assertEquals("", viewModel.state.value.challengeRating)
        assertFalse(viewModel.state.value.isSaving)
        assertFalse(viewModel.state.value.isSaved)
    }

    @Test
    fun `initial state loads available data from API`() = runTest(testDispatcher) {
        // Wait for init
        kotlinx.coroutines.delay(100)

        assertTrue(viewModel.state.value.availableAlignments.isNotEmpty())
        assertTrue(viewModel.state.value.availableLanguages.isNotEmpty())
    }

    // === Basic Info Tests ===

    @Test
    fun `NameChanged updates name in state`() = runTest(testDispatcher) {
        viewModel.onEvent(MonsterCreateEvent.NameChanged("Dragon"))

        assertEquals("Dragon", viewModel.state.value.name)
    }

    @Test
    fun `DescriptionChanged updates description in state`() = runTest(testDispatcher) {
        viewModel.onEvent(MonsterCreateEvent.DescriptionChanged("A fierce dragon"))

        assertEquals("A fierce dragon", viewModel.state.value.description)
    }

    @Test
    fun `ChallengeRatingChanged updates challengeRating in state`() = runTest(testDispatcher) {
        viewModel.onEvent(MonsterCreateEvent.ChallengeRatingChanged("5"))

        assertEquals("5", viewModel.state.value.challengeRating)
    }

    @Test
    fun `TypeChanged updates type in state`() = runTest(testDispatcher) {
        viewModel.onEvent(MonsterCreateEvent.TypeChanged("Dragon"))

        assertEquals("Dragon", viewModel.state.value.type)
    }

    @Test
    fun `AlignmentChanged updates alignment in state`() = runTest(testDispatcher) {
        viewModel.onEvent(MonsterCreateEvent.AlignmentChanged("Chaotic Evil"))

        assertEquals("Chaotic Evil", viewModel.state.value.alignment)
    }

    @Test
    fun `SizeChanged updates size in state`() = runTest(testDispatcher) {
        viewModel.onEvent(MonsterCreateEvent.SizeChanged("Huge"))

        assertEquals("Huge", viewModel.state.value.size)
    }

    // === Combat Stats Tests ===

    @Test
    fun `MaxHpChanged updates maxHp in state`() = runTest(testDispatcher) {
        viewModel.onEvent(MonsterCreateEvent.MaxHpChanged("50"))

        assertEquals("50", viewModel.state.value.maxHp)
    }

    @Test
    fun `ArmorClassChanged updates armorClass in state`() = runTest(testDispatcher) {
        viewModel.onEvent(MonsterCreateEvent.ArmorClassChanged("18"))

        assertEquals("18", viewModel.state.value.armorClass)
    }

    @Test
    fun `SpeedChanged updates speed in state`() = runTest(testDispatcher) {
        viewModel.onEvent(MonsterCreateEvent.SpeedChanged("40"))

        assertEquals("40", viewModel.state.value.speed)
    }

    @Test
    fun `HitDiceChanged updates hitDice in state`() = runTest(testDispatcher) {
        viewModel.onEvent(MonsterCreateEvent.HitDiceChanged("4d8+4"))

        assertEquals("4d8+4", viewModel.state.value.hitDice)
    }

    // === Ability Scores Tests ===

    @Test
    fun `StrengthChanged updates strength in state`() = runTest(testDispatcher) {
        viewModel.onEvent(MonsterCreateEvent.StrengthChanged("18"))

        assertEquals("18", viewModel.state.value.strength)
    }

    @Test
    fun `DexterityChanged updates dexterity in state`() = runTest(testDispatcher) {
        viewModel.onEvent(MonsterCreateEvent.DexterityChanged("16"))

        assertEquals("16", viewModel.state.value.dexterity)
    }

    @Test
    fun `ConstitutionChanged updates constitution in state`() = runTest(testDispatcher) {
        viewModel.onEvent(MonsterCreateEvent.ConstitutionChanged("14"))

        assertEquals("14", viewModel.state.value.constitution)
    }

    @Test
    fun `IntelligenceChanged updates intelligence in state`() = runTest(testDispatcher) {
        viewModel.onEvent(MonsterCreateEvent.IntelligenceChanged("12"))

        assertEquals("12", viewModel.state.value.intelligence)
    }

    @Test
    fun `WisdomChanged updates wisdom in state`() = runTest(testDispatcher) {
        viewModel.onEvent(MonsterCreateEvent.WisdomChanged("10"))

        assertEquals("10", viewModel.state.value.wisdom)
    }

    @Test
    fun `CharismaChanged updates charisma in state`() = runTest(testDispatcher) {
        viewModel.onEvent(MonsterCreateEvent.CharismaChanged("8"))

        assertEquals("8", viewModel.state.value.charisma)
    }

    // === Languages and Immunities Tests ===

    @Test
    fun `AddLanguage adds language to selectedLanguages`() = runTest(testDispatcher) {
        viewModel.onEvent(MonsterCreateEvent.AddLanguage("Draconic"))

        assertTrue("Draconic" in viewModel.state.value.selectedLanguages)
    }

    @Test
    fun `RemoveLanguage removes language from selectedLanguages`() = runTest(testDispatcher) {
        viewModel.onEvent(MonsterCreateEvent.AddLanguage("Draconic"))
        viewModel.onEvent(MonsterCreateEvent.RemoveLanguage("Draconic"))

        assertFalse("Draconic" in viewModel.state.value.selectedLanguages)
    }

    @Test
    fun `AddConditionImmunity adds immunity to selectedConditionImmunities`() = runTest(testDispatcher) {
        viewModel.onEvent(MonsterCreateEvent.AddConditionImmunity("Charmed"))

        assertTrue("Charmed" in viewModel.state.value.selectedConditionImmunities)
    }

    @Test
    fun `RemoveConditionImmunity removes immunity from selectedConditionImmunities`() = runTest(testDispatcher) {
        viewModel.onEvent(MonsterCreateEvent.AddConditionImmunity("Charmed"))
        viewModel.onEvent(MonsterCreateEvent.RemoveConditionImmunity("Charmed"))

        assertFalse("Charmed" in viewModel.state.value.selectedConditionImmunities)
    }

    @Test
    fun `AddDamageImmunity adds immunity to selectedDamageImmunities`() = runTest(testDispatcher) {
        viewModel.onEvent(MonsterCreateEvent.AddDamageImmunity("Fire"))

        assertTrue("Fire" in viewModel.state.value.selectedDamageImmunities)
    }

    @Test
    fun `RemoveDamageImmunity removes immunity from selectedDamageImmunities`() = runTest(testDispatcher) {
        viewModel.onEvent(MonsterCreateEvent.AddDamageImmunity("Fire"))
        viewModel.onEvent(MonsterCreateEvent.RemoveDamageImmunity("Fire"))

        assertFalse("Fire" in viewModel.state.value.selectedDamageImmunities)
    }

    @Test
    fun `AddDamageResistance adds resistance to selectedDamageResistances`() = runTest(testDispatcher) {
        viewModel.onEvent(MonsterCreateEvent.AddDamageResistance("Cold"))

        assertTrue("Cold" in viewModel.state.value.selectedDamageResistances)
    }

    @Test
    fun `RemoveDamageResistance removes resistance from selectedDamageResistances`() = runTest(testDispatcher) {
        viewModel.onEvent(MonsterCreateEvent.AddDamageResistance("Cold"))
        viewModel.onEvent(MonsterCreateEvent.RemoveDamageResistance("Cold"))

        assertFalse("Cold" in viewModel.state.value.selectedDamageResistances)
    }

    @Test
    fun `AddDamageVulnerability adds vulnerability to selectedDamageVulnerabilities`() = runTest(testDispatcher) {
        viewModel.onEvent(MonsterCreateEvent.AddDamageVulnerability("Radiant"))

        assertTrue("Radiant" in viewModel.state.value.selectedDamageVulnerabilities)
    }

    @Test
    fun `RemoveDamageVulnerability removes vulnerability from selectedDamageVulnerabilities`() = runTest(testDispatcher) {
        viewModel.onEvent(MonsterCreateEvent.AddDamageVulnerability("Radiant"))
        viewModel.onEvent(MonsterCreateEvent.RemoveDamageVulnerability("Radiant"))

        assertFalse("Radiant" in viewModel.state.value.selectedDamageVulnerabilities)
    }

    // === Abilities and Actions Tests ===

    @Test
    fun `AddSpecialAbility adds ability to specialAbilities`() = runTest(testDispatcher) {
        val ability = MonsterSpecialAbilityDto(
            name = "Frightful Presence",
            desc = "AoE fear"
        )

        viewModel.onEvent(MonsterCreateEvent.AddSpecialAbility(ability))

        assertTrue(viewModel.state.value.specialAbilities.contains(ability))
    }

    @Test
    fun `RemoveSpecialAbility removes ability from specialAbilities`() = runTest(testDispatcher) {
        val ability = MonsterSpecialAbilityDto(
            name = "Frightful Presence",
            desc = "AoE fear"
        )

        viewModel.onEvent(MonsterCreateEvent.AddSpecialAbility(ability))
        viewModel.onEvent(MonsterCreateEvent.RemoveSpecialAbility(ability))

        assertFalse(viewModel.state.value.specialAbilities.contains(ability))
    }

    @Test
    fun `AddAction adds action to actions`() = runTest(testDispatcher) {
        val action = MonsterActionDto(
            name = "Claw",
            desc = "Melee attack"
        )

        viewModel.onEvent(MonsterCreateEvent.AddAction(action))

        assertTrue(viewModel.state.value.actions.contains(action))
    }

    @Test
    fun `RemoveAction removes action from actions`() = runTest(testDispatcher) {
        val action = MonsterActionDto(
            name = "Claw",
            desc = "Melee attack"
        )

        viewModel.onEvent(MonsterCreateEvent.AddAction(action))
        viewModel.onEvent(MonsterCreateEvent.RemoveAction(action))

        assertFalse(viewModel.state.value.actions.contains(action))
    }

    @Test
    fun `AddLegendaryAction adds action to legendaryActions`() = runTest(testDispatcher) {
        val action = MonsterActionDto(
            name = "Detect",
            desc = "Detects enemies"
        )

        viewModel.onEvent(MonsterCreateEvent.AddLegendaryAction(action))

        assertTrue(viewModel.state.value.legendaryActions.contains(action))
    }

    @Test
    fun `RemoveLegendaryAction removes action from legendaryActions`() = runTest(testDispatcher) {
        val action = MonsterActionDto(
            name = "Detect",
            desc = "Detects enemies"
        )

        viewModel.onEvent(MonsterCreateEvent.AddLegendaryAction(action))
        viewModel.onEvent(MonsterCreateEvent.RemoveLegendaryAction(action))

        assertFalse(viewModel.state.value.legendaryActions.contains(action))
    }

    @Test
    fun `AddReaction adds action to reactions`() = runTest(testDispatcher) {
        val action = MonsterActionDto(
            name = "Parry",
            desc = "Parry attack"
        )

        viewModel.onEvent(MonsterCreateEvent.AddReaction(action))

        assertTrue(viewModel.state.value.reactions.contains(action))
    }

    @Test
    fun `RemoveReaction removes action from reactions`() = runTest(testDispatcher) {
        val action = MonsterActionDto(
            name = "Parry",
            desc = "Parry attack"
        )

        viewModel.onEvent(MonsterCreateEvent.AddReaction(action))
        viewModel.onEvent(MonsterCreateEvent.RemoveReaction(action))

        assertFalse(viewModel.state.value.reactions.contains(action))
    }

    @Test
    fun `AddProficiency adds proficiency to monsterProficiencies`() = runTest(testDispatcher) {
        val proficiency = MonsterProficiencyDto(
            value = 2,
            proficiency = ApiReferenceDto(name = "Skill: Perception", index = "perception")
        )
        viewModel.onEvent(MonsterCreateEvent.AddProficiency(proficiency))

        assertTrue(proficiency in viewModel.state.value.monsterProficiencies)
    }

    @Test
    fun `RemoveProficiency removes proficiency from monsterProficiencies`() = runTest(testDispatcher) {
        val proficiency = MonsterProficiencyDto(
            value = 2,
            proficiency = ApiReferenceDto(name = "Skill: Perception", index = "perception")
        )
        viewModel.onEvent(MonsterCreateEvent.AddProficiency(proficiency))
        viewModel.onEvent(MonsterCreateEvent.RemoveProficiency(proficiency))

        assertFalse(proficiency in viewModel.state.value.monsterProficiencies)
    }

    // === Save Monster Tests ===

    @Test
    fun `SaveMonster saves to repository`() = runTest(testDispatcher) {
        fakeRepository.saveMonsterResult = Result.Success(Unit)

        viewModel.onEvent(MonsterCreateEvent.NameChanged("Test Monster"))
        viewModel.onEvent(MonsterCreateEvent.SaveMonster)

        // Wait for save
        kotlinx.coroutines.delay(100)

        assertTrue(fakeRepository.saveMonsterCalls > 0)
        assertFalse(viewModel.state.value.isSaving)
        assertTrue(viewModel.state.value.isSaved)
    }

    @Test
    fun `SaveMonster shows error on failure`() = runTest(testDispatcher) {
        fakeRepository.saveMonsterResult = Result.Error(com.dnd.helper.domain.common.AppError.Network)

        viewModel.onEvent(MonsterCreateEvent.NameChanged("Test Monster"))
        viewModel.onEvent(MonsterCreateEvent.SaveMonster)

        // Wait for save
        kotlinx.coroutines.delay(100)

        assertFalse(viewModel.state.value.isSaving)
        assertNotNull(viewModel.state.value.error)
    }

    // === Load Monster Tests ===

    @Test
    fun `LoadMonster populates state with monster data`() = runTest(testDispatcher) {
        val monster = Monster(
            id = "monster-1",
            name = "Red Dragon",
            description = "Ancient red dragon",
            challengeRating = "20",
            type = "Dragon",
            alignment = "Chaotic Evil",
            size = "Huge",
            maxHp = 300,
            currentHp = 300,
            armorClass = 22,
            hitDice = "20d12+200",
            speed = 40,
        )

        viewModel.onEvent(MonsterCreateEvent.LoadMonster(monster))

        assertEquals("Red Dragon", viewModel.state.value.name)
        assertEquals("Dragon", viewModel.state.value.type)
        assertEquals("Chaotic Evil", viewModel.state.value.alignment)
        assertEquals("20", viewModel.state.value.challengeRating)
        assertEquals("Huge", viewModel.state.value.size)
        assertEquals("300", viewModel.state.value.maxHp)
        assertEquals("22", viewModel.state.value.armorClass)
    }

    // === Image Generation Tests ===

    @Test
    fun `GenerateImage starts generation and updates imageUrl`() = runTest(testDispatcher) {
        viewModel.onEvent(MonsterCreateEvent.NameChanged("Dragon"))
        viewModel.onEvent(MonsterCreateEvent.GenerateImage)

        // Wait for generation
        kotlinx.coroutines.delay(100)

        assertEquals("monster", fakeEditingRepository.lastStartedEntityType)
        assertTrue(viewModel.state.value.imageUrl.startsWith("generating:"))
    }

    // === Edge Cases ===

    @Test
    fun `handles rapid field updates correctly`() = runTest(testDispatcher) {
        repeat(3) { index ->
            viewModel.onEvent(MonsterCreateEvent.StrengthChanged(index.toString()))
        }

        assertEquals("2", viewModel.state.value.strength)
    }

    @Test
    fun `SaveMonster uses defaults for empty fields`() = runTest(testDispatcher) {
        fakeRepository.saveMonsterResult = Result.Success(Unit)

        viewModel.onEvent(MonsterCreateEvent.SaveMonster)

        // Wait for save
        kotlinx.coroutines.delay(100)

        assertTrue(fakeRepository.saveMonsterCalls > 0)
        // Should save with default values for empty fields
        assertTrue(viewModel.state.value.isSaved)
    }
}
