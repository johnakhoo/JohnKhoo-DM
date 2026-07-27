package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DriveLogDao {
    @Query("SELECT * FROM drive_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<DriveLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: DriveLog)

    @Query("DELETE FROM drive_logs")
    suspend fun clearAllLogs()
}
