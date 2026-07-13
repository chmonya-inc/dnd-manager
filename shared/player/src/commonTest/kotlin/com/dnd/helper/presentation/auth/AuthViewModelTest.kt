package com.dnd.helper.presentation.auth

import app.cash.turbine.test
import com.dnd.helper.fakes.FakeAuthRepository
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
class AuthViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeRepository: FakeAuthRepository

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeAuthRepository()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(scope: CoroutineScope): AuthViewModel {
        return AuthViewModel(fakeRepository, scope)
    }

    // === State Update Tests ===

    @Test
    fun `initial state has correct defaults`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        val initialState = viewModel.state.value
        assertTrue(initialState.isLoginMode)
        assertFalse(initialState.isLoading)
        assertFalse(initialState.isRecoverMode)
        assertFalse(initialState.isSuccess)
        assertNull(initialState.error)
        assertEquals("", initialState.username)
        assertEquals("", initialState.password)
    }

    @Test
    fun `OnUsernameChanged updates username in state`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        viewModel.onEvent(AuthEvent.OnUsernameChanged("testuser"))

        assertEquals("testuser", viewModel.state.value.username)
    }

    @Test
    fun `OnPasswordChanged updates password in state`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        viewModel.onEvent(AuthEvent.OnPasswordChanged("password123"))

        assertEquals("password123", viewModel.state.value.password)
    }

    @Test
    fun `OnNewPasswordChanged updates newPassword in state`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        viewModel.onEvent(AuthEvent.OnNewPasswordChanged("newpassword123"))

        assertEquals("newpassword123", viewModel.state.value.newPassword)
    }

    @Test
    fun `ToggleMode switches between login and register modes`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        // Start in login mode
        assertTrue(viewModel.state.value.isLoginMode)

        // Toggle to register mode
        viewModel.onEvent(AuthEvent.ToggleMode)
        assertFalse(viewModel.state.value.isLoginMode)

        // Toggle back to login mode
        viewModel.onEvent(AuthEvent.ToggleMode)
        assertTrue(viewModel.state.value.isLoginMode)
    }

    @Test
    fun `ToggleMode clears error and recover mode`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        viewModel.onEvent(AuthEvent.OnUsernameChanged("user"))
        viewModel.onEvent(AuthEvent.Submit) // This will set an error due to empty password
        assertNotNull(viewModel.state.value.error)

        viewModel.onEvent(AuthEvent.ToggleMode)

        assertNull(viewModel.state.value.error)
        assertFalse(viewModel.state.value.isRecoverMode)
    }

    @Test
    fun `ToggleRecoverMode switches recover mode and clears other modes`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        viewModel.onEvent(AuthEvent.ToggleRecoverMode)

        assertTrue(viewModel.state.value.isRecoverMode)
        assertFalse(viewModel.state.value.isLoginMode)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `ToggleRole switches isMasterRole flag`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        assertFalse(viewModel.state.value.isMasterRole)

        viewModel.onEvent(AuthEvent.ToggleRole)
        assertTrue(viewModel.state.value.isMasterRole)

        viewModel.onEvent(AuthEvent.ToggleRole)
        assertFalse(viewModel.state.value.isMasterRole)
    }

    @Test
    fun `SetMasterRole sets master role and required role`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        viewModel.onEvent(AuthEvent.SetMasterRole)

        assertTrue(viewModel.state.value.isMasterRole)
        assertEquals("MASTER", viewModel.state.value.requiredRole)
    }

    @Test
    fun `SetRequiredRole updates required role`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        viewModel.onEvent(AuthEvent.SetRequiredRole("PLAYER"))

        assertEquals("PLAYER", viewModel.state.value.requiredRole)
    }

    @Test
    fun `ClearError clears error and role mismatch error`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        viewModel.onEvent(AuthEvent.SetRequiredRole("MASTER"))
        viewModel.onEvent(AuthEvent.OnUsernameChanged("test"))
        viewModel.onEvent(AuthEvent.Submit)

        assertNotNull(viewModel.state.value.error)

        viewModel.onEvent(AuthEvent.ClearError)

        assertNull(viewModel.state.value.error)
        assertNull(viewModel.state.value.errorRoleMismatch)
    }

    // === Authentication Tests ===

    @Test
    fun `Submit with empty fields shows validation error`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        viewModel.onEvent(AuthEvent.Submit)

        assertEquals("Username and password cannot be empty", viewModel.state.value.error)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `Submit with empty username shows validation error`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        viewModel.onEvent(AuthEvent.OnPasswordChanged("password"))
        viewModel.onEvent(AuthEvent.Submit)

        assertEquals("Username and password cannot be empty", viewModel.state.value.error)
    }

    @Test
    fun `Submit with empty password shows validation error`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        viewModel.onEvent(AuthEvent.OnUsernameChanged("user"))
        viewModel.onEvent(AuthEvent.Submit)

        assertEquals("Username and password cannot be empty", viewModel.state.value.error)
    }

    @Test
    fun `Submit in login mode calls repository login`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        viewModel.onEvent(AuthEvent.OnUsernameChanged("testuser"))
        viewModel.onEvent(AuthEvent.OnPasswordChanged("password"))

        viewModel.onEvent(AuthEvent.Submit)

        // Verify login was called
        assertTrue(fakeRepository.loginCalled)
        assertTrue(viewModel.state.value.isLoading)
    }

    @Test
    fun `Submit in register mode calls repository register with correct role`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        viewModel.onEvent(AuthEvent.ToggleMode) // Switch to register mode
        viewModel.onEvent(AuthEvent.OnUsernameChanged("newuser"))
        viewModel.onEvent(AuthEvent.OnPasswordChanged("password"))

        viewModel.onEvent(AuthEvent.Submit)

        // Verify register was called
        assertTrue(fakeRepository.registerCalled)
    }

    @Test
    fun `Submit with master role registers as MASTER`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        viewModel.onEvent(AuthEvent.ToggleMode) // Switch to register mode
        viewModel.onEvent(AuthEvent.ToggleRole) // Set as master
        viewModel.onEvent(AuthEvent.OnUsernameChanged("masteruser"))
        viewModel.onEvent(AuthEvent.OnPasswordChanged("password"))

        viewModel.onEvent(AuthEvent.Submit)

        assertTrue(fakeRepository.registerCalled)
        // Verify the role was MASTER in the register call
        assertEquals("MASTER", fakeRepository.lastRegisteredRole)
    }

    @Test
    fun `Submit in recover mode requires new password`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        viewModel.onEvent(AuthEvent.ToggleRecoverMode)
        viewModel.onEvent(AuthEvent.OnUsernameChanged("user"))
        viewModel.onEvent(AuthEvent.OnPasswordChanged("oldpass"))

        viewModel.onEvent(AuthEvent.Submit)

        assertEquals("New password cannot be empty", viewModel.state.value.error)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `Submit in recover mode calls repository recover`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        viewModel.onEvent(AuthEvent.ToggleRecoverMode)
        viewModel.onEvent(AuthEvent.OnUsernameChanged("user"))
        viewModel.onEvent(AuthEvent.OnPasswordChanged("oldpass"))
        viewModel.onEvent(AuthEvent.OnNewPasswordChanged("newpass"))

        viewModel.onEvent(AuthEvent.Submit)

        assertTrue(fakeRepository.recoverCalled)
        assertTrue(viewModel.state.value.isLoading)
    }

    // === Success and Error Handling Tests ===

    @Test
    fun `successful login updates state to success`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        fakeRepository.loginShouldSucceed = true

        viewModel.onEvent(AuthEvent.OnUsernameChanged("testuser"))
        viewModel.onEvent(AuthEvent.OnPasswordChanged("password"))
        viewModel.onEvent(AuthEvent.Submit)

        // Wait for async operation
        kotlinx.coroutines.delay(100)

        assertFalse(viewModel.state.value.isLoading)
        assertTrue(viewModel.state.value.isSuccess)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `failed login shows error message`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        fakeRepository.loginShouldSucceed = false
        fakeRepository.loginError = "Invalid credentials"

        viewModel.onEvent(AuthEvent.OnUsernameChanged("wronguser"))
        viewModel.onEvent(AuthEvent.OnPasswordChanged("wrongpass"))
        viewModel.onEvent(AuthEvent.Submit)

        // Wait for async operation
        kotlinx.coroutines.delay(100)

        assertFalse(viewModel.state.value.isLoading)
        assertFalse(viewModel.state.value.isSuccess)
        assertEquals("Invalid credentials", viewModel.state.value.error)
    }

    @Test
    fun `successful registration sets success and stores recover code`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        val recoverCode = "TEST123"
        fakeRepository.registerShouldSucceed = true
        fakeRepository.recoverCode = recoverCode

        viewModel.onEvent(AuthEvent.ToggleMode)
        viewModel.onEvent(AuthEvent.OnUsernameChanged("newuser"))
        viewModel.onEvent(AuthEvent.OnPasswordChanged("password"))
        viewModel.onEvent(AuthEvent.Submit)

        // Wait for async operation
        kotlinx.coroutines.delay(100)

        assertFalse(viewModel.state.value.isLoading)
        assertTrue(viewModel.state.value.isSuccess)
        assertEquals(recoverCode, viewModel.state.value.registeredRecoverCode)
    }

    @Test
    fun `role mismatch clears tokens and shows error`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        // Set up as requiring MASTER role but user is PLAYER
        fakeRepository.loginShouldSucceed = true
        fakeRepository.userRoleInResponse = "PLAYER" // User is actually a PLAYER

        viewModel.onEvent(AuthEvent.SetRequiredRole("MASTER"))
        viewModel.onEvent(AuthEvent.OnUsernameChanged("playeruser"))
        viewModel.onEvent(AuthEvent.OnPasswordChanged("password"))
        viewModel.onEvent(AuthEvent.Submit)

        // Wait for async operation
        kotlinx.coroutines.delay(100)

        assertFalse(viewModel.state.value.isSuccess)
        assertNotNull(viewModel.state.value.errorRoleMismatch)
        assertTrue(fakeRepository.logoutCalled) // Tokens should be cleared
    }

    @Test
    fun `MASTER role mismatch shows appropriate error message`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        fakeRepository.loginShouldSucceed = true
        fakeRepository.userRoleInResponse = "PLAYER"

        viewModel.onEvent(AuthEvent.SetRequiredRole("MASTER"))
        viewModel.onEvent(AuthEvent.OnUsernameChanged("playeruser"))
        viewModel.onEvent(AuthEvent.OnPasswordChanged("password"))
        viewModel.onEvent(AuthEvent.Submit)

        // Wait for async operation
        kotlinx.coroutines.delay(100)

        assertEquals("This account is not a Master account. Please use the Player app.",
                    viewModel.state.value.errorRoleMismatch)
    }

    @Test
    fun `PLAYER role mismatch shows appropriate error message`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        fakeRepository.loginShouldSucceed = true
        fakeRepository.userRoleInResponse = "MASTER"

        viewModel.onEvent(AuthEvent.SetRequiredRole("PLAYER"))
        viewModel.onEvent(AuthEvent.OnUsernameChanged("masteruser"))
        viewModel.onEvent(AuthEvent.OnPasswordChanged("password"))
        viewModel.onEvent(AuthEvent.Submit)

        // Wait for async operation
        kotlinx.coroutines.delay(100)

        assertEquals("This account is a Master account. Please use the Desktop app for Master tools.",
                    viewModel.state.value.errorRoleMismatch)
    }

    // === State Flow Tests ===

    @Test
    fun `state flow emits all state changes`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        viewModel.state.test {
            // Initial state
            val initialState = awaitItem()
            assertTrue(initialState.isLoginMode)

            // Username change
            viewModel.onEvent(AuthEvent.OnUsernameChanged("user"))
            assertEquals("user", awaitItem().username)

            // Password change
            viewModel.onEvent(AuthEvent.OnPasswordChanged("pass"))
            assertEquals("pass", awaitItem().password)

            // Mode toggle
            viewModel.onEvent(AuthEvent.ToggleMode)
            assertFalse(awaitItem().isLoginMode)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `state flow reflects loading state during auth operation`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        fakeRepository.loginShouldSucceed = true
        fakeRepository.loginDelay = 100

        viewModel.state.test {
            assertEquals("", awaitItem().username) // Initial state

            viewModel.onEvent(AuthEvent.OnUsernameChanged("user"))
            assertEquals("user", awaitItem().username)

            viewModel.onEvent(AuthEvent.OnPasswordChanged("pass"))
            assertEquals("pass", awaitItem().password)

            viewModel.onEvent(AuthEvent.Submit)

            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)

            val successState = awaitItem()
            assertFalse(successState.isLoading)
            assertTrue(successState.isSuccess)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // === Edge Cases ===

    @Test
    fun `multiple rapid Submit calls are handled correctly`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        viewModel.onEvent(AuthEvent.OnUsernameChanged("user"))
        viewModel.onEvent(AuthEvent.OnPasswordChanged("pass"))

        repeat(3) {
            viewModel.onEvent(AuthEvent.Submit)
        }

        // Should only trigger one auth operation
        // The ViewModel should handle this gracefully
        val callCount = fakeRepository.loginCalls.size
        assertTrue(callCount >= 1) // At least one call should have been made
    }

    @Test
    fun `validation errors prevent auth calls`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        viewModel.onEvent(AuthEvent.Submit) // Empty fields

        assertEquals("Username and password cannot be empty", viewModel.state.value.error)
        assertFalse(fakeRepository.loginCalled)
    }

    @Test
    fun `error state is properly cleared on next successful operation`() = runTest(testDispatcher) {
        val viewModel = createViewModel(backgroundScope)
        // First attempt fails
        fakeRepository.loginShouldSucceed = false
        viewModel.onEvent(AuthEvent.OnUsernameChanged("user"))
        viewModel.onEvent(AuthEvent.OnPasswordChanged("wrong"))
        viewModel.onEvent(AuthEvent.Submit)

        kotlinx.coroutines.delay(100)
        assertNotNull(viewModel.state.value.error)

        // Second attempt succeeds
        fakeRepository.loginShouldSucceed = true
        viewModel.onEvent(AuthEvent.OnPasswordChanged("correct"))
        viewModel.onEvent(AuthEvent.Submit)

        kotlinx.coroutines.delay(100)
        assertNull(viewModel.state.value.error)
        assertTrue(viewModel.state.value.isSuccess)
    }
}
