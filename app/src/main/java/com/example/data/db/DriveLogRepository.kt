package com.example.data.db

import kotlinx.coroutines.flow.Flow

class DriveLogRepository(private val dao: DriveLogDao) {
    val allLogs: Flow<List<DriveLog>> = dao.getAllLogs()

    suspend fun addLog(log: DriveLog) {
        dao.insertLog(log)
    }

    suspend fun clearLogs() {
        dao.clearAllLogs()
    }
}
