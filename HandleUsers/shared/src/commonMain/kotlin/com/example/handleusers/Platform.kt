package com.example.handleusers

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform