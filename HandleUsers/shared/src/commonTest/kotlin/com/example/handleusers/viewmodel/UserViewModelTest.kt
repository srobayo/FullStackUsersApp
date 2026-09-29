package com.example.handleusers.viewmodel

import com.example.handleusers.models.User
import com.example.handleusers.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class UserViewModelTest {
    @Test
    fun initialLoadPublishesUsersAndConnectedState() = runViewModelTest {
        val users = listOf(user(id = "1", name = "Ana"))
        val repository = FakeUserRepository(usersResult = Result.success(users))

        val viewModel = UserViewModel(SERVER_URL, repository)
        advanceUntilIdle()

        assertEquals(users, viewModel.uiState.value.users)
        assertTrue(viewModel.uiState.value.isServerConnected == true)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)
        assertEquals(1, repository.getUsersCalls)
    }

    @Test
    fun initialLoadPublishesRepositoryError() = runViewModelTest {
        val repository = FakeUserRepository(
            usersResult = Result.failure(IllegalStateException("Servidor no disponible"))
        )

        val viewModel = UserViewModel(SERVER_URL, repository)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isServerConnected == false)
        assertEquals("Servidor no disponible", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun createUserTrimsInputAndReloadsUsers() = runViewModelTest {
        val createdUser = user(id = "2", name = "Beatriz")
        val repository = FakeUserRepository(
            usersResult = Result.success(listOf(createdUser)),
            createResult = Result.success(createdUser)
        )
        val viewModel = UserViewModel(SERVER_URL, repository)
        advanceUntilIdle()

        viewModel.createUser("  Beatriz  ", "  beatriz@example.com  ")
        advanceUntilIdle()

        assertEquals(CreateCall("Beatriz", "beatriz@example.com"), repository.createCalls.single())
        assertEquals(2, repository.getUsersCalls)
        assertEquals(listOf(createdUser), viewModel.uiState.value.users)
        assertEquals("Usuario 'Beatriz' creado con éxito", viewModel.uiState.value.successMessage)
        assertFalse(viewModel.uiState.value.isActionInProgress)
    }

    @Test
    fun createUserPublishesRepositoryErrorWithoutReloading() = runViewModelTest {
        val repository = FakeUserRepository(
            usersResult = Result.success(emptyList()),
            createResult = Result.failure(IllegalStateException("El correo ya existe"))
        )
        val viewModel = UserViewModel(SERVER_URL, repository)
        advanceUntilIdle()

        viewModel.createUser("Ana", "ana@example.com")
        advanceUntilIdle()

        assertEquals("El correo ya existe", viewModel.uiState.value.errorMessage)
        assertEquals(1, repository.getUsersCalls)
        assertFalse(viewModel.uiState.value.isActionInProgress)
    }

    @Test
    fun updateUserDelegatesChangesAndReloadsUsers() = runViewModelTest {
        val updatedUser = user(id = "1", name = "Ana María", active = false)
        val repository = FakeUserRepository(
            usersResult = Result.success(listOf(updatedUser)),
            updateResult = Result.success(updatedUser)
        )
        val viewModel = UserViewModel(SERVER_URL, repository)
        advanceUntilIdle()

        viewModel.updateUser("1", "  Ana María  ", "  ana@example.com  ", active = false)
        advanceUntilIdle()

        assertEquals(
            UpdateCall("1", "Ana María", "ana@example.com", active = false),
            repository.updateCalls.single()
        )
        assertEquals(2, repository.getUsersCalls)
        assertEquals("Usuario 'Ana María' actualizado", viewModel.uiState.value.successMessage)
        assertFalse(viewModel.uiState.value.isActionInProgress)
    }

    @Test
    fun deleteUserClearsConfirmationAndReloadsUsers() = runViewModelTest {
        val repository = FakeUserRepository(
            usersResult = Result.success(emptyList()),
            deleteResult = Result.success(Unit)
        )
        val selectedUser = user(id = "1", name = "Ana")
        val viewModel = UserViewModel(SERVER_URL, repository)
        advanceUntilIdle()
        viewModel.openDeleteConfirmation(selectedUser)

        viewModel.deleteUser(selectedUser.id)
        advanceUntilIdle()

        assertEquals(listOf("1"), repository.deleteCalls)
        assertEquals(2, repository.getUsersCalls)
        assertNull(viewModel.uiState.value.showDeleteConfirmation)
        assertEquals("Usuario eliminado correctamente", viewModel.uiState.value.successMessage)
        assertFalse(viewModel.uiState.value.isActionInProgress)
    }

    @Test
    fun invalidCreateInputDoesNotCallRepository() = runViewModelTest {
        val repository = FakeUserRepository(usersResult = Result.success(emptyList()))
        val viewModel = UserViewModel(SERVER_URL, repository)
        advanceUntilIdle()

        viewModel.createUser("   ", "invalid")

        assertEquals("El nombre no puede estar vacío", viewModel.uiState.value.errorMessage)
        assertTrue(repository.createCalls.isEmpty())
    }

    @Test
    fun updateServerUrlTrimsValueAndReloadsUsers() = runViewModelTest {
        val repository = FakeUserRepository(usersResult = Result.success(emptyList()))
        val viewModel = UserViewModel(SERVER_URL, repository)
        advanceUntilIdle()

        viewModel.updateServerUrl("  https://api.example.test/  ")
        advanceUntilIdle()

        assertEquals("https://api.example.test/", repository.updatedServerUrls.single())
        assertEquals("https://api.example.test/", viewModel.uiState.value.serverUrl)
        assertEquals(2, repository.getUsersCalls)
    }

    @Test
    fun failedConnectionTestIncludesCurrentServerUrl() = runViewModelTest {
        val repository = FakeUserRepository(
            usersResult = Result.success(emptyList()),
            connectionResult = false
        )
        val viewModel = UserViewModel(SERVER_URL, repository)
        advanceUntilIdle()

        viewModel.testServerConnection()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isServerConnected == false)
        assertEquals("No se pudo conectar a $SERVER_URL", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isActionInProgress)
    }

    private fun runViewModelTest(block: suspend TestScope.() -> Unit) = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            block()
        } finally {
            Dispatchers.resetMain()
        }
    }

    private fun user(
        id: String,
        name: String,
        active: Boolean = true
    ) = User(
        id = id,
        name = name,
        email = "${name.lowercase().replace(" ", ".")}@example.com",
        active = active
    )

    private companion object {
        const val SERVER_URL = "http://example.test"
    }
}

private data class CreateCall(
    val name: String,
    val email: String
)

private data class UpdateCall(
    val id: String,
    val name: String?,
    val email: String?,
    val active: Boolean?
)

private class FakeUserRepository(
    var usersResult: Result<List<User>>,
    var createResult: Result<User> = Result.failure(IllegalStateException("createUser no configurado")),
    var updateResult: Result<User> = Result.failure(IllegalStateException("updateUser no configurado")),
    var deleteResult: Result<Unit> = Result.failure(IllegalStateException("deleteUser no configurado")),
    var connectionResult: Boolean = true
) : UserRepository {
    var getUsersCalls = 0
    val createCalls = mutableListOf<CreateCall>()
    val updateCalls = mutableListOf<UpdateCall>()
    val deleteCalls = mutableListOf<String>()
    val updatedServerUrls = mutableListOf<String>()

    override suspend fun getUsers(): Result<List<User>> {
        getUsersCalls += 1
        return usersResult
    }

    override suspend fun createUser(name: String, email: String): Result<User> {
        createCalls += CreateCall(name, email)
        return createResult
    }

    override suspend fun updateUser(
        id: String,
        name: String?,
        email: String?,
        active: Boolean?
    ): Result<User> {
        updateCalls += UpdateCall(id, name, email, active)
        return updateResult
    }

    override suspend fun deleteUser(id: String): Result<Unit> {
        deleteCalls += id
        return deleteResult
    }

    override suspend fun testConnection(): Boolean = connectionResult

    override fun updateServerUrl(serverUrl: String) {
        updatedServerUrls += serverUrl
    }

    override fun close() {
        // No resources are owned by this test double.
    }
}
