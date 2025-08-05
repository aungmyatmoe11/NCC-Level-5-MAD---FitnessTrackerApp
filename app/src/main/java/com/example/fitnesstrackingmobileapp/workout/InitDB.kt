package com.example.fitnesstrackingmobileapp.workout

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class InitDB:Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())

    private val dataBase by lazy { AppDatabase.getDatabase(this, applicationScope) }
}