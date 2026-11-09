package com.example.itunes_search.ui.profile

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.itunes_search.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: ProfileViewModel, onEdit: () -> Unit) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val resume by viewModel.resumeState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(resume.document) {
        val document = resume.document ?: return@LaunchedEffect
        val uri = Uri.parse(document.uri)
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, document.mimeType)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            .apply { clipData = ClipData.newRawUri(null, uri) }
        val opened = try {
            context.startActivity(intent)
            true
        } catch (_: ActivityNotFoundException) {
            false
        } catch (_: SecurityException) {
            false
        }
        viewModel.finishOpeningResume(opened)
    }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text(stringResource(R.string.tab_profile)) },
            actions = {
                IconButton(onClick = onEdit) {
                    Icon(painterResource(R.drawable.ic_edit), stringResource(R.string.profile_edit))
                }
            }
        )
    }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
                .verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfileAvatar(profile.avatarUri)
            Text(
                text = profile.fullName.ifBlank { stringResource(R.string.profile_name_empty) },
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = viewModel::downloadResume,
                enabled = profile.resumeUrl.isNotBlank() && !resume.isDownloading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.profile_resume))
            }
            if (resume.isDownloading) CircularProgressIndicator()
            resume.error?.let { Text(stringResource(it), color = MaterialTheme.colorScheme.error) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(viewModel: EditProfileViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showPhotoSources by rememberSaveable { mutableStateOf(false) }

    // Photo Picker даёт доступ только к выбранному фото. Если он недоступен,
    // PickVisualMedia использует системный выбор файла через ACTION_OPEN_DOCUMENT.
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { viewModel.selectGalleryPhoto(it.toString()) }
    }
    // TakePicture делегирует съёмку приложению камеры и передаёт URI FileProvider.
    // Разрешения CAMERA и доступа к общему хранилищу для этого сценария не нужны.
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture(), viewModel::finishCamera)

    val editable = !state.isBusy && !state.isSaved
    BackHandler(enabled = !editable) {}
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onBack()
    }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text(stringResource(R.string.profile_edit)) },
            navigationIcon = {
                IconButton(onClick = onBack, enabled = editable) {
                    Icon(painterResource(R.drawable.ic_back), stringResource(R.string.profile_back))
                }
            }
        )
    }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
                .verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfileAvatar(
                avatarUri = state.draft.avatarUri,
                modifier = Modifier.clickable(
                    enabled = editable,
                    role = Role.Button,
                    onClickLabel = stringResource(R.string.profile_change_photo)
                ) { showPhotoSources = true }
            )
            TextButton(onClick = { showPhotoSources = true }, enabled = editable) {
                Text(stringResource(R.string.profile_change_photo))
            }
            OutlinedTextField(
                value = state.draft.fullName,
                onValueChange = viewModel::changeName,
                label = { Text(stringResource(R.string.profile_name)) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                enabled = editable,
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("profile_name_input")
            )
            OutlinedTextField(
                value = state.draft.resumeUrl,
                onValueChange = viewModel::changeResumeUrl,
                label = { Text(stringResource(R.string.profile_resume_url)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                supportingText = {
                    Text(stringResource(
                        if (state.invalidResumeUrl) R.string.profile_resume_url_invalid else R.string.profile_resume_hint
                    ))
                },
                isError = state.invalidResumeUrl,
                enabled = editable,
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("profile_resume_input")
            )
            state.error?.let { Text(stringResource(it), color = MaterialTheme.colorScheme.error) }
            Button(onClick = viewModel::save, enabled = editable, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.done))
            }
            if (state.isBusy) CircularProgressIndicator()
        }
    }

    if (showPhotoSources) {
        AlertDialog(
            onDismissRequest = { showPhotoSources = false },
            title = { Text(stringResource(R.string.profile_photo_source)) },
            text = {
                Column {
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            showPhotoSources = false
                            try {
                                gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            } catch (_: ActivityNotFoundException) {
                                viewModel.galleryUnavailable()
                            } catch (_: SecurityException) {
                                viewModel.galleryUnavailable()
                            }
                        }
                    ) { Text(stringResource(R.string.profile_gallery)) }
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            showPhotoSources = false
                            viewModel.prepareCamera()?.let { uri ->
                                try {
                                    camera.launch(Uri.parse(uri))
                                } catch (_: ActivityNotFoundException) {
                                    viewModel.cameraUnavailable()
                                } catch (_: SecurityException) {
                                    viewModel.cameraUnavailable()
                                }
                            }
                        }
                    ) { Text(stringResource(R.string.profile_camera)) }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPhotoSources = false }) {
                    Text(stringResource(R.string.profile_cancel))
                }
            }
        )
    }
}

@Composable
private fun ProfileAvatar(avatarUri: String?, modifier: Modifier = Modifier) {
    val placeholder = painterResource(R.drawable.ic_profile_avatar)
    AsyncImage(
        model = avatarUri?.let(Uri::parse),
        contentDescription = stringResource(R.string.profile_avatar),
        placeholder = placeholder,
        error = placeholder,
        fallback = placeholder,
        contentScale = ContentScale.Crop,
        modifier = Modifier.size(128.dp).clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondaryContainer).then(modifier)
    )
}
