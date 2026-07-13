package com.dnd.helper.presentation.charactercreate

import com.dnd.helper.data.remote.dto.character.ClassDto
import com.dnd.helper.data.remote.dto.character.RaceDto
import com.dnd.helper.data.remote.dto.common.ApiReferenceDto
import com.dnd.helper.data.remote.dto.common.ApiReferenceListDto
import com.dnd.helper.domain.common.Result
import com.dnd.helper.domain.model.Character
import com.dnd.helper.fakes.FakeCharacterStorage
import com.dnd.helper.fakes.FakeDndApiDataSource
import com.dnd.helper.fakes.FakeEditingRepository
import com.dnd.helper.fakes.FakeRemoteDataSourceForStart
import kotlinx.coroutines.CoroutineScope
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerCharacterCreateViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeRemoteDataSource: FakeRemoteDataSourceForStart
    private lateinit var fakeEditingRepository: FakeEditingRepository
    private lateinit var fakeApiDataSource: FakeDndApiDataSource
    private lateinit var fakeStorage: FakeCharacterStorage

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRemoteDataSource = FakeRemoteDataSourceForStart()
        fakeEditingRepository = FakeEditingRepository()
        fakeApiDataSource = FakeDndApiDataSource()
        fakeStorage = FakeCharacterStorage()

        // Setup default API data
        fakeApiDataSource.getClassesResult = Result.Success(ApiReferenceListDto(
            count = 1,
            results = listOf(ApiReferenceDto(name = "Fighter", index = "fighter"))
        ))
        fakeApiDataSource.getRacesResult = Result.Success(ApiReferenceListDto(
            count = 1,
            results = listOf(ApiReferenceDto(name = "Human", index = "human"))
        ))
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(scope: CoroutineScope): PlayerCharacterCreateViewModel {
        return PlayerCharacterCreateViewModel(
            remoteDataSource = fakeRemoteDataSource,
            editingRepository = fakeEditingRepository,
            api = fakeApiDataSource,
            storage = fakeStorage,
            coroutineScope = scope
        )
    }

    // === Initial State Tests ===

    @Test
    fun `initial state is empty`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        assertEquals("", viewModel.state.value.name)
        assertEquals("", viewModel.state.value.playerName)
        assertEquals("", viewModel.state.value.race)
        assertEquals("", viewModel.state.value.characterClass)
        assertFalse(viewModel.state.value.isSaving)
        assertFalse(viewModel.state.value.isSaved)
    }

    @Test
    fun `initial state loads available data from API`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        // Wait for init
        kotlinx.coroutines.delay(100)

        assertTrue(viewModel.state.value.availableClasses.isNotEmpty())
        assertTrue(viewModel.state.value.availableRaces.isNotEmpty())
    }

    // === Basic Info Tests ===

    @Test
    fun `NameChanged updates name in state`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        viewModel.onEvent(PlayerCharacterCreateEvent.NameChanged("Test Character"))

        assertEquals("Test Character", viewModel.state.value.name)
    }

    @Test
    fun `PlayerNameChanged updates playerName in state`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        viewModel.onEvent(PlayerCharacterCreateEvent.PlayerNameChanged("Player"))

        assertEquals("Player", viewModel.state.value.playerName)
    }

    @Test
    fun `RaceChanged updates race and loads subraces`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        fakeApiDataSource.getRaceResult = Result.Success(RaceDto(
            subraces = listOf(ApiReferenceDto(name = "Mountain Dwarf", index = "mountain-dwarf"))
        ))

        viewModel.onEvent(PlayerCharacterCreateEvent.RaceChanged("Dwarf"))

        assertEquals("Dwarf", viewModel.state.value.race)
        assertEquals("", viewModel.state.value.subrace) // Should reset subrace

        kotlinx.coroutines.delay(100)
        assertTrue(viewModel.state.value.availableSubraces.isNotEmpty())
    }

    @Test
    fun `ClassChanged updates class and loads subclasses`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        fakeApiDataSource.getClassResult = Result.Success(ClassDto(
            subclasses = listOf(ApiReferenceDto(name = "Champion", index = "champion"))
        ))

        viewModel.onEvent(PlayerCharacterCreateEvent.ClassChanged("Fighter"))

        assertEquals("Fighter", viewModel.state.value.characterClass)
        assertEquals("", viewModel.state.value.subclass) // Should reset subclass

        kotlinx.coroutines.delay(100)
        assertTrue(viewModel.state.value.availableSubclasses.isNotEmpty())
    }

    // === Save Character Tests ===

    @Test
    fun `SaveCharacter validates name is required`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        viewModel.onEvent(PlayerCharacterCreateEvent.SaveCharacter)

        assertNotNull(viewModel.state.value.error)
        assertTrue(viewModel.state.value.error?.contains("required") == true)
    }

    @Test
    fun `SaveCharacter saves to remote data source`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        fakeRemoteDataSource.createMyCharacterResult = Result.Success(Unit)

        viewModel.onEvent(PlayerCharacterCreateEvent.NameChanged("Test Character"))
        viewModel.onEvent(PlayerCharacterCreateEvent.SaveCharacter)

        // Wait for save
        kotlinx.coroutines.delay(100)

        assertTrue(fakeRemoteDataSource.createMyCharacterCalls > 0)
        assertFalse(viewModel.state.value.isSaving)
        assertTrue(viewModel.state.value.isSaved)
    }

    @Test
    fun `SaveCharacter shows error on failure`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        fakeRemoteDataSource.createMyCharacterResult = Result.Error(com.dnd.helper.domain.common.AppError.Network)

        viewModel.onEvent(PlayerCharacterCreateEvent.NameChanged("Test Character"))
        viewModel.onEvent(PlayerCharacterCreateEvent.SaveCharacter)

        // Wait for save
        kotlinx.coroutines.delay(100)

        assertFalse(viewModel.state.value.isSaving)
        assertNotNull(viewModel.state.value.error)
    }

    // === Load Character Tests ===

    @Test
    fun `LoadCharacter loads character from remote data source`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        val character = Character(
            id = "char-123",
            name = "Loaded Character",
            playerName = "Player",
            race = "Elf",
            characterClass = "Wizard",
            level = 3,
            description = "Test",
            maxHp = 20,
            currentHp = 18,
        )
        fakeRemoteDataSource.getMyCharacterResult = Result.Success(character)

        viewModel.onEvent(PlayerCharacterCreateEvent.LoadCharacter("char-123"))

        // Wait for load
        kotlinx.coroutines.delay(100)

        assertEquals("Loaded Character", viewModel.state.value.name)
        assertEquals("Elf", viewModel.state.value.race)
        assertEquals("Wizard", viewModel.state.value.characterClass)
        assertEquals("3", viewModel.state.value.level)
        assertTrue(viewModel.state.value.isEditMode)
    }

    @Test
    fun `LoadCharacter shows error on failure`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        fakeRemoteDataSource.getMyCharacterResult = Result.Error(com.dnd.helper.domain.common.AppError.NotFound)

        viewModel.onEvent(PlayerCharacterCreateEvent.LoadCharacter("char-123"))

        // Wait for load
        kotlinx.coroutines.delay(100)

        assertNotNull(viewModel.state.value.error)
        assertTrue(viewModel.state.value.error?.contains("Failed to load") == true)
    }

    // === Image Generation Tests ===

    @Test
    fun `GenerateImage starts generation and updates imageUrl`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        viewModel.onEvent(PlayerCharacterCreateEvent.NameChanged("Test Character"))
        viewModel.onEvent(PlayerCharacterCreateEvent.GenerateImage)

        // Wait for generation
        kotlinx.coroutines.delay(100)

        assertEquals("character", fakeEditingRepository.lastStartedEntityType)
        assertTrue(viewModel.state.value.imageUrl.startsWith("generating:"))
    }

    @Test
    fun `GenerateItemImage starts item generation`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        viewModel.onEvent(PlayerCharacterCreateEvent.AddItem)
        val index = viewModel.state.value.items.size - 1

        viewModel.onEvent(PlayerCharacterCreateEvent.ItemNameChanged(index, "Sword"))
        viewModel.onEvent(PlayerCharacterCreateEvent.GenerateItemImage(index))

        // Wait for generation
        kotlinx.coroutines.delay(100)

        assertEquals("item", fakeEditingRepository.lastStartedEntityType)
        assertTrue(viewModel.state.value.items[index].imageUrl?.startsWith("generating:") == true)
    }

    // === Item Management Tests ===

    @Test
    fun `AddItem adds new item to items`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        val initialSize = viewModel.state.value.items.size

        viewModel.onEvent(PlayerCharacterCreateEvent.AddItem)

        assertEquals(initialSize + 1, viewModel.state.value.items.size)
        assertEquals("New Item", viewModel.state.value.items.last().name)
    }

    @Test
    fun `RemoveItem removes item from items`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        viewModel.onEvent(PlayerCharacterCreateEvent.AddItem)
        val initialSize = viewModel.state.value.items.size

        viewModel.onEvent(PlayerCharacterCreateEvent.RemoveItem(initialSize - 1))

        assertEquals(initialSize - 1, viewModel.state.value.items.size)
    }

    @Test
    fun `ItemNameChanged updates item name`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        viewModel.onEvent(PlayerCharacterCreateEvent.AddItem)
        val index = viewModel.state.value.items.size - 1

        viewModel.onEvent(PlayerCharacterCreateEvent.ItemNameChanged(index, "Longsword"))

        assertEquals("Longsword", viewModel.state.value.items[index].name)
    }

    // === Weapon Management Tests ===

    @Test
    fun `AddWeapon adds new weapon to weapons`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        val initialSize = viewModel.state.value.weapons.size

        viewModel.onEvent(PlayerCharacterCreateEvent.AddWeapon)

        assertEquals(initialSize + 1, viewModel.state.value.weapons.size)
        assertEquals("New Weapon", viewModel.state.value.weapons.last().name)
    }

    @Test
    fun `RemoveWeapon removes weapon from weapons`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        viewModel.onEvent(PlayerCharacterCreateEvent.AddWeapon)
        val initialSize = viewModel.state.value.weapons.size

        viewModel.onEvent(PlayerCharacterCreateEvent.RemoveWeapon(initialSize - 1))

        assertEquals(initialSize - 1, viewModel.state.value.weapons.size)
    }

    // === Spell Management Tests ===

    @Test
    fun `AddSpell adds new spell to spellList`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        val initialSize = viewModel.state.value.spellList.size

        viewModel.onEvent(PlayerCharacterCreateEvent.AddSpell)

        assertEquals(initialSize + 1, viewModel.state.value.spellList.size)
        assertEquals("New Spell", viewModel.state.value.spellList.last().name)
    }

    @Test
    fun `RemoveSpell removes spell from spellList`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        viewModel.onEvent(PlayerCharacterCreateEvent.AddSpell)
        val initialSize = viewModel.state.value.spellList.size

        viewModel.onEvent(PlayerCharacterCreateEvent.RemoveSpell(initialSize - 1))

        assertEquals(initialSize - 1, viewModel.state.value.spellList.size)
    }

    // === Proficiency Tests ===

    @Test
    fun `AddLanguage adds language to selectedLanguages`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        viewModel.onEvent(PlayerCharacterCreateEvent.AddLanguage("Elven"))

        assertTrue("Elven" in viewModel.state.value.selectedLanguages)
    }

    @Test
    fun `RemoveLanguage removes language from selectedLanguages`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        viewModel.onEvent(PlayerCharacterCreateEvent.AddLanguage("Elven"))
        viewModel.onEvent(PlayerCharacterCreateEvent.RemoveLanguage("Elven"))

        assertFalse("Elven" in viewModel.state.value.selectedLanguages)
    }

    @Test
    fun `AddProficiencySkill adds skill to selectedSkills`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        viewModel.onEvent(PlayerCharacterCreateEvent.AddProficiencySkill("Athletics"))

        assertTrue("Athletics" in viewModel.state.value.selectedSkills)
    }

    @Test
    fun `RemoveProficiencySkill removes skill from selectedSkills`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        viewModel.onEvent(PlayerCharacterCreateEvent.AddProficiencySkill("Athletics"))
        viewModel.onEvent(PlayerCharacterCreateEvent.RemoveProficiencySkill("Athletics"))

        assertFalse("Athletics" in viewModel.state.value.selectedSkills)
    }

    // === Background Generation Image Completion Tests ===

    @Test
    fun `background image generation completion updates character imageUrl`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        viewModel.onEvent(PlayerCharacterCreateEvent.NameChanged("Test Character"))
        viewModel.onEvent(PlayerCharacterCreateEvent.GenerateImage)

        val taskId = viewModel.state.value.imageUrl.substringAfter("generating:")

        // Simulate completion
        fakeEditingRepository.completeTask(taskId, "https://example.com/generated.png")

        // Wait for state update
        kotlinx.coroutines.delay(100)

        assertEquals("https://example.com/generated.png", viewModel.state.value.imageUrl)
    }

    // === Error Dismissal Tests ===

    @Test
    fun `DismissError clears error from state`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        viewModel.onEvent(PlayerCharacterCreateEvent.SaveCharacter)

        assertNotNull(viewModel.state.value.error)

        viewModel.onEvent(PlayerCharacterCreateEvent.DismissError)

        assertNull(viewModel.state.value.error)
    }

    // === Edge Cases ===

    @Test
    fun `handles rapid field updates correctly`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        repeat(3) { index ->
            viewModel.onEvent(PlayerCharacterCreateEvent.StrengthChanged(index.toString()))
        }

        assertEquals("2", viewModel.state.value.strength)
    }

    @Test
    fun `LoadCharacter with invalid id shows error`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        fakeRemoteDataSource.getMyCharacterResult = Result.Error(com.dnd.helper.domain.common.AppError.NotFound)

        viewModel.onEvent(PlayerCharacterCreateEvent.LoadCharacter("invalid-id"))

        // Wait for load
        kotlinx.coroutines.delay(100)

        assertNotNull(viewModel.state.value.error)
    }

    @Test
    fun `GenerateImage without name does nothing`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        val beforeUrl = viewModel.state.value.imageUrl

        viewModel.onEvent(PlayerCharacterCreateEvent.GenerateImage)

        // Should not generate image without name
        assertEquals(beforeUrl, viewModel.state.value.imageUrl)
    }

    @Test
    fun `handles empty API data gracefully`() = runTest(testDispatcher) {
        fakeApiDataSource.getClassesResult = Result.Success(ApiReferenceListDto(
            count = 0,
            results = emptyList()
        ))
        fakeApiDataSource.getRacesResult = Result.Success(ApiReferenceListDto(
            count = 0,
            results = emptyList()
        ))

        val newViewModel = createViewModel(backgroundScope)

        // Wait for init
        kotlinx.coroutines.delay(100)

        // Should handle empty data gracefully
        assertTrue(newViewModel.state.value.availableClasses.isEmpty())
        assertTrue(newViewModel.state.value.availableRaces.isEmpty())
    }
}
