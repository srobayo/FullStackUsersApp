package com.example.handleusers

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.handleusers.ui.UserManagementScreen

private val LightColors = lightColorScheme(
    primary = Color(0xFF673AB7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE7F6),
    onPrimaryContainer = Color(0xFF311B92),
    secondary = Color(0xFF009688),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2F1),
    onSecondaryContainer = Color(0xFF004D40),
    background = Color(0xFFF8F9FA),
    surface = Color.White,
    surfaceContainer = Color(0xFFF1F3F5)
)

@Composable
@Preview
fun App() {
    MaterialTheme(
        colorScheme = LightColors
    ) {
        Surface {
            UserManagementScreen()
        }
    }
}