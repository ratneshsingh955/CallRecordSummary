package com.ratnesh.callrecordsummary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ratnesh.callrecordsummary.ui.MainScreen
import com.ratnesh.callrecordsummary.utils.PermissionHelper
import com.ratnesh.callrecordsummary.viewmodel.PdfViewModel

class MainActivity : ComponentActivity() {
    private lateinit var permissionHelper: PermissionHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        // Initialize PermissionHelper after super.onCreate() but before setContent
        permissionHelper = PermissionHelper(this)

        setContent {
            val viewModel: PdfViewModel = viewModel()
            
            MainScreen(
                viewModel = viewModel,
                onNavigateToChat = { /* TODO: Navigate to chat */ },
                modifier = Modifier.fillMaxSize(),
                permissionHelper = permissionHelper
            )
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}