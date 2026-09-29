package com.example.handleusers.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.handleusers.models.User
import com.example.handleusers.viewmodel.ActiveFilter
import com.example.handleusers.viewmodel.UserViewModel

object AppIcons {
    val Add: ImageVector by lazy {
        ImageVector.Builder(name = "Add", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(19f, 13f); horizontalLineTo(13f); verticalLineTo(19f); horizontalLineTo(11f); verticalLineTo(13f); horizontalLineTo(5f); verticalLineTo(11f); horizontalLineTo(11f); verticalLineTo(5f); horizontalLineTo(13f); verticalLineTo(11f); horizontalLineTo(19f); verticalLineTo(13f); close()
            }
        }.build()
    }
    val Refresh: ImageVector by lazy {
        ImageVector.Builder(name = "Refresh", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(17.65f, 6.35f); curveTo(16.2f, 4.9f, 14.21f, 4f, 12f, 4f); curveTo(7.58f, 4f, 4.01f, 7.58f, 4.01f, 12f); curveTo(4.01f, 16.42f, 7.58f, 20f, 12f, 20f); curveTo(15.73f, 20f, 18.84f, 17.45f, 19.73f, 14f); horizontalLineTo(17.65f); curveTo(16.83f, 16.33f, 14.61f, 18f, 12f, 18f); curveTo(8.69f, 18f, 6f, 15.31f, 6f, 12f); curveTo(6f, 8.69f, 8.69f, 6f, 12f, 6f); curveTo(13.66f, 6f, 15.14f, 6.69f, 16.22f, 7.78f); lineTo(13f, 11f); horizontalLineTo(20f); verticalLineTo(4f); lineTo(17.65f, 6.35f); close()
            }
        }.build()
    }
    val Settings: ImageVector by lazy {
        ImageVector.Builder(name = "Settings", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(19.43f, 12.98f); curveTo(19.47f, 12.66f, 19.5f, 12.34f, 19.5f, 12f); curveTo(19.5f, 11.66f, 19.47f, 11.34f, 19.43f, 11.02f); lineTo(21.54f, 9.37f); curveTo(21.73f, 9.22f, 21.78f, 8.95f, 21.66f, 8.73f); lineTo(19.66f, 5.27f); curveTo(19.54f, 5.05f, 19.27f, 4.96f, 19.05f, 5.05f); lineTo(16.56f, 6.05f); curveTo(16.04f, 5.66f, 15.48f, 5.32f, 14.87f, 5.07f); lineTo(14.49f, 2.42f); curveTo(14.46f, 2.18f, 14.25f, 2f, 14f, 2f); horizontalLineTo(10f); curveTo(9.75f, 2f, 9.54f, 2.18f, 9.51f, 2.42f); lineTo(9.13f, 5.07f); curveTo(8.52f, 5.32f, 7.96f, 5.66f, 7.44f, 6.05f); lineTo(4.95f, 5.05f); curveTo(4.73f, 4.96f, 4.46f, 5.05f, 4.34f, 5.27f); lineTo(2.34f, 8.73f); curveTo(2.21f, 8.95f, 2.27f, 9.22f, 2.46f, 9.37f); lineTo(4.57f, 11.02f); curveTo(4.53f, 11.34f, 4.5f, 11.67f, 4.5f, 12f); curveTo(4.5f, 12.33f, 4.53f, 12.66f, 4.57f, 12.98f); lineTo(2.46f, 14.63f); curveTo(2.27f, 14.78f, 2.21f, 15.05f, 2.34f, 15.27f); lineTo(4.34f, 18.73f); curveTo(4.46f, 18.95f, 4.73f, 19.03f, 4.95f, 18.95f); lineTo(7.44f, 17.95f); curveTo(7.96f, 18.34f, 8.52f, 18.68f, 9.13f, 18.93f); lineTo(9.51f, 21.58f); curveTo(9.54f, 21.82f, 9.75f, 22f, 10f, 22f); horizontalLineTo(14f); curveTo(14.25f, 22f, 14.46f, 21.82f, 14.49f, 21.58f); lineTo(14.87f, 18.93f); curveTo(15.48f, 18.68f, 16.04f, 18.34f, 16.56f, 17.95f); lineTo(19.05f, 18.95f); curveTo(19.27f, 19.03f, 19.54f, 18.95f, 19.66f, 18.73f); lineTo(21.66f, 15.27f); curveTo(21.78f, 15.05f, 21.73f, 14.78f, 21.54f, 14.63f); lineTo(19.43f, 12.98f); close(); moveTo(12f, 15.5f); curveTo(10.07f, 15.5f, 8.5f, 13.93f, 8.5f, 12f); curveTo(8.5f, 10.07f, 10.07f, 8.5f, 12f, 8.5f); curveTo(13.93f, 8.5f, 15.5f, 10.07f, 15.5f, 12f); curveTo(15.5f, 13.93f, 13.93f, 15.5f, 12f, 15.5f); close()
            }
        }.build()
    }
    val Search: ImageVector by lazy {
        ImageVector.Builder(name = "Search", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(15.5f, 14f); horizontalLineTo(14.71f); lineTo(14.43f, 13.73f); curveTo(15.41f, 12.59f, 16f, 11.11f, 16f, 9.5f); curveTo(16f, 5.91f, 13.09f, 3f, 9.5f, 3f); curveTo(5.91f, 3f, 3f, 5.91f, 3f, 9.5f); curveTo(3f, 13.09f, 5.91f, 16f, 9.5f, 16f); curveTo(11.11f, 16f, 12.59f, 15.41f, 13.73f, 14.43f); lineTo(14f, 14.71f); verticalLineTo(15.5f); lineTo(19f, 20.49f); lineTo(20.49f, 19f); lineTo(15.5f, 14f); close(); moveTo(9.5f, 14f); curveTo(7.01f, 14f, 5f, 11.99f, 5f, 9.5f); curveTo(5f, 7.01f, 7.01f, 5f, 9.5f, 5f); curveTo(11.99f, 5f, 14f, 7.01f, 14f, 9.5f); curveTo(14f, 11.99f, 11.99f, 14f, 9.5f, 14f); close()
            }
        }.build()
    }
    val Clear: ImageVector by lazy {
        ImageVector.Builder(name = "Clear", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(19f, 6.41f); lineTo(17.59f, 5f); lineTo(12f, 10.59f); lineTo(6.41f, 5f); lineTo(5f, 6.41f); lineTo(10.59f, 12f); lineTo(5f, 17.59f); lineTo(6.41f, 19f); lineTo(12f, 13.41f); lineTo(17.59f, 19f); lineTo(19f, 17.59f); lineTo(13.41f, 12f); close()
            }
        }.build()
    }
    val Person: ImageVector by lazy {
        ImageVector.Builder(name = "Person", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 12f); curveTo(14.21f, 12f, 16f, 10.21f, 16f, 8f); curveTo(16f, 5.79f, 14.21f, 4f, 12f, 4f); curveTo(9.79f, 4f, 8f, 5.79f, 8f, 8f); curveTo(8f, 10.21f, 9.79f, 12f, 12f, 12f); close(); moveTo(12f, 14f); curveTo(9.33f, 14f, 4f, 15.34f, 4f, 18f); verticalLineTo(20f); horizontalLineTo(20f); verticalLineTo(18f); curveTo(20f, 15.34f, 14.67f, 14f, 12f, 14f); close()
            }
        }.build()
    }
    val Edit: ImageVector by lazy {
        ImageVector.Builder(name = "Edit", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(3f, 17.25f); verticalLineTo(21f); horizontalLineTo(6.75f); lineTo(17.81f, 9.94f); lineTo(14.06f, 6.19f); lineTo(3f, 17.25f); close(); moveTo(20.71f, 7.04f); curveTo(21.1f, 6.65f, 21.1f, 6.02f, 20.71f, 5.63f); lineTo(18.37f, 3.29f); curveTo(17.98f, 2.9f, 17.35f, 2.9f, 16.96f, 3.29f); lineTo(15.13f, 5.12f); lineTo(18.88f, 8.87f); lineTo(20.71f, 7.04f); close()
            }
        }.build()
    }
    val Delete: ImageVector by lazy {
        ImageVector.Builder(name = "Delete", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(6f, 19f); curveTo(6f, 20.1f, 6.9f, 21f, 8f, 21f); horizontalLineTo(16f); curveTo(17.1f, 21f, 18f, 20.1f, 18f, 19f); verticalLineTo(7f); horizontalLineTo(6f); verticalLineTo(19f); close(); moveTo(19f, 4f); horizontalLineTo(15.5f); lineTo(14.5f, 3f); horizontalLineTo(9.5f); lineTo(8.5f, 4f); horizontalLineTo(5f); verticalLineTo(6f); horizontalLineTo(19f); verticalLineTo(4f); close()
            }
        }.build()
    }
    val Warning: ImageVector by lazy {
        ImageVector.Builder(name = "Warning", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(1f, 21f); horizontalLineTo(23f); lineTo(12f, 2f); lineTo(1f, 21f); close(); moveTo(13f, 18f); horizontalLineTo(11f); verticalLineTo(16f); horizontalLineTo(13f); verticalLineTo(18f); close(); moveTo(13f, 14f); horizontalLineTo(11f); verticalLineTo(10f); horizontalLineTo(13f); verticalLineTo(14f); close()
            }
        }.build()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    viewModel: UserViewModel = remember { UserViewModel() }
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar("⚠️ $it")
            viewModel.clearMessages()
        }
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar("✅ $it")
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Gestión de Usuarios",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (uiState.isServerConnected) {
                                            true -> Color(0xFF4CAF50)
                                            false -> Color(0xFFF44336)
                                            null -> Color(0xFFFFC107)
                                        }
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = uiState.serverUrl.removePrefix("http://"),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadUsers(showRefreshing = true) }) {
                        Icon(AppIcons.Refresh, contentDescription = "Recargar usuarios")
                    }
                    IconButton(onClick = { viewModel.openServerConfigDialog() }) {
                        Icon(AppIcons.Settings, contentDescription = "Configurar Servidor Axum")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddUserDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(AppIcons.Add, contentDescription = "Agregar usuario")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Summary Banner
            SummaryCardsBanner(
                totalCount = uiState.totalCount,
                activeCount = uiState.activeCount,
                inactiveCount = uiState.inactiveCount
            )

            // Search & Filters Bar
            SearchAndFilterBar(
                searchQuery = uiState.searchQuery,
                onSearchChange = { viewModel.setSearchQuery(it) },
                activeFilter = uiState.activeFilter,
                onFilterSelect = { viewModel.setActiveFilter(it) }
            )

            // Content List / Loading / Empty State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                if (uiState.isLoading && uiState.users.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Conectando con Axum Server...")
                        }
                    }
                } else if (uiState.filteredUsers.isEmpty()) {
                    EmptyUsersState(
                        isSearchActive = uiState.searchQuery.isNotBlank() || uiState.activeFilter != ActiveFilter.ALL,
                        onResetFilters = {
                            viewModel.setSearchQuery("")
                            viewModel.setActiveFilter(ActiveFilter.ALL)
                        },
                        onReload = { viewModel.loadUsers() }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item { Spacer(modifier = Modifier.height(4.dp)) }
                        items(uiState.filteredUsers, key = { it.id }) { user ->
                            UserCard(
                                user = user,
                                onToggleActive = { viewModel.toggleUserActive(user) },
                                onEdit = { viewModel.openEditUserDialog(user) },
                                onDelete = { viewModel.openDeleteConfirmation(user) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }

    // Dialogs
    if (uiState.showUserDialog) {
        AddEditUserDialog(
            userToEdit = uiState.selectedUserForEdit,
            isActionInProgress = uiState.isActionInProgress,
            onDismiss = { viewModel.closeUserDialog() },
            onConfirm = { name, email, active ->
                if (uiState.selectedUserForEdit == null) {
                    viewModel.createUser(name, email)
                } else {
                    viewModel.updateUser(uiState.selectedUserForEdit!!.id, name, email, active)
                }
            }
        )
    }

    uiState.showDeleteConfirmation?.let { userToDelete ->
        DeleteUserConfirmationDialog(
            user = userToDelete,
            isActionInProgress = uiState.isActionInProgress,
            onDismiss = { viewModel.closeDeleteConfirmation() },
            onConfirm = { viewModel.deleteUser(userToDelete.id) }
        )
    }

    if (uiState.showServerConfigDialog) {
        ServerConfigDialog(
            currentUrl = uiState.serverUrl,
            isConnected = uiState.isServerConnected,
            isTesting = uiState.isActionInProgress,
            onDismiss = { viewModel.closeServerConfigDialog() },
            onSave = { newUrl ->
                viewModel.updateServerUrl(newUrl)
                viewModel.closeServerConfigDialog()
            },
            onTestConnection = { viewModel.testServerConnection() }
        )
    }
}

@Composable
fun SummaryCardsBanner(
    totalCount: Int,
    activeCount: Int,
    inactiveCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard(
            title = "Total",
            count = totalCount.toString(),
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            title = "Activos",
            count = activeCount.toString(),
            color = Color(0xFFE8F5E9),
            contentColor = Color(0xFF2E7D32),
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            title = "Inactivos",
            count = inactiveCount.toString(),
            color = Color(0xFFFFEBEE),
            contentColor = Color(0xFFC62828),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun SummaryCard(
    title: String,
    count: String,
    color: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = contentColor.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun SearchAndFilterBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    activeFilter: ActiveFilter,
    onFilterSelect: (ActiveFilter) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar por nombre o correo...") },
            leadingIcon = { Icon(AppIcons.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(AppIcons.Clear, contentDescription = "Limpiar búsqueda")
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = activeFilter == ActiveFilter.ALL,
                onClick = { onFilterSelect(ActiveFilter.ALL) },
                label = { Text("Todos") },
                shape = RoundedCornerShape(20.dp)
            )
            FilterChip(
                selected = activeFilter == ActiveFilter.ACTIVE,
                onClick = { onFilterSelect(ActiveFilter.ACTIVE) },
                label = { Text("Activos") },
                shape = RoundedCornerShape(20.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFC8E6C9),
                    selectedLabelColor = Color(0xFF1B5E20)
                )
            )
            FilterChip(
                selected = activeFilter == ActiveFilter.INACTIVE,
                onClick = { onFilterSelect(ActiveFilter.INACTIVE) },
                label = { Text("Inactivos") },
                shape = RoundedCornerShape(20.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFFFCDD2),
                    selectedLabelColor = Color(0xFFB71C1C)
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun UserCard(
    user: User,
    onToggleActive: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val avatarGradient = remember(user.id) {
        val colors = listOf(
            listOf(Color(0xFF673AB7), Color(0xFF512DA8)),
            listOf(Color(0xFF009688), Color(0xFF00796B)),
            listOf(Color(0xFF3F51B5), Color(0xFF303F9F)),
            listOf(Color(0xFFE91E63), Color(0xFFC2185B)),
            listOf(Color(0xFFFF5722), Color(0xFFE64A19))
        )
        val index = (user.id.hashCode() and Int.MAX_VALUE) % colors.size
        Brush.linearGradient(colors[index])
    }

    val initials = remember(user.name) {
        user.name.split(" ")
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .take(2)
            .joinToString("")
            .ifEmpty { "U" }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(avatarGradient),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Status Badge
                    Surface(
                        color = if (user.active) Color(0xFFE8F5E9) else Color(0xFFEEEEEE),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (user.active) "Activo" else "Inactivo",
                            color = if (user.active) Color(0xFF2E7D32) else Color(0xFF757575),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = user.email,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Quick Actions
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = user.active,
                    onCheckedChange = { onToggleActive() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF4CAF50)
                    ),
                    modifier = Modifier.size(36.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(
                        AppIcons.Edit,
                        contentDescription = "Editar",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(
                        AppIcons.Delete,
                        contentDescription = "Eliminar",
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyUsersState(
    isSearchActive: Boolean,
    onResetFilters: () -> Unit,
    onReload: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            AppIcons.Person,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (isSearchActive) "No se encontraron usuarios" else "No hay usuarios registrados",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (isSearchActive) "Intenta modificar el filtro o término de búsqueda." else "Presiona el botón '+' para agregar el primer usuario.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (isSearchActive) {
            Button(onClick = onResetFilters) {
                Text("Limpiar Filtros")
            }
        } else {
            Button(onClick = onReload) {
                Text("Reintentar Carga")
            }
        }
    }
}

@Composable
fun AddEditUserDialog(
    userToEdit: User?,
    isActionInProgress: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (name: String, email: String, active: Boolean) -> Unit
) {
    var name by remember { mutableStateOf(userToEdit?.name ?: "") }
    var email by remember { mutableStateOf(userToEdit?.email ?: "") }
    var active by remember { mutableStateOf(userToEdit?.active ?: true) }

    AlertDialog(
        onDismissRequest = { if (!isActionInProgress) onDismiss() },
        title = {
            Text(
                text = if (userToEdit == null) "Nuevo Usuario" else "Editar Usuario",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre Completo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo Electrónico") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (userToEdit != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { active = !active }
                    ) {
                        Checkbox(checked = active, onCheckedChange = { active = it })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Usuario Activo")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, email, active) },
                enabled = !isActionInProgress && name.isNotBlank() && email.isNotBlank()
            ) {
                if (isActionInProgress) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(if (userToEdit == null) "Crear" else "Guardar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isActionInProgress) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun DeleteUserConfirmationDialog(
    user: User,
    isActionInProgress: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isActionInProgress) onDismiss() },
        icon = { Icon(AppIcons.Warning, contentDescription = null, tint = Color(0xFFE53935)) },
        title = { Text("¿Eliminar Usuario?", fontWeight = FontWeight.Bold) },
        text = {
            Text("¿Estás seguro de que deseas eliminar a '${user.name}' (${user.email})? Esta acción no se puede deshacer.")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                enabled = !isActionInProgress
            ) {
                if (isActionInProgress) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White
                    )
                } else {
                    Text("Eliminar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isActionInProgress) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun ServerConfigDialog(
    currentUrl: String,
    isConnected: Boolean?,
    isTesting: Boolean,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    onTestConnection: () -> Unit
) {
    var urlText by remember { mutableStateOf(currentUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(AppIcons.Settings, contentDescription = null) },
        title = { Text("Configuración Axum Server", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Especifica la URL base del microservicio Axum en Rust:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = urlText,
                    onValueChange = { urlText = it },
                    label = { Text("URL del Servidor") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Presets rápidos:", fontSize = 12.sp, fontWeight = FontWeight.Medium)

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = { urlText = "http://10.0.2.2:3000" },
                        colors = ButtonDefaults.outlinedButtonColors(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Android (10.0.2.2)", fontSize = 11.sp)
                    }
                    Button(
                        onClick = { urlText = "http://127.0.0.1:3000" },
                        colors = ButtonDefaults.outlinedButtonColors(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("iOS/Localhost", fontSize = 11.sp)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = onTestConnection,
                        enabled = !isTesting,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        if (isTesting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                        } else {
                            Text("Probar Conexión")
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    if (isConnected != null) {
                        Text(
                            text = if (isConnected) "Conectado OK" else "Sin Conexión",
                            color = if (isConnected) Color(0xFF2E7D32) else Color(0xFFC62828),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(urlText) }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
