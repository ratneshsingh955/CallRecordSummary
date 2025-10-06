package com.ratnesh.callrecordsummary.ui

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ratnesh.callrecordsummary.ui.dialogs.AudioProcessingDialog
import com.ratnesh.callrecordsummary.ui.dialogs.AudioSummaryDialog
import com.ratnesh.callrecordsummary.ui.dialogs.CallRecordingsDialog
import com.ratnesh.callrecordsummary.ui.dialogs.LanguageSelectionDialog
import com.ratnesh.callrecordsummary.service.CallRecordingsService
import com.ratnesh.callrecordsummary.utils.PermissionHelper
import com.ratnesh.callrecordsummary.viewmodel.PdfViewModel
import kotlinx.coroutines.launch

/**
 * Main screen for PDF management functionality.
 * This screen provides UI for uploading, viewing, and managing PDF files.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: PdfViewModel,
    onNavigateToChat: () -> Unit = {},
    modifier: Modifier,
    permissionHelper: PermissionHelper
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Permission launcher for requesting permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        Log.d("MainScreen", "🔐 Permission request result: $allGranted")
    }

    // Collect state from ViewModel
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val uploadSuccess by viewModel.uploadSuccess.collectAsStateWithLifecycle()

    // Audio Summary State
    val isProcessingAudioSummary by viewModel.isProcessingAudioSummary.collectAsStateWithLifecycle()
    val audioSummaryText by viewModel.audioSummaryText.collectAsStateWithLifecycle()
    val audioSummaryError by viewModel.audioSummaryError.collectAsStateWithLifecycle()

    // Call Recordings State
    val callRecordings by viewModel.callRecordings.collectAsStateWithLifecycle()
    val isLoadingCallRecordings by viewModel.isLoadingCallRecordings.collectAsStateWithLifecycle()
    val callRecordingsError by viewModel.callRecordingsError.collectAsStateWithLifecycle()


    
    // Audio Summary UI State
    var showAudioSummaryDialog by remember { mutableStateOf(false) }
    var showAudioProcessingDialog by remember { mutableStateOf(false) }
    var showLanguageSelectionDialog by remember { mutableStateOf(false) }
    var showCallRecordingsDialog by remember { mutableStateOf(false) }
    var selectedAudioUri by remember { mutableStateOf<Uri?>(null) }
    var selectedAudioFileName by remember { mutableStateOf("") }
    var selectedAudioData by remember { mutableStateOf<ByteArray?>(null) }
    var selectedLanguage by remember { mutableStateOf("English") }
    var selectedCallRecording by remember { mutableStateOf<CallRecordingsService.CallRecording?>(null) }


    // Audio file picker launcher for Call Summary
    val audioFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedAudioUri = it
            // Extract filename from URI
            val fileName = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex >= 0) {
                    cursor.getString(nameIndex)
                } else null
            } ?: "audio_file.mp3"
            
            selectedAudioFileName = fileName
            
            // Read audio data and show language selection dialog
            scope.launch {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    inputStream?.use { stream ->
                        val audioData = stream.readBytes()
                        selectedAudioData = audioData
                        showLanguageSelectionDialog = true
                    }
                } catch (e: Exception) {
                    Log.e("MainScreen", "Error reading audio file", e)
                }
            }
        }
    }

    // Show snackbar for errors
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            viewModel.clearError()
        }
    }

    // Handle audio summary results
    LaunchedEffect(audioSummaryText, audioSummaryError) {
        when {
            audioSummaryText.isNotEmpty() -> {
                showAudioProcessingDialog = false
                showAudioSummaryDialog = true
            }
            audioSummaryError != null -> {
                showAudioProcessingDialog = false
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Call Summary",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            Row {
                IconButton(
                    onClick = { 
                        viewModel.clearAllData()
                    },
                    enabled = !isLoading
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Clear Database",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                
                IconButton(
                    onClick = onNavigateToChat,
                    enabled = true
                ) {
                    Icon(
                        imageVector = Icons.Filled.Email,
                        contentDescription = "Chat",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Upload Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { 
                            Log.d("MainScreen", "🔘 Call Summary button clicked")
                            Log.d("MainScreen", "🔐 All permissions granted: ${permissionHelper.areAllCallRecordingPermissionsGranted()}")
                            Log.d("MainScreen", "🔐 Call log permissions: ${permissionHelper.areCallLogPermissionsGranted()}")
                            Log.d("MainScreen", "🔐 Storage permissions: ${permissionHelper.areStoragePermissionsGranted()}")
                            
                            // Try to show dialog and fetch recordings regardless of permissions
                            // MediaStore should work without explicit permissions on modern Android
                            Log.d("MainScreen", "🚀 Attempting to show dialog and fetch recordings")
                            showCallRecordingsDialog = true
                            viewModel.fetchCallRecordings()
                            
                            // Request permissions if not granted using Compose launcher
                            if (!permissionHelper.areAllCallRecordingPermissionsGranted()) {
                                Log.d("MainScreen", "🔐 Requesting permissions via Compose launcher")
                                val permissions = mutableListOf<String>()
                                
                                if (!permissionHelper.areCallLogPermissionsGranted()) {
                                    permissions.addAll(listOf(
                                        android.Manifest.permission.READ_CALL_LOG,
                                        android.Manifest.permission.READ_PHONE_STATE
                                    ))
                                }
                                
                                if (!permissionHelper.areStoragePermissionsGranted()) {
                                    val androidVersion = android.os.Build.VERSION.SDK_INT
                                    val storagePermission = if (androidVersion >= 33) {
                                        android.Manifest.permission.READ_MEDIA_AUDIO
                                    } else {
                                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                                    }
                                    permissions.add(storagePermission)
                                }
                                
                                if (permissions.isNotEmpty()) {
                                    permissionLauncher.launch(permissions.toTypedArray())
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading && !isProcessingAudioSummary
                    ) {
                        Icon(Icons.Filled.Search, contentDescription = "Call Summary")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Call Summary")
                    }
                }

                Text(
                    text = "Supports call recordings",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

    // Call Recordings Dialog
    if (showCallRecordingsDialog) {
        Log.d("MainScreen", "📱 Showing Call Recordings Dialog")
        CallRecordingsDialog(
            recordings = callRecordings,
            isLoading = isLoadingCallRecordings,
            error = callRecordingsError,
            onRecordingSelected = { recording ->
                selectedCallRecording = recording
                showCallRecordingsDialog = false
                showLanguageSelectionDialog = true
            },
            onRefresh = {
                viewModel.fetchCallRecordings()
            },
            onBrowseManually = {
                showCallRecordingsDialog = false
                viewModel.clearCallRecordingsData()
                audioFilePickerLauncher.launch("audio/*")
            },
            onDismiss = {
                showCallRecordingsDialog = false
                viewModel.clearCallRecordingsData()
            }
        )
    }
    
    // Language Selection Dialog
    if (showLanguageSelectionDialog) {
        LanguageSelectionDialog(
            fileName = selectedCallRecording?.contactName ?: selectedCallRecording?.phoneNumber ?: selectedAudioFileName,
            selectedLanguage = selectedLanguage,
            onLanguageChange = { selectedLanguage = it },
            onConfirm = {
                selectedCallRecording?.let { recording ->
                    showLanguageSelectionDialog = false
                    showAudioProcessingDialog = true
                    viewModel.processCallRecording(recording, selectedLanguage)
                } ?: selectedAudioData?.let { audioData ->
                    showLanguageSelectionDialog = false
                    showAudioProcessingDialog = true
                    viewModel.processAudioFileForSummary(selectedAudioFileName, audioData, selectedLanguage)
                }
            },
            onDismiss = {
                showLanguageSelectionDialog = false
                selectedCallRecording = null
                selectedAudioData = null
                selectedAudioFileName = ""
            }
        )
    }
    
    // Audio Processing Dialog
    if (showAudioProcessingDialog) {
        AudioProcessingDialog(
            fileName = selectedAudioFileName,
            isLoading = isProcessingAudioSummary,
            error = audioSummaryError,
            onDismiss = {
                showAudioProcessingDialog = false
                viewModel.clearAudioSummaryData()
            }
        )
    }
    
    // Audio Summary Dialog
    if (showAudioSummaryDialog) {
        AudioSummaryDialog(
            summary = audioSummaryText,
            isLoading = isProcessingAudioSummary,
            error = audioSummaryError,
            onDismiss = {
                showAudioSummaryDialog = false
                viewModel.clearAudioSummaryData()
            }
        )
    }
}

}
