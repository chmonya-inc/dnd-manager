package com.dnd.helper.presentation.desktop

import com.dnd.helper.domain.common.AppError
import com.dnd.helper.domain.common.Result
import com.dnd.helper.fakes.FakeCharacterRepository
import com.dnd.helper.fakes.FakeRemoteDataSource
import kotlinx.coroutines.CompletableDeferred
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AssignCharacterViewModelTest {

    private lateinit var viewModel: AssignCharacterViewModel
    private lateinit var remote: FakeRemoteDataSource
    private lateinit var repo: FakeCharacterRepository

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        remote = FakeRemoteDataSource()
        repo = FakeCharacterRepository()
        viewModel = AssignCharacterViewModel(remote, repo)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `empty username sets error and sends nothing`() = runTest {
        viewModel.assignCharacter("char-1", "session-1")

        assertEquals("Username cannot be empty", viewModel.state.value.error)
        assertEquals(0, remote.createAssignmentCalls)
    }

    @Test
    fun `successful assign sends one request and marks success`() = runTest {
        viewModel.onUsernameChanged("player1")
        viewModel.assignCharacter("char-1", "session-1")

        assertEquals(1, remote.createAssignmentCalls)
        assertTrue(viewModel.state.value.success)
        assertFalse(viewModel.state.value.isAssigning)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `rapid double assign sends only one request`() = runTest {
        // Regression guard for the double-send bug: the dialog's "Send Request" button can't
        // disable synchronously (Swing's Main dispatcher is not immediate), so a double-click
        // used to fire two createAssignment requests. The isAssigning guard rejects the 2nd.
        val gate = CompletableDeferred<Unit>()
        remote.createAssignmentGate = gate
        viewModel.onUsernameChanged("player1")

        viewModel.assignCharacter("char-1", "session-1") // increments counter, then awaits gate
        viewModel.assignCharacter("char-1", "session-1") // rejected by the isAssigning guard

        assertEquals(1, remote.createAssignmentCalls)
        gate.complete(Unit)
    }

    @Test
    fun `unassign calls assign-by-username with null and marks success`() = runTest {
        viewModel.unassignCharacter("char-1", "session-1")

        assertEquals(1, remote.unassignByUsernameCalls)
        assertTrue(viewModel.state.value.success)
    }

    @Test
    fun `failed assign surfaces an error message`() = runTest {
        remote.createAssignmentResult = Result.Error(AppError.Unknown("boom"))
        viewModel.onUsernameChanged("player1")
        viewModel.assignCharacter("char-1", "session-1")

        assertEquals(1, remote.createAssignmentCalls)
        assertFalse(viewModel.state.value.success)
        assertFalse(viewModel.state.value.isAssigning)
        assertEquals("boom", viewModel.state.value.error)
    }
}
