package com.example.itunes_search.ui.profile

import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.example.itunes_search.domain.repository.LessonReminderScheduler
import com.example.itunes_search.domain.repository.ProfilePhotoStorage
import com.example.itunes_search.domain.repository.ProfileRepository
import com.example.itunes_search.domain.repository.ResumeRepository

object ProfileRoutes {
    const val GRAPH = "profile_graph"
    internal const val VIEW = "profile"
    const val EDIT = "profile/edit"
}

fun NavGraphBuilder.profileGraph(
    navController: NavController,
    repository: ProfileRepository,
    photos: ProfilePhotoStorage,
    resumeRepository: ResumeRepository,
    reminders: LessonReminderScheduler
) {
    navigation(startDestination = ProfileRoutes.VIEW, route = ProfileRoutes.GRAPH) {
        composable(ProfileRoutes.VIEW) {
            val factory = remember(repository, resumeRepository) {
                ProfileViewModel.factory(repository, resumeRepository)
            }
            val profileViewModel: ProfileViewModel = viewModel(factory = factory)
            ProfileScreen(profileViewModel, onEdit = {
                navController.navigate(ProfileRoutes.EDIT) { launchSingleTop = true }
            })
        }
        composable(ProfileRoutes.EDIT) {
            val factory = remember(repository, photos, reminders) {
                EditProfileViewModel.factory(repository, photos, reminders)
            }
            val editProfileViewModel: EditProfileViewModel = viewModel(factory = factory)
            EditProfileScreen(editProfileViewModel, onBack = { navController.popBackStack() })
        }
    }
}
