# EV Companion & Telematics Dashboard

A modern, connected electric vehicle (EV) companion Android application built with **Kotlin** and **Jetpack Compose**. Featuring a custom interactive 3D vector-rendered vehicle canvas with dynamic camera controls, real-time telematics monitoring, remote climate/vehicle controls, and AI-powered vehicle diagnostics.

---

## 🌟 Key Features

### 🚘 Interactive 3D Vehicle Renderer
- **Custom 3D Projection Canvas**: Lightweight, high-performance 3D projection engine built directly with Jetpack Compose `Canvas` and depth-sorted rendering components.
- **Dynamic Camera Controls**: 360° interactive touch gestures (orbit, pitch, yaw) with reversed gesture feel and smooth camera preset animations (3/4 Front, Side, Rear, Top).
- **Customization & Paint Studio**: Real-time metallic paint swatches, lighting environment toggles (Studio, Sunset, Cyber Neon, Midnight), and wheel arch/chassis details.
- **Dynamic Telemetry & Light Controls**: Real-time functional headlights, lightbar, taillights, hazard signals, and animated charging port status.

### 📊 Vehicle Telematics & Diagnostics
- **Battery & Range Management**: State of charge (SOC %), estimated driving range, and battery temperature monitor.
- **TPMS & Safety System**: Individual tire pressure sensors, brake wear indicators, and door lock status.
- **Remote Vehicle Control**: One-tap door locks/unlocks, headlight toggles, hazard lights, horn, and charging port opener.
- **Climate Control Suite**: Precise cabin temperature slider, dual-zone seat heating, and windshield defroster settings.

### 🤖 AI Assistant & Vehicle Intelligence
- Integrated Gemini API assistant for real-time vehicle manual lookup, trip efficiency advice, maintenance alerts, and contextual diagnostics.

---

## 🛠️ Tech Stack & Architecture

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material Design 3
- **Architecture**: MVVM (Model-View-ViewModel) + `StateFlow` / `collectAsStateWithLifecycle`
- **Asynchronous Operations**: Kotlin Coroutines & Flow
- **Build System**: Gradle (Kotlin DSL - `.gradle.kts`)

---

## 📋 Prerequisites

Before building the project, ensure you have the following installed:

- **Android Studio**: Ladybug (2024.2.1) or newer recommended
- **Java Development Kit (JDK)**: JDK 17 or higher
- **Android SDK**:
  - `compileSdk`: 35
  - `minSdk`: 24
  - `targetSdk`: 35

---

## 🚀 Building & Running the Project

### 1. Clone the Repository
```bash
git clone https://github.com/your-username/ev-companion-app.git
cd ev-companion-app
```

### 2. Open in Android Studio
1. Launch **Android Studio**.
2. Select **Open** and choose the cloned repository directory.
3. Allow Gradle to sync dependencies automatically.

### 3. Build via Command Line (Gradle)
To compile and assemble a Debug APK:
```bash
./gradlew assembleDebug
```
The output APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`

### 4. Install & Run
- Connect an Android device with USB Debugging enabled, or start an Android Emulator (API 24+).
- Press **Run** (`Shift + F10`) in Android Studio or install the APK via ADB:
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 📄 License

This project is open-source and available under the [MIT License](LICENSE).
