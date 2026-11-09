package com.example.itunes_search.domain.repository

import com.example.itunes_search.domain.model.ResumeDocument
import com.example.itunes_search.domain.model.UserProfile
import kotlinx.coroutines.flow.StateFlow

interface ProfileRepository {
    val profile: StateFlow<UserProfile>

    suspend fun save(profile: UserProfile)
}

interface ProfilePhotoStorage {
    suspend fun copyGalleryPhoto(sourceUri: String): String
    fun createCameraOutput(): String
    fun hasCameraPhoto(uri: String): Boolean
    fun discardDraft(uri: String)
}

interface ResumeRepository {
    suspend fun download(url: String): ResumeDocument
}
