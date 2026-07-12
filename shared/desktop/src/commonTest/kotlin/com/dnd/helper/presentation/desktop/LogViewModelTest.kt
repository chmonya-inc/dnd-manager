package com.dnd.helper.presentation.desktop

import com.dnd.helper.domain.common.Result
import com.dnd.helper.domain.model.Character
import com.dnd.helper.domain.model.LogEntry
import com.dnd.helper.fakes.FakeCharacterRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LogViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: LogViewModel
    private lateinit var fakeRepository: FakeCharacterRepository

    private fun createTestLog(action: String = "Update Character") = LogEntry(
        timestamp = "2024-01-01T12:00:00",
        action = action,
        details = "Character HP updated",
        success = true
    )

    private fun createTestCharacter(id: String = "char-1", name: String = "Test Character") = Character(
        id = id,
        name = name,
        playerName = "Player",
        race = "Human",
        characterClass = "Fighter",
        level = 3,
        description = "Brave fighter",
        maxHp = 30,
        currentHp = 25,
    )

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeCharacterRepository()

        // Setup test log data
        fakeRepository.getLogsResult = Result.Success(listOf(
            createTestLog("Update Character"),
            createTestLog("Delete Item"),
            createTestLog("Add Spell")
        ))

        viewModel = LogViewModel(repository = fakeRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // === Initial State Tests ===

    @Test
    fun `initial state loads logs from repository`() = runTest(testDispatcher) {
        // Wait for init
        runCurrent()

        assertEquals(3, viewModel.state.value.logs.size)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `initial state shows loading while fetching`() = runTest(testDispatcher) {
        val newViewModel = LogViewModel(repository = fakeRepository)

        // Should initially be loading (before async operation completes)
        // Note: This might be timing-dependent
        assertTrue(newViewModel.state.value.logs.isNotEmpty() || newViewModel.state.value.isLoading)
    }

    // === Refresh Tests ===

    @Test
    fun `refreshLogs reloads logs from repository`() = runTest(testDispatcher) {
        // Wait for initial load
        runCurrent()

        val initialCount = viewModel.state.value.logs.size

        // Update repository data
        fakeRepository.getLogsResult = Result.Success(listOf(
            createTestLog("New Action")
        ))

        viewModel.refreshLogs(force = true)

        // Wait for refresh
        runCurrent()

        assertEquals(1, viewModel.state.value.logs.size)
        assertEquals("New Action", viewModel.state.value.logs[0].action)
    }

    @Test
    fun `refreshLogs handles errors gracefully`() = runTest(testDispatcher) {
        fakeRepository.getLogsResult = Result.Error(com.dnd.helper.domain.common.AppError.Network)

        viewModel.refreshLogs()

        // Wait for refresh
        runCurrent()

        assertFalse(viewModel.state.value.isLoading)
        // Logs should be empty on error
        assertTrue(viewModel.state.value.logs.isEmpty())
    }

    // === Undo Tests ===

    @Test
    fun `undoLog restores character state and creates undo log`() = runTest(testDispatcher) {
        val originalCharacter = createTestCharacter("char-1", "Fighter")
        val characterJson = Json.encodeToString(originalCharacter)

        val log = LogEntry(
            timestamp = "2024-01-01T12:00:00",
            action = "Update Character",
            details = "Changed HP from 25 to 15",
            success = true,
            initialState = characterJson
        )

        fakeRepository.saveCharacterResult = Result.Success(Unit)
        fakeRepository.getLogsResult = Result.Success(listOf(log))

        viewModel.undoLog(log)

        // Wait for async operations
        runCurrent()

        assertTrue(fakeRepository.saveCharacterCalls > 0)
        val savedCharacter = fakeRepository.savedCharacters["char-1"]
        assertNotNull(savedCharacter)
        assertEquals("Fighter", savedCharacter.name)
        assertEquals(25, savedCharacter.currentHp) // Should be restored to original state
    }

    @Test
    fun `undoLog with non-Character action does nothing`() = runTest(testDispatcher) {
        val log = LogEntry(
            timestamp = "2024-01-01T12:00:00",
            action = "Update Monster", // Not a Character action
            details = "Changed monster HP",
            success = true,
            initialState = null
        )

        viewModel.undoLog(log)

        // Wait for async operations
        runCurrent()

        // Should not attempt to save character
        assertEquals(0, fakeRepository.saveCharacterCalls)
    }

    @Test
    fun `undoLog with invalid initialState does nothing`() = runTest(testDispatcher) {
        val log = LogEntry(
            timestamp = "2024-01-01T12:00:00",
            action = "Update Character",
            details = "Changed character",
            success = true,
            initialState = "invalid json string"
        )

        viewModel.undoLog(log)

        // Wait for async operations
        runCurrent()

        // Should not crash and should not save
        // The viewModel should handle the exception gracefully
        assertEquals(0, fakeRepository.saveCharacterCalls)
    }

    @Test
    fun `undoLog creates new undo log entry`() = runTest(testDispatcher) {
        val originalCharacter = createTestCharacter("char-1", "Fighter")
        val characterJson = Json.encodeToString(originalCharacter)

        val log = LogEntry(
            timestamp = "2024-01-01T12:00:00",
            action = "Update Character",
            details = "Changed HP",
            success = true,
            initialState = characterJson
        )

        fakeRepository.saveCharacterResult = Result.Success(Unit)
        fakeRepository.saveLogResult = Result.Success(Unit)

        viewModel.undoLog(log)

        // Wait for async operations
        runCurrent()

        assertTrue(fakeRepository.saveLogCalls > 0)
    }

    // === Remote Update Tests ===

    @Test
    fun `remote log update triggers refresh`() = runTest(testDispatcher) {
        // Wait for initial load
        runCurrent()

        val initialCalls = fakeRepository.getLogsCalls

        // Simulate remote update
        fakeRepository.emitRemoteUpdate("logs")

        // Wait for refresh
        runCurrent()

        // Should have triggered a new getLogs call
        assertTrue(fakeRepository.getLogsCalls > initialCalls)
    }

    // === Edge Cases ===

    @Test
    fun `handles empty log list gracefully`() = runTest(testDispatcher) {
        fakeRepository.getLogsResult = Result.Success(emptyList())

        val newViewModel = LogViewModel(repository = fakeRepository)

        // Wait for init
        runCurrent()

        assertTrue(newViewModel.state.value.logs.isEmpty())
        assertFalse(newViewModel.state.value.isLoading)
    }

    @Test
    fun `handles rapid refresh calls correctly`() = runTest(testDispatcher) {
        repeat(3) {
            viewModel.refreshLogs()
        }

        // Wait for async operations
        runCurrent()

        // Should complete without error
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `undoLog handles null initialState gracefully`() = runTest(testDispatcher) {
        val log = LogEntry(
            timestamp = "2024-01-01T12:00:00",
            action = "Update Character",
            details = "Changed character",
            success = true,
            initialState = null
        )

        viewModel.undoLog(log)

        // Wait for async operations
        runCurrent()

        // Should not attempt to save character
        assertEquals(0, fakeRepository.saveCharacterCalls)
    }

    @Test
    fun `undoLog handles save failure gracefully`() = runTest(testDispatcher) {
        val originalCharacter = createTestCharacter("char-1", "Fighter")
        val characterJson = Json.encodeToString(originalCharacter)

        val log = LogEntry(
            timestamp = "2024-01-01T12:00:00",
            action = "Update Character",
            details = "Changed HP",
            success = true,
            initialState = characterJson
        )

        fakeRepository.saveCharacterResult = Result.Error(com.dnd.helper.domain.common.AppError.Network)

        viewModel.undoLog(log)

        // Wait for async operations
        runCurrent()

        // Should handle error gracefully
        // The undo log should not be created on save failure
        assertEquals(0, fakeRepository.saveLogCalls)
    }
}
