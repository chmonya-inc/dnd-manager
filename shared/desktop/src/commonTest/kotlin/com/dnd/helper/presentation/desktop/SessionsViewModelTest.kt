package com.dnd.helper.presentation.desktop

import com.dnd.helper.data.remote.dto.auth.CampaignDto
import com.dnd.helper.domain.common.Result
import com.dnd.helper.domain.model.Character
import com.dnd.helper.domain.model.InitialData
import com.dnd.helper.fakes.FakeCharacterRepository
import com.dnd.helper.fakes.FakeCharacterStorage
import com.dnd.helper.fakes.FakeRemoteDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
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
class SessionsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: SessionsViewModel
    private lateinit var fakeStorage: FakeCharacterStorage
    private lateinit var fakeRemoteDataSource: FakeRemoteDataSource
    private lateinit var fakeCharacterRepository: FakeCharacterRepository

    private fun createTestCampaign(
        id: String = "session-1",
        name: String = "Test Campaign",
        isStarted: Boolean = false
    ) = CampaignDto(
        id = id,
        name = name,
        ownerId = "owner-1",
        sessionId = id,
        isStarted = isStarted
    )

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeStorage = FakeCharacterStorage()
        fakeRemoteDataSource = FakeRemoteDataSource()
        fakeCharacterRepository = FakeCharacterRepository()

        // Setup default campaign data
        fakeRemoteDataSource.campaignsResult = Result.Success(listOf(
            createTestCampaign("session-1", "Campaign 1"),
            createTestCampaign("session-2", "Campaign 2")
        ))
        fakeRemoteDataSource.getInitialDataResult = Result.Success(InitialData(
            characters = emptyList(),
            locations = emptyList(),
            monsters = emptyList(),
            npcs = emptyList(),
            lastModified = ""
        ))

        viewModel = SessionsViewModel(
            storage = fakeStorage,
            remoteDataSource = fakeRemoteDataSource,
            characterRepository = fakeCharacterRepository,
            pollingIntervalMs = 0 // Disable polling for tests
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // === Initial State Tests ===

    @Test
    fun `initial state loads campaigns from remote data source`() = runTest(testDispatcher) {
        // Wait for init
        runCurrent()

        assertTrue(viewModel.state.value.campaigns.size >= 2)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `initial state loads activeTableId from storage`() = runTest(testDispatcher) {
        fakeStorage.storedTableId = "session-active"

        val newViewModel = SessionsViewModel(
            storage = fakeStorage,
            remoteDataSource = fakeRemoteDataSource,
            characterRepository = fakeCharacterRepository,
            pollingIntervalMs = 0 // Disable polling for tests
        )

        // Wait for init
        runCurrent()

        assertEquals("session-active", newViewModel.state.value.activeTableId)
    }

    // === Campaign Selection Tests ===

    @Test
    fun `selectCampaign updates activeTableId and saves to storage`() = runTest(testDispatcher) {
        viewModel.selectCampaign("session-new")

        assertEquals("session-new", viewModel.state.value.activeTableId)
        assertEquals("session-new", fakeStorage.storedTableId)
    }

    @Test
    fun `selectCampaignForPreview updates previewCampaignId and loads data`() = runTest(testDispatcher) {
        // Wait for initial load
        runCurrent()

        viewModel.selectCampaignForPreview("session-1")

        // Wait for preview load
        runCurrent()

        assertEquals("session-1", viewModel.state.value.previewCampaignId)
        assertNotNull(viewModel.state.value.previewData)
        assertFalse(viewModel.state.value.isPreviewLoading)
    }

    // === Campaign Management Tests ===

    @Test
    fun `toggleCampaignStart performs optimistic update`() = runTest(testDispatcher) {
        runCurrent() // Wait for initial load
        
        // Ensure campaign exists in state
        assertTrue(viewModel.state.value.campaigns.any { it.id == "session-1" })

        // Update fake so that refresh doesn't overwrite with old data
        // This simulates the server state being updated before the refresh happens
        fakeRemoteDataSource.campaignsResult = Result.Success(listOf(
            createTestCampaign("session-1", "Campaign 1", isStarted = true),
            createTestCampaign("session-2", "Campaign 2")
        ))

        viewModel.toggleCampaignStart("session-1", true)

        // Should update immediately (optimistic)
        val campaign = viewModel.state.value.campaigns.find { it.id == "session-1" }
        assertEquals(true, campaign?.isStarted)
    }

    @Test
    fun `toggleCampaignStart reverts on failure`() = runTest(testDispatcher) {
        fakeRemoteDataSource.toggleCampaignStartResult = Result.Error(com.dnd.helper.domain.common.AppError.Network)

        // Wait for initial load
        runCurrent()

        val campaignBefore = viewModel.state.value.campaigns.find { it.id == "session-1" }
        val startedBefore = campaignBefore?.isStarted

        viewModel.toggleCampaignStart("session-1", !startedBefore!!)

        // Wait for async operation
        runCurrent()

        // Should revert to original state
        val campaignAfter = viewModel.state.value.campaigns.find { it.id == "session-1" }
        assertEquals(startedBefore, campaignAfter?.isStarted)
        assertNotNull(viewModel.state.value.error)
    }

    @Test
    fun `addCampaign creates new campaign and refreshes list`() = runTest(testDispatcher) {
        val newCampaignDto = createTestCampaign("session-3", "New Campaign")
        fakeRemoteDataSource.createCampaignResult = Result.Success(newCampaignDto)
        
        // Since addCampaign calls loadCampaigns(), we must update the fake's source list
        val currentCampaigns = (fakeRemoteDataSource.campaignsResult as Result.Success).data
        fakeRemoteDataSource.campaignsResult = Result.Success(currentCampaigns + newCampaignDto)

        viewModel.addCampaign("New Campaign", "")

        // Wait for async operations
        runCurrent()

        // Should refresh the list
        assertTrue(viewModel.state.value.campaigns.any { it.name == "New Campaign" })
    }

    @Test
    fun `addCampaign shows error on failure`() = runTest(testDispatcher) {
        fakeRemoteDataSource.createCampaignResult = Result.Error(com.dnd.helper.domain.common.AppError.Network)

        viewModel.addCampaign("Failed Campaign", "")

        // Wait for async operations
        runCurrent()

        assertNotNull(viewModel.state.value.error)
    }

    @Test
    fun `deleteCampaignLocal removes campaign from state and clears activeTableId`() = runTest(testDispatcher) {
        runCurrent() // Wait for initial load
        viewModel.selectCampaign("session-1")

        viewModel.deleteCampaignLocal("session-1")

        assertFalse(viewModel.state.value.campaigns.any { it.id == "session-1" })
        assertEquals("", viewModel.state.value.activeTableId)
        assertEquals("", fakeStorage.storedTableId)
    }

    // === Character Transfer Tests ===

    @Test
    fun `transferOrCopyCharacter saves character to target session`() = runTest(testDispatcher) {
        val character = Character(
            id = "char-1",
            name = "Test Character",
            playerName = "Player",
            race = "Human",
            characterClass = "Fighter",
            level = 1,
            description = "Test",
            maxHp = 20,
            currentHp = 20,
        )
        val targetCampaign = Campaign("session-2", "Target Campaign", false)

        viewModel.transferOrCopyCharacter(character, "session-1", targetCampaign, false, true)

        // Wait for async operations
        runCurrent()

        assertTrue(fakeRemoteDataSource.saveCharacterToSessionCalls > 0)
        assertEquals(targetCampaign.id, fakeRemoteDataSource.lastSaveTargetSession)
        assertFalse(viewModel.state.value.isTransferring)
    }

    @Test
    fun `transferOrCopyCharacter with copy creates new character id`() = runTest(testDispatcher) {
        val character = Character(
            id = "char-1",
            name = "Test Character",
            playerName = "Player",
            race = "Human",
            characterClass = "Fighter",
            level = 1,
            description = "Test",
            maxHp = 20,
            currentHp = 20,
        )
        val targetCampaign = Campaign("session-2", "Target Campaign", false)

        viewModel.transferOrCopyCharacter(character, "session-1", targetCampaign, true, true)

        // Wait for async operations
        runCurrent()

        val savedCharacter = fakeRemoteDataSource.lastSavedToSessionCharacter
        // Should have a different ID when copied
        assertTrue(savedCharacter?.id != "char-1")
    }

    @Test
    fun `transferOrCopyCharacter without items saves empty item list`() = runTest(testDispatcher) {
        val character = Character(
            id = "char-1",
            name = "Test Character",
            playerName = "Player",
            race = "Human",
            characterClass = "Fighter",
            level = 1,
            description = "Test",
            maxHp = 20,
            currentHp = 20,
            items = listOf(
                com.dnd.helper.domain.model.Item(
                    id = "item-1",
                    name = "Sword",
                    slot = com.dnd.helper.domain.model.EquipmentSlot.MAIN_HAND,
                    rarity = com.dnd.helper.domain.model.ItemRarity.COMMON,
                    description = "A sword",
                    weight = 2.0
                )
            )
        )
        val targetCampaign = Campaign("session-2", "Target Campaign", false)

        viewModel.transferOrCopyCharacter(character, "session-1", targetCampaign, false, false)

        // Wait for async operations
        runCurrent()

        val savedCharacter = fakeRemoteDataSource.lastSavedToSessionCharacter
        assertTrue(savedCharacter?.items?.isEmpty() == true)
    }

    @Test
    fun `transferOrCopyCharacter shows error on save failure`() = runTest(testDispatcher) {
        val character = Character(
            id = "char-1",
            name = "Test Character",
            playerName = "Player",
            race = "Human",
            characterClass = "Fighter",
            level = 1,
            description = "Test",
            maxHp = 20,
            currentHp = 20,
        )
        val targetCampaign = Campaign("session-2", "Target Campaign", false)
        fakeRemoteDataSource.saveToSessionResult = Result.Error(com.dnd.helper.domain.common.AppError.Network)

        viewModel.transferOrCopyCharacter(character, "session-1", targetCampaign, false, true)

        // Wait for async operations
        runCurrent()

        assertNotNull(viewModel.state.value.error)
        assertFalse(viewModel.state.value.isTransferring)
    }

    // === Error Handling Tests ===

    @Test
    fun `clearError clears error from state`() = runTest(testDispatcher) {
        fakeRemoteDataSource.campaignsResult = Result.Error(com.dnd.helper.domain.common.AppError.Network)

        viewModel.loadCampaigns()

        // Wait for async operations
        runCurrent()

        assertNotNull(viewModel.state.value.error)

        viewModel.clearError()

        assertNull(viewModel.state.value.error)
    }

    // === Edge Cases ===

    @Test
    fun `loadCampaigns with empty result handles gracefully`() = runTest(testDispatcher) {
        fakeRemoteDataSource.campaignsResult = Result.Success(emptyList())

        viewModel.loadCampaigns()

        // Wait for async operations
        runCurrent()

        assertTrue(viewModel.state.value.campaigns.isEmpty())
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `handles rapid campaign selection correctly`() = runTest(testDispatcher) {
        repeat(3) { index ->
            viewModel.selectCampaign("session-$index")
        }

        assertEquals("session-2", viewModel.state.value.activeTableId)
    }
}
