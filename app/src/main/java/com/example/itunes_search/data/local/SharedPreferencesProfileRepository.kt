package com.example.itunes_search.data.local

import android.content.SharedPreferences
import com.example.itunes_search.domain.model.UserProfile
import com.example.itunes_search.domain.repository.ProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.IOException

class SharedPreferencesProfileRepository(
    private val preferences: SharedPreferences,
    private val photos: ProfilePhotoFiles
) : ProfileRepository {
    private val saveMutex = Mutex()
    private val currentProfile = MutableStateFlow(
        UserProfile(
            fullName = preferences.getString(FULL_NAME, "").orEmpty(),
            avatarUri = preferences.getString(AVATAR_URI, null),
            resumeUrl = preferences.getString(RESUME_URL, "").orEmpty()
        )
    )
    override val profile = currentProfile.asStateFlow()

    override suspend fun save(profile: UserProfile): Unit = withContext(Dispatchers.IO) {
        saveMutex.withLock {
            val previousAvatar = currentProfile.value.avatarUri
            val newAvatar = profile.avatarUri?.takeIf { it != previousAvatar }?.let(photos::saveAvatar)
            val saved = profile.copy(avatarUri = newAvatar ?: profile.avatarUri)
            val committed = preferences.edit()
                .putString(FULL_NAME, saved.fullName)
                .putString(AVATAR_URI, saved.avatarUri)
                .putString(RESUME_URL, saved.resumeUrl)
                .commit()
            if (!committed) {
                newAvatar?.let(photos::deleteAvatar)
                throw IOException("Cannot save profile")
            }
            currentProfile.value = saved
            if (previousAvatar != null && previousAvatar != saved.avatarUri) {
                photos.deleteAvatar(previousAvatar)
            }
        }
    }

    private companion object {
        const val FULL_NAME = "full_name"
        const val AVATAR_URI = "avatar_uri"
        const val RESUME_URL = "resume_url"
    }
}
