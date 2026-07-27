package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BleTelematicsManager
import com.example.data.DriveMode
import com.example.data.RegenLevel
import com.example.data.VehicleState
import com.example.data.db.AppDatabase
import com.example.data.db.DriveLog
import com.example.data.db.DriveLogRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class DriveViewModel(application: Application) : AndroidViewModel(application) {

    private val telematicsManager = BleTelematicsManager()
    private val repository: DriveLogRepository

    val vehicleState: StateFlow<VehicleState> = telematicsManager.vehicleState

    // 7 focusable cards:
    // 0: Hero Speedometer & Power Gauge Widget
    // 1: Battery & Range Card
    // 2: Speed & Throttle Control Card
    // 3: Drive Mode & Regen Selector
    // 4: Vehicle Quick Controls (Lights, Lock, Pre-con)
    // 5: Telematics & TPMS Diagnostics
    // 6: Drive History Logs
    private val _focusedCardIndex = MutableStateFlow(0)
    val focusedCardIndex: StateFlow<Int> = _focusedCardIndex.asStateFlow()

    private val _hapticFeedbackEvent = MutableSharedFlow<String>()
    val hapticFeedbackEvent: SharedFlow<String> = _hapticFeedbackEvent.asSharedFlow()

    val driveLogs: StateFlow<List<DriveLog>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = DriveLogRepository(database.driveLogDao())

        driveLogs = repository.allLogs.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Seed initial history logs if empty
        viewModelScope.launch {
            repository.allLogs.collect { logs ->
                if (logs.isEmpty()) {
                    repository.addLog(
                        DriveLog(
                            title = "Morning Commute to Downtown",
                            distanceKm = 24.5,
                            avgSpeedKmh = 48,
                            energyUsedKwh = 3.1,
                            avgEfficiencyWhKm = 126,
                            driveMode = "ECO",
                            timestamp = System.currentTimeMillis() - 86400000L
                        )
                    )
                    repository.addLog(
                        DriveLog(
                            title = "Coastal Highway Test Drive",
                            distanceKm = 42.0,
                            avgSpeedKmh = 65,
                            energyUsedKwh = 6.8,
                            avgEfficiencyWhKm = 162,
                            driveMode = "SPORT",
                            timestamp = System.currentTimeMillis() - 172800000L
                        )
                    )
                }
            }
        }
    }

    fun cycleFocusCardNext() {
        _focusedCardIndex.value = (_focusedCardIndex.value + 1) % 7
        emitHaptic("Focus: Card ${_focusedCardIndex.value}")
    }

    fun cycleFocusCardPrevious() {
        _focusedCardIndex.value = if (_focusedCardIndex.value == 0) 6 else _focusedCardIndex.value - 1
        emitHaptic("Focus: Card ${_focusedCardIndex.value}")
    }

    fun setFocusedCard(index: Int) {
        if (index in 0..6) {
            _focusedCardIndex.value = index
        }
    }

    fun executeFocusedCardAction() {
        val currentVehicle = vehicleState.value
        when (_focusedCardIndex.value) {
            0 -> setTargetSpeed(if (currentVehicle.speedKmh == 0) 45 else 0)
            1 -> toggleCharging()
            2 -> setTargetSpeed(if (currentVehicle.speedKmh == 0) 45 else 0)
            3 -> cycleDriveMode()
            4 -> toggleLock()
            5 -> togglePreconditioning()
            6 -> saveCurrentTripLog()
        }
        emitHaptic("D-Pad Select Executed")
    }

    fun setTargetSpeed(speed: Int) {
        telematicsManager.setTargetSpeed(speed)
    }

    fun setDriveMode(mode: DriveMode) {
        telematicsManager.setDriveMode(mode)
    }

    fun cycleDriveMode() {
        val modes = DriveMode.values()
        val currentIdx = modes.indexOf(vehicleState.value.driveMode)
        val nextMode = modes[(currentIdx + 1) % modes.size]
        telematicsManager.setDriveMode(nextMode)
    }

    fun setRegenLevel(level: RegenLevel) {
        telematicsManager.setRegenLevel(level)
    }

    fun toggleCharging() {
        telematicsManager.toggleCharging()
    }

    fun setTargetChargeLimit(limit: Int) {
        telematicsManager.setTargetChargeLimit(limit)
    }

    fun toggleLock() {
        telematicsManager.toggleLock()
    }

    fun toggleHeadlights() {
        telematicsManager.toggleHeadlights()
    }

    fun toggleHazard() {
        telematicsManager.toggleHazard()
    }

    fun togglePreconditioning() {
        telematicsManager.togglePreconditioning()
    }

    fun resetTrip() {
        telematicsManager.resetTrip()
    }

    fun saveCurrentTripLog() {
        viewModelScope.launch {
            val state = vehicleState.value
            if (state.tripDistanceKm > 0.1) {
                val energyKwh = ((state.tripDistanceKm * state.avgEfficiencyWhKm) / 1000.0 * 10).roundToInt() / 10.0
                repository.addLog(
                    DriveLog(
                        title = "JohnKhooForDM Session #${(10..99).random()}",
                        distanceKm = state.tripDistanceKm,
                        avgSpeedKmh = state.speedKmh.coerceAtLeast(32),
                        energyUsedKwh = energyKwh,
                        avgEfficiencyWhKm = state.avgEfficiencyWhKm,
                        driveMode = state.driveMode.name
                    )
                )
                telematicsManager.resetTrip()
                emitHaptic("Trip Saved to Log")
            }
        }
    }

    private fun emitHaptic(msg: String) {
        viewModelScope.launch {
            _hapticFeedbackEvent.emit(msg)
        }
    }
}
