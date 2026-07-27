package com.example.ui

import com.example.R
import kotlin.math.roundToInt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ElectricCar
import androidx.compose.material.icons.filled.EvStation
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.TireRepair
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.DriveMode
import com.example.data.RegenLevel
import com.example.data.VehicleState
import com.example.data.db.DriveLog
import com.example.modifier.interceptHardwareKeyEvents
import com.example.ui.theme.EvAmberAlert
import com.example.ui.theme.EvBackground
import com.example.ui.theme.EvFocusYellow
import com.example.ui.theme.EvPrimaryCyan
import com.example.ui.theme.EvPurpleHyper
import com.example.ui.theme.EvRedDanger
import com.example.ui.theme.EvSecondaryLime
import com.example.ui.theme.EvSurface
import com.example.ui.theme.EvSurfaceBorder
import com.example.ui.theme.EvSurfaceVariant
import com.example.ui.theme.EvTextDim
import com.example.ui.theme.EvTextPrimary
import com.example.ui.theme.EvTextSecondary
import com.example.viewmodel.DriveViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GlanceableDriveScreen(
    viewModel: DriveViewModel,
    modifier: Modifier = Modifier
) {
    val vehicleState by viewModel.vehicleState.collectAsStateWithLifecycle()
    val focusedCardIndex by viewModel.focusedCardIndex.collectAsStateWithLifecycle()
    val driveLogs by viewModel.driveLogs.collectAsStateWithLifecycle()
    val hapticEvent by viewModel.hapticFeedbackEvent.collectAsStateWithLifecycle(initialValue = "")

    val listState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }
    var showSplashScreen by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        listState.scrollToItem(0)
    }

    LaunchedEffect(focusedCardIndex) {
        if (focusedCardIndex >= 0) {
            val targetItemIndex = focusedCardIndex + 1
            var layoutInfo = listState.layoutInfo
            var targetItem = layoutInfo.visibleItemsInfo.firstOrNull { it.index == targetItemIndex }

            if (targetItem == null) {
                listState.animateScrollToItem(targetItemIndex)
                layoutInfo = listState.layoutInfo
                targetItem = layoutInfo.visibleItemsInfo.firstOrNull { it.index == targetItemIndex }
            }

            if (targetItem != null) {
                val viewportStart = layoutInfo.viewportStartOffset
                val viewportEnd = layoutInfo.viewportEndOffset
                val topOffset = targetItem.offset
                val bottomOffset = targetItem.offset + targetItem.size
                val padding = 32 // Margin in pixels to ensure card is fully inside viewport

                if (bottomOffset + padding > viewportEnd) {
                    val delta = (bottomOffset + padding) - viewportEnd
                    listState.animateScrollBy(delta.toFloat())
                } else if (topOffset - padding < viewportStart) {
                    val delta = (topOffset - padding) - viewportStart
                    listState.animateScrollBy(delta.toFloat())
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(EvBackground)
            .focusRequester(focusRequester)
            .focusable()
            .interceptHardwareKeyEvents(
                onPreviousCard = { viewModel.cycleFocusCardPrevious() },
                onNextCard = { viewModel.cycleFocusCardNext() },
                onSelectCard = { viewModel.executeFocusedCardAction() }
            )
            .testTag("glanceable_drive_screen")
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Bar
            item {
                HeaderTopBar(
                    vehicleState = vehicleState,
                    hapticMsg = hapticEvent,
                    onShowSplash = { showSplashScreen = true }
                )
            }

            // CARD 0: Hero Speedometer & SoC Progress Ring
            item {
                HeroSpeedometerRingSection(
                    vehicleState = vehicleState,
                    isFocused = (focusedCardIndex == 0),
                    onCardClick = { viewModel.setFocusedCard(0) },
                    onSpeedChange = { viewModel.setTargetSpeed(it) }
                )
            }

            // CARD 1: SoC & Battery Telematics
            item {
                FocusableCardWrapper(
                    cardIndex = 1,
                    focusedIndex = focusedCardIndex,
                    title = "Battery & Dynamic Range Telematics",
                    icon = Icons.Default.BatteryChargingFull,
                    onCardClick = { viewModel.setFocusedCard(1) }
                ) {
                    SocAndRangeCardContent(
                        vehicleState = vehicleState,
                        onToggleCharging = { viewModel.toggleCharging() },
                        onSetTargetLimit = { viewModel.setTargetChargeLimit(it) }
                    )
                }
            }

            // CARD 2: Speed Throttle & Performance Simulator
            item {
                FocusableCardWrapper(
                    cardIndex = 2,
                    focusedIndex = focusedCardIndex,
                    title = "Speed Throttle & Motor Power Simulator",
                    icon = Icons.Default.Speed,
                    onCardClick = { viewModel.setFocusedCard(2) }
                ) {
                    SpeedSimulatorCardContent(
                        vehicleState = vehicleState,
                        onSetSpeed = { viewModel.setTargetSpeed(it) }
                    )
                }
            }

            // CARD 3: Drive Mode & Regenerative Braking
            item {
                FocusableCardWrapper(
                    cardIndex = 3,
                    focusedIndex = focusedCardIndex,
                    title = "Drive Mode & Regenerative Braking",
                    icon = Icons.Default.Bolt,
                    onCardClick = { viewModel.setFocusedCard(3) }
                ) {
                    DriveModeAndRegenCardContent(
                        vehicleState = vehicleState,
                        onSelectMode = { viewModel.setDriveMode(it) },
                        onSelectRegen = { viewModel.setRegenLevel(it) }
                    )
                }
            }

            // CARD 4: Vehicle Remote Controls
            item {
                FocusableCardWrapper(
                    cardIndex = 4,
                    focusedIndex = focusedCardIndex,
                    title = "Remote Vehicle Telematics Controls",
                    icon = Icons.Default.Key,
                    onCardClick = { viewModel.setFocusedCard(4) }
                ) {
                    QuickControlsCardContent(
                        vehicleState = vehicleState,
                        onToggleLock = { viewModel.toggleLock() },
                        onToggleLights = { viewModel.toggleHeadlights() },
                        onToggleHazard = { viewModel.toggleHazard() },
                        onTogglePreconditioning = { viewModel.togglePreconditioning() }
                    )
                }
            }

            // CARD 5: TPMS & Battery Health Diagnostics
            item {
                FocusableCardWrapper(
                    cardIndex = 5,
                    focusedIndex = focusedCardIndex,
                    title = "TPMS & Battery Health Diagnostics",
                    icon = Icons.Default.TireRepair,
                    onCardClick = { viewModel.setFocusedCard(5) }
                ) {
                    DiagnosticsCardContent(vehicleState = vehicleState)
                }
            }

            // CARD 6: Saved Drive Sessions History (Room DB)
            item {
                FocusableCardWrapper(
                    cardIndex = 6,
                    focusedIndex = focusedCardIndex,
                    title = "Drive Logs & Trip Persistence",
                    icon = Icons.Default.History,
                    onCardClick = { viewModel.setFocusedCard(6) }
                ) {
                    DriveLogsCardContent(
                        vehicleState = vehicleState,
                        driveLogs = driveLogs,
                        onSaveTrip = { viewModel.saveCurrentTripLog() },
                        onResetTrip = { viewModel.resetTrip() }
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = showSplashScreen,
            enter = fadeIn(tween(300)),
            exit = fadeOut(tween(300))
        ) {
            HondaPrologueSplashScreen(
                onDismiss = { showSplashScreen = false }
            )
        }
    }
}

@Composable
fun HeaderTopBar(
    vehicleState: VehicleState,
    hapticMsg: String,
    onShowSplash: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(EvSurface)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (vehicleState.bleConnected) EvSecondaryLime else EvRedDanger)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "John Khoo for DM",
                        color = EvTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Splash badge button
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = EvPrimaryCyan.copy(alpha = 0.2f),
                        modifier = Modifier
                            .clickable { onShowSplash() }
                            .testTag("show_splash_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ElectricCar,
                                contentDescription = "Honda Prologue EV Splash",
                                tint = EvPrimaryCyan,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "PROLOGUE EV",
                                color = EvPrimaryCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Lock badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (vehicleState.isLocked) EvAmberAlert.copy(alpha = 0.2f) else EvSecondaryLime.copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (vehicleState.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = "Lock State",
                                tint = if (vehicleState.isLocked) EvAmberAlert else EvSecondaryLime,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (vehicleState.isLocked) "LOCKED" else "READY",
                                color = if (vehicleState.isLocked) EvAmberAlert else EvSecondaryLime,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        if (hapticMsg.isNotEmpty()) {
            Text(
                text = "⚡ $hapticMsg",
                color = EvSecondaryLime,
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun HeroSpeedometerRingSection(
    vehicleState: VehicleState,
    isFocused: Boolean = false,
    onCardClick: () -> Unit = {},
    onSpeedChange: (Int) -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) EvFocusYellow else EvSurfaceBorder,
        animationSpec = tween(300),
        label = "FocusBorder"
    )

    val animatedSpeed by animateFloatAsState(
        targetValue = vehicleState.speedKmh.toFloat(),
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "SpeedAnimation"
    )

    val animatedSoc by animateFloatAsState(
        targetValue = vehicleState.socPercentage.toFloat(),
        animationSpec = tween(durationMillis = 800),
        label = "SocAnimation"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "PulseRing")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    val chargeStrokeDp by infiniteTransition.animateFloat(
        initialValue = 14f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1100
                14f at 0
                20f at 220 using FastOutLinearInEasing
                14f at 1100 using LinearOutSlowInEasing
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "ChargeStrokePulse"
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = EvSurface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(
                width = if (isFocused) 3.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(24.dp)
            )
            .pointerInput(onCardClick) {
                awaitEachGesture {
                    awaitFirstDown(pass = PointerEventPass.Initial)
                    onCardClick()
                }
            }
            .clickable { onCardClick() }
            .testTag("hero_speedometer_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(240.dp)
            ) {
                // Canvas Ring for Circular State of Charge & Power Meter
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val baseStrokeWidth = 18.dp.toPx()
                    val maxStrokeWidth = 20.dp.toPx()
                    val radius = (size.minDimension - maxStrokeWidth) / 2f
                    val diameter = radius * 2f
                    val topLeft = Offset(
                        x = (size.width - diameter) / 2f,
                        y = (size.height - diameter) / 2f
                    )
                    val arcSize = Size(diameter, diameter)

                    // Background Track Ring
                    drawCircle(
                        color = EvSurfaceBorder,
                        radius = radius,
                        center = center,
                        style = Stroke(width = baseStrokeWidth, cap = StrokeCap.Round)
                    )

                    // Battery SoC Arc
                    val sweepAngle = (animatedSoc / 100f) * 280f
                    val socColor = if (vehicleState.isCharging) {
                        EvSecondaryLime
                    } else {
                        when {
                            animatedSoc < 20f -> EvRedDanger
                            animatedSoc < 40f -> EvAmberAlert
                            else -> EvPrimaryCyan
                        }
                    }

                    val activeStrokeWidth = if (vehicleState.isCharging) {
                        chargeStrokeDp.dp.toPx()
                    } else {
                        baseStrokeWidth
                    }

                    drawArc(
                        color = socColor,
                        startAngle = 130f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = activeStrokeWidth, cap = StrokeCap.Round)
                    )
                }

                // Inner Display Content
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Charging Indicator or Drive Mode Badge
                    if (vehicleState.isCharging) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EvStation,
                                contentDescription = "Charging",
                                tint = EvSecondaryLime,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "CHARGING ${vehicleState.chargingRateKw}kW",
                                color = EvSecondaryLime,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = when (vehicleState.driveMode) {
                                DriveMode.ECO -> EvPrimaryCyan.copy(alpha = 0.2f)
                                DriveMode.CITY -> EvSecondaryLime.copy(alpha = 0.2f)
                                DriveMode.SPORT -> EvAmberAlert.copy(alpha = 0.2f)
                                DriveMode.HYPER -> EvPurpleHyper.copy(alpha = 0.2f)
                            }
                        ) {
                            Text(
                                text = vehicleState.driveMode.name,
                                color = when (vehicleState.driveMode) {
                                    DriveMode.ECO -> EvPrimaryCyan
                                    DriveMode.CITY -> EvSecondaryLime
                                    DriveMode.SPORT -> EvAmberAlert
                                    DriveMode.HYPER -> EvPurpleHyper
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // HUGE SPEED DISPLAY
                    Text(
                        text = "${animatedSpeed.toInt()}",
                        color = EvTextPrimary,
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-2).sp
                    )

                    Text(
                        text = "KM / H",
                        color = EvTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Power Draw / Regen Meter
                    Text(
                        text = if (vehicleState.motorPowerKw < 0) {
                            "REGEN: ${vehicleState.motorPowerKw} kW"
                        } else {
                            "POWER: ${vehicleState.motorPowerKw} kW"
                        },
                        color = if (vehicleState.motorPowerKw < 0) EvSecondaryLime else EvPrimaryCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Stats Row under Speedometer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickStatItem(
                    label = "BATTERY",
                    value = "${vehicleState.socPercentage}%",
                    color = EvPrimaryCyan
                )
                QuickStatItem(
                    label = "EST RANGE",
                    value = "${vehicleState.estimatedRangeKm} km",
                    color = EvSecondaryLime
                )
                QuickStatItem(
                    label = "TEMP",
                    value = "${vehicleState.batteryTempC}°C",
                    color = if (vehicleState.batteryTempC > 40.0) EvAmberAlert else EvTextPrimary
                )
            }
        }
    }
}

@Composable
fun QuickStatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            color = EvTextDim,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Text(
            text = value,
            color = color,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun FocusableCardWrapper(
    cardIndex: Int,
    focusedIndex: Int,
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onCardClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val isFocused = (cardIndex == focusedIndex)
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) EvFocusYellow else EvSurfaceBorder,
        animationSpec = tween(300),
        label = "FocusBorder"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = EvSurface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(
                width = if (isFocused) 3.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .pointerInput(onCardClick) {
                awaitEachGesture {
                    awaitFirstDown(pass = PointerEventPass.Initial)
                    onCardClick()
                }
            }
            .clickable { onCardClick() }
            .testTag("card_wrapper_$cardIndex")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = if (isFocused) EvFocusYellow else EvPrimaryCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = title.uppercase(),
                        color = if (isFocused) EvFocusYellow else EvTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            content()
        }
    }
}

@Composable
fun SocAndRangeCardContent(
    vehicleState: VehicleState,
    onToggleCharging: () -> Unit,
    onSetTargetLimit: (Int) -> Unit
) {
    val activeBarColor = if (vehicleState.isCharging) {
        EvSecondaryLime
    } else {
        when {
            vehicleState.socPercentage < 20 -> EvRedDanger
            vehicleState.socPercentage < 40 -> EvAmberAlert
            else -> EvSecondaryLime
        }
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ESTIMATED DYNAMIC RANGE",
                    color = EvTextDim,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${vehicleState.estimatedRangeKm} KM",
                    color = EvSecondaryLime,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Button(
                onClick = onToggleCharging,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (vehicleState.isCharging) EvRedDanger else EvSecondaryLime
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("toggle_charging_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.EvStation,
                    contentDescription = "Charge",
                    tint = Color.Black
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (vehicleState.isCharging) "STOP CHARGE" else "PLUG IN CHARGER",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ANIMATED CHARGING & BATTERY LEVEL PROGRESS BAR
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(EvSurfaceVariant, shape = RoundedCornerShape(16.dp))
                .border(
                    width = 1.dp,
                    color = if (vehicleState.isCharging) activeBarColor.copy(alpha = 0.8f) else EvSurfaceBorder,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (vehicleState.isCharging) Icons.Default.Bolt else Icons.Default.BatteryChargingFull,
                        contentDescription = "Battery State",
                        tint = activeBarColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (vehicleState.isCharging) "⚡ CHARGING IN PROGRESS" else "BATTERY STATE OF CHARGE",
                        color = activeBarColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }

                Text(
                    text = "${vehicleState.socPercentage}%",
                    color = activeBarColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Bar Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(EvSurfaceBorder)
            ) {
                // Progress Bar Fill
                Box(
                    modifier = Modifier
                        .fillMaxWidth((vehicleState.socPercentage / 100f).coerceIn(0.05f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    activeBarColor.copy(alpha = 0.8f),
                                    activeBarColor
                                )
                            )
                        )
                )
            }

            if (vehicleState.isCharging) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚡ Rate: ${vehicleState.chargingRateKw} kW",
                        color = EvTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Target Limit: ${vehicleState.targetChargeLimitPct}%",
                        color = EvTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "TARGET CHARGE LIMIT",
            color = EvTextDim,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(80, 90, 100).forEach { limit ->
                val isSelected = vehicleState.targetChargeLimitPct == limit
                Button(
                    onClick = { onSetTargetLimit(limit) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) EvPrimaryCyan else EvSurfaceVariant
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "$limit%",
                        color = if (isSelected) Color.Black else EvTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SpeedSimulatorCardContent(
    vehicleState: VehicleState,
    onSetSpeed: (Int) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "THROTTLE & MOTOR POWER COMMAND",
                    color = EvTextDim,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = EvPrimaryCyan.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "CURRENT: ${vehicleState.speedKmh} KM/H",
                            color = EvPrimaryCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = EvSecondaryLime.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "TARGET: ${vehicleState.targetSpeedKmh} KM/H",
                            color = EvSecondaryLime,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Text(
                text = "${vehicleState.speedKmh} KM/H",
                color = EvPrimaryCyan,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        ThrottleProgressControl(
            currentSpeedKmh = vehicleState.speedKmh,
            targetSpeedKmh = vehicleState.targetSpeedKmh,
            maxSpeedKmh = 120,
            onTargetSpeedChanged = onSetSpeed
        )
    }
}

@Composable
fun ThrottleProgressControl(
    currentSpeedKmh: Int,
    targetSpeedKmh: Int,
    maxSpeedKmh: Int = 120,
    onTargetSpeedChanged: (Int) -> Unit
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragTargetSpeed by remember { mutableFloatStateOf(targetSpeedKmh.toFloat()) }

    LaunchedEffect(targetSpeedKmh) {
        if (!isDragging) {
            dragTargetSpeed = targetSpeedKmh.toFloat()
        }
    }

    val activeTargetSpeed = if (isDragging) dragTargetSpeed.toInt() else targetSpeedKmh

    Column {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .testTag("speed_slider")
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val newFraction = (offset.x / size.width).coerceIn(0f, 1f)
                        val newTarget = (newFraction * maxSpeedKmh).roundToInt()
                        dragTargetSpeed = newTarget.toFloat()
                        onTargetSpeedChanged(newTarget)
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            val newFraction = (offset.x / size.width).coerceIn(0f, 1f)
                            val newTarget = (newFraction * maxSpeedKmh).roundToInt()
                            dragTargetSpeed = newTarget.toFloat()
                            onTargetSpeedChanged(newTarget)
                        },
                        onDragEnd = { isDragging = false },
                        onDragCancel = { isDragging = false },
                        onDrag = { change, _ ->
                            change.consume()
                            val newFraction = (change.position.x / size.width).coerceIn(0f, 1f)
                            val newTarget = (newFraction * maxSpeedKmh).roundToInt()
                            dragTargetSpeed = newTarget.toFloat()
                            onTargetSpeedChanged(newTarget)
                        }
                    )
                }
        ) {
            val currentFraction = (currentSpeedKmh.toFloat() / maxSpeedKmh).coerceIn(0f, 1f)
            val targetFraction = (activeTargetSpeed.toFloat() / maxSpeedKmh).coerceIn(0f, 1f)

            Canvas(modifier = Modifier.fillMaxSize()) {
                val trackHeight = 18.dp.toPx()
                val trackTop = (size.height - trackHeight) / 2f
                val cornerRadius = CornerRadius(trackHeight / 2f, trackHeight / 2f)

                // 1. Inactive Track Background
                drawRoundRect(
                    color = EvSurfaceBorder,
                    topLeft = Offset(0f, trackTop),
                    size = Size(size.width, trackHeight),
                    cornerRadius = cornerRadius
                )

                // 2. Current Throttle Progress Fill
                val currentX = size.width * currentFraction
                if (currentX > 0) {
                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(EvPrimaryCyan.copy(alpha = 0.7f), EvPrimaryCyan)
                        ),
                        topLeft = Offset(0f, trackTop),
                        size = Size(currentX, trackHeight),
                        cornerRadius = cornerRadius
                    )
                }

                // 4. Tick Marks (0, 30, 60, 90, 120)
                listOf(0f, 0.25f, 0.5f, 0.75f, 1.0f).forEach { fraction ->
                    val tickX = size.width * fraction
                    drawLine(
                        color = EvTextDim.copy(alpha = 0.4f),
                        start = Offset(tickX, trackTop - 3.dp.toPx()),
                        end = Offset(tickX, trackTop + trackHeight + 3.dp.toPx()),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }

            // Draggable Target Thumb Marker
            val thumbSizeDp = 30.dp
            val maxOffsetPx = constraints.maxWidth.toFloat() - with(LocalDensity.current) { thumbSizeDp.toPx() }
            val thumbOffsetX = with(LocalDensity.current) {
                (maxOffsetPx * targetFraction).toDp()
            }

            Box(
                modifier = Modifier
                    .offset(x = thumbOffsetX)
                    .size(thumbSizeDp)
                    .align(Alignment.CenterStart)
                    .shadow(8.dp, CircleShape)
                    .background(
                        color = if (isDragging) EvSecondaryLime else EvTextPrimary,
                        shape = CircleShape
                    )
                    .border(2.5.dp, EvSecondaryLime, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = "Target Throttle Thumb",
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Axis Speed Markers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("0", color = EvTextDim, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Text("30", color = EvTextDim, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Text("60", color = EvTextDim, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Text("90", color = EvTextDim, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Text("120 KM/H", color = EvTextDim, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DriveModeAndRegenCardContent(
    vehicleState: VehicleState,
    onSelectMode: (DriveMode) -> Unit,
    onSelectRegen: (RegenLevel) -> Unit
) {
    Column {
        Text(
            text = "SELECT DRIVE MODE",
            color = EvTextDim,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            DriveMode.values().forEach { mode ->
                val isSelected = vehicleState.driveMode == mode
                val modeColor = when (mode) {
                    DriveMode.ECO -> EvPrimaryCyan
                    DriveMode.CITY -> EvSecondaryLime
                    DriveMode.SPORT -> EvAmberAlert
                    DriveMode.HYPER -> EvPurpleHyper
                }

                Button(
                    onClick = { onSelectMode(mode) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) modeColor else EvSurfaceVariant
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = mode.label,
                        color = if (isSelected) Color.Black else EvTextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "REGENERATIVE BRAKING LEVEL",
            color = EvTextDim,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            RegenLevel.values().forEach { regen ->
                val isSelected = vehicleState.regenLevel == regen
                Button(
                    onClick = { onSelectRegen(regen) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) EvSecondaryLime else EvSurfaceVariant
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = regen.label,
                        color = if (isSelected) Color.Black else EvTextPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun QuickControlsCardContent(
    vehicleState: VehicleState,
    onToggleLock: () -> Unit,
    onToggleLights: () -> Unit,
    onToggleHazard: () -> Unit,
    onTogglePreconditioning: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ControlToggleButton(
            title = if (vehicleState.isLocked) "LOCKED" else "UNLOCKED",
            icon = if (vehicleState.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
            isActive = vehicleState.isLocked,
            activeColor = EvAmberAlert,
            onClick = onToggleLock,
            modifier = Modifier.weight(1f)
        )

        ControlToggleButton(
            title = if (vehicleState.headlightOn) "LIGHTS ON" else "LIGHTS OFF",
            icon = Icons.Default.Lightbulb,
            isActive = vehicleState.headlightOn,
            activeColor = EvPrimaryCyan,
            onClick = onToggleLights,
            modifier = Modifier.weight(1f)
        )

        ControlToggleButton(
            title = if (vehicleState.hazardOn) "HAZARD ON" else "HAZARD OFF",
            icon = Icons.Default.AddAlert,
            isActive = vehicleState.hazardOn,
            activeColor = EvRedDanger,
            onClick = onToggleHazard,
            modifier = Modifier.weight(1f)
        )

        ControlToggleButton(
            title = if (vehicleState.preconditioningActive) "CLIMATE ON" else "CLIMATE OFF",
            icon = Icons.Default.AcUnit,
            isActive = vehicleState.preconditioningActive,
            activeColor = EvSecondaryLime,
            onClick = onTogglePreconditioning,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ControlToggleButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isActive) activeColor.copy(alpha = 0.25f) else EvSurfaceVariant
        ),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(8.dp),
        modifier = modifier.height(72.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isActive) activeColor else EvTextSecondary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                color = if (isActive) activeColor else EvTextSecondary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun DiagnosticsCardContent(vehicleState: VehicleState) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DiagnosticMetricPill(
                label = "BATTERY TEMP",
                value = "${vehicleState.batteryTempC}°C",
                subtext = if (vehicleState.batteryTempC < 38.0) "Optimal" else "Warming",
                color = if (vehicleState.batteryTempC < 38.0) EvSecondaryLime else EvAmberAlert
            )
            DiagnosticMetricPill(
                label = "MOTOR TEMP",
                value = "${vehicleState.motorTempC}°C",
                subtext = "Normal",
                color = EvPrimaryCyan
            )
            DiagnosticMetricPill(
                label = "AVG EFFICIENCY",
                value = "${vehicleState.avgEfficiencyWhKm} Wh/km",
                subtext = "Eco Grade",
                color = EvSecondaryLime
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // TPMS Tire Pressure Section
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = EvSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TireRepair,
                        contentDescription = "Front Tire",
                        tint = EvSecondaryLime,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text("FRONT TIRE", color = EvTextDim, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text("${vehicleState.frontTirePsi} PSI", color = EvTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(28.dp)
                        .background(EvSurfaceBorder)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TireRepair,
                        contentDescription = "Rear Tire",
                        tint = EvSecondaryLime,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text("REAR TIRE", color = EvTextDim, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text("${vehicleState.rearTirePsi} PSI", color = EvTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DiagnosticMetricPill(
    label: String,
    value: String,
    subtext: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = EvSurfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, color = EvTextDim, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Text(text = value, color = color, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(text = subtext, color = EvTextSecondary, fontSize = 10.sp)
        }
    }
}

@Composable
fun DriveLogsCardContent(
    vehicleState: VehicleState,
    driveLogs: List<DriveLog>,
    onSaveTrip: () -> Unit,
    onResetTrip: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ACTIVE TRIP DISTANCE",
                    color = EvTextDim,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${vehicleState.tripDistanceKm} KM",
                    color = EvPrimaryCyan,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onResetTrip,
                    colors = ButtonDefaults.buttonColors(containerColor = EvSurfaceVariant),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset", tint = EvTextPrimary)
                }

                Button(
                    onClick = onSaveTrip,
                    colors = ButtonDefaults.buttonColors(containerColor = EvPrimaryCyan),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("save_trip_btn")
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = "Save", tint = Color.Black)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("SAVE TRIP", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "SAVED DRIVE LOGS",
            color = EvTextDim,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (driveLogs.isEmpty()) {
            Text(
                text = "No saved drive logs yet. Complete a drive session and tap Save Trip!",
                color = EvTextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                driveLogs.take(4).forEach { log ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = EvSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = log.title,
                                    color = EvTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = dateFormat.format(Date(log.timestamp)),
                                    color = EvTextDim,
                                    fontSize = 10.sp
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${log.distanceKm} km",
                                        color = EvSecondaryLime,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${log.avgEfficiencyWhKm} Wh/km",
                                        color = EvTextSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HondaPrologueSplashScreen(
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EvBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = EvPrimaryCyan.copy(alpha = 0.15f),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ElectricCar,
                        contentDescription = "EV Icon",
                        tint = EvPrimaryCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "HONDA PROLOGUE EV",
                        color = EvPrimaryCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                }
            }

            Text(
                text = "All-Electric SUV",
                color = EvTextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Connected Vehicle Telematics & Command Center",
                color = EvTextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = EvSurface),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, EvSurfaceBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .shadow(12.dp, RoundedCornerShape(24.dp))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_honda_prologue_ev),
                        contentDescription = "Honda Prologue EV Image",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SpecBadge(title = "296 MI", subtitle = "EPA Range", modifier = Modifier.weight(1f))
                SpecBadge(title = "85 kWh", subtitle = "Ultium Battery", modifier = Modifier.weight(1f))
                SpecBadge(title = "AWD", subtitle = "288 HP Dual Motor", modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = EvPrimaryCyan),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("launch_dashboard_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "LAUNCH CONNECTED DASHBOARD",
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = "Launch",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SpecBadge(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = EvSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, EvSurfaceBorder),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp)
        ) {
            Text(
                text = title,
                color = EvSecondaryLime,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = EvTextDim,
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
