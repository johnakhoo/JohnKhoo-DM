package com.example.data

enum class DriveMode(val label: String, val powerFactor: Double) {
    ECO("ECO", 0.75),
    CITY("CITY", 1.0),
    SPORT("SPORT", 1.35),
    HYPER("HYPER", 1.7)
}

enum class RegenLevel(val label: String, val regenFactor: Double) {
    OFF("OFF", 0.0),
    LOW("LOW 1", 0.5),
    MED("MED 2", 1.0),
    HIGH("HIGH 3", 1.6)
}

data class VehicleState(
    val socPercentage: Int = 82,
    val estimatedRangeKm: Int = 148,
    val isCharging: Boolean = false,
    val speedKmh: Int = 0,
    val targetSpeedKmh: Int = 0,
    val batteryTempC: Double = 31.5,
    
    // Additional telematics metrics
    val motorPowerKw: Double = 0.0,
    val motorTempC: Double = 38.0,
    val driveMode: DriveMode = DriveMode.CITY,
    val regenLevel: RegenLevel = RegenLevel.MED,
    val tripDistanceKm: Double = 18.4,
    val avgEfficiencyWhKm: Int = 125,
    val frontTirePsi: Double = 32.5,
    val rearTirePsi: Double = 34.0,
    val headlightOn: Boolean = true,
    val hazardOn: Boolean = false,
    val isLocked: Boolean = false,
    val chargingRateKw: Double = 0.0,
    val targetChargeLimitPct: Int = 90,
    val preconditioningActive: Boolean = false,
    val batteryHealthPct: Int = 98,
    val bleConnected: Boolean = true,
    val odometerKm: Double = 3842.0
)
