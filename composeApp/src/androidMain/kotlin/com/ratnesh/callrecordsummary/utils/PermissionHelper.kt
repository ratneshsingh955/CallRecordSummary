package com.ratnesh.callrecordsummary.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

/**
 * Helper class to handle runtime permissions for audio recording and call log access.
 */
class PermissionHelper(private val activity: ComponentActivity) {

    companion object {
        const val RECORD_AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO
        const val READ_CALL_LOG_PERMISSION = Manifest.permission.READ_CALL_LOG
        const val READ_PHONE_STATE_PERMISSION = Manifest.permission.READ_PHONE_STATE
        const val READ_EXTERNAL_STORAGE_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE
        const val MANAGE_EXTERNAL_STORAGE_PERMISSION = Manifest.permission.MANAGE_EXTERNAL_STORAGE
    }

    private var onPermissionResult: ((Boolean) -> Unit)? = null

    // Lazy initialization of launchers to avoid lifecycle issues
    private val requestPermissionLauncher by lazy {
        activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            onPermissionResult?.invoke(isGranted)
            onPermissionResult = null
        }
    }

    private val requestMultiplePermissionsLauncher by lazy {
        activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.values.all { it }
            onPermissionResult?.invoke(allGranted)
            onPermissionResult = null
        }
    }

    /**
     * Check if audio recording permission is granted.
     */
    fun isAudioPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            RECORD_AUDIO_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Check if call log permissions are granted.
     */
    fun areCallLogPermissionsGranted(): Boolean {
        val readCallLog = ContextCompat.checkSelfPermission(
            activity,
            READ_CALL_LOG_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED
        val readPhoneState = ContextCompat.checkSelfPermission(
            activity,
            READ_PHONE_STATE_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED
        return readCallLog && readPhoneState
    }

    /**
     * Check if storage permissions are granted.
     */
    fun areStoragePermissionsGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.READ_MEDIA_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                activity,
                READ_EXTERNAL_STORAGE_PERMISSION
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Check if all necessary permissions for call recording summary are granted.
     */
    fun areAllCallRecordingPermissionsGranted(): Boolean {
        return isAudioPermissionGranted() && areCallLogPermissionsGranted() && areStoragePermissionsGranted()
    }

    /**
     * Request all necessary permissions for call recording summary.
     * @param onResult Callback to be invoked with the permission grant status.
     */
    fun requestAllCallRecordingPermissions(onResult: (Boolean) -> Unit) {
        this.onPermissionResult = onResult
        val permissionsToRequest = mutableListOf<String>()

        if (!isAudioPermissionGranted()) {
            permissionsToRequest.add(RECORD_AUDIO_PERMISSION)
        }
        if (!areCallLogPermissionsGranted()) {
            permissionsToRequest.add(READ_CALL_LOG_PERMISSION)
            permissionsToRequest.add(READ_PHONE_STATE_PERMISSION)
        }
        if (!areStoragePermissionsGranted()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_AUDIO)
            } else {
                permissionsToRequest.add(READ_EXTERNAL_STORAGE_PERMISSION)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestMultiplePermissionsLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            onResult(true) // All permissions already granted
        }
    }

    /**
     * Request a single permission.
     * @param permission The permission to request.
     * @param onResult Callback to be invoked with the permission grant status.
     */
    fun requestPermission(permission: String, onResult: (Boolean) -> Unit) {
        this.onPermissionResult = onResult
        requestPermissionLauncher.launch(permission)
    }
}