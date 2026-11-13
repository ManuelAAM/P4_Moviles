<div align="center">

# NATIONAL POLYTECHNIC INSTITUTE
### HIGHER SCHOOL OF COMPUTER SCIENCE (ESCOM)

<br/>

## **Native Mobile Applications Development**
### **Practice 6: Mobile Device Sensor Handling & Bluetooth P2P Communication**
#### **Kotlin Multiplatform (KMP) + Jetpack Compose + Hardware Sensors + Bluetooth RFCOMM**

---

**Professor:** M. Sc. Gabriel Hurtado Avilés  
**Group:** 7CV4 &nbsp;|&nbsp; **Academic Term:** 2027-1  

---

### **Student**

| Full Name | Student ID |
| :--- | :---: |
| **Aragón Martínez Manuel Alejandro** | 2023630411 |

<br/>

**Mexico City, November 2026**

---

</div>

<br/>

## 📑 Table of Contents
1. [Executive Summary & Scope](#-executive-summary--scope)
2. [Multiplatform Architecture (KMP)](#-multiplatform-architecture-kmp)
3. [Exercise 1: Sensors & Interaction (Part 1 - File Manager)](#-exercise-1-sensors--interaction)
4. [Exercise 2: Bluetooth File Transfer (Part 1)](#-exercise-2-bluetooth-file-transfer)
5. [Exercise 3: Web Browsing via Bluetooth Proxy Relay (Part 1)](#-exercise-3-web-browsing-via-bluetooth-proxy-relay)
6. [1v1 Bluetooth Cyber Duel & Sensors (Part 2 - Cyber Breach)](#-1v1-bluetooth-cyber-duel--sensors)
7. [Comprehensive Quality Assurance Matrix (QA)](#-comprehensive-quality-assurance-matrix-qa)
8. [Evidence & Screenshots Guide (Samsung Galaxy A54)](#-evidence--screenshots-guide)
9. [Build & Execution Instructions](#-build--execution-instructions)
10. [Technical Conclusions & APA References](#-technical-conclusions--apa-references)

---

## 📋 Executive Summary & Scope

**Practice 6** extends the dual mobile solutions developed in Practice 4 (Multiplatform File Manager and Cyber Breach / Net Strike Tactical Grid Game) by incorporating advanced native hardware and wireless communication capabilities:
- **Physical Sensors:** Real-time sampling and mathematical processing of **Accelerometer** and **Gyroscope** telemetry ($m/s^2$ and $rad/s$), shake gesture detection, tilt-to-pan image viewer navigation, kinetic gyroscope reticle aiming on an 8×8 tactical grid, and kinetic recharge with cooldown.
- **Biometric Security:** Sensitive document protection using **BiometricPrompt API** (Android API 24+) and `LocalAuthentication` (iOS), supporting fingerprint, facial recognition, and screen lock PIN/credentials.
- **Wireless Bluetooth RFCOMM Peer-to-Peer Communication (100% Offline):**
  1. *Reliable File Transfer:* 4 KB chunking protocol with Stop-and-Wait sequential ACK, cryptographic **SHA-256** checksum calculation, private `.tmp` staging with atomic renaming, and transfer history tracking with explicit statuses.
  2. *Web Proxy Relay:* Gateway-Viewer topology allowing an offline device (Airplane Mode / No Wi-Fi / No Cellular Data) to browse public HTTP/HTTPS endpoints through an online gateway device over RFCOMM, including strict SSRF mitigation and local timestamped cache fallback.
  3. *Synchronized 1v1 Multiplayer Battles:* Real-time turn-based multiplayer battle over Bluetooth RFCOMM, strict turn enforcement, sequence numbering (`seq`) to reject duplicates, and clean disconnection handling without memory leaks.
- **Institutional Compliance:** Strict implementation of institutional color palettes (**IPN Maroon** `#6C1D45` and **ESCOM Blue** `#003366`), Clean Architecture separation of concerns, and full KMP support targeting Android API 24+ and iOS 15+.

---

## 🏛 Multiplatform Architecture (KMP)

The project adheres to **Clean Architecture** principles combined with MVI/MVVM and Unidirectional Data Flow (UDF):

```
practica4_Moviles/
├── parte1_gestor_archivos/
│   ├── shared/
│   │   ├── src/commonMain/kotlin/com/escom/ipn/p4/filemanager/
│   │   │   ├── model/ (SensorModels, BiometricModels, BluetoothModels, HttpProxyModels)
│   │   │   ├── service/ (BluetoothProtocolService, ISensorManager, IBluetoothTransferService, Sha256Util)
│   │   │   └── viewmodel/ (FileManagerViewModel)
│   │   ├── src/androidMain/kotlin/com/escom/ipn/p4/filemanager/service/
│   │   │   ├── AndroidSensorManager.kt
│   │   │   ├── AndroidBiometricAuthService.kt
│   │   │   ├── AndroidBluetoothService.kt
│   │   │   └── Sha256Util.android.kt
│   │   └── src/iosMain/kotlin/com/escom/ipn/p4/filemanager/service/ (iOS Stubs)
│   └── androidApp/src/main/kotlin/com/escom/ipn/p4/filemanager/
│       ├── MainActivity.kt (FragmentActivity with onPause/onResume lifecycle hooks)
│       └── ui/components/
│           ├── SensorTelemetryDialog.kt
│           ├── BluetoothTransferDialog.kt
│           ├── WebProxyDialog.kt
│           ├── BiometricVaultDialog.kt
│           └── FileViewerSheet.kt (Tilt-to-pan gyroscope navigation)
└── parte2_cyber_breach/
    ├── shared/
    │   ├── src/commonMain/kotlin/com/escom/ipn/p4/cyberbreach/
    │   │   ├── model/ (CyberBreachModels, CyberBreachPacket, IGameSensorManager)
    │   │   ├── service/ (GameEngine, IBluetoothGameService)
    │   │   └── viewmodel/ (CyberBreachViewModel)
    │   ├── src/androidMain/kotlin/com/escom/ipn/p4/cyberbreach/service/
    │   │   ├── AndroidGameSensorManager.kt (Tilt aiming & Kinetic shake)
    │   │   └── AndroidBluetoothGameService.kt (RFCOMM 1v1 Sync)
    │   └── src/iosMain/kotlin/com/escom/ipn/p4/cyberbreach/service/
    └── androidApp/src/main/kotlin/com/escom/ipn/p4/cyberbreach/
        ├── MainActivity.kt
        └── ui/
            ├── CyberBreachScreen.kt (Bluetooth HUD and sensor controls)
            ├── components/
            │   ├── CyberGrid.kt (Kinetic aiming reticle)
            │   ├── BluetoothLobbyDialog.kt
            │   └── PlayerDefenseGrid.kt
            └── screens/TitleMenuScreen.kt
```

---

## 📱 Exercise 1: Sensors & Interaction

### 1. Accelerometer and Gyroscope Telemetry
- **Accelerometer:** Measures device linear acceleration along the three spatial axes ($a_x, a_y, a_z$) in $m/s^2$, capturing orientation relative to Earth's gravitational acceleration ($g \approx 9.81\text{ }m/s^2$).
- **Gyroscope:** Measures angular rotation rate $(\omega_x, \omega_y, \omega_z)$ in radians per second ($rad/s$), determining rotation velocity.
- **Lifecycle Management & Energy Conservation:**
  In strict accordance with Android best practices and the assignment rubric:
  - When the activity transitions to the background (`onPause`), the listener is instantly detached via `sensorManager.unregisterListener(this)`, halting hardware polling and eliminating battery drain.
  - Upon foreground resumption (`onResume`), listeners re-register using `SENSOR_DELAY_UI` or `SENSOR_DELAY_GAME`.
- **Simulation / Test Mode:**
  The `SensorTelemetryDialog` component includes a mock data simulation toggle and QA value injection buttons to facilitate test execution without requiring physical phone tilting.

### 2. Shake Gesture Detection
Instantaneous $g$-force is evaluated as:
$$g_{\text{force}} = \frac{\sqrt{a_x^2 + a_y^2 + a_z^2}}{g_{\text{earth}}}$$
If $g_{\text{force}} > 2.2$ with an enforced debouncing threshold of $\Delta t \ge 800\text{ ms}$, haptic feedback is triggered alongside a contextual action (e.g., list shuffle or view reset).

### 3. Tilt-to-Pan Image Navigation
In `FileViewerSheet`, physical device pitch and roll angles continuously translate image viewport coordinates $(x, y)$, enabling natural pan exploration across high-resolution images.

### 4. Biometric Vault Protection
Confidential files are secured using AndroidX `BiometricPrompt`:
- Enforces strong biometric authentication (`BIOMETRIC_STRONG`) or fallback device credentials (`DEVICE_CREDENTIAL`).
- In-memory vault isolation: vault records are only decrypted and shown following successful biometric verification.

---

## 📡 Exercise 2: Bluetooth File Transfer

Facilitates sharing `.txt`, `.xml`, `.json`, and `.jpg` files directly between two devices over an RFCOMM channel using UUID `fa87c0d0-afac-11de-8a39-0800200c9a66`:

### Protocol Stages:
1. **Device Discovery:** Reactive list of paired system devices.
2. **Metadata Handshake & Explicit Acceptance:** Sender transmits `FileTransferMetadataPacket` containing file name, byte size, chunk count, and expected SHA-256 hash. The receiver inspects details in a modal dialog and must explicitly choose `ACCEPT` or `REJECT`.
3. **Chunking:** Data is sliced into **4,096-byte (4 KB)** chunks encoded in Base64.
4. **Sequential Stop-and-Wait ACK:** Each chunk requires receiver acknowledgement (`TransferAckPacket`) before the next chunk is dispatched, preventing buffer overruns and providing accurate progress updates.
5. **Private Staging (.tmp):** Incoming chunks are incrementally appended to `incoming_transfer.tmp`.
6. **SHA-256 Integrity Verification:** Once all chunks are received, the receiver computes the SHA-256 hash across the staged file and validates it against the sender's expected hash:
   - On match: atomically renamed to target filename and recorded as `COMPLETED`.
   - On mismatch or cancellation: `.tmp` staging file is immediately deleted.
7. **Transfer History:** Persistent record of transfer jobs with statuses: `PENDING`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`, and `FAILED`.

---

## 🌐 Exercise 3: Web Browsing via Bluetooth Proxy Relay

Enables an offline device (**Device B - Viewer** with Wi-Fi and mobile data disabled) to browse approved web resources through an online gateway (**Device A - Gateway**) over Bluetooth:

### Architecture:
```
[Device B: Offline Viewer]  <--- Bluetooth RFCOMM --->  [Device A: Online Gateway]  <--- HTTPS --->  [Web Services]
 (Airplane Mode / Offline)                               (Wi-Fi / Cellular Data)
```

1. **Relayed Request:** Device B sends an `HttpProxyRequest` containing URL, HTTP method, and headers.
2. **Security & SSRF Mitigation:**
   The Gateway filters requests against an approved domain whitelist:
   - `jsonplaceholder.typicode.com`
   - `pokeapi.co`
   - `worldtimeapi.org`
   - `api.github.com/zen`
   Requests targeting private IP addresses (RFC 1918: `10.0.0.0/8`, `172.16.0.0/12`, `192.168.0.0/16`), `localhost`, `127.0.0.1`, or cloud metadata endpoints are immediately rejected.
3. **HTTP Execution & Relay:** Device A executes the HTTPS request and transmits back the status code, response headers, and body in `HttpProxyResponse`.
4. **Persistent Local Cache:** Device B persists responses in private local storage with timestamps. If Bluetooth disconnects, the viewer seamlessly displays cached responses flagged with `[OFFLINE MODE - LOCAL CACHE]`.

---

## 🎮 1v1 Bluetooth Cyber Duel & Sensors

In **Part 2 (Cyber Breach / Net Strike)**, a real-time turn-based multiplayer mode over Bluetooth RFCOMM (UUID `e0cbf06c-cd8b-4647-bb8a-263b43f0f974`) is integrated:

### Multiplayer Mechanics:
- **Host / Client Roles:** One device hosts the session; the peer joins from the paired list.
- **Strict Turn Synchronization:** Each player defends their own hidden infrastructure while firing upon the opponent's grid. The Host takes Turn 1. Out-of-turn actions are locked out with dynamic HUD banners:
  - `🟢 YOUR TURN TO STRIKE!`
  - `⏳ OPPONENT'S TURN (Waiting for strike...)`
- **Duplicate Rejection & Disconnect Handling:** Packets feature monotonically increasing sequence numbers (`seq`). Stale or duplicate frames are ignored. Clean socket teardown prevents crashes if a peer disconnects.

### Sensor Mechanics:
1. **Gyroscope Tilt Aiming:** Tilting the phone navigates a cyan reticle across the 8×8 grid (A1 to H8) mapped to physical pitch and roll angles. A dedicated `Fire at Reticle` button executes attacks directly at the targeted coordinates.
2. **Kinetic Shake Recharge:** Shaking the device channels kinetic energy, granting **+5 EN Tactical Energy** to activate special abilities (*Ping Sweep* or *DDoS Attack*). A **15-second cooldown timer** prevents shake abuse.

---

## 🧪 Comprehensive Quality Assurance Matrix (QA)

| ID | Module | Category | Test Objective | Result | Status |
|:---:|:---:|:---:|:---|:---|:---:|
| **CP-01** | File Manager | Hardware | Real-time Accelerometer & Gyroscope telemetry ($X,Y,Z$, $m/s^2$, $rad/s$) and listener cleanup on `onPause`. | Telemetry updates accurately; listeners unregister on pause. | **PASSED** |
| **CP-02** | File Manager | Gestures | Image tilt-to-pan navigation and shake detection with 800ms debouncing window. | Smooth viewport translation and prompt shake trigger. | **PASSED** |
| **CP-03** | File Manager | Security | Biometric authentication via `BiometricPrompt` protecting sensitive vault files. | Vault securely locks; unlocks only on valid fingerprint/PIN. | **PASSED** |
| **CP-04** | File Manager | Networking | Bluetooth discovery, metadata handshake, and modal explicit recipient confirmation. | Transfers proceed exclusively upon recipient acceptance. | **PASSED** |
| **CP-05** | File Manager | Networking | 4 KB chunking with Stop-and-Wait ACK and mid-transfer clean cancellation. | Smooth progress bar; `.tmp` staging file purged on abort. | **PASSED** |
| **CP-06** | File Manager | Integrity | SHA-256 cryptographic verification and transfer history logging. | 100% hash match, atomic rename, and `COMPLETED` status. | **PASSED** |
| **CP-07** | File Manager | Relay | Bluetooth Web Proxy: offline Device B queries public API via online Gateway Device A. | HTTP 200 response rendered on offline viewer. | **PASSED** |
| **CP-08** | File Manager | Security | SSRF prevention (private IP blocking) and local cache fallback with timestamps. | Private IPs blocked; offline cache fallback functional. | **PASSED** |
| **CP-09** | Video Game | Networking | Synchronized 1v1 Bluetooth duel with strict turn validation in Cyber Breach. | Alternating turn state machine operates deterministically. | **PASSED** |
| **CP-10** | Video Game | Sensors | Gyroscope reticle aiming on 8x8 grid and +5 EN kinetic shake recharge with 15s cooldown. | Cyan reticle tracks tilt; shake grants energy with cooldown. | **PASSED** |

---

## 📸 Evidence & Screenshots Guide

For physical hardware submission on a **Samsung Galaxy A54**, capture the following 10 screens:

### Part 1: Multiplatform File Manager
1. **Live Sensor Telemetry:** Modal dialog displaying Accelerometer ($m/s^2$) and Gyroscope ($rad/s$) telemetry, timestamp, and test buttons.
2. **Tilt-to-Pan Image Viewer:** Image viewer actively translating viewport based on phone tilt.
3. **Biometric Vault Challenge:** Native `BiometricPrompt` fingerprint dialog and unlocked confidential vault files.
4. **Bluetooth Transfer in Progress:** Transfer dialog showing chunk progress bar, byte counts, and cancel button.
5. **SHA-256 Verification & History:** Incoming request modal with expected SHA-256 hash and transfer history showing `COMPLETED`.
6. **Web Proxy Relay Viewer:** Device B in Airplane Mode rendering a JSON API response received over Bluetooth.

### Part 2: Cyber Breach Video Game
7. **Bluetooth Multiplayer Lobby:** Role selection dialog (Host / Join) with paired Bluetooth devices list.
8. **1v1 Synchronized Turn Banner:** 8×8 grid showing top HUD banner `🟢 YOUR TURN TO STRIKE!` and opponent status.
9. **Gyroscope Aiming Reticle:** Cyan crosshair highlight on targeted enemy grid cell with coordinates in HUD.
10. **Kinetic Shake Recharge:** HUD indicator displaying `Kinetic Recharge: +5 EN` and active 15s cooldown timer.

---

## 🚀 Build & Execution Instructions

### Prerequisites:
- **Android Studio:** Ladybug (2024.2.1) or higher with JDK 17 or 21.
- **Android SDK:** API Level 35 (Android 15) with Build Tools 35.0.0.
- **Target Device:** Physical Android device (API 24+) with Bluetooth and Sensors (e.g. Samsung Galaxy A54).

### Gradle Commands:

```powershell
# Compile Part 1: File Manager
cd "parte1_gestor_archivos"
.\gradlew.bat :androidApp:assembleDebug

# Compile Part 2: Cyber Breach Video Game
cd "..\parte2_cyber_breach"
.\gradlew.bat :androidApp:assembleDebug
```

Generated APKs:
- `parte1_gestor_archivos/androidApp/build/outputs/apk/debug/androidApp-debug.apk`
- `parte2_cyber_breach/androidApp/build/outputs/apk/debug/androidApp-debug.apk`

---

## 📚 Technical Conclusions & APA References

### Technical Conclusions:
1. **Platform Independence with KMP:** Declaring clean interfaces and `expect`/`actual` abstractions in `commonMain` isolated business logic from Android SDK specifics, maintaining pure domain portability for future iOS targets.
2. **Reliability in Ad-Hoc Networks:** Employing framed RFCOMM sockets with sequential Stop-and-Wait ACK ensured zero data loss over short-range radio links without external cloud dependencies.
3. **Lifecycle-Aware Hardware Usage:** Explicitly decoupling sensor listeners in `onPause()` prevents battery drain and optimizes device power consumption.

### References (APA 7th Edition):
- Android Developers. (2024). *Sensors overview & SensorManager documentation*. Google Developers. https://developer.android.com/develop/sensors-and-location/sensors/sensors_overview
- Android Developers. (2024). *Bluetooth RFCOMM and device pairing guide*. Google Developers. https://developer.android.com/develop/connectivity/bluetooth
- Android Developers. (2024). *BiometricPrompt API reference*. Google Developers. https://developer.android.com/reference/androidx/biometric/BiometricPrompt
- JetBrains. (2024). *Kotlin Multiplatform overview and shared code architecture*. JetBrains s.r.o. https://kotlinlang.org/docs/multiplatform.html
- Martin, R. C. (2018). *Clean Architecture: A craftsman's guide to software structure and design*. Prentice Hall.
