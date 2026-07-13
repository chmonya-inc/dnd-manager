package com.dnd.helper.presentation.characterlist

import app.cash.turbine.test
import com.dnd.helper.domain.common.AppError
import com.dnd.helper.domain.common.Result
import com.dnd.helper.domain.model.Character
import com.dnd.helper.domain.model.EquipmentSlot
import com.dnd.helper.domain.model.InitialData
import com.dnd.helper.domain.model.Item
import com.dnd.helper.domain.model.ItemRarity
import com.dnd.helper.domain.model.Spell
import com.dnd.helper.domain.repository.GenerationStatus
import com.dnd.helper.domain.repository.GenerationTask
import com.dnd.helper.fakes.FakeCharacterRepository
import com.dnd.helper.fakes.FakeCharacterStorage
import com.dnd.helper.fakes.FakeEditingRepository
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
class CharacterListViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeCharacterRepository: FakeCharacterRepository
    private lateinit var fakeEditingRepository: FakeEditingRepository
    private lateinit var fakeStorage: FakeCharacterStorage

    private fun createTestCharacter(id: String = "char-1", name: String = "Test Character") = Character(
        id = id,
        name = name,
        playerName = "Player",
        race = "Human",
        characterClass = "Fighter",
        level = 1,
        description = "Test",
        maxHp = 20,
        currentHp = 20,
        items = emptyList(),
        spells = emptyList()
    )

    private fun createTestItem(id: String = "item-1") = Item(
        id = id,
        name = "Test Item",
        description = "Test",
        slot = EquipmentSlot.MAIN_HAND,
        rarity = ItemRarity.COMMON,
        weight = 1.0,
        imageUrl = ""
    )

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeCharacterRepository = FakeCharacterRepository()
        fakeEditingRepository = FakeEditingRepository()
        fakeStorage = FakeCharacterStorage()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(scope: CoroutineScope): CharacterListViewModel {
        return CharacterListViewModel(
            repository = fakeCharacterRepository,
            editingRepository = fakeEditingRepository,
            storage = fakeStorage,
            coroutineScope = scope
        )
    }

    // === Initial State Tests ===

    @Test
    fun `initial state has empty characters list`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        assertTrue(viewModel.state.value.characters.isEmpty())
        assertFalse(viewModel.state.value.isLoading)
        assertNull(viewModel.state.value.error)
    }

    // === Character Loading Tests ===

    @Test
    fun `init loads initial data from repository`() = runTest(testDispatcher) {
        val characters = listOf(
            createTestCharacter("char-1", "Character A"),
            createTestCharacter("char-2", "Character B")
        )
        fakeCharacterRepository.getInitialDataResult = Result.Success(
            InitialData(
                characters = characters,
                locations = emptyList(),
                monsters = emptyList(),
                npcs = emptyList(),
                lastModified = ""
            )
        )

        val newViewModel = createViewModel(backgroundScope)

        // Wait for init to complete
        kotlinx.coroutines.delay(100)

        assertEquals(2, newViewModel.state.value.characters.size)
        assertFalse(newViewModel.state.value.isLoading)
    }

    @Test
    fun `init falls back to separate loading if bulk loading fails`() = runTest(testDispatcher) {
        fakeCharacterRepository.getInitialDataResult = Result.Error(AppError.Network)
        fakeCharacterRepository.getCharactersResult = Result.Success(listOf(
            createTestCharacter("char-1")
        ))

        val newViewModel = createViewModel(backgroundScope)

        // Wait for init to complete
        kotlinx.coroutines.delay(100)

        // Should fall back to getCharacters
        assertEquals(1, fakeCharacterRepository.getCharactersCalls)
    }

    // === Refresh Tests ===

    @Test
    fun `Refresh loads characters from repository`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        val characters = listOf(
            createTestCharacter("char-1", "Character A"),
            createTestCharacter("char-2", "Character B")
        )
        fakeCharacterRepository.getCharactersResult = Result.Success(characters)

        viewModel.onEvent(CharacterListEvent.Refresh)

        // Wait for async operation
        kotlinx.coroutines.delay(100)

        assertEquals(2, viewModel.state.value.characters.size)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `Refresh sorts characters by name`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        val characters = listOf(
            createTestCharacter("char-3", "Zebra"),
            createTestCharacter("char-1", "Apple"),
            createTestCharacter("char-2", "Banana")
        )
        fakeCharacterRepository.getCharactersResult = Result.Success(characters)

        viewModel.onEvent(CharacterListEvent.Refresh)

        // Wait for async operation
        kotlinx.coroutines.delay(100)

        assertEquals(3, viewModel.state.value.characters.size)
        assertEquals("Apple", viewModel.state.value.characters[0].name)
        assertEquals("Banana", viewModel.state.value.characters[1].name)
        assertEquals("Zebra", viewModel.state.value.characters[2].name)
    }

    @Test
    fun `Refresh shows error on failure`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        fakeCharacterRepository.getCharactersResult = Result.Error(AppError.Network)

        viewModel.onEvent(CharacterListEvent.Refresh)

        // Wait for async operation
        kotlinx.coroutines.delay(100)

        assertFalse(viewModel.state.value.isLoading)
        assertNotNull(viewModel.state.value.error)
    }

    // === Character Clicked Tests ===

    @Test
    fun `CharacterClicked saves character ID to storage`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        viewModel.onEvent(CharacterListEvent.CharacterClicked("char-123"))

        assertEquals("char-123", fakeStorage.getCharacterId())
    }

    // === Item Movement Tests ===

    @Test
    fun `moveItemBetweenCharacters performs optimistic update`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        val item = createTestItem("item-1")
        val char1 = createTestCharacter("char-1", "Character 1").copy(items = listOf(item))
        val char2 = createTestCharacter("char-2", "Character 2")

        fakeCharacterRepository.getCharactersResult = Result.Success(listOf(char1, char2))

        viewModel.onEvent(CharacterListEvent.Refresh)
        kotlinx.coroutines.delay(100)

        viewModel.moveItemBetweenCharacters(item, "char-1", "char-2")

        // Verify optimistic update
        val updatedState = viewModel.state.value
        assertEquals(0, updatedState.characters.find { it.id == "char-1" }?.items?.size)
        assertEquals(1, updatedState.characters.find { it.id == "char-2" }?.items?.size)
    }

    @Test
    fun `moveItemBetweenCharacters creates new item with different ID`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        val item = createTestItem("item-1")
        val char1 = createTestCharacter("char-1", "Character 1").copy(items = listOf(item))
        val char2 = createTestCharacter("char-2", "Character 2")

        fakeCharacterRepository.getCharactersResult = Result.Success(listOf(char1, char2))

        viewModel.onEvent(CharacterListEvent.Refresh)
        kotlinx.coroutines.delay(100)

        viewModel.moveItemBetweenCharacters(item, "char-1", "char-2")

        val targetChar = viewModel.state.value.characters.find { it.id == "char-2" }
        assertEquals(1, targetChar?.items?.size)
        assertTrue(targetChar?.items?.get(0)?.id?.startsWith("item-") == true)
        assertTrue(targetChar?.items?.get(0)?.id != "item-1") // Different ID
    }

    @Test
    fun `moveItemBetweenCharacters does nothing if source and target are same`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        val item = createTestItem("item-1")
        val char1 = createTestCharacter("char-1", "Character 1").copy(items = listOf(item))

        fakeCharacterRepository.getCharactersResult = Result.Success(listOf(char1))

        viewModel.onEvent(CharacterListEvent.Refresh)
        kotlinx.coroutines.delay(100)

        val beforeState = viewModel.state.value

        viewModel.moveItemBetweenCharacters(item, "char-1", "char-1")

        assertEquals(beforeState, viewModel.state.value)
    }

    // === Image Generation Integration Tests ===

    @Test
    fun `activeTasks flow updates character imageUrl when generation completes`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        val char = createTestCharacter("char-1", "Character 1").copy(imageUrl = "generating:task-1")
        fakeCharacterRepository.getCharactersResult = Result.Success(listOf(char))

        viewModel.onEvent(CharacterListEvent.Refresh)
        kotlinx.coroutines.delay(100)

        // Simulate image generation completion
        val task = GenerationTask(
            id = "task-1",
            entityId = "char-1",
            entityType = "character",
            prompt = "Test",
            status = GenerationStatus.COMPLETED,
            resultUrl = "https://example.com/generated.png"
        )

        fakeEditingRepository.addTask(task)

        // Wait for state update
        kotlinx.coroutines.delay(100)

        val updatedChar = viewModel.state.value.characters.find { it.id == "char-1" }
        assertEquals("https://example.com/generated.png", updatedChar?.imageUrl)
    }

    @Test
    fun `activeTasks flow updates item imageUrl when generation completes`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        val item = createTestItem("item-1").copy(imageUrl = "generating:task-1")
        val char = createTestCharacter("char-1", "Character 1").copy(items = listOf(item))
        fakeCharacterRepository.getCharactersResult = Result.Success(listOf(char))

        viewModel.onEvent(CharacterListEvent.Refresh)
        kotlinx.coroutines.delay(100)

        // Simulate item image generation completion
        val task = GenerationTask(
            id = "task-1",
            entityId = "char-1:item-1",
            entityType = "item",
            prompt = "Test",
            status = GenerationStatus.COMPLETED,
            resultUrl = "https://example.com/item.png"
        )

        fakeEditingRepository.addTask(task)

        // Wait for state update
        kotlinx.coroutines.delay(100)

        val updatedChar = viewModel.state.value.characters.find { it.id == "char-1" }
        assertEquals("https://example.com/item.png", updatedChar?.items?.get(0)?.imageUrl)
    }

    @Test
    fun `activeTasks flow updates spell iconUrl when generation completes`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        val spell = Spell(
            id = "spell-1",
            name = "Fireball",
            iconUrl = "generating:task-1",
            description = "Test",
            level = 3,
            school = "Evocation"
        )
        val char = createTestCharacter("char-1", "Character 1").copy(spells = listOf(spell))
        fakeCharacterRepository.getCharactersResult = Result.Success(listOf(char))

        viewModel.onEvent(CharacterListEvent.Refresh)
        kotlinx.coroutines.delay(100)

        // Simulate spell icon generation completion
        val task = GenerationTask(
            id = "task-1",
            entityId = "char-1:spell-1",
            entityType = "spell",
            prompt = "Test",
            status = GenerationStatus.COMPLETED,
            resultUrl = "https://example.com/spell.png"
        )

        fakeEditingRepository.addTask(task)

        // Wait for state update
        kotlinx.coroutines.delay(100)

        val updatedChar = viewModel.state.value.characters.find { it.id == "char-1" }
        assertEquals("https://example.com/spell.png", updatedChar?.spells?.get(0)?.iconUrl)
    }

    // === Repository Update Integration Tests ===

    @Test
    fun `characterUpdates flow triggers refresh when no pending saves`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        fakeCharacterRepository.getCharactersResult = Result.Success(listOf(
            createTestCharacter("char-1", "Updated Character")
        ))

        // Emit character update
        fakeCharacterRepository.emitCharacterUpdate("char-1")

        // Wait for refresh
        kotlinx.coroutines.delay(100)

        assertEquals(1, fakeCharacterRepository.getCharactersCalls)
    }

    @Test
    fun `characterUpdates flow does not trigger refresh during pending saves`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        fakeCharacterRepository.getCharactersResult = Result.Success(listOf(
            createTestCharacter("char-1", "Character 1")
        ))

        viewModel.onEvent(CharacterListEvent.Refresh)
        kotlinx.coroutines.delay(100)

        // Start an item move (which creates pending save)
        val item = createTestItem("item-1")
        val char = viewModel.state.value.characters[0].copy(items = listOf(item))
        viewModel.moveItemBetweenCharacters(item, "char-1", "char-1")

        // Emit character update while pending save count > 0
        fakeCharacterRepository.emitCharacterUpdate("char-1")

        // Should not trigger additional getCharacters call
        val callsAfterRefresh = fakeCharacterRepository.getCharactersCalls
        kotlinx.coroutines.delay(100)
        assertEquals(callsAfterRefresh, fakeCharacterRepository.getCharactersCalls)
    }

    // === Server Address Change Tests ===

    @Test
    fun `server address change triggers initial data reload`() = runTest(testDispatcher) {
        createViewModel(backgroundScope)
        fakeCharacterRepository.getInitialDataResult = Result.Success(
            InitialData(
                characters = listOf(createTestCharacter("char-1")),
                locations = emptyList(),
                monsters = emptyList(),
                npcs = emptyList(),
                lastModified = ""
            )
        )

        fakeStorage.saveServerAddress("http://new-server.com")

        // Wait for refresh triggered by address change
        kotlinx.coroutines.delay(100)

        assertEquals(1, fakeCharacterRepository.getInitialDataCalls)
    }

    // === State Flow Tests ===

    @Test
    fun `state flow emits loading state during refresh`() = runTest(testDispatcher) {
        fakeCharacterRepository.getCharactersDelay = 100
        fakeCharacterRepository.getCharactersResult = Result.Success(emptyList())

        val viewModel = createViewModel(backgroundScope)

        viewModel.state.test {
            val initialState = awaitItem()
            assertFalse(initialState.isLoading)

            viewModel.onEvent(CharacterListEvent.Refresh)

            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)

            val loadedState = awaitItem()
            assertFalse(loadedState.isLoading)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // === Background Save Tests ===

    @Test
    fun `background save is scheduled after item movement`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        val item = createTestItem("item-1")
        val char1 = createTestCharacter("char-1", "Character 1").copy(items = listOf(item))
        val char2 = createTestCharacter("char-2", "Character 2")

        fakeCharacterRepository.getCharactersResult = Result.Success(listOf(char1, char2))

        viewModel.onEvent(CharacterListEvent.Refresh)
        kotlinx.coroutines.delay(100)

        viewModel.moveItemBetweenCharacters(item, "char-1", "char-2")

        // Wait for background save delay
        kotlinx.coroutines.delay(350)

        // Should have scheduled saves for both characters
        assertTrue(fakeCharacterRepository.saveCharacterCalls >= 2)
    }

    // === Edge Cases ===

    @Test
    fun `handles empty character list gracefully`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        fakeCharacterRepository.getCharactersResult = Result.Success(emptyList())

        viewModel.onEvent(CharacterListEvent.Refresh)

        // Wait for async operation
        kotlinx.coroutines.delay(100)

        assertTrue(viewModel.state.value.characters.isEmpty())
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `handles character with empty items list`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        val char = createTestCharacter("char-1", "Character 1").copy(items = emptyList())
        fakeCharacterRepository.getCharactersResult = Result.Success(listOf(char))

        viewModel.onEvent(CharacterListEvent.Refresh)

        // Wait for async operation
        kotlinx.coroutines.delay(100)

        assertEquals(1, viewModel.state.value.characters.size)
        assertTrue(viewModel.state.value.characters[0].items.isEmpty())
    }
}
