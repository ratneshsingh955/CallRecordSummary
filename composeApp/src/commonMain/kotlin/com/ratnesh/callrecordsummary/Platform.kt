package com.ratnesh.callrecordsummary

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform