package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drive_logs")
data class DriveLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val distanceKm: Double,
    val avgSpeedKmh: Int,
    val energyUsedKwh: Double,
    val avgEfficiencyWhKm: Int,
    val driveMode: String,
    val timestamp: Long = System.currentTimeMillis()
)
