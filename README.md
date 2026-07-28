# EV Companion & Telematics Dashboard

A modern, connected electric vehicle (EV) companion Android application built with **Kotlin** and **Jetpack Compose**. Featuring a custom interactive 3D vector-rendered vehicle canvas with dynamic camera controls, real-time telematics monitoring, drive controls, climate settings, and local trip logging.

---

## 🌟 Key Features

### 🚘 Interactive 3D Vehicle Canvas
- **Custom 3D Vector Engine**: Lightweight, high-performance 3D projection renderer built directly with Jetpack Compose `Canvas` and depth-sorted graphics pipeline (Painter's Algorithm).
- **Interactive Camera Controls**: 360° touch gesture rotation (yaw and pitch orbit controls) and dynamic camera preset buttons (3/4 Front, Profile Side, Rear Quarter, Top Down) with smooth animation interpolation.
- **Studio Lighting & Environments**: 4 ambient lighting environments (Studio Spotlight, Cyber Neon, Sunset Flare, Midnight Stealth) to preview vehicle geometry under varying lighting conditions.
- **Interactive Vehicle Lighting**: Functional toggle controls for LED headlights, connecting cyber lightbars, tail lightbars, hazard lights, and animated charging flap status.

### 📊 Telematics & Vehicle Controls
- **EV Power & Battery Dashboard**: Real-time State of Charge (SoC %), estimated remaining driving range, motor power output (kW), and battery/motor temperature metrics.
- **Drive Mode & Regen Settings**: Selectable drive modes (ECO, CITY, SPORT, HYPER) and 4-level regenerative braking settings (OFF, LOW, MED, HIGH).
- **Cruise Control & Speed Management**: Cruise speed target slider with real-time speed adjustment.
- **Charging & Battery Conditioning**: Charge state toggle, target charge limit percentage selector, and battery thermal preconditioning control.
- **Tire Pressure Monitoring (TPMS)**: Live individual tire pressure readings for front and rear tires.
- **Remote Lock Control**: Remote door lock/unlock status toggle.

### 📜 Drive Logs & Local History
- **Room Local Persistence**: Local database integration using Room to automatically record and persist completed drive sessions.
- **Analytics & Summaries**: Trip logs tracking distance (km), trip duration, total energy consumption (kWh), and average efficiency (Wh/km).

---

## 🛠️ Tech Stack & Architecture

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material Design 3
- **Architecture**: MVVM (Model-View-ViewModel) with `StateFlow` and Coroutines
- **Database**: Room Database with KSP
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
