package com.example.handleusers

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController(serverUrl: String) = ComposeUIViewController {
    App(serverUrl = serverUrl)
}
