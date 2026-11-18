# README for AI Agents & Machine Interpreters
## Mobile Application Development — Practice 6: Mobile Device Sensors & Bluetooth Communication
**Institution:** Instituto Politécnico Nacional (IPN) — Escuela Superior de Cómputo (ESCOM)  
**Academic Term:** 2027-1 | **Course:** Desarrollo de Aplicaciones Móviles Nativas  
**Professor:** M. en C. Gabriel Hurtado Avilés  
**Student:** Aragón Martínez Manuel Alejandro (Boleta: 2023630411)  

---

## 1. Architectural Blueprint & KMP Strategy

This repository implements a **Kotlin Multiplatform (KMP)** native application ecosystem partitioned into two independent sub-projects:
1. `parte1_gestor_archivos`: Multiplatform File Manager extended with hardware sensor telemetry, tilt image panning, biometric vault security, Bluetooth RFCOMM chunked file transfers (TXT, JSON, XML, JPG) with SHA-256 integrity, and an offline Web Proxy Relay.
2. `parte2_cyber_breach`: Cyber Breach (Net Strike) tactical turn-based grid game (8x8) extended with Bluetooth 1v1 synchronized peer-to-peer multiplayer, gyroscope reticle tilt aiming, and kinetic shake recharge with cooldown.

### Technology Stack & Compilation Target
- **Kotlin Language Version:** 2.2.0 (JVM target 17 / 24 compatible)
- **UI Framework:** Jetpack Compose Multiplatform (Compose BOM 2024.11.00)
- **Android Target:** minSdk 24 (Android 7.0 Nougat), targetSdk 35 (Android 15)
- **iOS Target:** iOS 15.0+ (via Kotlin Multiplatform Native Apple Frameworks: `iosArm64`, `iosX64`, `iosSimulatorArm64`)
- **Coroutines & Serialization:** `kotlinx.coroutines:1.9.0`, `kotlinx.serialization:1.7.3`, `kotlinx.datetime:0.6.1`
- **Security:** `androidx.biometric:biometric:1.1.0` (AndroidX BiometricPrompt API)

---

## 2. Sensor Engineering & Lifecycle Safety

### Accelerometer and Gyroscope Telemetry
- **Interface:** `ISensorManager` / `IGameSensorManager` in `commonMain`.
- **Platform Implementation:** `AndroidSensorManager` & `AndroidGameSensorManager` in `androidMain`.
- **Telemetry Tuple:**
  $$\text{Accelerometer} = (a_x, a_y, a_z) \quad [m/s^2]$$
  $$\text{Gyroscope} = (\omega_x, \omega_y, \omega_z) \quad [rad/s]$$
- **Shake Detection Algorithm:**
  $$g_{\text{force}} = \frac{\sqrt{a_x^2 + a_y^2 + a_z^2}}{g_{\text{earth}}}$$
  Triggers a shake event when $g_{\text{force}} > 2.2$ with an enforced debouncing window of $\Delta t \ge 800\text{ ms}$.
- **Tilt-to-Pan (File Viewer) & Aiming Reticle (Cyber Breach):**
  Continuous mapping of pitch ($\theta$) and roll ($\phi$) angles to 2D coordinate displacement $(\Delta x, \Delta y)$ and grid cells $(r, c) \in [0, 7] \times [0, 7]$.
- **Energy Conservation & Lifecycle Rule:**
  Sensors MUST NOT run when the activity is backgrounded.
  - `onPause()`: Invokes `sensorManager.unregisterListener(this)` and transitions status to `PAUSED`.
  - `onResume()`: Re-registers listeners with `SENSOR_DELAY_UI` or `SENSOR_DELAY_GAME`.

---

## 3. Bluetooth RFCOMM Communication Protocol

### Service UUIDs
- **File Transfer & Web Proxy Relay (Part 1):** `fa87c0d0-afac-11de-8a39-0800200c9a66`
- **Cyber Breach Multiplayer (Part 2):** `e0cbf06c-cd8b-4647-bb8a-263b43f0f974`

### Packet Framing & Transfer State Machine
Communications run over raw RFCOMM Bluetooth sockets using framed JSON packets delimited by newline (`\n`):
1. **Metadata Handshake (`FileTransferMetadataPacket`):**
   - Transmits `fileName`, `fileSizeBytes`, `sha256Expected`, `mimeType`, and `totalChunks`.
   - Requires explicit recipient modal confirmation (`ACCEPT` / `REJECT`).
2. **Chunked Streaming (`FileChunkPacket`):**
   - Chunk payload: 4,096 bytes (4 KB) Base64 encoded.
   - Sequential Stop-and-Wait ACK (`TransferAckPacket`): Client acknowledges `chunkIndex`.
   - Staging: Data is written incrementally into a private cache `.tmp` file.
3. **Cryptographic Integrity Verification:**
   - Upon receiving all chunks, the receiver calculates:
     $$\text{SHA-256}(\text{receivedBytes}) \stackrel{?}{=} \text{sha256Expected}$$
   - Only on exact match is the file atomically moved/renamed to the target directory. If integrity fails, the `.tmp` staging file is purged immediately.

---

## 4. Web Proxy Relay Protocol (Exercise 3)

### Topology
- **Device A (Gateway):** Connected to Cellular/Wi-Fi + Bluetooth.
- **Device B (Client/Viewer):** In Flight Mode (Wi-Fi/Data OFF) + Bluetooth ON.

### Security Guarantees
- **Whitelist Enforcement:** Only pre-approved public HTTPS domains are allowed:
  - `jsonplaceholder.typicode.com`
  - `pokeapi.co`
  - `worldtimeapi.org`
  - `api.github.com/zen`
- **SSRF Mitigation:** Requests containing private IP ranges (RFC 1918: `10.0.0.0/8`, `172.16.0.0/12`, `192.168.0.0/16`), `localhost`, `127.0.0.1`, or cloud metadata endpoints (`169.254.169.254`) are intercepted and rejected by the Gateway.
- **Local Persistent Cache:** Device B stores all fetched HTTP responses in a local key-value store with timestamping. If Bluetooth disconnects, the viewer seamlessly falls back to cached records.

---

## 5. Cyber Breach 1v1 Bluetooth Architecture

- **State Machine:** Deterministic turn alternating between Host and Client:
  - Host initiates turn: strikes cell $\to$ sends `CyberBreachPacket(type = ATTACK, seq, cell)`.
  - Client processes damage on local defense grid $\to$ sends `CyberBreachPacket(type = ATTACK_RESULT, hitResult, isGameOver)`.
  - Turn flips to Client.
- **Duplicate Packet Rejection:** Each packet increments `seq`. Packets with $\text{seq} \le \text{lastReceivedSeq}$ are discarded.
- **Hardware Integration:**
  - Tilt Reticle Aiming: Gyroscope tilt maps to 8x8 cell coordinates.
  - Kinetic Recharge: Device shake injects $+5\text{ EN}$ with a strict 15-second cooldown timer.

---

## 6. AI Development & Assistance Disclosure
In accordance with ethical AI standards, the scaffolding, multiplatform abstractions, RFCOMM packet serialization schemas, and QA matrix were developed with AI co-engineering assistance (Antigravity Agent by Google DeepMind), rigorously tested and verified through local Gradle builds and hardware telemetry validation.
