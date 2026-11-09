package com.example.itunes_search.ui.profile

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.itunes_search.R
import com.example.itunes_search.domain.model.UserProfile
import com.example.itunes_search.domain.repository.ProfilePhotoStorage
import com.example.itunes_search.domain.repository.ProfileRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.URI
import java.net.URISyntaxException

data class EditProfileUiState(
    val draft: UserProfile,
    val isBusy: Boolean = false,
    val isSaved: Boolean = false,
    val invalidResumeUrl: Boolean = false,
    @StringRes val error: Int? = null
)

class EditProfileViewModel(
    private val repository: ProfileRepository,
    private val photos: ProfilePhotoStorage,
    private val savedState: SavedStateHandle
) : ViewModel() {
    private val savedProfile = repository.profile.value
    private val state = MutableStateFlow(
        EditProfileUiState(
            draft = UserProfile(
                fullName = savedState[FULL_NAME] ?: savedProfile.fullName,
                avatarUri = savedState[AVATAR_URI] ?: savedProfile.avatarUri,
                resumeUrl = savedState[RESUME_URL] ?: savedProfile.resumeUrl
            )
        )
    )
    val uiState = state.asStateFlow()

    fun changeName(name: String) = updateDraft(state.value.draft.copy(fullName = name))

    fun changeResumeUrl(url: String) = updateDraft(state.value.draft.copy(resumeUrl = url))

    fun selectGalleryPhoto(uri: String) {
        if (state.value.isBusy) return
        state.update { it.copy(isBusy = true, error = null) }
        viewModelScope.launch {
            try {
                replaceDraftPhoto(photos.copyGalleryPhoto(uri))
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                showError(R.string.profile_photo_error)
            } finally {
                state.update { it.copy(isBusy = false) }
            }
        }
    }

    fun prepareCamera(): String? {
        return try {
            photos.createCameraOutput().also { savedState[CAMERA_URI] = it }
        } catch (_: Exception) {
            showError(R.string.profile_photo_error)
            null
        }
    }

    fun finishCamera(captured: Boolean) {
        val uri = savedState.remove<String>(CAMERA_URI) ?: return
        try {
            if (captured && photos.hasCameraPhoto(uri)) {
                replaceDraftPhoto(uri)
                return
            }
            if (captured) showError(R.string.profile_photo_error)
        } catch (_: Exception) {
            showError(R.string.profile_photo_error)
        }
        photos.discardDraft(uri)
    }

    fun cameraUnavailable() {
        finishCamera(false)
        showError(R.string.profile_camera_error)
    }

    fun galleryUnavailable() = showError(R.string.profile_gallery_error)

    fun save() {
        if (state.value.isBusy || state.value.isSaved) return
        val current = state.value.draft
        val draft = current.copy(fullName = current.fullName.trim(), resumeUrl = current.resumeUrl.trim())
        if (draft.resumeUrl.isNotEmpty() && !isHttpsUrl(draft.resumeUrl)) {
            state.update { it.copy(invalidResumeUrl = true) }
            return
        }
        state.update { it.copy(isBusy = true, error = null) }
        viewModelScope.launch {
            try {
                repository.save(draft)
                updateDraft(repository.profile.value)
                draft.avatarUri?.let(photos::discardDraft)
                state.update { it.copy(isSaved = true) }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                showError(R.string.profile_save_error)
            } finally {
                state.update { it.copy(isBusy = false) }
            }
        }
    }

    override fun onCleared() {
        savedState.get<String>(CAMERA_URI)?.let(photos::discardDraft)
        state.value.draft.avatarUri?.let(photos::discardDraft)
        super.onCleared()
    }

    private fun replaceDraftPhoto(uri: String) {
        val previousUri = state.value.draft.avatarUri
        updateDraft(state.value.draft.copy(avatarUri = uri))
        previousUri?.let(photos::discardDraft)
    }

    private fun updateDraft(draft: UserProfile) {
        savedState[FULL_NAME] = draft.fullName
        savedState[AVATAR_URI] = draft.avatarUri
        savedState[RESUME_URL] = draft.resumeUrl
        state.update {
            it.copy(
                draft = draft,
                error = null,
                invalidResumeUrl = it.invalidResumeUrl && it.draft.resumeUrl == draft.resumeUrl
            )
        }
    }

    private fun showError(@StringRes error: Int) {
        state.update { it.copy(error = error) }
    }

    private fun isHttpsUrl(value: String): Boolean = try {
        val uri = URI(value)
        uri.scheme.equals("https", ignoreCase = true) && !uri.host.isNullOrBlank()
    } catch (_: URISyntaxException) {
        false
    }

    companion object {
        private const val FULL_NAME = "profile_full_name"
        private const val AVATAR_URI = "profile_avatar_uri"
        private const val RESUME_URL = "profile_resume_url"
        private const val CAMERA_URI = "profile_camera_uri"

        fun factory(repository: ProfileRepository, photos: ProfilePhotoStorage): ViewModelProvider.Factory =
            viewModelFactory { initializer { EditProfileViewModel(repository, photos, createSavedStateHandle()) } }
    }
}
