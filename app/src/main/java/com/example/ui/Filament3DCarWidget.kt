package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import kotlinx.coroutines.launch
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VehicleState
import com.example.ui.theme.*
import kotlin.math.*

/**
 * Paint Finish Color Options for Honda Prologue EV
 */
data class CarColorOption(
    val name: String,
    val primaryColor: Color,
    val darkShade: Color,
    val highlightColor: Color
)

val CarPaintOptions = listOf(
    CarColorOption("Pacific Blue", Color(0xFF0077FF), Color(0xFF002266), Color(0xFF80C0FF)),
    CarColorOption("Raven Black", Color(0xFF20232A), Color(0xFF0B0D11), Color(0xFF555A66)),
    CarColorOption("Snow Pearl", Color(0xFFE2E8F0), Color(0xFF8A95A5), Color(0xFFFFFFFF)),
    CarColorOption("Crimson Flare", Color(0xFFFF2A55), Color(0xFF660018), Color(0xFFFF99AA)),
    CarColorOption("Cyber Cyan", Color(0xFF00E5FF), Color(0xFF004D57), Color(0xFFA6FAFF))
)

/**
 * Environment Lighting Modes
 */
enum class LightingEnvironment(val label: String, val lightVector: Offset, val ambientColor: Color, val bgGradient: List<Color>) {
    STUDIO("Studio Spotlight", Offset(0.4f, -0.9f), Color(0xFF1E2638), listOf(Color(0xFF0F1420), Color(0xFF06080F))),
    CYBERPUNK("Cyber Neon", Offset(-0.7f, -0.5f), Color(0xFF2A003B), listOf(Color(0xFF190028), Color(0xFF090012))),
    SUNSET("Golden Hour", Offset(0.9f, -0.3f), Color(0xFF3B1E00), listOf(Color(0xFF241000), Color(0xFF0A0400))),
    STEALTH("Midnight Dark", Offset(0.0f, -1.0f), Color(0xFF10141D), listOf(Color(0xFF0A0D14), Color(0xFF030407)))
}

@Composable
fun Filament3DCarWidget(
    vehicleState: VehicleState,
    modifier: Modifier = Modifier
) {
    // Interactive 3D Camera Orbit State
    val coroutineScope = rememberCoroutineScope()
    val animatableYaw = remember { Animatable(35f) }
    val animatablePitch = remember { Animatable(22f) }
    var isAutoRotating by remember { mutableStateOf(true) }
    var selectedPaintIndex by remember { mutableIntStateOf(0) }
    var selectedLighting by remember { mutableStateOf(LightingEnvironment.STUDIO) }
    var zoomScale by remember { mutableFloatStateOf(1.0f) }

    // Auto-rotate animation when enabled
    val infiniteTransition = rememberInfiniteTransition(label = "Filament3DRotation")
    val autoYaw by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "AutoYaw"
    )

    // Effective Yaw depending on user drag or auto-rotation
    val currentYaw = if (isAutoRotating) (autoYaw + animatableYaw.value) % 360f else animatableYaw.value % 360f
    val pitchAngle = animatablePitch.value

    // Smooth camera transition (lerp) helper for view presets
    fun animateToPreset(targetYaw: Float, targetPitch: Float) {
        val currentYawVal = if (isAutoRotating) (autoYaw + animatableYaw.value) else animatableYaw.value
        isAutoRotating = false
        coroutineScope.launch {
            animatableYaw.snapTo(currentYawVal)

            val current = animatableYaw.value
            val diff = (targetYaw - current) % 360f
            val shortestDiff = when {
                diff > 180f -> diff - 360f
                diff < -180f -> diff + 360f
                else -> diff
            }
            val finalTargetYaw = current + shortestDiff

            launch {
                animatableYaw.animateTo(
                    targetValue = finalTargetYaw,
                    animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
                )
            }
            launch {
                animatablePitch.animateTo(
                    targetValue = targetPitch.coerceIn(-10f, 65f),
                    animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
                )
            }
        }
    }

    // Wheel spin animation offset driven by vehicle speed
    val wheelSpinDeg by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (15000 / (vehicleState.speedKmh.coerceAtLeast(1))).coerceIn(200, 5000),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "WheelSpin"
    )

    val currentPaint = CarPaintOptions[selectedPaintIndex]

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(selectedLighting.bgGradient)
            )
            .border(1.dp, EvSurfaceBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        // Widget Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(EvPrimaryCyan.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = "Filament 3D Engine",
                        tint = EvPrimaryCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "HONDA PROLOGUE EV",
                            color = EvTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(EvSecondaryLime.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "HIGH-POLY PBR",
                                color = EvSecondaryLime,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    Text(
                        text = "Photorealistic 3D Realtime Drag Viewport",
                        color = EvTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            // Auto Rotate Toggle Button
            IconButton(
                onClick = { isAutoRotating = !isAutoRotating },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isAutoRotating) EvPrimaryCyan.copy(alpha = 0.2f) else EvSurfaceVariant)
            ) {
                Icon(
                    imageVector = if (isAutoRotating) Icons.Default.Autorenew else Icons.Default.Pause,
                    contentDescription = "Toggle Orbit",
                    tint = if (isAutoRotating) EvPrimaryCyan else EvTextDim
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 3D REALTIME CANVAS VIEWPORT WITH DRAG GESTURES
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            selectedLighting.ambientColor,
                            selectedLighting.bgGradient.last()
                        )
                    )
                )
                .border(1.dp, EvSurfaceBorder.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            if (isAutoRotating) {
                                val currentYawVal = (autoYaw + animatableYaw.value)
                                coroutineScope.launch { animatableYaw.snapTo(currentYawVal) }
                                isAutoRotating = false
                            }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            coroutineScope.launch {
                                animatableYaw.snapTo(animatableYaw.value - dragAmount.x * 0.5f)
                                animatablePitch.snapTo((animatablePitch.value + dragAmount.y * 0.3f).coerceIn(-10f, 65f))
                            }
                        }
                    )
                }
                .testTag("filament_3d_car_canvas")
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val center = Offset(canvasWidth / 2f, canvasHeight / 2f + 10f)

                // Render Ground Grid & Shadows
                draw3DGroundGridAndShadow(
                    center = center,
                    yawDeg = currentYaw,
                    pitchDeg = pitchAngle,
                    lighting = selectedLighting
                )

                // Render Photorealistic High-Poly Mid-Size Crossover SUV 3D Mesh
                draw3DMidSizeCrossoverSUV(
                    center = center,
                    yawDeg = currentYaw,
                    pitchDeg = pitchAngle,
                    zoom = zoomScale,
                    paint = currentPaint,
                    lighting = selectedLighting,
                    headlightsOn = vehicleState.headlightOn,
                    hazardOn = vehicleState.hazardOn,
                    isCharging = vehicleState.isCharging,
                    wheelSpinDeg = if (vehicleState.speedKmh > 0) wheelSpinDeg else 0f
                )
            }

            // Floating Telemetry & Controls Overlay
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(EvSecondaryLime)
                    )
                    Text(
                        text = "60 FPS • Vulkan PBR",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "YAW: ${currentYaw.roundToInt()}° | PITCH: ${pitchAngle.roundToInt()}°",
                    color = EvTextDim,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.4f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            // Quick Camera View Presets
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CameraPresetButton("3/4 Front") { animateToPreset(35f, 20f) }
                CameraPresetButton("Side") { animateToPreset(180f, -18f) }
                CameraPresetButton("Rear") { animateToPreset(270f, 18f) }
                CameraPresetButton("Top") { animateToPreset(0f, 55f) }
            }

            // Drag indicator tooltip banner
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(EvSurface.copy(alpha = 0.85f))
                    .border(0.5.dp, EvSurfaceBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.TouchApp,
                    contentDescription = "Drag to Rotate",
                    tint = EvPrimaryCyan,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Drag to Rotate",
                    color = EvTextPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // CUSTOMIZATION CONTROLS: Lighting Environment
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Lighting Environment Selector
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "LIGHTING MODE",
                    color = EvTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    LightingEnvironment.values().forEach { env ->
                        val isSelected = (env == selectedLighting)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) EvPrimaryCyan.copy(alpha = 0.2f) else EvSurfaceVariant)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) EvPrimaryCyan else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedLighting = env }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                .testTag("lighting_env_${env.name}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = env.label.split(" ").first(),
                                color = if (isSelected) EvPrimaryCyan else EvTextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CameraPresetButton(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color.Black.copy(alpha = 0.65f))
            .border(0.5.dp, EvSurfaceBorder, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Custom 3D Projection Engine drawing Ground Plane & Shadow
 */
private fun DrawScope.draw3DGroundGridAndShadow(
    center: Offset,
    yawDeg: Float,
    pitchDeg: Float,
    lighting: LightingEnvironment
) {
    val yawRad = Math.toRadians(yawDeg.toDouble())
    val pitchRad = Math.toRadians(pitchDeg.toDouble())

    // Ground Contact Oval Shadow under car
    drawOval(
        color = Color.Black.copy(alpha = 0.6f),
        topLeft = Offset(center.x - 110f, center.y + 35f),
        size = Size(220f, 65f)
    )

    // Soft Ambient Lighting Glow under chassis
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(lighting.ambientColor.copy(alpha = 0.8f), Color.Transparent),
            center = Offset(center.x, center.y + 55f),
            radius = 140f
        ),
        topLeft = Offset(center.x - 140f, center.y - 10f),
        size = Size(280f, 130f)
    )
}

private data class Pt3D(val x: Float, val y: Float, val z: Float)

private data class TransformedPt(
    val pt2d: Offset,
    val rx: Float,
    val ry: Float,
    val rz: Float,
    val finalZ: Float,
    val local: Pt3D
)

private class RenderComponent(
    val depth: Float,
    val draw: DrawScope.() -> Unit
)

/**
 * 3D Photorealistic High-Poly Projection Renderer for Honda Prologue EV SUV
 */
private fun DrawScope.draw3DMidSizeCrossoverSUV(
    center: Offset,
    yawDeg: Float,
    pitchDeg: Float,
    zoom: Float,
    paint: CarColorOption,
    lighting: LightingEnvironment,
    headlightsOn: Boolean,
    hazardOn: Boolean,
    isCharging: Boolean,
    wheelSpinDeg: Float
) {
    val yawRad = Math.toRadians(yawDeg.toDouble()).toFloat()
    val pitchRad = Math.toRadians(pitchDeg.toDouble()).toFloat()

    val cosY = cos(yawRad)
    val sinY = sin(yawRad)
    val cosP = cos(pitchRad)
    val sinP = sin(pitchRad)

    val scale = 1.15f * zoom

    // Helper 3D to 2D perspective projection function
    fun projectPt(x: Float, y: Float, z: Float): TransformedPt {
        val rx = x * cosY - z * sinY
        val rz = x * sinY + z * cosY

        val ry = y * cosP + rz * sinP
        val finalZ = -y * sinP + rz * cosP

        val perspective = 1f + (finalZ / 650f)
        val px = center.x + (rx * scale) * perspective
        val py = center.y + (ry * scale) * perspective
        return TransformedPt(Offset(px, py), rx, ry, rz, finalZ, Pt3D(x, y, z))
    }

    // Calculate normal-based face shading
    fun calcShade(
        p0: Pt3D, p1: Pt3D, p2: Pt3D,
        baseColor: Color
    ): Color {
        val ax = p1.x - p0.x
        val ay = p1.y - p0.y
        val az = p1.z - p0.z
        val bx = p2.x - p0.x
        val by = p2.y - p0.y
        val bz = p2.z - p0.z

        var nx = ay * bz - az * by
        var ny = az * bx - ax * bz
        var nz = ax * by - ay * bx
        val len = sqrt(nx * nx + ny * ny + nz * nz).coerceAtLeast(0.001f)
        nx /= len; ny /= len; nz /= len

        val lx = lighting.lightVector.x
        val ly = lighting.lightVector.y
        val lz = 0.5f

        val dot = (nx * lx + ny * ly + nz * lz).coerceIn(-1f, 1f)
        val factor = (dot * 0.35f + 0.65f).coerceIn(0.25f, 1.0f)

        return Color(
            red = (baseColor.red * factor).coerceIn(0f, 1f),
            green = (baseColor.green * factor).coerceIn(0f, 1f),
            blue = (baseColor.blue * factor).coerceIn(0f, 1f),
            alpha = baseColor.alpha
        )
    }

    // High-Poly Mid-Size Crossover SUV Geometry Keypoints
    val frontBumpL = projectPt(115f, 18f, -50f)
    val frontBumpR = projectPt(115f, 18f, 50f)
    val frontLowerL = projectPt(112f, 26f, -46f)
    val frontLowerR = projectPt(112f, 26f, 46f)

    val rearBumpL = projectPt(-115f, 16f, -50f)
    val rearBumpR = projectPt(-115f, 16f, 50f)
    val rearLowerL = projectPt(-112f, 26f, -46f)
    val rearLowerR = projectPt(-112f, 26f, 46f)

    val hoodFrontL = projectPt(108f, -2f, -46f)
    val hoodFrontR = projectPt(108f, -2f, 46f)
    val hoodMidL = projectPt(75f, -8f, -45f)
    val hoodMidR = projectPt(75f, -8f, 45f)
    val hoodBackL = projectPt(38f, -14f, -44f)
    val hoodBackR = projectPt(38f, -14f, 44f)

    val windshieldTopL = projectPt(12f, -44f, -38f)
    val windshieldTopR = projectPt(12f, -44f, 38f)

    val roofFrontL = windshieldTopL
    val roofFrontR = windshieldTopR
    val roofMidL = projectPt(-30f, -48f, -38f)
    val roofMidR = projectPt(-30f, -48f, 38f)
    val roofRearL = projectPt(-70f, -42f, -37f)
    val roofRearR = projectPt(-70f, -42f, 37f)
    val spoilerL = projectPt(-82f, -44f, -36f)
    val spoilerR = projectPt(-82f, -44f, 36f)

    val hatchMidL = projectPt(-98f, -15f, -42f)
    val hatchMidR = projectPt(-98f, -15f, 42f)

    val sillL = projectPt(0f, 24f, -50f)
    val sillR = projectPt(0f, 24f, 50f)

    // Wheel Arch Cutout Geometry Keypoints
    val fArchFrontL = projectPt(85f, 25f, -50f)
    val fArchTopFrontL = projectPt(75f, 6f, -50f)
    val fArchTopL = projectPt(65f, 2f, -50f)
    val fArchTopBackL = projectPt(55f, 6f, -50f)
    val fArchBackL = projectPt(45f, 25f, -50f)

    val rArchFrontL = projectPt(-42f, 25f, -50f)
    val rArchTopFrontL = projectPt(-52f, 6f, -50f)
    val rArchTopL = projectPt(-62f, 2f, -50f)
    val rArchTopBackL = projectPt(-72f, 6f, -50f)
    val rArchBackL = projectPt(-82f, 25f, -50f)

    val fArchFrontR = projectPt(85f, 25f, 50f)
    val fArchTopFrontR = projectPt(75f, 6f, 50f)
    val fArchTopR = projectPt(65f, 2f, 50f)
    val fArchTopBackR = projectPt(55f, 6f, 50f)
    val fArchBackR = projectPt(45f, 25f, 50f)

    val rArchFrontR = projectPt(-42f, 25f, 50f)
    val rArchTopFrontR = projectPt(-52f, 6f, 50f)
    val rArchTopR = projectPt(-62f, 2f, 50f)
    val rArchTopBackR = projectPt(-72f, 6f, 50f)
    val rArchBackR = projectPt(-82f, 25f, 50f)

    val renderComponents = mutableListOf<RenderComponent>()

    // 1. UNDERCARRIAGE & EV BATTERY CHASSIS
    val underDepth = (frontLowerL.finalZ + frontLowerR.finalZ + rearLowerR.finalZ + rearLowerL.finalZ) / 4f
    renderComponents.add(RenderComponent(underDepth - 10f) {
        val path = Path().apply {
            moveTo(frontLowerL.pt2d.x, frontLowerL.pt2d.y)
            lineTo(frontLowerR.pt2d.x, frontLowerR.pt2d.y)
            lineTo(rearLowerR.pt2d.x, rearLowerR.pt2d.y)
            lineTo(rearLowerL.pt2d.x, rearLowerL.pt2d.y)
            close()
        }
        drawPath(path, Color(0xFF0D121A))

        // EV Battery Pack Sub-Shield (centered underneath)
        val battFrontL = projectPt(60f, 26f, -38f)
        val battFrontR = projectPt(60f, 26f, 38f)
        val battRearR = projectPt(-55f, 26f, 38f)
        val battRearL = projectPt(-55f, 26f, -38f)

        val battPath = Path().apply {
            moveTo(battFrontL.pt2d.x, battFrontL.pt2d.y)
            lineTo(battFrontR.pt2d.x, battFrontR.pt2d.y)
            lineTo(battRearR.pt2d.x, battRearR.pt2d.y)
            lineTo(battRearL.pt2d.x, battRearL.pt2d.y)
            close()
        }
        drawPath(battPath, Color(0xFF171E28))
        drawPath(battPath, Color(0xFF00E5FF).copy(alpha = 0.25f), style = Stroke(width = 1.2f))

        // Structural Battery Ribs
        for (i in -2..2) {
            val ribStart = projectPt(i * 20f, 26f, -36f)
            val ribEnd = projectPt(i * 20f, 26f, 36f)
            drawLine(
                color = Color(0xFF283344),
                start = ribStart.pt2d,
                end = ribEnd.pt2d,
                strokeWidth = 1.5f
            )
        }
    })

    // 2. REAR BUMPER & LOWER DIFFUSER
    val rearBumperDepth = (rearBumpL.finalZ + rearBumpR.finalZ + rearLowerR.finalZ + rearLowerL.finalZ) / 4f
    renderComponents.add(RenderComponent(rearBumperDepth) {
        val rearBumperColor = calcShade(rearBumpL.local, rearBumpR.local, rearLowerL.local, Color(0xFF1B2330))
        val path = Path().apply {
            moveTo(rearBumpL.pt2d.x, rearBumpL.pt2d.y)
            lineTo(rearBumpR.pt2d.x, rearBumpR.pt2d.y)
            lineTo(rearLowerR.pt2d.x, rearLowerR.pt2d.y)
            lineTo(rearLowerL.pt2d.x, rearLowerL.pt2d.y)
            close()
        }
        drawPath(path, rearBumperColor)
        drawPath(path, Color(0xFF3B485A), style = Stroke(width = 1.2f))

        val skidPath = Path().apply {
            val rsl = projectPt(-110f, 24f, -25f)
            val rsr = projectPt(-110f, 24f, 25f)
            moveTo(rsl.pt2d.x, rsl.pt2d.y)
            lineTo(rsr.pt2d.x, rsr.pt2d.y)
            lineTo(rearLowerR.pt2d.x, rearLowerR.pt2d.y)
            lineTo(rearLowerL.pt2d.x, rearLowerL.pt2d.y)
            close()
        }
        drawPath(skidPath, Brush.verticalGradient(listOf(Color(0xFF8090A5), Color(0xFF2B3545))))
    })

    // 3. REAR HATCH / TAILGATE SOLID FACE
    val rearHatchDepth = (hatchMidL.finalZ + hatchMidR.finalZ + rearBumpR.finalZ + rearBumpL.finalZ) / 4f
    renderComponents.add(RenderComponent(rearHatchDepth) {
        val hatchShade = calcShade(hatchMidL.local, hatchMidR.local, rearBumpL.local, paint.primaryColor)
        val path = Path().apply {
            moveTo(hatchMidL.pt2d.x, hatchMidL.pt2d.y)
            lineTo(hatchMidR.pt2d.x, hatchMidR.pt2d.y)
            lineTo(rearBumpR.pt2d.x, rearBumpR.pt2d.y)
            lineTo(rearBumpL.pt2d.x, rearBumpL.pt2d.y)
            close()
        }
        drawPath(path, hatchShade)
        drawPath(path, paint.highlightColor.copy(alpha = 0.3f), style = Stroke(width = 1f))

        val rearLogoCenter = projectPt(-106f, 2f, 0f)
        drawCircle(color = Color(0xFF00E5FF), radius = 4f * scale, center = rearLogoCenter.pt2d)
    })

    // 4. REAR WINDOW (GLASS)
    val rearWinDepth = (roofRearL.finalZ + roofRearR.finalZ + hatchMidR.finalZ + hatchMidL.finalZ) / 4f
    renderComponents.add(RenderComponent(rearWinDepth) {
        val path = Path().apply {
            moveTo(roofRearL.pt2d.x, roofRearL.pt2d.y)
            lineTo(roofRearR.pt2d.x, roofRearR.pt2d.y)
            lineTo(hatchMidR.pt2d.x, hatchMidR.pt2d.y)
            lineTo(hatchMidL.pt2d.x, hatchMidL.pt2d.y)
            close()
        }
        drawPath(path, Brush.linearGradient(listOf(Color(0xEE0B121C), Color(0xCC00E5FF))))
        drawPath(path, Color(0xFF00E5FF).copy(alpha = 0.4f), style = Stroke(width = 1f))
    })

    // 5. FRONT DIAMOND/MATRIX GRILLE & FASCIA
    val grilleDepth = (hoodFrontL.finalZ + hoodFrontR.finalZ + frontBumpR.finalZ + frontBumpL.finalZ) / 4f
    renderComponents.add(RenderComponent(grilleDepth) {
        val grillePath = Path().apply {
            moveTo(hoodFrontL.pt2d.x, hoodFrontL.pt2d.y)
            lineTo(hoodFrontR.pt2d.x, hoodFrontR.pt2d.y)
            lineTo(frontBumpR.pt2d.x, frontBumpR.pt2d.y)
            lineTo(frontLowerR.pt2d.x, frontLowerR.pt2d.y)
            lineTo(frontLowerL.pt2d.x, frontLowerL.pt2d.y)
            lineTo(frontBumpL.pt2d.x, frontBumpL.pt2d.y)
            close()
        }
        drawPath(grillePath, Color(0xFF101622))
        drawPath(grillePath, Color(0xFF3B485A), style = Stroke(width = 1.5f))

        for (i in -3..3) {
            val gx = projectPt(111f, 8f + i * 2.5f, -28f)
            val gy = projectPt(111f, 8f + i * 2.5f, 28f)
            drawLine(
                color = Color(0xFF00E5FF).copy(alpha = 0.5f),
                start = gx.pt2d,
                end = gy.pt2d,
                strokeWidth = 1f
            )
        }

        val skidPlatePath = Path().apply {
            val fsl = projectPt(90f, 24f, -25f)
            val fsr = projectPt(90f, 24f, 25f)
            moveTo(fsl.pt2d.x, fsl.pt2d.y)
            lineTo(fsr.pt2d.x, fsr.pt2d.y)
            lineTo(frontLowerR.pt2d.x, frontLowerR.pt2d.y)
            lineTo(frontLowerL.pt2d.x, frontLowerL.pt2d.y)
            close()
        }
        drawPath(skidPlatePath, Brush.verticalGradient(listOf(Color(0xFF8090A5), Color(0xFF2B3545))))
    })

    // 6. SCULPTED HOOD & POWER DOMES
    val hoodDepth = (hoodFrontL.finalZ + hoodFrontR.finalZ + hoodBackR.finalZ + hoodBackL.finalZ) / 4f
    renderComponents.add(RenderComponent(hoodDepth) {
        val hoodShade = calcShade(hoodFrontL.local, hoodFrontR.local, hoodBackL.local, paint.primaryColor)
        val hoodPath = Path().apply {
            moveTo(hoodFrontL.pt2d.x, hoodFrontL.pt2d.y)
            lineTo(hoodFrontR.pt2d.x, hoodFrontR.pt2d.y)
            lineTo(hoodMidR.pt2d.x, hoodMidR.pt2d.y)
            lineTo(hoodBackR.pt2d.x, hoodBackR.pt2d.y)
            lineTo(hoodBackL.pt2d.x, hoodBackL.pt2d.y)
            lineTo(hoodMidL.pt2d.x, hoodMidL.pt2d.y)
            close()
        }
        drawPath(hoodPath, hoodShade)

        val hoodCenterFront = projectPt(108f, -2f, 0f)
        val hoodCenterBack = projectPt(38f, -14f, 0f)
        drawLine(
            color = paint.highlightColor.copy(alpha = 0.6f),
            start = hoodCenterFront.pt2d,
            end = hoodCenterBack.pt2d,
            strokeWidth = 1.8f
        )
        drawPath(hoodPath, paint.highlightColor.copy(alpha = 0.4f), style = Stroke(width = 1.2f))
    })

    // 7. WINDSHIELD
    val windshieldDepth = (hoodBackL.finalZ + hoodBackR.finalZ + windshieldTopR.finalZ + windshieldTopL.finalZ) / 4f
    renderComponents.add(RenderComponent(windshieldDepth) {
        val windshieldPath = Path().apply {
            moveTo(hoodBackL.pt2d.x, hoodBackL.pt2d.y)
            lineTo(hoodBackR.pt2d.x, hoodBackR.pt2d.y)
            lineTo(windshieldTopR.pt2d.x, windshieldTopR.pt2d.y)
            lineTo(windshieldTopL.pt2d.x, windshieldTopL.pt2d.y)
            close()
        }
        drawPath(
            windshieldPath,
            Brush.linearGradient(listOf(Color(0xCC00E5FF), Color(0xEE0B121C)))
        )
        drawPath(windshieldPath, Color(0xFF00E5FF).copy(alpha = 0.4f), style = Stroke(width = 1f))
    })

    // 8. ROOF & PANORAMIC GLASS
    val roofDepth = (roofFrontL.finalZ + roofFrontR.finalZ + roofRearR.finalZ + roofRearL.finalZ) / 4f
    renderComponents.add(RenderComponent(roofDepth) {
        val roofPath = Path().apply {
            moveTo(roofFrontL.pt2d.x, roofFrontL.pt2d.y)
            lineTo(roofFrontR.pt2d.x, roofFrontR.pt2d.y)
            lineTo(roofMidR.pt2d.x, roofMidR.pt2d.y)
            lineTo(roofRearR.pt2d.x, roofRearR.pt2d.y)
            lineTo(roofRearL.pt2d.x, roofRearL.pt2d.y)
            lineTo(roofMidL.pt2d.x, roofMidL.pt2d.y)
            close()
        }
        drawPath(
            roofPath,
            Brush.linearGradient(listOf(Color(0xFF0F1722), paint.primaryColor.copy(alpha = 0.85f)))
        )
    })

    // 9. ROOF SPOILER
    val spoilerDepth = (roofRearL.finalZ + roofRearR.finalZ + spoilerR.finalZ + spoilerL.finalZ) / 4f
    renderComponents.add(RenderComponent(spoilerDepth) {
        val spoilerPath = Path().apply {
            moveTo(roofRearL.pt2d.x, roofRearL.pt2d.y)
            lineTo(roofRearR.pt2d.x, roofRearR.pt2d.y)
            lineTo(spoilerR.pt2d.x, spoilerR.pt2d.y)
            lineTo(spoilerL.pt2d.x, spoilerL.pt2d.y)
            close()
        }
        drawPath(spoilerPath, paint.darkShade)
    })

    // 10. LEFT SIDE FLANK & WINDOWS
    val leftFlankDepth = (hoodFrontL.finalZ + roofRearL.finalZ + rearBumpL.finalZ + sillL.finalZ) / 4f
    renderComponents.add(RenderComponent(leftFlankDepth) {
        val leftFlankShade = calcShade(Pt3D(115f, 18f, -50f), Pt3D(-115f, 16f, -50f), Pt3D(0f, -48f, -50f), paint.primaryColor)
        val leftFlankPath = Path().apply {
            moveTo(hoodFrontL.pt2d.x, hoodFrontL.pt2d.y)
            lineTo(hoodBackL.pt2d.x, hoodBackL.pt2d.y)
            lineTo(roofRearL.pt2d.x, roofRearL.pt2d.y)
            lineTo(hatchMidL.pt2d.x, hatchMidL.pt2d.y)
            lineTo(rearBumpL.pt2d.x, rearBumpL.pt2d.y)
            lineTo(rearLowerL.pt2d.x, rearLowerL.pt2d.y)
            // Rear Wheel Arch Cutout
            lineTo(rArchBackL.pt2d.x, rArchBackL.pt2d.y)
            lineTo(rArchTopBackL.pt2d.x, rArchTopBackL.pt2d.y)
            lineTo(rArchTopL.pt2d.x, rArchTopL.pt2d.y)
            lineTo(rArchTopFrontL.pt2d.x, rArchTopFrontL.pt2d.y)
            lineTo(rArchFrontL.pt2d.x, rArchFrontL.pt2d.y)
            // Rocker Sill
            lineTo(sillL.pt2d.x, sillL.pt2d.y)
            // Front Wheel Arch Cutout
            lineTo(fArchBackL.pt2d.x, fArchBackL.pt2d.y)
            lineTo(fArchTopBackL.pt2d.x, fArchTopBackL.pt2d.y)
            lineTo(fArchTopL.pt2d.x, fArchTopL.pt2d.y)
            lineTo(fArchTopFrontL.pt2d.x, fArchTopFrontL.pt2d.y)
            lineTo(fArchFrontL.pt2d.x, fArchFrontL.pt2d.y)
            // Front Bumper Lower
            lineTo(frontLowerL.pt2d.x, frontLowerL.pt2d.y)
            lineTo(frontBumpL.pt2d.x, frontBumpL.pt2d.y)
            close()
        }
        drawPath(leftFlankPath, leftFlankShade)

        val leftWindowPath = Path().apply {
            moveTo(hoodBackL.pt2d.x, hoodBackL.pt2d.y)
            lineTo(windshieldTopL.pt2d.x, windshieldTopL.pt2d.y)
            lineTo(roofRearL.pt2d.x, roofRearL.pt2d.y)
            lineTo(hatchMidL.pt2d.x, hatchMidL.pt2d.y)
            close()
        }
        drawPath(leftWindowPath, Color(0xDD0D141F))

        val mirrorL = projectPt(32f, -18f, -54f)
        drawCircle(color = leftFlankShade, radius = 5f * scale, center = mirrorL.pt2d)
        drawCircle(color = Color(0xFF00E5FF), radius = 2f * scale, center = mirrorL.pt2d)
    })

    // 11. RIGHT SIDE FLANK & WINDOWS
    val rightFlankDepth = (hoodFrontR.finalZ + roofRearR.finalZ + rearBumpR.finalZ + sillR.finalZ) / 4f
    renderComponents.add(RenderComponent(rightFlankDepth) {
        val rightFlankShade = calcShade(Pt3D(115f, 18f, 50f), Pt3D(0f, -48f, 50f), Pt3D(-115f, 16f, 50f), paint.primaryColor)
        val rightFlankPath = Path().apply {
            moveTo(hoodFrontR.pt2d.x, hoodFrontR.pt2d.y)
            lineTo(hoodBackR.pt2d.x, hoodBackR.pt2d.y)
            lineTo(roofRearR.pt2d.x, roofRearR.pt2d.y)
            lineTo(hatchMidR.pt2d.x, hatchMidR.pt2d.y)
            lineTo(rearBumpR.pt2d.x, rearBumpR.pt2d.y)
            lineTo(rearLowerR.pt2d.x, rearLowerR.pt2d.y)
            // Rear Wheel Arch Cutout
            lineTo(rArchBackR.pt2d.x, rArchBackR.pt2d.y)
            lineTo(rArchTopBackR.pt2d.x, rArchTopBackR.pt2d.y)
            lineTo(rArchTopR.pt2d.x, rArchTopR.pt2d.y)
            lineTo(rArchTopFrontR.pt2d.x, rArchTopFrontR.pt2d.y)
            lineTo(rArchFrontR.pt2d.x, rArchFrontR.pt2d.y)
            // Rocker Sill
            lineTo(sillR.pt2d.x, sillR.pt2d.y)
            // Front Wheel Arch Cutout
            lineTo(fArchBackR.pt2d.x, fArchBackR.pt2d.y)
            lineTo(fArchTopBackR.pt2d.x, fArchTopBackR.pt2d.y)
            lineTo(fArchTopR.pt2d.x, fArchTopR.pt2d.y)
            lineTo(fArchTopFrontR.pt2d.x, fArchTopFrontR.pt2d.y)
            lineTo(fArchFrontR.pt2d.x, fArchFrontR.pt2d.y)
            // Front Bumper Lower
            lineTo(frontLowerR.pt2d.x, frontLowerR.pt2d.y)
            lineTo(frontBumpR.pt2d.x, frontBumpR.pt2d.y)
            close()
        }
        drawPath(rightFlankPath, rightFlankShade)

        val rightWindowPath = Path().apply {
            moveTo(hoodBackR.pt2d.x, hoodBackR.pt2d.y)
            lineTo(windshieldTopR.pt2d.x, windshieldTopR.pt2d.y)
            lineTo(roofRearR.pt2d.x, roofRearR.pt2d.y)
            lineTo(hatchMidR.pt2d.x, hatchMidR.pt2d.y)
            close()
        }
        drawPath(rightWindowPath, Color(0xDD0D141F))

        val mirrorR = projectPt(32f, -18f, 54f)
        drawCircle(color = rightFlankShade, radius = 5f * scale, center = mirrorR.pt2d)
        drawCircle(color = Color(0xFF00E5FF), radius = 2f * scale, center = mirrorR.pt2d)
    })

    // 12. 4 INDIVIDUAL 3D BLACK CYLINDER WHEELS WITH ACCURATE DEPTH SORTING & WHEEL WELLS
    data class WheelSpec(val x: Float, val y: Float, val zOuter: Float, val zInner: Float)
    val wheelSpecs = listOf(
        WheelSpec(65f, 18f, -52f, -40f),  // Front Left
        WheelSpec(65f, 18f, 52f, 40f),    // Front Right
        WheelSpec(-62f, 18f, -52f, -40f), // Rear Left
        WheelSpec(-62f, 18f, 52f, 40f)    // Rear Right
    )

    wheelSpecs.forEach { spec ->
        val outerCenter = projectPt(spec.x, spec.y, spec.zOuter)
        val innerCenter = projectPt(spec.x, spec.y, spec.zInner)
        val wheelRadius = 14f * scale
        val wheelDepth = maxOf(outerCenter.finalZ, innerCenter.finalZ)

        // Dark Wheel Well Inner Housing (drawn right behind wheel)
        renderComponents.add(RenderComponent(wheelDepth - 1.5f) {
            drawCircle(
                color = Color(0xFF0D1117),
                radius = wheelRadius * 1.15f,
                center = innerCenter.pt2d
            )
        })

        // Wheel 3D Cylinder & Alloy Rim Assembly
        renderComponents.add(RenderComponent(wheelDepth + 0.5f) {
            val dx = outerCenter.pt2d.x - innerCenter.pt2d.x
            val dy = outerCenter.pt2d.y - innerCenter.pt2d.y
            val dist = sqrt(dx * dx + dy * dy).coerceAtLeast(0.1f)
            val nx = -dy / dist * wheelRadius
            val ny = dx / dist * wheelRadius

            val barrelPath = Path().apply {
                moveTo(innerCenter.pt2d.x + nx, innerCenter.pt2d.y + ny)
                lineTo(outerCenter.pt2d.x + nx, outerCenter.pt2d.y + ny)
                lineTo(outerCenter.pt2d.x - nx, outerCenter.pt2d.y - ny)
                lineTo(innerCenter.pt2d.x - nx, innerCenter.pt2d.y - ny)
                close()
            }
            drawPath(
                path = barrelPath,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF080B0F), Color(0xFF1A2029), Color(0xFF080B0F)),
                    start = Offset(innerCenter.pt2d.x, innerCenter.pt2d.y),
                    end = Offset(outerCenter.pt2d.x, outerCenter.pt2d.y)
                )
            )

            drawCircle(
                color = Color(0xFF0A0D12),
                radius = wheelRadius,
                center = outerCenter.pt2d
            )
            drawCircle(
                color = Color(0xFF1C222C),
                radius = wheelRadius - 1.5f,
                center = outerCenter.pt2d,
                style = Stroke(width = 2f)
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF222B38), Color(0xFF10141C), Color(0xFF05070A)),
                    center = outerCenter.pt2d,
                    radius = wheelRadius * 0.75f
                ),
                radius = wheelRadius * 0.75f,
                center = outerCenter.pt2d
            )

            drawCircle(
                color = Color(0xFF2C3646),
                radius = wheelRadius * 0.72f,
                center = outerCenter.pt2d,
                style = Stroke(width = 1.5f)
            )

            drawCircle(
                color = Color(0xFF080A0E),
                radius = wheelRadius * 0.28f,
                center = outerCenter.pt2d
            )

            rotate(wheelSpinDeg, pivot = outerCenter.pt2d) {
                for (i in 0 until 8) {
                    val angle = Math.toRadians((i * 45).toDouble())
                    val endX = outerCenter.pt2d.x + (wheelRadius * 0.70f * cos(angle)).toFloat()
                    val endY = outerCenter.pt2d.y + (wheelRadius * 0.70f * sin(angle)).toFloat()
                    drawLine(
                        color = Color(0xFF1B222E),
                        start = outerCenter.pt2d,
                        end = Offset(endX, endY),
                        strokeWidth = 2.5f
                    )
                }
            }
        })
    }

    // 13. HEADLIGHTS & FRONT LIGHTBAR
    val headL = projectPt(109f, 2f, -38f)
    val headR = projectPt(109f, 2f, 38f)
    val headDepth = (headL.finalZ + headR.finalZ) / 2f
    renderComponents.add(RenderComponent(headDepth + 1f) {
        if (headlightsOn || hazardOn) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, Color(0xFF00E5FF), Color.Transparent),
                    center = headL.pt2d,
                    radius = 32f
                ),
                center = headL.pt2d,
                radius = 32f
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, Color(0xFF00E5FF), Color.Transparent),
                    center = headR.pt2d,
                    radius = 32f
                ),
                center = headR.pt2d,
                radius = 32f
            )
        }
        drawLine(
            color = if (headlightsOn) Color(0xFF00E5FF) else Color(0xDD80E5FF),
            start = headL.pt2d,
            end = headR.pt2d,
            strokeWidth = 3.5f * scale
        )
    })

    // 14. REAR TAIL LIGHTBAR
    val tailL = projectPt(-112f, 2f, -42f)
    val tailR = projectPt(-112f, 2f, 42f)
    val tailDepth = (tailL.finalZ + tailR.finalZ) / 2f
    renderComponents.add(RenderComponent(tailDepth + 1f) {
        drawLine(
            color = Color(0xFFFF2A55),
            start = tailL.pt2d,
            end = tailR.pt2d,
            strokeWidth = 3.5f * scale
        )
    })

    // 15. CHARGING PORT INDICATOR
    if (isCharging) {
        val chargePort = projectPt(-28f, 2f, 50f)
        renderComponents.add(RenderComponent(chargePort.finalZ + 2f) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, Color(0xFF00FF87), Color.Transparent),
                    center = chargePort.pt2d,
                    radius = 18f
                ),
                center = chargePort.pt2d,
                radius = 18f
            )
            drawLine(
                color = Color(0xFF00FF87),
                start = chargePort.pt2d,
                end = Offset(chargePort.pt2d.x + 45f, chargePort.pt2d.y + 55f),
                strokeWidth = 3.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
            )
        })
    }

    // SORT COMPONENTS BY DEPTH (PAINTER'S ALGORITHM: FURTHEST TO CLOSEST)
    renderComponents.sortBy { it.depth }
    renderComponents.forEach { component ->
        component.draw(this)
    }
}
