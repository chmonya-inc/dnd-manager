package com.dnd.helper.presentation.characterdetail

import app.cash.turbine.test
import com.dnd.helper.domain.common.AppError
import com.dnd.helper.domain.common.Result
import com.dnd.helper.domain.model.Character
import com.dnd.helper.domain.model.CharacterProficiencies
import com.dnd.helper.domain.model.CharacterStats
import com.dnd.helper.domain.repository.GenerationStatus
import com.dnd.helper.domain.repository.GenerationTask
import com.dnd.helper.fakes.FakeCharacterRepository
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
class CharacterDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeCharacterRepository: FakeCharacterRepository
    private lateinit var fakeEditingRepository: FakeEditingRepository
    private val characterId = "char-123"

    private fun createTestCharacter() = Character(
        id = characterId,
        name = "Test Character",
        playerName = "Player",
        race = "Human",
        characterClass = "Fighter",
        level = 1,
        description = "Test",
        maxHp = 20,
        currentHp = 20,
        imageUrl = "",
        items = emptyList(),
        spells = emptyList(),
        stats = CharacterStats(
            strength = 10,
            dexterity = 12,
            constitution = 14,
            intelligence = 8,
            wisdom = 13,
            charisma = 11
        ),
        proficiencies = CharacterProficiencies()
    )

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeCharacterRepository = FakeCharacterRepository()
        fakeEditingRepository = FakeEditingRepository()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(scope: CoroutineScope): CharacterDetailViewModel {
        return CharacterDetailViewModel(
            repository = fakeCharacterRepository,
            editingRepository = fakeEditingRepository,
            characterId = characterId,
            coroutineScope = scope
        )
    }

    // === Initial State Tests ===

    @Test
    fun `initial state loads character from repository`() = runTest(testDispatcher) {
        fakeCharacterRepository.getCharacterResult = Result.Success(createTestCharacter())

        val viewModel = createViewModel(backgroundScope)

        // Wait for init to complete
        kotlinx.coroutines.delay(100)

        assertNotNull(viewModel.state.value.character)
        assertEquals("Test Character", viewModel.state.value.character?.name)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `initial state shows loading while fetching character`() = runTest(testDispatcher) {
        fakeCharacterRepository.getCharacterDelay = 500
        fakeCharacterRepository.getCharacterResult = Result.Success(createTestCharacter())

        val viewModel = createViewModel(backgroundScope)

        assertTrue(viewModel.state.value.isLoading)
        assertNull(viewModel.state.value.character)

        kotlinx.coroutines.delay(600)

        assertFalse(viewModel.state.value.isLoading)
        assertNotNull(viewModel.state.value.character)
    }

    // === Stat Update Tests ===

    @Test
    fun `UpdateStat increases stat value`() = runTest(testDispatcher) {
        fakeCharacterRepository.getCharacterResult = Result.Success(createTestCharacter())

        val viewModel = createViewModel(backgroundScope)

        // Wait for init
        kotlinx.coroutines.delay(100)

        viewModel.onEvent(CharacterDetailEvent.UpdateStat("strength", 2))

        // Wait for debounced save
        kotlinx.coroutines.delay(1200)

        val savedChar = fakeCharacterRepository.savedCharacters[characterId]
        assertEquals(12, savedChar?.stats?.strength)
    }

    @Test
    fun `UpdateStat decreases stat value`() = runTest(testDispatcher) {
        fakeCharacterRepository.getCharacterResult = Result.Success(createTestCharacter())

        val viewModel = createViewModel(backgroundScope)

        // Wait for init
        kotlinx.coroutines.delay(100)

        viewModel.onEvent(CharacterDetailEvent.UpdateStat("dexterity", -1))

        // Wait for debounced save
        kotlinx.coroutines.delay(1200)

        val savedChar = fakeCharacterRepository.savedCharacters[characterId]
        assertEquals(11, savedChar?.stats?.dexterity)
    }

    @Test
    fun `UpdateStat sets hasUnsavedChanges flag`() = runTest(testDispatcher) {
        fakeCharacterRepository.getCharacterResult = Result.Success(createTestCharacter())

        val viewModel = createViewModel(backgroundScope)

        // Wait for init
        kotlinx.coroutines.delay(100)

        viewModel.onEvent(CharacterDetailEvent.UpdateStat("strength", 1))

        assertTrue(viewModel.state.value.hasUnsavedChanges)
    }

    // === HP Update Tests ===

    @Test
    fun `UpdateHp increases current HP`() = runTest(testDispatcher) {
        // Start with HP 10/20 so we can increase it
        fakeCharacterRepository.getCharacterResult = Result.Success(
            createTestCharacter().copy(currentHp = 10, maxHp = 20)
        )

        val viewModel = createViewModel(backgroundScope)

        // Wait for init
        kotlinx.coroutines.delay(100)

        viewModel.onEvent(CharacterDetailEvent.UpdateHp(5))

        // Wait for debounced save
        kotlinx.coroutines.delay(1200)

        val savedChar = fakeCharacterRepository.savedCharacters[characterId]
        assertEquals(15, savedChar?.currentHp)
    }

    @Test
    fun `UpdateHp decreases current HP but not below zero`() = runTest(testDispatcher) {
        fakeCharacterRepository.getCharacterResult = Result.Success(createTestCharacter())

        val viewModel = createViewModel(backgroundScope)

        // Wait for init
        kotlinx.coroutines.delay(100)

        viewModel.onEvent(CharacterDetailEvent.UpdateHp(-30))

        // Wait for debounced save
        kotlinx.coroutines.delay(1200)

        val savedChar = fakeCharacterRepository.savedCharacters[characterId]
        assertEquals(0, savedChar?.currentHp) // Should clamp to 0
    }

    @Test
    fun `test HP healing does not exceed maxHp`() = runTest(testDispatcher) {
        fakeCharacterRepository.getCharacterResult = Result.Success(
            createTestCharacter().copy(currentHp = 8, maxHp = 10)
        )

        val viewModel = createViewModel(backgroundScope)

        // Wait for init
        kotlinx.coroutines.delay(100)

        viewModel.onEvent(CharacterDetailEvent.UpdateHp(5)) // Heal 5

        // Should not exceed maxHp
        assertEquals(10, viewModel.state.value.character?.currentHp)
    }

    @Test
    fun `test HP damage clamps to zero`() = runTest(testDispatcher) {
        fakeCharacterRepository.getCharacterResult = Result.Success(
            createTestCharacter().copy(currentHp = 5, maxHp = 10)
        )

        val viewModel = createViewModel(backgroundScope)

        // Wait for init
        kotlinx.coroutines.delay(100)

        viewModel.onEvent(CharacterDetailEvent.UpdateHp(-10)) // Damage 10

        assertEquals(0, viewModel.state.value.character?.currentHp)
    }

    @Test
    fun `test Level update clamps to 1`() = runTest(testDispatcher) {
        fakeCharacterRepository.getCharacterResult = Result.Success(
            createTestCharacter().copy(level = 1)
        )

        val viewModel = createViewModel(backgroundScope)

        // Wait for init
        kotlinx.coroutines.delay(100)

        viewModel.onEvent(CharacterDetailEvent.UpdateLevel(-1)) // Try to decrease below 1

        assertEquals(1, viewModel.state.value.character?.level)
    }

    // === Toggle Edit Tests ===

    @Test
    fun `ToggleEdit enters edit mode with AI prompt`() = runTest(testDispatcher) {
        fakeCharacterRepository.getCharacterResult = Result.Success(createTestCharacter())

        val viewModel = createViewModel(backgroundScope)

        // Wait for init
        kotlinx.coroutines.delay(100)

        viewModel.onEvent(CharacterDetailEvent.ToggleEdit)

        assertTrue(viewModel.state.value.isEditing)
        assertNotNull(viewModel.state.value.editedCharacter)
        assertNotNull(viewModel.state.value.aiPrompt)
        assertTrue(viewModel.state.value.aiPrompt.contains("Test Character"))
    }

    @Test
    fun `ToggleEdit exits edit mode and discards changes`() = runTest(testDispatcher) {
        fakeCharacterRepository.getCharacterResult = Result.Success(createTestCharacter())

        val viewModel = createViewModel(backgroundScope)

        // Wait for init
        kotlinx.coroutines.delay(100)

        // Enter edit mode
        viewModel.onEvent(CharacterDetailEvent.ToggleEdit)
        assertTrue(viewModel.state.value.isEditing)

        // Make a change
        val editedChar = viewModel.state.value.editedCharacter?.copy(name = "Edited Name")
        assertNotNull(editedChar)
        viewModel.onEvent(CharacterDetailEvent.EditCharacter(editedChar))
        assertEquals("Edited Name", viewModel.state.value.editedCharacter?.name)

        // Exit edit mode
        viewModel.onEvent(CharacterDetailEvent.ToggleEdit)

        assertFalse(viewModel.state.value.isEditing)
        assertNull(viewModel.state.value.editedCharacter)
        assertEquals("Test Character", viewModel.state.value.character?.name) // Original restored
    }

    // === Save Changes Tests ===

    @Test
    fun `SaveChanges saves edited character to repository`() = runTest(testDispatcher) {
        fakeCharacterRepository.getCharacterResult = Result.Success(createTestCharacter())
        fakeCharacterRepository.saveCharacterResult = Result.Success(Unit)

        val viewModel = createViewModel(backgroundScope)

        // Wait for init
        kotlinx.coroutines.delay(100)

        // Enter edit mode and make changes
        viewModel.onEvent(CharacterDetailEvent.ToggleEdit)
        val editedChar = viewModel.state.value.editedCharacter?.copy(name = "Saved Character")
        assertNotNull(editedChar)
        viewModel.onEvent(CharacterDetailEvent.EditCharacter(editedChar))

        viewModel.onEvent(CharacterDetailEvent.SaveChanges)

        // Wait for save
        kotlinx.coroutines.delay(100)

        assertEquals("Saved Character", viewModel.state.value.character?.name)
        assertFalse(viewModel.state.value.isSaving)
        assertFalse(viewModel.state.value.isEditing)
        assertNull(viewModel.state.value.editedCharacter)
    }

    @Test
    fun `SaveChanges shows error on failure`() = runTest(testDispatcher) {
        fakeCharacterRepository.getCharacterResult = Result.Success(createTestCharacter())
        fakeCharacterRepository.saveCharacterResult = Result.Error(AppError.Network)

        val viewModel = createViewModel(backgroundScope)

        // Wait for init
        kotlinx.coroutines.delay(100)

        // Enter edit mode and make changes
        viewModel.onEvent(CharacterDetailEvent.ToggleEdit)
        val editedChar = viewModel.state.value.editedCharacter?.copy(name = "Failed Save")
        assertNotNull(editedChar)
        viewModel.onEvent(CharacterDetailEvent.EditCharacter(editedChar))

        viewModel.onEvent(CharacterDetailEvent.SaveChanges)

        // Wait for save
        kotlinx.coroutines.delay(100)

        assertFalse(viewModel.state.value.isSaving)
        assertNotNull(viewModel.state.value.error)
    }

    // === Image Generation Tests ===

    @Test
    fun `GenerateImage starts generation and updates imageUrl`() = runTest(testDispatcher) {
        fakeCharacterRepository.getCharacterResult = Result.Success(createTestCharacter())

        val viewModel = createViewModel(backgroundScope)

        // Wait for init
        kotlinx.coroutines.delay(100)

        viewModel.onEvent(CharacterDetailEvent.ToggleEdit)
        viewModel.onEvent(CharacterDetailEvent.GenerateImage)

        // Verify generation was started
        assertEquals("character", fakeEditingRepository.lastStartedEntityType)
        assertTrue(viewModel.state.value.editedCharacter?.imageUrl?.startsWith("generating:") == true)
    }

    @Test
    fun `GenerateImage uses custom AI prompt when provided`() = runTest(testDispatcher) {
        fakeCharacterRepository.getCharacterResult = Result.Success(createTestCharacter())

        val viewModel = createViewModel(backgroundScope)

        // Wait for init
        kotlinx.coroutines.delay(100)

        viewModel.onEvent(CharacterDetailEvent.ToggleEdit)
        viewModel.onEvent(CharacterDetailEvent.UpdateAiPrompt("Custom AI prompt"))

        viewModel.onEvent(CharacterDetailEvent.GenerateImage)

        // Verify custom prompt was used
        assertEquals("Custom AI prompt", fakeEditingRepository.lastPrompt)
    }

    // === State Flow Tests ===

    @Test
    fun `state flow emits all state changes`() = runTest(testDispatcher) {
        fakeCharacterRepository.getCharacterDelay = 100
        fakeCharacterRepository.getCharacterResult = Result.Success(createTestCharacter())

        val viewModel = createViewModel(backgroundScope)

        viewModel.state.test {
            // Initial loading state
            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)

            // Character loaded state
            val loadedState = awaitItem()
            assertNotNull(loadedState.character)
            assertFalse(loadedState.isLoading)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `state flow emits error state on load failure`() = runTest(testDispatcher) {
        fakeCharacterRepository.getCharacterDelay = 100
        fakeCharacterRepository.getCharacterResult = Result.Error(AppError.Network)

        val viewModel = createViewModel(backgroundScope)

        viewModel.state.test {
            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)

            val errorState = awaitItem()
            assertFalse(errorState.isLoading)
            assertNotNull(errorState.error)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // === Delete Character Tests ===

    @Test
    fun `DeleteCharacter calls repository delete`() = runTest(testDispatcher) {
        fakeCharacterRepository.getCharacterResult = Result.Success(createTestCharacter())
        fakeCharacterRepository.deleteCharacterResult = Result.Success(Unit)

        val viewModel = createViewModel(backgroundScope)

        // Wait for init
        kotlinx.coroutines.delay(100)

        viewModel.onEvent(CharacterDetailEvent.DeleteCharacter)

        // Wait for delete
        kotlinx.coroutines.delay(100)

        assertTrue(fakeCharacterRepository.deleteCharacterCalls > 0)
    }

    // === Background Generation Tests ===

    @Test
    fun `background image generation completion updates character imageUrl`() = runTest(testDispatcher) {
        fakeCharacterRepository.getCharacterResult = Result.Success(
            createTestCharacter().copy(imageUrl = "generating:task-1")
        )

        val viewModel = createViewModel(backgroundScope)

        // Wait for init
        kotlinx.coroutines.delay(100)

        // Simulate completion
        val taskId = "task-1"
        fakeEditingRepository.addTask(GenerationTask(
            id = taskId,
            entityId = characterId,
            entityType = "character",
            prompt = "Test",
            status = GenerationStatus.COMPLETED,
            resultUrl = "https://example.com/generated.png"
        ))

        // Wait for state update
        kotlinx.coroutines.delay(100)

        assertEquals("https://example.com/generated.png", viewModel.state.value.character?.imageUrl)
    }

    // === Edge Cases ===

    @Test
    fun `handles character not found error gracefully`() = runTest(testDispatcher) {
        fakeCharacterRepository.getCharacterResult = Result.Error(AppError.NotFound)

        val viewModel = createViewModel(backgroundScope)

        // Wait for init
        kotlinx.coroutines.delay(100)

        assertNull(viewModel.state.value.character)
        assertNotNull(viewModel.state.value.error)
    }

    @Test
    fun `multiple rapid stat updates are debounced correctly`() = runTest(testDispatcher) {
        fakeCharacterRepository.getCharacterResult = Result.Success(createTestCharacter())

        val viewModel = createViewModel(backgroundScope)

        // Wait for init
        kotlinx.coroutines.delay(100)

        // Make multiple rapid updates
        repeat(5) {
            viewModel.onEvent(CharacterDetailEvent.UpdateStat("strength", 1)) // each adds 1
        }

        // Should only save once with final value after debounce delay
        kotlinx.coroutines.delay(1500)

        assertEquals(1, fakeCharacterRepository.saveCharacterCalls)
        val savedChar = fakeCharacterRepository.savedCharacters[characterId]
        assertEquals(15, savedChar?.stats?.strength) // Initial 10 + 5 updates
    }
}
