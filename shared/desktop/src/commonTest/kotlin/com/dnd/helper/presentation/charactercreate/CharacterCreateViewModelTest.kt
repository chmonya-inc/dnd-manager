package com.dnd.helper.presentation.charactercreate

import com.dnd.helper.data.remote.dto.character.ClassDto
import com.dnd.helper.data.remote.dto.character.RaceDto
import com.dnd.helper.data.remote.dto.common.ApiReferenceDto
import com.dnd.helper.data.remote.dto.common.ApiReferenceListDto
import com.dnd.helper.domain.common.Result
import com.dnd.helper.domain.model.Character
import com.dnd.helper.fakes.FakeCharacterRepository
import com.dnd.helper.fakes.FakeCharacterStorage
import com.dnd.helper.fakes.FakeDndApiDataSource
import com.dnd.helper.fakes.FakeEditingRepository
import com.dnd.helper.fakes.FakeRemoteDataSource
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
class CharacterCreateViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: CharacterCreateViewModel
    private lateinit var fakeRepository: FakeCharacterRepository
    private lateinit var fakeRemoteDataSource: FakeRemoteDataSource
    private lateinit var fakeEditingRepository: FakeEditingRepository
    private lateinit var fakeApiDataSource: FakeDndApiDataSource
    private lateinit var fakeStorage: FakeCharacterStorage

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeCharacterRepository()
        fakeRemoteDataSource = FakeRemoteDataSource()
        fakeEditingRepository = FakeEditingRepository()
        fakeStorage = FakeCharacterStorage()
        fakeApiDataSource = FakeDndApiDataSource()

        // Setup fake API data
        fakeApiDataSource.getClassesResult = Result.Success(ApiReferenceListDto(
            count = 1,
            results = listOf(ApiReferenceDto(index = "fighter", name = "Fighter", url = "/api/classes/fighter"))
        ))
        fakeApiDataSource.getRacesResult = Result.Success(ApiReferenceListDto(
            count = 1,
            results = listOf(ApiReferenceDto(index = "human", name = "Human", url = "/api/races/human"))
        ))

        viewModel = CharacterCreateViewModel(
            repository = fakeRepository,
            remoteDataSource = fakeRemoteDataSource,
            editingRepository = fakeEditingRepository,
            api = fakeApiDataSource,
            storage = fakeStorage
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // === Initial State Tests ===

    @Test
    fun `initial state is empty`() = runTest(testDispatcher) {
        // Wait for any init-related state changes (like loading API data)
        kotlinx.coroutines.delay(100)

        assertEquals("", viewModel.state.value.name)
        assertEquals("", viewModel.state.value.playerName)
        assertEquals("", viewModel.state.value.race)
        assertEquals("", viewModel.state.value.characterClass)
        assertFalse(viewModel.state.value.isSaving)
        assertFalse(viewModel.state.value.isSaved)
    }

    @Test
    fun `initial state loads available data from API`() = runTest(testDispatcher) {
        // Wait for init
        kotlinx.coroutines.delay(100)

        assertTrue(viewModel.state.value.availableClasses.isNotEmpty())
        assertTrue(viewModel.state.value.availableRaces.isNotEmpty())
    }

    // === Basic Info Tests ===

    @Test
    fun `NameChanged updates name in state`() = runTest(testDispatcher) {
        viewModel.onEvent(CharacterCreateEvent.NameChanged("Test Character"))

        assertEquals("Test Character", viewModel.state.value.name)
    }

    @Test
    fun `PlayerNameChanged updates playerName in state`() = runTest(testDispatcher) {
        viewModel.onEvent(CharacterCreateEvent.PlayerNameChanged("Player"))

        assertEquals("Player", viewModel.state.value.playerName)
    }

    @Test
    fun `RaceChanged updates race and loads subraces`() = runTest(testDispatcher) {
        fakeApiDataSource.getRaceResult = Result.Success(RaceDto(
            index = "human",
            name = "Human",
            url = "/api/races/human",
            subraces = listOf(ApiReferenceDto(index = "test-subrace", name = "Test Subrace"))
        ))

        viewModel.onEvent(CharacterCreateEvent.RaceChanged("Human"))

        assertEquals("Human", viewModel.state.value.race)
        assertEquals("", viewModel.state.value.subrace)

        // Wait for async operations
        kotlinx.coroutines.delay(100)
        assertEquals(1, viewModel.state.value.availableSubraces.size)
    }

    @Test
    fun `ClassChanged updates class and loads subclasses`() = runTest(testDispatcher) {
        fakeApiDataSource.getClassResult = Result.Success(ClassDto(
            index = "fighter",
            name = "Fighter",
            url = "/api/classes/fighter",
            subclasses = listOf(ApiReferenceDto(index = "test-subclass", name = "Test Subclass"))
        ))

        viewModel.onEvent(CharacterCreateEvent.ClassChanged("Fighter"))

        assertEquals("Fighter", viewModel.state.value.characterClass)
        assertEquals("", viewModel.state.value.subclass)

        // Wait for async operations
        kotlinx.coroutines.delay(100)
        assertEquals(1, viewModel.state.value.availableSubclasses.size)
    }

    @Test
    fun `LevelChanged updates level in state`() = runTest(testDispatcher) {
        viewModel.onEvent(CharacterCreateEvent.LevelChanged("5"))

        assertEquals("5", viewModel.state.value.level)
    }

    // === Appearance Tests ===

    @Test
    fun `AgeChanged updates age in state`() = runTest(testDispatcher) {
        viewModel.onEvent(CharacterCreateEvent.AgeChanged("25"))

        assertEquals("25", viewModel.state.value.age)
    }

    @Test
    fun `GenderChanged updates gender in state`() = runTest(testDispatcher) {
        viewModel.onEvent(CharacterCreateEvent.GenderChanged("Male"))

        assertEquals("Male", viewModel.state.value.gender)
    }

    // === Stats Tests ===

    @Test
    fun `StrengthChanged updates strength in state`() = runTest(testDispatcher) {
        viewModel.onEvent(CharacterCreateEvent.StrengthChanged("16"))

        assertEquals("16", viewModel.state.value.strength)
    }

    @Test
    fun `DexterityChanged updates dexterity in state`() = runTest(testDispatcher) {
        viewModel.onEvent(CharacterCreateEvent.DexterityChanged("14"))

        assertEquals("14", viewModel.state.value.dexterity)
    }

    // === HP & Combat Tests ===

    @Test
    fun `MaxHpChanged updates maxHp in state`() = runTest(testDispatcher) {
        viewModel.onEvent(CharacterCreateEvent.MaxHpChanged("30"))

        assertEquals("30", viewModel.state.value.maxHp)
    }

    @Test
    fun `CurrentHpChanged updates currentHp in state`() = runTest(testDispatcher) {
        viewModel.onEvent(CharacterCreateEvent.CurrentHpChanged("25"))

        assertEquals("25", viewModel.state.value.currentHp)
    }

    @Test
    fun `ArmorClassChanged updates armorClass in state`() = runTest(testDispatcher) {
        viewModel.onEvent(CharacterCreateEvent.ArmorClassChanged("18"))

        assertEquals("18", viewModel.state.value.armorClass)
    }

    // === Proficiency Tests ===

    @Test
    fun `AddLanguage adds language to selectedLanguages`() = runTest(testDispatcher) {
        viewModel.onEvent(CharacterCreateEvent.AddLanguage("Elven"))

        assertTrue("Elven" in viewModel.state.value.selectedLanguages)
    }

    @Test
    fun `RemoveLanguage removes language from selectedLanguages`() = runTest(testDispatcher) {
        viewModel.onEvent(CharacterCreateEvent.AddLanguage("Elven"))
        viewModel.onEvent(CharacterCreateEvent.RemoveLanguage("Elven"))

        assertFalse("Elven" in viewModel.state.value.selectedLanguages)
    }

    @Test
    fun `AddProficiencySkill adds skill to selectedSkills`() = runTest(testDispatcher) {
        viewModel.onEvent(CharacterCreateEvent.AddProficiencySkill("Athletics"))

        assertTrue("Athletics" in viewModel.state.value.selectedSkills)
    }

    @Test
    fun `RemoveProficiencySkill removes skill from selectedSkills`() = runTest(testDispatcher) {
        viewModel.onEvent(CharacterCreateEvent.AddProficiencySkill("Athletics"))
        viewModel.onEvent(CharacterCreateEvent.RemoveProficiencySkill("Athletics"))

        assertFalse("Athletics" in viewModel.state.value.selectedSkills)
    }

    // === Item Tests ===

    @Test
    fun `AddItem adds new item to items`() = runTest(testDispatcher) {
        val initialSize = viewModel.state.value.items.size

        viewModel.onEvent(CharacterCreateEvent.AddItem)

        assertEquals(initialSize + 1, viewModel.state.value.items.size)
        assertEquals("New Item", viewModel.state.value.items.last().name)
    }

    @Test
    fun `RemoveItem removes item from items`() = runTest(testDispatcher) {
        viewModel.onEvent(CharacterCreateEvent.AddItem)
        val initialSize = viewModel.state.value.items.size

        viewModel.onEvent(CharacterCreateEvent.RemoveItem(initialSize - 1))

        assertEquals(initialSize - 1, viewModel.state.value.items.size)
    }

    @Test
    fun `ItemNameChanged updates item name`() = runTest(testDispatcher) {
        viewModel.onEvent(CharacterCreateEvent.AddItem)
        val index = viewModel.state.value.items.size - 1

        viewModel.onEvent(CharacterCreateEvent.ItemNameChanged(index, "Sword"))

        assertEquals("Sword", viewModel.state.value.items[index].name)
    }

    // === Weapon Tests ===

    @Test
    fun `AddWeapon adds new weapon to weapons`() = runTest(testDispatcher) {
        val initialSize = viewModel.state.value.weapons.size

        viewModel.onEvent(CharacterCreateEvent.AddWeapon)

        assertEquals(initialSize + 1, viewModel.state.value.weapons.size)
        assertEquals("New Weapon", viewModel.state.value.weapons.last().name)
    }

    @Test
    fun `RemoveWeapon removes weapon from weapons`() = runTest(testDispatcher) {
        viewModel.onEvent(CharacterCreateEvent.AddWeapon)
        val initialSize = viewModel.state.value.weapons.size

        viewModel.onEvent(CharacterCreateEvent.RemoveWeapon(initialSize - 1))

        assertEquals(initialSize - 1, viewModel.state.value.weapons.size)
    }

    @Test
    fun `WeaponNameChanged updates weapon name`() = runTest(testDispatcher) {
        viewModel.onEvent(CharacterCreateEvent.AddWeapon)
        val index = viewModel.state.value.weapons.size - 1

        viewModel.onEvent(CharacterCreateEvent.WeaponNameChanged(index, "Longsword"))

        assertEquals("Longsword", viewModel.state.value.weapons[index].name)
    }

    // === Spell Tests ===

    @Test
    fun `AddSpell adds new spell to spellList`() = runTest(testDispatcher) {
        val initialSize = viewModel.state.value.spellList.size

        viewModel.onEvent(CharacterCreateEvent.AddSpell)

        assertEquals(initialSize + 1, viewModel.state.value.spellList.size)
        assertEquals("New Spell", viewModel.state.value.spellList.last().name)
    }

    @Test
    fun `RemoveSpell removes spell from spellList`() = runTest(testDispatcher) {
        viewModel.onEvent(CharacterCreateEvent.AddSpell)
        val initialSize = viewModel.state.value.spellList.size

        viewModel.onEvent(CharacterCreateEvent.RemoveSpell(initialSize - 1))

        assertEquals(initialSize - 1, viewModel.state.value.spellList.size)
    }

    @Test
    fun `SpellNameChanged updates spell name`() = runTest(testDispatcher) {
        viewModel.onEvent(CharacterCreateEvent.AddSpell)
        val index = viewModel.state.value.spellList.size - 1

        viewModel.onEvent(CharacterCreateEvent.SpellNameChanged(index, "Fireball"))

        assertEquals("Fireball", viewModel.state.value.spellList[index].name)
    }

    // === Save Character Tests ===

    @Test
    fun `SaveCharacter validates name is required`() = runTest(testDispatcher) {
        viewModel.onEvent(CharacterCreateEvent.SaveCharacter)

        assertNotNull(viewModel.state.value.error)
        assertTrue(viewModel.state.value.error?.contains("required") == true)
    }

    @Test
    fun `SaveCharacter with valid data saves to repository`() = runTest(testDispatcher) {
        fakeRepository.saveCharacterResult = Result.Success(Unit)

        viewModel.onEvent(CharacterCreateEvent.NameChanged("Test Character"))
        viewModel.onEvent(CharacterCreateEvent.SaveCharacter)

        // Wait for save
        kotlinx.coroutines.delay(100)

        assertTrue(fakeRepository.saveCharacterCalls > 0)
        assertFalse(viewModel.state.value.isSaving)
        assertTrue(viewModel.state.value.isSaved)
    }

    @Test
    fun `SaveCharacter shows error on failure`() = runTest(testDispatcher) {
        fakeRepository.saveCharacterResult = Result.Error(com.dnd.helper.domain.common.AppError.Network)

        viewModel.onEvent(CharacterCreateEvent.NameChanged("Test Character"))
        viewModel.onEvent(CharacterCreateEvent.SaveCharacter)

        // Wait for save
        kotlinx.coroutines.delay(100)

        assertFalse(viewModel.state.value.isSaving)
        assertNotNull(viewModel.state.value.error)
    }

    // === Load Character Tests ===

    @Test
    fun `LoadCharacter populates state with character data`() = runTest(testDispatcher) {
        val character = Character(
            id = "char-1",
            name = "Loaded Character",
            playerName = "Player",
            race = "Elf",
            characterClass = "Wizard",
            level = 3,
            description = "Test",
            maxHp = 20,
            currentHp = 18,
        )

        viewModel.onEvent(CharacterCreateEvent.LoadCharacter(character))

        assertEquals("Loaded Character", viewModel.state.value.name)
        assertEquals("Elf", viewModel.state.value.race)
        assertEquals("Wizard", viewModel.state.value.characterClass)
        assertEquals("3", viewModel.state.value.level)
    }

    // === Image Generation Tests ===

    @Test
    fun `GenerateImage starts generation and updates imageUrl`() = runTest(testDispatcher) {
        viewModel.onEvent(CharacterCreateEvent.NameChanged("Test Character"))
        viewModel.onEvent(CharacterCreateEvent.GenerateImage)

        assertEquals("character", fakeEditingRepository.lastStartedEntityType)
        assertTrue(viewModel.state.value.imageUrl.startsWith("generating:"))
    }

    @Test
    fun `GenerateItemImage starts item generation`() = runTest(testDispatcher) {
        viewModel.onEvent(CharacterCreateEvent.AddItem)
        val index = viewModel.state.value.items.size - 1

        viewModel.onEvent(CharacterCreateEvent.ItemNameChanged(index, "Sword"))
        viewModel.onEvent(CharacterCreateEvent.GenerateItemImage(index))

        assertEquals("item", fakeEditingRepository.lastStartedEntityType)
        assertTrue(viewModel.state.value.items[index].imageUrl?.startsWith("generating:") == true)
    }

    // === Edge Cases ===

    @Test
    fun `handles rapid field updates correctly`() = runTest(testDispatcher) {
        repeat(3) { index ->
            viewModel.onEvent(CharacterCreateEvent.StrengthChanged(index.toString()))
        }

        assertEquals("2", viewModel.state.value.strength)
    }

    @Test
    fun `LoadTemplates loads templates from remote data source`() = runTest(testDispatcher) {
        val character = Character(
            id = "char-1",
            name = "Template Character",
            playerName = "Player",
            race = "Human",
            characterClass = "Fighter",
            level = 1,
            description = "Template",
            maxHp = 10,
            currentHp = 10,
        )

        fakeRemoteDataSource.myCharactersResponse = com.dnd.helper.data.remote.dto.auth.MyCharactersResponse(
            templates = listOf(com.dnd.helper.data.remote.dto.auth.CharacterTemplateDto(
                template = character,
                instances = emptyList()
            )),
            standaloneInstances = emptyList()
        )

        viewModel.onEvent(CharacterCreateEvent.LoadTemplates)

        // Wait for load
        kotlinx.coroutines.delay(100)

        assertTrue(viewModel.state.value.templates.isNotEmpty())
        assertEquals("Template Character", viewModel.state.value.templates[0].name)
    }
}
