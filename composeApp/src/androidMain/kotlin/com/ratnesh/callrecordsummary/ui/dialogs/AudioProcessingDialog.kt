package com.ratnesh.callrecordsummary.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Dialog for displaying audio processing status.
 */
@Composable
fun AudioProcessingDialog(
    fileName: String,
    isLoading: Boolean,
    error: String?,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Filled.Email, contentDescription = "Audio Processing")
                Text("🎵 Processing Audio")
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
            ) {
                Text(
                    text = "File: $fileName",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                if (isLoading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Column {
                            Text(
                                text = "Processing audio file...",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Extracting text and generating AI summary",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Processing steps
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ProcessingStep(
                            text = "Reading audio file",
                            isCompleted = true
                        )
                        ProcessingStep(
                            text = "Extracting text using AI",
                            isCompleted = isLoading
                        )
                        ProcessingStep(
                            text = "Generating summary",
                            isCompleted = false
                        )
                    }
                } else if (error != null) {
                    Text(
                        text = "Error: $error",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    Text(
                        text = "Processing completed",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text(if (isLoading) "Processing..." else "Close")
            }
        }
    )
}

/**
 * Individual processing step component.
 */
@Composable
private fun ProcessingStep(
    text: String,
    isCompleted: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (isCompleted) {
            Icon(
                imageVector = Icons.Filled.Search, // Using search icon as checkmark
                contentDescription = "Completed",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        } else {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp
            )
        }
        
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

