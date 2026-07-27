package com.example.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class BleTelematicsManager {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _vehicleState = MutableStateFlow(VehicleState())
    val vehicleState: StateFlow<VehicleState> = _vehicleState.asStateFlow()

    private var targetSpeedKmh = 0

    init {
        startTelematicsLoop()
    }

    private fun startTelematicsLoop() {
        scope.launch {
            while (true) {
                delay(1000L) // Updates telematics stream every 1000ms
                updateTelematicsStep()
            }
        }
    }

    private fun updateTelematicsStep() {
        _vehicleState.update { current ->
            var currentSpeed = current.speedKmh

            // Smoothly interpolate current speed towards target speed
            if (currentSpeed < targetSpeedKmh) {
                currentSpeed = min(targetSpeedKmh, currentSpeed + 4)
            } else if (currentSpeed > targetSpeedKmh) {
                // Apply regen braking or coasting slowdown
                val decel = if (current.regenLevel != RegenLevel.OFF) 6 else 3
                currentSpeed = max(targetSpeedKmh, currentSpeed - decel)
            }

            var isCharging = current.isCharging
            var soc = current.socPercentage
            var temp = current.batteryTempC
            var trip = current.tripDistanceKm
            var odo = current.odometerKm
            var chargingRate = current.chargingRateKw

            // Calculate Motor Power draw (kW) or Regen charging
            val motorPower = if (currentSpeed > 0) {
                val basePower = (currentSpeed / 10.0) * current.driveMode.powerFactor
                (basePower * 10).roundToInt() / 10.0
            } else if (targetSpeedKmh < current.speedKmh && current.regenLevel != RegenLevel.OFF) {
                // Regenerative energy recovery
                -1.5 * current.regenLevel.regenFactor
            } else {
                0.0
            }

            // SoC drain/recharge simulation
            if (isCharging) {
                chargingRate = 7.2 // 7.2 kW Level 2 AC charger
                if (soc < current.targetChargeLimitPct) {
                    soc = min(current.targetChargeLimitPct, soc + 1)
                    temp = max(25.0, temp - 0.1) // Cooling while charging
                } else {
                    isCharging = false
                    chargingRate = 0.0
                }
            } else {
                chargingRate = 0.0
                if (currentSpeed > 0) {
                    // Update trip & odometer distance based on speed
                    val distanceThisSecondKm = currentSpeed / 3600.0
                    trip += distanceThisSecondKm
                    odo += distanceThisSecondKm

                    // Drain SoC based on speed and drive mode
                    val drainProbability = (currentSpeed * current.driveMode.powerFactor) / 200.0
                    if (Math.random() < drainProbability) {
                        soc = max(0, soc - 1)
                    }

                    // Battery temperature warms up slightly with heavy drive load
                    val targetTemp = 30.0 + (currentSpeed * 0.15 * current.driveMode.powerFactor)
                    temp += (targetTemp - temp) * 0.05
                } else {
                    // Cool down towards ambient 28.0 C
                    temp += (28.0 - temp) * 0.02
                }
            }

            // Calculate dynamic remaining range based on SoC and Drive Mode efficiency
            // Standard full battery range ~180 km in ECO, ~150 in CITY, ~120 in SPORT, ~100 in HYPER
            val fullRangeKm = when (current.driveMode) {
                DriveMode.ECO -> 190
                DriveMode.CITY -> 160
                DriveMode.SPORT -> 130
                DriveMode.HYPER -> 105
            }
            val dynamicRange = ((soc / 100.0) * fullRangeKm).roundToInt()

            // Calculate dynamic Wh/km efficiency
            val efficiency = when (current.driveMode) {
                DriveMode.ECO -> 105
                DriveMode.CITY -> 125
                DriveMode.SPORT -> 155
                DriveMode.HYPER -> 185
            }

            current.copy(
                socPercentage = soc,
                estimatedRangeKm = dynamicRange,
                isCharging = isCharging,
                speedKmh = currentSpeed,
                targetSpeedKmh = targetSpeedKmh,
                batteryTempC = (temp * 10).roundToInt() / 10.0,
                motorPowerKw = motorPower,
                tripDistanceKm = (trip * 100).roundToInt() / 100.0,
                odometerKm = (odo * 10).roundToInt() / 10.0,
                avgEfficiencyWhKm = efficiency,
                chargingRateKw = chargingRate
            )
        }
    }

    fun setTargetSpeed(speed: Int) {
        targetSpeedKmh = speed.coerceIn(0, 120)
        _vehicleState.update { it.copy(targetSpeedKmh = targetSpeedKmh) }
    }

    fun setDriveMode(mode: DriveMode) {
        _vehicleState.update { it.copy(driveMode = mode) }
    }

    fun setRegenLevel(level: RegenLevel) {
        _vehicleState.update { it.copy(regenLevel = level) }
    }

    fun toggleCharging() {
        _vehicleState.update {
            val nextCharging = !it.isCharging
            if (nextCharging) targetSpeedKmh = 0 // Stop vehicle when plugged in
            it.copy(isCharging = nextCharging, targetSpeedKmh = targetSpeedKmh)
        }
    }

    fun setTargetChargeLimit(limitPct: Int) {
        _vehicleState.update { it.copy(targetChargeLimitPct = limitPct.coerceIn(50, 100)) }
    }

    fun toggleLock() {
        _vehicleState.update { it.copy(isLocked = !it.isLocked) }
    }

    fun toggleHeadlights() {
        _vehicleState.update { it.copy(headlightOn = !it.headlightOn) }
    }

    fun toggleHazard() {
        _vehicleState.update { it.copy(hazardOn = !it.hazardOn) }
    }

    fun togglePreconditioning() {
        _vehicleState.update { it.copy(preconditioningActive = !it.preconditioningActive) }
    }

    fun resetTrip() {
        _vehicleState.update { it.copy(tripDistanceKm = 0.0) }
    }
}
