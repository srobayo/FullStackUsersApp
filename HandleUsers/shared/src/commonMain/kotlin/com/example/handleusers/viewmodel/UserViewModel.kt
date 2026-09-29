package com.example.handleusers.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.handleusers.models.User
import com.example.handleusers.network.AxumApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ActiveFilter {
    ALL, ACTIVE, INACTIVE
}

data class UserUiState(
    val users: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val searchQuery: String = "",
    val activeFilter: ActiveFilter = ActiveFilter.ALL,
    val serverUrl: String = "http://10.0.2.2:3000",
    val isServerConnected: Boolean? = null,
    val selectedUserForEdit: User? = null,
    val showUserDialog: Boolean = false,
    val showDeleteConfirmation: User? = null,
    val showServerConfigDialog: Boolean = false,
    val isActionInProgress: Boolean = false
) {
    val filteredUsers: List<User>
        get() = users.filter { user ->
            val matchesSearch = searchQuery.isBlank() ||
                    user.name.contains(searchQuery, ignoreCase = true) ||
                    user.email.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (activeFilter) {
                ActiveFilter.ALL -> true
                ActiveFilter.ACTIVE -> user.active
                ActiveFilter.INACTIVE -> !user.active
            }

            matchesSearch && matchesFilter
        }

    val totalCount: Int get() = users.size
    val activeCount: Int get() = users.count { it.active }
    val inactiveCount: Int get() = users.size - activeCount
}

class UserViewModel(
    initialServerUrl: String = "http://10.0.2.2:3000"
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserUiState(serverUrl = initialServerUrl))
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    private var apiClient: AxumApiClient = AxumApiClient(initialServerUrl)

    init {
        loadUsers()
    }

    fun updateServerUrl(newUrl: String) {
        val cleanUrl = newUrl.trim()
        apiClient.baseUrl = cleanUrl
        _uiState.update { it.copy(serverUrl = cleanUrl, isServerConnected = null) }
        loadUsers()
    }

    fun loadUsers(showRefreshing: Boolean = false) {
        viewModelScope.launch {
            if (showRefreshing) {
                _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            } else {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            }

            val result = apiClient.getUsers()

            result.fold(
                onSuccess = { userList ->
                    _uiState.update {
                        it.copy(
                            users = userList,
                            isLoading = false,
                            isRefreshing = false,
                            isServerConnected = true,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            isServerConnected = false,
                            errorMessage = error.message ?: "Error al cargar la lista de usuarios"
                        )
                    }
                }
            )
        }
    }

    fun createUser(name: String, email: String) {
        if (name.trim().isEmpty()) {
            _uiState.update { it.copy(errorMessage = "El nombre no puede estar vacío") }
            return
        }
        if (!email.contains("@")) {
            _uiState.update { it.copy(errorMessage = "Formato de correo electrónico inválido") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isActionInProgress = true, errorMessage = null) }

            val result = apiClient.createUser(name = name.trim(), email = email.trim())

            result.fold(
                onSuccess = { createdUser ->
                    _uiState.update {
                        it.copy(
                            isActionInProgress = false,
                            showUserDialog = false,
                            successMessage = "Usuario '${createdUser.name}' creado con éxito",
                            errorMessage = null
                        )
                    }
                    loadUsers()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isActionInProgress = false,
                            errorMessage = error.message ?: "Error al crear el usuario"
                        )
                    }
                }
            )
        }
    }

    fun updateUser(id: String, name: String, email: String, active: Boolean) {
        if (name.trim().isEmpty()) {
            _uiState.update { it.copy(errorMessage = "El nombre no puede estar vacío") }
            return
        }
        if (!email.contains("@")) {
            _uiState.update { it.copy(errorMessage = "Formato de email inválido") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isActionInProgress = true, errorMessage = null) }

            val result = apiClient.updateUser(
                id = id,
                name = name.trim(),
                email = email.trim(),
                active = active
            )

            result.fold(
                onSuccess = { updatedUser ->
                    _uiState.update {
                        it.copy(
                            isActionInProgress = false,
                            showUserDialog = false,
                            selectedUserForEdit = null,
                            successMessage = "Usuario '${updatedUser.name}' actualizado",
                            errorMessage = null
                        )
                    }
                    loadUsers()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isActionInProgress = false,
                            errorMessage = error.message ?: "Error al actualizar el usuario"
                        )
                    }
                }
            )
        }
    }

    fun toggleUserActive(user: User) {
        viewModelScope.launch {
            val updatedActive = !user.active
            val result = apiClient.updateUser(
                id = user.id,
                name = null,
                email = null,
                active = updatedActive
            )

            result.fold(
                onSuccess = {
                    loadUsers()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(errorMessage = error.message ?: "No se pudo cambiar el estado")
                    }
                }
            )
        }
    }

    fun deleteUser(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isActionInProgress = true, errorMessage = null) }

            val result = apiClient.deleteUser(id)

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isActionInProgress = false,
                            showDeleteConfirmation = null,
                            successMessage = "Usuario eliminado correctamente",
                            errorMessage = null
                        )
                    }
                    loadUsers()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isActionInProgress = false,
                            showDeleteConfirmation = null,
                            errorMessage = error.message ?: "Error al eliminar el usuario"
                        )
                    }
                }
            )
        }
    }

    fun testServerConnection() {
        viewModelScope.launch {
            _uiState.update { it.copy(isActionInProgress = true) }
            val connected = apiClient.testConnection()
            _uiState.update {
                it.copy(
                    isActionInProgress = false,
                    isServerConnected = connected,
                    successMessage = if (connected) "Conexión exitosa a Axum Server" else null,
                    errorMessage = if (!connected) "No se pudo conectar a ${apiClient.baseUrl}" else null
                )
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setActiveFilter(filter: ActiveFilter) {
        _uiState.update { it.copy(activeFilter = filter) }
    }

    fun openAddUserDialog() {
        _uiState.update {
            it.copy(
                selectedUserForEdit = null,
                showUserDialog = true,
                errorMessage = null
            )
        }
    }

    fun openEditUserDialog(user: User) {
        _uiState.update {
            it.copy(
                selectedUserForEdit = user,
                showUserDialog = true,
                errorMessage = null
            )
        }
    }

    fun closeUserDialog() {
        _uiState.update {
            it.copy(
                showUserDialog = false,
                selectedUserForEdit = null,
                errorMessage = null
            )
        }
    }

    fun openDeleteConfirmation(user: User) {
        _uiState.update { it.copy(showDeleteConfirmation = user) }
    }

    fun closeDeleteConfirmation() {
        _uiState.update { it.copy(showDeleteConfirmation = null) }
    }

    fun openServerConfigDialog() {
        _uiState.update { it.copy(showServerConfigDialog = true) }
    }

    fun closeServerConfigDialog() {
        _uiState.update { it.copy(showServerConfigDialog = false) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        apiClient.close()
    }
}
