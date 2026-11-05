# Matriz de Pruebas de Calidad (QA) - Práctica 4

## Ficha de Información de la Práctica
- **Institución:** Instituto Politécnico Nacional (IPN)
- **Unidad Académica:** Escuela Superior de Cómputo (ESCOM)
- **Materia:** Desarrollo de Aplicaciones Móviles Nativas
- **Profesor:** M. en C. Gabriel Hurtado Avilés
- **Grupo:** 7CV4 | **Ciclo Escolar:** 2027-1
- **Alumno:**
  - **Aragón Martínez Manuel Alejandro** (Boleta: 2023630411)
- **Tecnología Implementada:** Kotlin Multiplatform (KMP) con Kotlin 2.2.0, Jetpack Compose y Clean Architecture.

---

## Matriz de 6 Casos de Prueba Estructurados

| # | Categoría de Prueba | Módulo Evaluado | Precondición | Pasos de Ejecución | Resultado Esperado | Resultado Obtenido | Evidencia Visual | Estado |
|---|---|---|---|---|---|---|---|---|
| **CP-01** | **Ruta Feliz (Happy Path)** | Gestor de Archivos (`parte1`) | App instalada y ejecutándose offline sin conexión | 1. Abrir la app.<br>2. Navegar a carpeta `Datos`.<br>3. Tocar `alumnos_escom.json`.<br>4. Abrir `configuracion_red.xml`. | Se abre el visor modal con el contenido formateado y coloreado por sintaxis (claves, strings, números, tags y atributos en distintos colores legibles). | El visor tokeniza el archivo y aplica los estilos de sintaxis con botones funcionales de copiar y compartir. | `capturas/p1_03_visor_json_highlight.png`<br>`capturas/p1_04_visor_xml_highlight.png` | **EXITOSO** |
| **CP-02** | **Condición Límite / Alterna (Edge Case & Resiliencia)** | Cyber Breach (`parte2`) | Almacenamiento local con partida corrupta intencional o vacía (0 bytes) | 1. Intentar cargar un archivo corrupto o con matriz incompleta.<br>2. Observar comportamiento del motor. | La aplicación no debe cerrarse de forma inesperada (crashear); debe mostrar un mensaje descriptivo de error en Snackbar y conservar intactas las demás partidas válidas. | `GameSerializationService` valida las 64 celdas y secciones obligatorias; notifica error legible sin afectar el catálogo. | Registro de error en consola de terminal y Snackbar | **EXITOSO** |
| **CP-03** | **Regresión** | Gestor de Archivos (`parte1`) | Archivos existentes en el espacio privado | 1. Marcar un archivo como favorito con la estrella.<br>2. Aplicar filtro "Favoritos".<br>3. Cerrar y reabrir la app.<br>4. Navegar entre carpetas usando los Breadcrumbs. | El estado de favorito persiste en SharedPreferences/almacenamiento; los breadcrumbs actualizan la pila de navegación y el botón Atrás regresa a la carpeta padre. | La persistencia de favoritos y recientes se mantiene al 100%; la navegación por breadcrumbs funciona fluidamente sin regresiones. | `capturas/p1_01_explorador_guinda.png`<br>`capturas/p1_02_explorador_azul_oscuro.png` | **EXITOSO** |
| **CP-04** | **Navegación y Ciclo de Vida** | Cyber Breach (`parte2`) | Partida activa en curso con puntuación y turnos ejecutados | 1. Iniciar modo Infiltración o Contrarreloj.<br>2. Realizar impactos en nodos.<br>3. Presionar botón Home o forzar detención de la app.<br>4. Reabrir la aplicación. | La sesión activa se restaura automáticamente con la cuadrícula exacta, energía acumulada, puntuación, tiempo y nodos descubiertos. | La app lee `active_session.json` al inicio y restaura el estado completo mostrando notificación de reanudación. | Reanudación automática demostrada en sesión activa | **EXITOSO** |
| **CP-05** | **Accesibilidad y Visualización** | Gestor de Archivos (`parte1`) | Dispositivo en modo vertical/horizontal con tema Guinda IPN o Azul ESCOM | 1. Cambiar tema a Guinda IPN y luego a Azul ESCOM.<br>2. Alternar tema oscuro/claro del sistema.<br>3. Consultar metadatos y realizar operaciones de archivo. | La interfaz adapta sus contrastes dinámicamente según Material 3; el visor de metadatos y diálogos respetan áreas táctiles y contraste. | Los temas respetan la identidad politécnica; diálogos accesibles con botones claros de acción destructiva. | `capturas/p1_01_explorador_guinda.png`<br>`capturas/p1_05_dialogos_crud_metadatos.png` | **EXITOSO** |
| **CP-06** | **Compatibilidad y Multi-Formato** | Cyber Breach (`parte2`) | Partida terminada con victoria o derrota | 1. Guardar la partida en formato TXT.<br>2. Guardarla en formato XML.<br>3. Guardarla en formato JSON.<br>4. Probar cargar cada una desde el gestor.<br>5. Abrir el visor de código crudo y compartir mediante selector del sistema. | Los tres formatos deserializan fielmente las 64 celdas, energía, historial y metadatos; el diálogo de compartir invoca el selector de Android/iOS. | Se verifica la interoperabilidad completa en .txt, .xml y .json con exportación vía FileProvider. | Serializadores y visores de archivo crudo operativos | **EXITOSO** |

---

## Verificación de Calidad y Quality Gates Locales
1. **Compilación KMP:** Ambos proyectos compilan con éxito (`BUILD SUCCESSFUL`) en modo Debug.
2. **Offline 100%:** Funcionamiento probado en modo avión sin solicitudes de red.
3. **Seguridad y Scoped Storage:** Operaciones acotadas a `filesDir` y Storage Access Framework sin requerir permisos invasivos de almacenamiento global (`MANAGE_EXTERNAL_STORAGE`).
