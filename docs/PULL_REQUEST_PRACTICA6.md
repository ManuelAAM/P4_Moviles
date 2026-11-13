# Pull Request: Práctica 6 - Sensores Hardware y Comunicación Bluetooth P2P Multiplataforma (KMP)

## 📌 Metadatos del Alumno y Asignación
- **Institución:** Instituto Politécnico Nacional (IPN)
- **Unidad Académica:** Escuela Superior de Cómputo (ESCOM)
- **Materia:** Desarrollo de Aplicaciones Móviles Nativas
- **Profesor:** M. en C. Gabriel Hurtado Avilés
- **Grupo:** 7CV4 | **Semestre:** 2027-1
- **Alumno Evaluado:**
  - **Aragón Martínez Manuel Alejandro** (Boleta: 2023630411)
- **Rama Origen:** `feature/practica-6-sensores-bluetooth`
- **Rama Destino:** `main` (o rama base del repositorio)

---

## 📋 Resumen del Pull Request

Este Pull Request implementa la **Práctica 6** extendiendo los desarrollos previos de la Práctica 4 en arquitectura multiplataforma **Kotlin Multiplatform (KMP)** con **Jetpack Compose** y soporte nativo Android/iOS. Se integran capacidades avanzadas de sensores de hardware y comunicación inalámbrica por proximidad mediante sockets Bluetooth RFCOMM:

### Parte 1: Gestor de Archivos Multiplataforma
1. **Telemetría y Control de Sensores:** Muestreo en vivo de Acelerómetro ($m/s^2$) y Giroscopio ($rad/s$) desacoplado con control estricto de ciclo de vida (`onPause`/`onResume`) para ahorro de energía.
2. **Visor de Imágenes con Tilt-to-Pan Dinámico:** Desplazamiento panorámico inercial guiado por inclinación del dispositivo con HUD 2D de burbuja de nivel, Pitch, Roll y offsets instantáneos.
3. **Bóveda Protegida y Autenticación Biométrica:** Protección de almacenamiento confidencial mediante `BiometricPrompt` nativo (huella dactilar/PIN).
4. **Transferencia Segura de Archivos por Bluetooth:**
   - Handshake con intercambio previo de metadatos (ID, nombre lógico, tipo MIME, tamaño y hash SHA-256).
   - Diálogo modal con aceptación explícita en el receptor antes de transferir datos.
   - Envío por bloques (chunking) en Base64 con reporte de progreso porcentual.
   - Almacenamiento seguro en carpeta temporal privada (`temp_transfers/`) previo a la validación matemática SHA-256.
   - Promoción a carpeta pública del gestor (`received_files/`) e historial persistente de transferencias.
   - Soporte adicional para compartir vía Bluetooth del sistema a PCs/laptops y modo didáctico QA.
5. **Proxy Relay Web vía Bluetooth (Gateway de Conectividad):** Consulta a APIs REST públicas desde un cliente aislado vía enlace RFCOMM Bluetooth con mitigación estricta de SSRF contra IPs privadas.

### Parte 2: Videojuego Cyber Breach
1. **Ciberduelo Multijugador Bluetooth 1v1:** Protocolo P2P con arquitectura Host/Cliente sobre sockets RFCOMM para partidas sincronizadas por turnos.
2. **Control Cinético por Giroscopio:** Selección y apuntado en cuadrícula táctica mediante inclinación física.
3. **Mecánica de Recarga Cinética por Agitación:** Detección de agitación vigorosa mediante acelerómetro con debouncing y temporizador de enfriamiento.
4. **Respuesta Háptica:** Retroalimentación táctil programada para impactos, colisiones y eventos críticos.

---

## 📸 Evidencias de Pruebas Físicas (Samsung Galaxy A54)

| Prueba / Funcionalidad | Evidencia Fotográfica | Descripción del Comportamiento |
| :--- | :---: | :--- |
| **Giroscopio (Tilt-to-Pan HUD)** | <img src="docs/capturas/parte1/02_visor_imagen_inclinacion_hud.jpg" width="200" /> | Visor de imagen con telemetría en vivo, burbuja de nivel 2D, badge `[ACTIVO]` en verde y cálculo en tiempo real de Pitch, Roll y Offset de paneo al inclinar el teléfono. |
| **Bóveda Segura & Biometría (Acceso)** | <img src="docs/capturas/parte1/boveda_protegida_acceso_autorizado_archivos.jpg" width="200" /> | Desbloqueo exitoso de la bóveda mediante autenticación biométrica (`BiometricPrompt` con huella dactilar/PIN). |
| **Bóveda Segura (Archivos Protegidos)** | <img src="docs/capturas/parte1/boveda_protegida_archivos.jpg" width="200" /> | Exploración autorizada de archivos confidenciales resguardados en almacenamiento privado con cifrado en memoria. |
| **Transferencia Bluetooth & Compatibilidad** | <img src="docs/capturas/parte1/03_bluetooth_seleccion_archivo_iphone_incompatible.jpg" width="200" /> | Menú con chips limpios `[Imagen]`, `[Archivo 1]`, `[Archivo 2]`, validación de dispositivos detectados y reporte de compatibilidad de pares. |
| **Recepción de Archivos por Bluetooth** | <img src="docs/capturas/parte1/05_bluetooth_recepcion_archivos.jpg" width="200" /> | Solicitud entrante con nombre de archivo, metadatos y diálogo modal de aceptación explícita previo a la transmisión. |
| **Historial de Transferencias & SHA-256** | <img src="docs/capturas/parte1/05_bluetooth_historial_completado.jpg" width="200" /> | Registro histórico con archivo transferido, tamaño, estado `EXITOSO`, marca de tiempo y verificación íntegra del hash SHA-256. |
| **Proxy Relay Web (Menú de Selección)** | <img src="docs/capturas/parte1/06_web_proxy_relay_respuesta_menu.jpg" width="200" /> | Diálogo de consulta web vía Bluetooth proxy relay con chips de presets (`Posts`, `Hora CDMX`, `PokeAPI`). |
| **Proxy Relay Web (Respuesta Posts JSON)** | <img src="docs/capturas/parte1/06_web_proxy_relay_respuesta_posts.jpg" width="200" /> | Recepción de payload JSON desde endpoint REST a través del salto proxy Bluetooth sin conexión directa a internet. |
| **Proxy Relay Web (Respuesta PokeAPI)** | <img src="docs/capturas/parte1/06_web_proxy_relay_respuesta_pokeapi.jpg" width="200" /> | Respuesta HTTP 200 OK con payload de API pública retransmitido íntegramente por el enlace Bluetooth local. |
| **Mitigación SSRF (IP Privada Prohibida)** | <img src="docs/capturas/parte1/06_web_proxy_relay_respuesta_ipprivada_prohibida.jpg" width="200" /> | Mecanismo de seguridad que rechaza de forma estricta peticiones dirigidas a IPs privadas o locales (prevención SSRF). |

---

## 🧪 Matriz de Calidad y Verificación (QA Gates)
- [x] **Compilación Limpia:** `:androidApp:assembleDebug` finalizado con éxito (**BUILD SUCCESSFUL**).
- [x] **Multiplataforma:** Implementaciones `expect`/`actual` compatibles con Android (API 24+) e iOS (iOS 15+).
- [x] **Paleta Institucional:** Cumplimiento de colores institucionales Guinda IPN (`#6A1B31` / `#800020`) y Azul ESCOM (`#003366` / `#006699`).
- [x] **Seguridad y Criptografía:** Verificación estricta de hash SHA-256 y mitigación SSRF en proxy relay.
- [x] **Documentación Bilingüe:** Documentación completa en Español (`README.md`), Inglés (`README.en.md`) y Contexto para Agentes (`README_FOR_IAS.md`).
