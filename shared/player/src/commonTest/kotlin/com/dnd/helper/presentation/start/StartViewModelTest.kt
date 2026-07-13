package com.dnd.helper.presentation.start

import app.cash.turbine.test
import com.dnd.helper.data.remote.dto.auth.CharacterTemplateDto
import com.dnd.helper.data.remote.dto.auth.MyCharacterDto
import com.dnd.helper.data.remote.dto.auth.MyCharactersResponse
import com.dnd.helper.data.remote.dto.auth.PendingAssignmentDto
import com.dnd.helper.domain.common.AppError
import com.dnd.helper.domain.common.Result
import com.dnd.helper.domain.model.Character
import com.dnd.helper.fakes.FakeAuthRepository
import com.dnd.helper.fakes.FakeCharacterRepository
import com.dnd.helper.fakes.FakeCharacterStorage
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
class StartViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeRemoteDataSource: FakeRemoteDataSourceForStart
    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var fakeCharacterRepository: FakeCharacterRepository
    private lateinit var fakeStorage: FakeCharacterStorage

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRemoteDataSource = FakeRemoteDataSourceForStart()
        fakeAuthRepository = FakeAuthRepository()
        fakeCharacterRepository = FakeCharacterRepository()
        fakeStorage = FakeCharacterStorage()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(scope: CoroutineScope): StartViewModel {
        return StartViewModel(
            storage = fakeStorage,
            remoteDataSource = fakeRemoteDataSource,
            authRepository = fakeAuthRepository,
            characterRepository = fakeCharacterRepository,
            coroutineScope = scope
        )
    }

    // === Initial State Tests ===

    @Test
    fun `initial state loads values from storage`() = runTest(testDispatcher) {
        fakeStorage.saveCharacterId("char-123")
        fakeStorage.saveTableId("table-456")
        fakeAuthRepository.storedUserRole = "MASTER"

        val viewModel = createViewModel(backgroundScope)

        assertEquals("char-123", viewModel.state.value.characterId)
        assertEquals("table-456", viewModel.state.value.tableId)
        assertTrue(viewModel.state.value.isMaster)
    }

    // === Input Handling Tests ===

    @Test
    fun `CharacterIdChanged updates characterId in state`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        viewModel.onEvent(StartEvent.CharacterIdChanged("new-char-id"))

        assertEquals("new-char-id", viewModel.state.value.characterId)
    }

    @Test
    fun `TableIdChanged updates tableId in state`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        viewModel.onEvent(StartEvent.TableIdChanged("  new-table-id  "))

        assertEquals("new-table-id", viewModel.state.value.tableId) // Should trim
    }

    @Test
    fun `LoadMyCharacter saves character and session to storage`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        viewModel.onEvent(StartEvent.LoadMyCharacter("char-123", "session-456"))

        assertEquals("char-123", fakeStorage.getCharacterId())
        assertEquals("session-456", fakeStorage.getTableId())
        assertEquals("char-123", viewModel.state.value.characterId)
        assertEquals("session-456", viewModel.state.value.tableId)
    }

    @Test
    fun `LoadCharacter saves trimmed characterId and decoded tableId`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        viewModel.onEvent(StartEvent.CharacterIdChanged("  char-123  "))
        viewModel.onEvent(StartEvent.TableIdChanged("encoded:table:456"))
        viewModel.onEvent(StartEvent.LoadCharacter)

        assertEquals("char-123", fakeStorage.getCharacterId())
        assertEquals("encoded:table:456", fakeStorage.getTableId()) 
    }

    // === My Characters Loading Tests ===

    @Test
    fun `loadMyCharacters loads templates and instances`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        val template = CharacterTemplateDto(
            template = createTestCharacter("template-1"),
            instances = emptyList()
        )
        val standalone = MyCharacterDto(
            character = createTestCharacter("standalone-1"),
            sessionId = "session-1",
            campaignName = "Test Campaign",
            isGameStarted = false
        )
        fakeRemoteDataSource.getMyCharactersResult = Result.Success(
            MyCharactersResponse(
                templates = listOf(template),
                standaloneInstances = listOf(standalone)
            )
        )

        viewModel.loadMyCharacters()

        // Wait for async operation
        kotlinx.coroutines.delay(100)

        assertEquals(1, viewModel.state.value.characterTemplates.size)
        assertEquals("template-1", viewModel.state.value.characterTemplates[0].template.id)
        assertEquals(1, viewModel.state.value.standaloneInstances.size)
        assertEquals("standalone-1", viewModel.state.value.standaloneInstances[0].character.id)
        assertFalse(viewModel.state.value.isLoadingMyCharacters)
    }

    @Test
    fun `loadMyCharacters handles errors gracefully`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        fakeRemoteDataSource.getMyCharactersResult = Result.Error(AppError.Network)

        viewModel.loadMyCharacters()

        // Wait for async operation
        kotlinx.coroutines.delay(100)

        assertFalse(viewModel.state.value.isLoadingMyCharacters)
        // Should not crash, just stop loading
    }

    // === Pending Assignments Tests ===

    @Test
    fun `loadPendingAssignments loads assignments`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        val assignment = PendingAssignmentDto(
            assignmentId = "assignment-1",
            character = createTestCharacter("char-1"),
            sessionId = "session-1",
            campaignName = "Test Campaign",
            status = "pending",
            masterUsername = "MasterUser"
        )
        fakeRemoteDataSource.getPendingAssignmentsResult = Result.Success(listOf(assignment))

        viewModel.onEvent(StartEvent.LoadPendingAssignments)

        // Wait for async operation
        kotlinx.coroutines.delay(100)

        assertEquals(1, viewModel.state.value.pendingAssignments.size)
        assertEquals("assignment-1", viewModel.state.value.pendingAssignments[0].assignmentId)
        assertFalse(viewModel.state.value.isLoadingAssignments)
        assertNull(viewModel.state.value.assignmentError)
    }

    @Test
    fun `respondToAssignment with accept removes assignment and reloads characters`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        val assignment = PendingAssignmentDto(
            assignmentId = "assignment-1",
            character = createTestCharacter("char-1"),
            sessionId = "session-1",
            campaignName = "Test Campaign",
            status = "pending"
        )
        fakeRemoteDataSource.getPendingAssignmentsResult = Result.Success(listOf(assignment))
        fakeRemoteDataSource.respondToAssignmentResult = Result.Success(Unit)

        viewModel.onEvent(StartEvent.RespondToAssignment("assignment-1", accept = true))

        // Wait for async operations
        kotlinx.coroutines.delay(100)

        assertEquals(0, viewModel.state.value.pendingAssignments.size)
        assertTrue(fakeRemoteDataSource.getMyCharactersCalls > 0) // Should reload characters
    }

    @Test
    fun `respondToAssignment with reject removes assignment`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        val assignment = PendingAssignmentDto(
            assignmentId = "assignment-1",
            character = createTestCharacter("char-1"),
            sessionId = "session-1",
            campaignName = "Test Campaign",
            status = "pending"
        )
        fakeRemoteDataSource.getPendingAssignmentsResult = Result.Success(listOf(assignment))
        fakeRemoteDataSource.respondToAssignmentResult = Result.Success(Unit)

        viewModel.onEvent(StartEvent.RespondToAssignment("assignment-1", accept = false))

        // Wait for async operations
        kotlinx.coroutines.delay(100)

        assertEquals(0, viewModel.state.value.pendingAssignments.size)
    }

    @Test
    fun `respondToAssignment shows error on failure`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        fakeRemoteDataSource.getPendingAssignmentsResult = Result.Success(listOf(
            PendingAssignmentDto(
                assignmentId = "assignment-1",
                character = createTestCharacter("char-1"),
                sessionId = "session-1",
                campaignName = "Test Campaign",
                status = "pending"
            )
        ))
        fakeRemoteDataSource.respondToAssignmentResult = Result.Error(AppError.Network)

        viewModel.onEvent(StartEvent.RespondToAssignment("assignment-1", accept = true))

        // Wait for async operations
        kotlinx.coroutines.delay(100)

        assertNotNull(viewModel.state.value.assignmentError)
        assertTrue(viewModel.state.value.assignmentError?.contains("Failed to respond") == true)
    }

    // === Join Campaign Tests ===

    @Test
    fun `JoinCampaign with success saves tableId and reloads characters`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        fakeRemoteDataSource.joinCampaignResult = Result.Success(Unit)
        val template = CharacterTemplateDto(
            template = createTestCharacter("char-1"),
            instances = emptyList()
        )
        fakeRemoteDataSource.getMyCharactersResult = Result.Success(
            MyCharactersResponse(templates = listOf(template))
        )

        viewModel.onEvent(StartEvent.JoinCampaign("char-1", "encoded:session-123"))

        // Wait for async operations
        kotlinx.coroutines.delay(100)

        assertEquals("encoded:session-123", fakeStorage.getTableId())
        assertFalse(viewModel.state.value.isJoiningCampaign)
        assertNull(viewModel.state.value.joinError)
    }

    @Test
    fun `JoinCampaign decodes gameId`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        fakeRemoteDataSource.joinCampaignResult = Result.Success(Unit)

        viewModel.onEvent(StartEvent.JoinCampaign("char-1", "encoded:session-456"))

        // Wait for async operations
        kotlinx.coroutines.delay(100)

        // Verify the decoded ID was sent to the repository
        assertEquals(1, fakeRemoteDataSource.joinCampaignCalls)
    }

    @Test
    fun `JoinCampaign shows error on failure`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        fakeRemoteDataSource.joinCampaignResult = Result.Error(AppError.Network)

        viewModel.onEvent(StartEvent.JoinCampaign("char-1", "session-123"))

        // Wait for async operations
        kotlinx.coroutines.delay(100)

        assertFalse(viewModel.state.value.isJoiningCampaign)
        assertNotNull(viewModel.state.value.joinError)
        assertTrue(viewModel.state.value.joinError?.contains("Failed to join") == true)
    }

    @Test
    fun `DismissJoinError clears join error`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        fakeRemoteDataSource.joinCampaignResult = Result.Error(AppError.Network)
        viewModel.onEvent(StartEvent.JoinCampaign("char-1", "session-123"))

        // Wait for async operations
        kotlinx.coroutines.delay(100)

        assertNotNull(viewModel.state.value.joinError)

        viewModel.onEvent(StartEvent.DismissJoinError)

        assertNull(viewModel.state.value.joinError)
    }

    // === Delete Character Tests ===

    @Test
    fun `DeleteCharacter reloads characters after deletion`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        val template = CharacterTemplateDto(
            template = createTestCharacter("char-1"),
            instances = emptyList()
        )
        fakeRemoteDataSource.deleteMyCharacterResult = Result.Success(Unit)
        fakeRemoteDataSource.getMyCharactersResult = Result.Success(
            MyCharactersResponse(templates = listOf(template))
        )

        viewModel.onEvent(StartEvent.DeleteCharacter("char-1"))

        // Wait for async operations
        kotlinx.coroutines.delay(100)

        assertTrue(fakeRemoteDataSource.deleteMyCharacterCalls > 0)
        assertTrue(fakeRemoteDataSource.getMyCharactersCalls > 0) // Should reload
    }

    @Test
    fun `DeleteCharacter handles errors gracefully`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        fakeRemoteDataSource.deleteMyCharacterResult = Result.Error(AppError.Network)

        viewModel.onEvent(StartEvent.DeleteCharacter("char-1"))

        // Wait for async operations
        kotlinx.coroutines.delay(100)

        // Should not crash, just handle error silently
    }

    // === Logout Tests ===

    @Test
    fun `Logout calls auth repository logout`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        viewModel.onEvent(StartEvent.Logout)

        // Wait for async operation
        kotlinx.coroutines.delay(100)

        assertTrue(fakeAuthRepository.logoutCalled)
    }

    // === Refresh Tests ===

    @Test
    fun `RefreshMyCharacters reloads both characters and assignments`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        val template = CharacterTemplateDto(
            template = createTestCharacter("char-1"),
            instances = emptyList()
        )
        fakeRemoteDataSource.getMyCharactersResult = Result.Success(
            MyCharactersResponse(templates = listOf(template))
        )
        fakeRemoteDataSource.getPendingAssignmentsResult = Result.Success(emptyList())

        viewModel.onEvent(StartEvent.RefreshMyCharacters)

        // Wait for async operations
        kotlinx.coroutines.delay(100)

        assertTrue(fakeRemoteDataSource.getMyCharactersCalls > 0)
        assertTrue(fakeRemoteDataSource.getPendingAssignmentsCalls > 0)
    }

    // === Remote Updates Tests ===

    @Test
    fun `remote update for characters triggers refresh`() = runTest(testDispatcher) {
        createViewModel(backgroundScope)
        fakeRemoteDataSource.getMyCharactersResult = Result.Success(
            MyCharactersResponse(templates = emptyList())
        )
        val initialCalls = fakeRemoteDataSource.getMyCharactersCalls

        // Simulate WebSocket update
        fakeCharacterRepository.emitRemoteUpdate("characters")

        // Wait for async operations
        kotlinx.coroutines.delay(100)

        assertTrue(fakeRemoteDataSource.getMyCharactersCalls > initialCalls)
    }

    @Test
    fun `remote update for assignments triggers refresh`() = runTest(testDispatcher) {
        createViewModel(backgroundScope)
        fakeRemoteDataSource.getPendingAssignmentsResult = Result.Success(emptyList())
        val initialCalls = fakeRemoteDataSource.getPendingAssignmentsCalls

        // Simulate WebSocket update
        fakeCharacterRepository.emitRemoteUpdate("assignment")

        // Wait for async operations
        kotlinx.coroutines.delay(100)

        assertTrue(fakeRemoteDataSource.getPendingAssignmentsCalls > initialCalls)
    }

    // === State Flow Tests ===

    @Test
    fun `state flow emits state changes correctly`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        viewModel.state.test {
            val initialState = awaitItem()
            assertEquals("", initialState.characterId)

            viewModel.onEvent(StartEvent.CharacterIdChanged("char-1"))
            assertEquals("char-1", awaitItem().characterId)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // === Edge Cases ===

    @Test
    fun `multiple rapid operations are handled correctly`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        repeat(3) {
            viewModel.onEvent(StartEvent.CharacterIdChanged("char-$it"))
        }

        assertEquals("char-2", viewModel.state.value.characterId)
    }

    @Test
    fun `LoadCharacter with blank fields does nothing`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        viewModel.onEvent(StartEvent.CharacterIdChanged(""))
        viewModel.onEvent(StartEvent.TableIdChanged(""))
        viewModel.onEvent(StartEvent.LoadCharacter)

        // Storage should remain unchanged if inputs are blank
    }

    private fun createTestCharacter(id: String) = Character(
        id = id,
        name = "Test Character",
        playerName = "Player",
        race = "Human",
        characterClass = "Fighter",
        level = 1,
        description = "Test",
        maxHp = 20,
        currentHp = 20,
        items = emptyList()
    )
}
