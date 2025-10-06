package com.ratnesh.callrecordsummary

import android.app.Application
import com.google.firebase.FirebaseApp

class CallRecordSummaryApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }
    }
}

