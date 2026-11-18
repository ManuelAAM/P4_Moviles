# feat(p6): integrate hardware sensors, biometric vault, Bluetooth RFCOMM P2P multiplayer and proxy relay across KMP apps

## 📌 Student & Course Metadata
- **Institution:** Instituto Politécnico Nacional (IPN) — Escuela Superior de Cómputo (ESCOM)
- **Course:** Desarrollo de Aplicaciones Móviles Nativas (`Grupo 7CV4` · `Semestre 2027-1`)
- **Professor:** M. en C. Gabriel Hurtado Avilés
- **Author:** Aragón Martínez Manuel Alejandro (Boleta: `2023630411`)
- **Source Branch:** `feature/practica-6-sensores-bluetooth`
- **Target Branch:** `main`

---

## 🎯 Problem & Motivation
Practice 6 requires extending both Kotlin Multiplatform (KMP) applications developed in Practice 4 (`parte1_gestor_archivos` and `parte2_cyber_breach`) with physical hardware sensors, Android 12+ runtime Bluetooth permissions, offline P2P communication over Bluetooth Classic RFCOMM, cryptographic transfer verification (SHA-256), and a Bluetooth Web Proxy Relay with strict SSRF protection, while preserving institutional IPN Guinda and ESCOM Blue themes and clean Compose UI across varied screen densities.

---

## 🛠️ Summary of Changes

### Part 1: Multiplatform File Manager (`parte1_gestor_archivos`)
- **Hardware Sensor Telemetry & Tilt-to-Pan:** Implemented `AndroidSensorManager` with lifecycle safety (`onPause`/`onResume`) and a spacious 2-row 2D bubble level HUD (`FileViewerSheet.kt`) with pitch/roll/pan offset telemetry.
- **Biometric Protected Vault:** Integrated native `BiometricPrompt` authentication to unlock confidential storage inside the app sandbox.
- **Binary Bluetooth RFCOMM File Transfer:** Added `readFileBytes` across repositories/ViewModel, metadata handshake, explicit acceptance modal, 2–4 KB Base64 chunking into `temp_transfers/`, SHA-256 verification prior to promotion into `received_files/`, transfer history log, and native system Bluetooth sharing (`Intent.ACTION_SEND`).
- **Offline Web Proxy Relay (SSRF Guarded):** Implemented Bluetooth web relay dialog (`WebProxyDialog.kt`) with preset chips (`Posts`, `Hora CDMX`, `PokeAPI`) and strict blocking of private IP/loopback addresses.
- **Runtime Permissions & UI Refinements:** Added Android 12+ (`BLUETOOTH_CONNECT`, `BLUETOOTH_SCAN`, `BLUETOOTH_ADVERTISE`) runtime permission prompt in `MainActivity.kt` and eliminated clipped chips/buttons.

### Part 2: Cyber Breach Video Game (`parte2_cyber_breach`)
- **Bluetooth 1v1 P2P Multiplayer (`AndroidBluetoothGameService`):** Implemented Host (`BluetoothServerSocket`) and Client (`BluetoothSocket`) RFCOMM synchronization on dedicated UUID `e0cbf06c-cd8b-4647-bb8a-263b43f0f974` with sequential packets (`seq`), turn locking banners, and async IO writes.
- **Runtime Permissions & Lobby Controls:** Added runtime Android 12+ Bluetooth permission prompts in `MainActivity.kt`, plus quick `Permisos BT` and `Ajustes Bluetooth` buttons inside `BluetoothLobbyDialog.kt`.
- **Optional Smooth Gyroscope Aiming & Calibrated Kinetic Shake:**
  - Replaced noisy angular velocity mapping with an inertial low-pass gravity vector filter ($\alpha = 0.12$) + 180 ms hysteresis debounce in `AndroidGameSensorManager.kt`.
  - Added optional `[Activar Giroscopio]` / `[Giroscopio: ACTIVO]` toggle in `CyberBreachViewModel.kt` and `CyberBreachScreen.kt` so players can switch seamlessly between touch and tilt aiming.
  - Raised kinetic shake recharge threshold to $3.2\text{ G}$ (1500 ms cooldown) to eliminate false triggers.
- **Layout & Scroll Fixes:** Added `verticalScroll` to `ModeSelectionDialog` in `TitleMenuScreen.kt` so all 3 game modes display without truncation, and redesigned the bottom sensor controls card to prevent vertical text wrapping.

---

## 🧪 How It Was Tested (Physical Hardware Validation)
- **Physical Test Pair:** **Samsung Galaxy A54** (`Android 14+`) ↔ **HONOR Magic 7 Pro** (`MagicOS / Android 14+`).
- **Tested Scenarios:** Live bidirectional Bluetooth Classic RFCOMM gameplay (Host vs Client turn locking, defense grid updates, combat console sync), optional gyroscope aiming & touch switch video test, biometric vault unlock, binary JPG Bluetooth transfer, and offline Web Proxy Relay with private IP SSRF blocking.
- **Declared Platform Scope (per Practice 6 Compatibility Matrix):** Android–Android classic RFCOMM validated physically on both handsets; Android–iOS cross-transport explicitly marked **UNVERIFIED** because iOS restricts open SPP/RFCOMM to MFi accessories and requires BLE GATT (`CoreBluetooth`) bridging.

---

## 📸 Visual Evidence

### Part 1: Multiplatform File Manager (Samsung Galaxy A54)

| Feature Verified | Physical Screenshot | Technical Description |
| :--- | :---: | :--- |
| **Tilt-to-Pan 2D HUD (Image Viewer)** | <img src="https://raw.githubusercontent.com/ManuelAAM/P4_Moviles/feature/practica-6-sensores-bluetooth/docs/capturas/parte1/02_visor_imagen_inclinacion_hud.jpg" width="200" alt="Tilt HUD" /> | Live 2D bubble level, green `[ACTIVO]` badge, and dynamic pitch/roll panning telemetry. |
| **Biometric Vault Challenge & Unlock** | <img src="https://raw.githubusercontent.com/ManuelAAM/P4_Moviles/feature/practica-6-sensores-bluetooth/docs/capturas/parte1/boveda_protegida_acceso_autorizado_archivos.jpg" width="200" alt="Vault Auth" /> | Native `BiometricPrompt` granting authorized access to protected storage. |
| **Protected Vault Contents** | <img src="https://raw.githubusercontent.com/ManuelAAM/P4_Moviles/feature/practica-6-sensores-bluetooth/docs/capturas/parte1/boveda_protegida_archivos.jpg" width="200" alt="Vault Files" /> | Confidential files isolated in app-private sandbox after biometric verification. |
| **Bluetooth Transfer & Compatibility** | <img src="https://raw.githubusercontent.com/ManuelAAM/P4_Moviles/feature/practica-6-sensores-bluetooth/docs/capturas/parte1/03_bluetooth_seleccion_archivo_iphone_incompatible.jpg" width="200" alt="BT Selection" /> | Clean selection chips (`Imagen`, `Archivo 1`, `Archivo 2`) and peer compatibility feedback. |
| **Incoming Bluetooth Reception Modal** | <img src="https://raw.githubusercontent.com/ManuelAAM/P4_Moviles/feature/practica-6-sensores-bluetooth/docs/capturas/parte1/05_bluetooth_recepcion_archivos.jpg" width="200" alt="BT Receive" /> | Explicit recipient acceptance dialog displaying file metadata before streaming. |
| **Transfer History & SHA-256 Match** | <img src="https://raw.githubusercontent.com/ManuelAAM/P4_Moviles/feature/practica-6-sensores-bluetooth/docs/capturas/parte1/05_bluetooth_historial_completado.jpg" width="200" alt="BT History" /> | Completed transfer audit log with file size, timestamp, and `EXITOSO` SHA-256 verification. |
| **Web Proxy Relay Preset Menu** | <img src="https://raw.githubusercontent.com/ManuelAAM/P4_Moviles/feature/practica-6-sensores-bluetooth/docs/capturas/parte1/06_web_proxy_relay_respuesta_menu.jpg" width="200" alt="Proxy Menu" /> | Bluetooth proxy relay query screen with clean preset chips (`Posts`, `Hora CDMX`, `PokeAPI`). |
| **Web Proxy Relay (JSON Posts)** | <img src="https://raw.githubusercontent.com/ManuelAAM/P4_Moviles/feature/practica-6-sensores-bluetooth/docs/capturas/parte1/06_web_proxy_relay_respuesta_posts.jpg" width="200" alt="Proxy Posts" /> | Offline client rendering REST JSON response forwarded through paired proxy host. |
| **Web Proxy Relay (PokeAPI)** | <img src="https://raw.githubusercontent.com/ManuelAAM/P4_Moviles/feature/practica-6-sensores-bluetooth/docs/capturas/parte1/06_web_proxy_relay_respuesta_pokeapi.jpg" width="200" alt="Proxy PokeAPI" /> | HTTP 200 OK public API payload received over Bluetooth RFCOMM. |
| **SSRF Mitigation (Private IP Blocked)** | <img src="https://raw.githubusercontent.com/ManuelAAM/P4_Moviles/feature/practica-6-sensores-bluetooth/docs/capturas/parte1/06_web_proxy_relay_respuesta_ipprivada_prohibida.jpg" width="200" alt="SSRF Blocked" /> | Gateway strictly blocks private network / loopback SSRF requests. |

---

### Part 2: Cyber Breach — Comparative Host (Samsung A54) vs Client (HONOR Magic 7 Pro)

| Match Stage | Host (Samsung Galaxy A54) | Client (HONOR Magic 7 Pro) | Practice 6 Requirement Verified |
| :--- | :---: | :---: | :--- |
| **1. Bluetooth P2P Lobby & Pairing** | <img src="https://raw.githubusercontent.com/ManuelAAM/P4_Moviles/feature/practica-6-sensores-bluetooth/docs/capturas/parte2/01_bluetooth_lobby_host_a54.jpg" width="200" alt="Host Lobby" /> | <img src="https://raw.githubusercontent.com/ManuelAAM/P4_Moviles/feature/practica-6-sensores-bluetooth/docs/capturas/parte2/02_bluetooth_lobby_cliente_honor.jpg" width="200" alt="Client Lobby" /> | Host binds `CyberBreachP2P` server socket (`¡Conectado con HONOR Magic7 Pro!`); Client grants runtime permissions, lists paired devices, and joins `A54 de Edna`. |
| **2. Synchronized Turn Lock** | <img src="https://raw.githubusercontent.com/ManuelAAM/P4_Moviles/feature/practica-6-sensores-bluetooth/docs/capturas/parte2/03_conexion_exitosa_host_a54.jpg" width="200" alt="Host Turn" /> | <img src="https://raw.githubusercontent.com/ManuelAAM/P4_Moviles/feature/practica-6-sensores-bluetooth/docs/capturas/parte2/04_conexion_exitosa_cliente_honor.jpg" width="200" alt="Client Wait" /> | Strict turn lock enforced via Bluetooth handshake: Host receives green `¡TU TURNO DE ATACAR!`, Client locks input showing `⏳ TURNO DEL CONTRINCANTE`. |
| **3. Live Defense Grid & Console Sync** | <img src="https://raw.githubusercontent.com/ManuelAAM/P4_Moviles/feature/practica-6-sensores-bluetooth/docs/capturas/parte2/05_red_defensa_host_a54.jpg" width="200" alt="Host Defense" /> | <img src="https://raw.githubusercontent.com/ManuelAAM/P4_Moviles/feature/practica-6-sensores-bluetooth/docs/capturas/parte2/06_red_defensa_cliente_honor.jpg" width="200" alt="Client Defense" /> | Sequential packet (`seq`) combat exchange synchronized across both defense grids (`H1`, `C3`, `G3`) and battle console logs. |

🎥 **Sensors & Gameplay Video Evidence (Samsung Galaxy A54):**
- [Watch `07_video_sensores_combate_cyber_breach.mp4` on GitHub](https://github.com/ManuelAAM/P4_Moviles/blob/feature/practica-6-sensores-bluetooth/docs/capturas/parte2/07_video_sensores_combate_cyber_breach.mp4) *(demonstrates toggling optional low-pass filtered gyroscope aiming, firing `Disparar Mira`, switching back to touch control, and scoring a direct hit on node `E3`).*

---

## ✅ QA & Review Checklist
- [x] `:androidApp:assembleDebug` compiles cleanly (`BUILD SUCCESSFUL`) for both `parte1_gestor_archivos` and `parte2_cyber_breach`.
- [x] Physical **Android ↔ Android** RFCOMM multiplayer verified (`Samsung Galaxy A54` ↔ `HONOR Magic 7 Pro`).
- [x] Runtime Android 12+ (`BLUETOOTH_CONNECT`, `BLUETOOTH_SCAN`, `BLUETOOTH_ADVERTISE`) permissions handled cleanly.
- [x] Bilingual documentation (`README.md`, `README.en.md`), AI Context (`README_FOR_IAS.md`), and QA Matrix (`docs/pruebas.md`) updated.
