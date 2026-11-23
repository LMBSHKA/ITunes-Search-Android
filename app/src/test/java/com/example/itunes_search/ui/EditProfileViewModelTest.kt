package com.example.itunes_search.ui

import androidx.lifecycle.SavedStateHandle
import com.example.itunes_search.domain.model.LessonTime
import com.example.itunes_search.domain.model.UserProfile
import com.example.itunes_search.domain.repository.LessonReminder
import com.example.itunes_search.domain.repository.LessonReminderScheduler
import com.example.itunes_search.domain.repository.ProfilePhotoStorage
import com.example.itunes_search.domain.repository.ProfileRepository
import com.example.itunes_search.profile.R
import com.example.itunes_search.testutil.MainDispatcherRule
import com.example.itunes_search.ui.profile.EditProfileViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class EditProfileViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()
    private val repository = TestProfileRepository()
    private val reminders = TestReminderScheduler()
    private val now = Calendar.getInstance().apply {
        clear()
        set(2026, Calendar.NOVEMBER, 16, 10, 0, 30)
    }.timeInMillis

    @Test fun emptyOrInvalidTimeCannotSaveOrSchedule() = runTest {
        val viewModel = createViewModel()
        for (time in listOf("", "hello", "24:00", "9:30")) {
            viewModel.changeLessonTime(time)
            assertTrue(viewModel.uiState.value.invalidLessonTime)
            viewModel.save()
            runCurrent()
        }
        assertEquals(0, repository.saveCount)
        assertTrue(reminders.scheduled.isEmpty())
        assertFalse(viewModel.uiState.value.isSaved)
    }

    @Test fun validSavePersistsTheTimeAndSchedulesTodaysReminderWithTheSavedName() = runTest {
        val viewModel = createViewModel()
        viewModel.changeName("  Иван Иванов  ")
        viewModel.changeLessonTime("18:05")
        viewModel.save()
        viewModel.save() // Повторный быстрый клик не создаёт второе напоминание.
        runCurrent()
        assertEquals("18:05", repository.profile.value.favoriteLessonTime)
        assertEquals("Иван Иванов", repository.profile.value.fullName)
        assertEquals(listOf(LessonReminder("Иван Иванов", LessonTime.parse("18:05")!!.todayAt(now))), reminders.scheduled)
        assertEquals(1, repository.saveCount)
        assertTrue(viewModel.uiState.value.isSaved)
        assertFalse(viewModel.uiState.value.isBusy)
        repository.profile.value = repository.profile.value.copy(fullName = "Другое имя")
        assertEquals("Иван Иванов", reminders.scheduled.single().fullName)
    }

    @Test fun pastTimeIsRejectedWithoutSchedulingTomorrow() = runTest {
        val viewModel = createViewModel()
        viewModel.changeLessonTime("09:59")
        viewModel.save()
        runCurrent()
        assertEquals(R.string.profile_lesson_time_past, viewModel.uiState.value.error)
        assertEquals(0, repository.saveCount)
        assertTrue(reminders.scheduled.isEmpty())
    }

    @Test fun invalidResumeStillPreventsProfileSaving() = runTest {
        val viewModel = createViewModel()
        viewModel.changeLessonTime("18:05")
        viewModel.changeResumeUrl("http://example.com/resume.pdf")
        viewModel.save()
        runCurrent()
        assertTrue(viewModel.uiState.value.invalidResumeUrl)
        assertEquals(0, repository.saveCount)
        assertTrue(reminders.scheduled.isEmpty())
    }

    @Test fun repositoryFailureDoesNotInstallAnAlarm() = runTest {
        repository.failSave = true
        val viewModel = createViewModel()
        viewModel.changeLessonTime("18:05")
        viewModel.save()
        runCurrent()
        assertEquals(R.string.profile_save_error, viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isSaved)
        assertFalse(viewModel.uiState.value.isBusy)
        assertTrue(reminders.scheduled.isEmpty())
    }

    @Test fun permissionRevokedDuringSaveDoesNotReportSuccessAndCanBeRetried() = runTest {
        reminders.failWithPermissionError = true
        val viewModel = createViewModel()
        viewModel.changeLessonTime("18:05")
        viewModel.save()
        runCurrent()
        assertEquals(R.string.profile_reminder_permissions_error, viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isSaved)
        assertFalse(viewModel.uiState.value.isBusy)
        reminders.failWithPermissionError = false
        viewModel.save()
        runCurrent()
        assertTrue(viewModel.uiState.value.isSaved)
        assertEquals(1, reminders.scheduled.size)
    }

    @Test fun draftTimeSurvivesViewModelRecreationWithoutSavingTheProfile() {
        val savedState = SavedStateHandle()
        val first = createViewModel(savedState)
        first.changeLessonTime("18:05")
        val second = createViewModel(savedState)
        assertEquals("18:05", second.uiState.value.draft.favoriteLessonTime)
        assertEquals("", repository.profile.value.favoriteLessonTime)
        assertFalse(second.uiState.value.invalidLessonTime)
    }

    private fun createViewModel(savedState: SavedStateHandle = SavedStateHandle()) =
        EditProfileViewModel(repository, TestPhotos, savedState, reminders) { now }

    private class TestProfileRepository : ProfileRepository {
        override val profile = MutableStateFlow(UserProfile())
        var saveCount = 0
        var failSave = false
        override suspend fun save(profile: UserProfile) {
            if (failSave) throw IOException("Test save failure")
            saveCount++
            this.profile.value = profile
        }
    }

    private class TestReminderScheduler : LessonReminderScheduler {
        val scheduled = mutableListOf<LessonReminder>()
        var failWithPermissionError = false
        override fun schedule(reminder: LessonReminder) {
            if (failWithPermissionError) throw SecurityException("Permission revoked")
            scheduled += reminder
        }
    }

    private object TestPhotos : ProfilePhotoStorage {
        override suspend fun copyGalleryPhoto(sourceUri: String) = sourceUri
        override fun createCameraOutput() = "content://test/photo"
        override fun hasCameraPhoto(uri: String) = true
        override fun discardDraft(uri: String) = Unit
    }
}
