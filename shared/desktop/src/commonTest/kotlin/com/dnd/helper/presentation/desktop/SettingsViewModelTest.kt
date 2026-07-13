package com.dnd.helper.presentation.desktop

import com.dnd.helper.fakes.FakeAuthRepository
import com.dnd.helper.fakes.FakeCharacterStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: SettingsViewModel
    private lateinit var fakeStorage: FakeCharacterStorage
    private lateinit var fakeAuthRepository: FakeAuthRepository

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeStorage = FakeCharacterStorage()
        fakeAuthRepository = FakeAuthRepository()

        viewModel = SettingsViewModel(
            storage = fakeStorage,
            authRepository = fakeAuthRepository
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // === Initial State Tests ===

    @Test
    fun `initial state loads values from storage`() = runTest(testDispatcher) {
        fakeStorage.storedServerAddress = "http://test-server.com"
        fakeStorage.storedComfyUiAddress = "192.168.1.100:8000"
        fakeStorage.storedComfyUiWorkflow = JsonObject(mapOf("test" to JsonPrimitive("value")))
        fakeStorage.storedGenerationSteps = 25

        val newViewModel = SettingsViewModel(
            storage = fakeStorage,
            authRepository = fakeAuthRepository
        )

        assertEquals("http://test-server.com", newViewModel.state.value.serverAddress)
        assertEquals("192.168.1.100:8000", newViewModel.state.value.comfyUiAddress)
        assertTrue(newViewModel.state.value.hasWorkflow)
        assertEquals(25, newViewModel.state.value.generationSteps)
    }

    @Test
    fun `initial state uses defaults when storage is null`() = runTest(testDispatcher) {
        assertEquals("http://localhost:9090", viewModel.state.value.serverAddress)
        assertEquals("127.0.0.1:8000", viewModel.state.value.comfyUiAddress)
        assertFalse(viewModel.state.value.hasWorkflow)
        assertEquals(20, viewModel.state.value.generationSteps)
    }

    // === Server Address Tests ===

    @Test
    fun `updateServerAddress saves to storage and updates state`() = runTest(testDispatcher) {
        viewModel.updateServerAddress("http://new-server.com")

        assertEquals("http://new-server.com", fakeStorage.storedServerAddress)
        assertEquals("http://new-server.com", viewModel.state.value.serverAddress)
    }

    @Test
    fun `updateServerAddress handles empty string`() = runTest(testDispatcher) {
        viewModel.updateServerAddress("")

        assertEquals("", viewModel.state.value.serverAddress)
        assertEquals("", fakeStorage.storedServerAddress)
    }

    // === ComfyUI Address Tests ===

    @Test
    fun `updateComfyUiAddress saves to storage and updates state`() = runTest(testDispatcher) {
        viewModel.updateComfyUiAddress("192.168.1.100:8000")

        assertEquals("192.168.1.100:8000", fakeStorage.storedComfyUiAddress)
        assertEquals("192.168.1.100:8000", viewModel.state.value.comfyUiAddress)
    }

    // === ComfyUI Workflow Tests ===

    @Test
    fun `updateComfyUiWorkflow with valid JSON saves to storage and updates state`() = runTest(testDispatcher) {
        val validJson = """{"test": "value", "number": 123}"""

        viewModel.updateComfyUiWorkflow(validJson)

        assertNotNull(fakeStorage.storedComfyUiWorkflow)
        assertTrue(viewModel.state.value.hasWorkflow)
    }

    @Test
    fun `updateComfyUiWorkflow with invalid JSON does not crash`() = runTest(testDispatcher) {
        val invalidJson = "not valid json"

        viewModel.updateComfyUiWorkflow(invalidJson)

        // Should not crash, just handle error silently
        assertFalse(viewModel.state.value.hasWorkflow)
    }

    @Test
    fun `updateComfyUiWorkflow with empty string does not crash`() = runTest(testDispatcher) {
        viewModel.updateComfyUiWorkflow("")

        // Should not crash
        assertFalse(viewModel.state.value.hasWorkflow)
    }

    // === Generation Steps Tests ===

    @Test
    fun `updateGenerationSteps saves to storage and updates state`() = runTest(testDispatcher) {
        viewModel.updateGenerationSteps(30)

        assertEquals(30, fakeStorage.storedGenerationSteps)
        assertEquals(30, viewModel.state.value.generationSteps)
    }

    @Test
    fun `updateGenerationSteps handles zero value`() = runTest(testDispatcher) {
        viewModel.updateGenerationSteps(0)

        assertEquals(0, viewModel.state.value.generationSteps)
        assertEquals(0, fakeStorage.storedGenerationSteps)
    }

    @Test
    fun `updateGenerationSteps handles negative value`() = runTest(testDispatcher) {
        viewModel.updateGenerationSteps(-5)

        assertEquals(-5, viewModel.state.value.generationSteps)
    }

    // === Logout Tests ===

    @Test
    fun `logout calls auth repository logout`() = runTest(testDispatcher) {
        viewModel.logout()

        // Wait for async operation
        kotlinx.coroutines.delay(100)

        assertTrue(fakeAuthRepository.logoutCalled)
    }

    // === State Persistence Tests ===

    @Test
    fun `state changes persist across updates`() = runTest(testDispatcher) {
        viewModel.updateServerAddress("http://server1.com")
        viewModel.updateComfyUiAddress("192.168.1.1:8000")
        viewModel.updateGenerationSteps(15)

        assertEquals("http://server1.com", viewModel.state.value.serverAddress)
        assertEquals("192.168.1.1:8000", viewModel.state.value.comfyUiAddress)
        assertEquals(15, viewModel.state.value.generationSteps)
    }

    // === Edge Cases ===

    @Test
    fun `handles rapid updates correctly`() = runTest(testDispatcher) {
        repeat(5) { index ->
            viewModel.updateServerAddress("http://server-$index.com")
        }

        // Should have final value
        assertEquals("http://server-4.com", viewModel.state.value.serverAddress)
        assertEquals("http://server-4.com", fakeStorage.storedServerAddress)
    }

    @Test
    fun `workflow with complex JSON structure is parsed correctly`() = runTest(testDispatcher) {
        val complexJson = """{
            "workflow": {
                "nodes": [
                    {"id": "1", "type": "input"},
                    {"id": "2", "type": "output"}
                ]
            }
        }"""

        viewModel.updateComfyUiWorkflow(complexJson)

        assertTrue(viewModel.state.value.hasWorkflow)
        assertNotNull(fakeStorage.storedComfyUiWorkflow)
    }
}
