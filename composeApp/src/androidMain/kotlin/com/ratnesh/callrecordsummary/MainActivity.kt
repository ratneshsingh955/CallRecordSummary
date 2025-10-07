package com.ratnesh.callrecordsummary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.ratnesh.callrecordsummary.utils.PermissionHelper

class MainActivity : ComponentActivity() {
    private lateinit var permissionHelper: PermissionHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        // Initialize PermissionHelper after super.onCreate() but before setContent
//        permissionHelper = PermissionHelper(this)

        setContent {
//            val viewModel: PdfViewModel = viewModel()
//
//            MainScreen(
//                viewModel = viewModel,
//                onNavigateToChat = { /* TODO: Navigate to chat */ },
//                modifier = Modifier.fillMaxSize(),
//                permissionHelper = permissionHelper
//            )
            LoginScreen()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    LoginScreen()
}