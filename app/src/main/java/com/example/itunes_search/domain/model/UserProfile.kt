package com.example.itunes_search.domain.model

data class UserProfile(
    val fullName: String = "",
    val avatarUri: String? = null,
    val resumeUrl: String = ""
)

data class ResumeDocument(val uri: String, val mimeType: String)
