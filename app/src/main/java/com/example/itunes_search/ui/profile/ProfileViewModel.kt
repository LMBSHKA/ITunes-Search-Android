package com.example.itunes_search.ui.profile

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.itunes_search.R
import com.example.itunes_search.domain.model.ResumeDocument
import com.example.itunes_search.domain.repository.ProfileRepository
import com.example.itunes_search.domain.repository.ResumeRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ResumeUiState(
    val isDownloading: Boolean = false,
    val document: ResumeDocument? = null,
    @StringRes val error: Int? = null
)

class ProfileViewModel(
    repository: ProfileRepository,
    private val resumes: ResumeRepository
) : ViewModel() {
    val profile = repository.profile
    private val resume = MutableStateFlow(ResumeUiState())
    val resumeState = resume.asStateFlow()

    fun downloadResume() {
        val url = profile.value.resumeUrl
        if (url.isBlank() || resume.value.isDownloading || resume.value.document != null) return
        resume.value = ResumeUiState(isDownloading = true)
        viewModelScope.launch {
            resume.value = try {
                ResumeUiState(document = resumes.download(url))
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                ResumeUiState(error = R.string.profile_download_error)
            }
        }
    }

    fun finishOpeningResume(opened: Boolean) {
        resume.value = ResumeUiState(error = if (opened) null else R.string.profile_viewer_error)
    }

    companion object {
        fun factory(repository: ProfileRepository, resumes: ResumeRepository): ViewModelProvider.Factory =
            viewModelFactory { initializer { ProfileViewModel(repository, resumes) } }
    }
}
