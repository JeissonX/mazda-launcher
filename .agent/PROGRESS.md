# Mazda OS - Seguimiento de Progreso

Proyecto de launcher automotriz de alto rendimiento para **Mazda 2 DE (2013)** sobre pantalla Revo (4GB RAM / 64GB ROM).
Directivas maestras: Edición quirúrgica, trazabilidad, cero dependencias infladas, prioridad absoluta a rendimiento y 60 FPS estables.

## Hitos de Desarrollo

- [x] Hito 1: Descarga de referencias e independización de repositorio Git.
- [x] Hito 2: Purga de servicios FM y desacoplamiento de radio analógica.
- [ ] Hito 3: Dock inferior persistente estilo Tesla y tarjeta de estado Mazda 2 DE.
- [ ] Hito 4: Módulo de mantenimiento post-overhaul (meta 2.000 km) y alertas TTS.
- [ ] Hito 5: Pipeline de compilación automática en GitHub Actions.

## Resumen de Cambios Recientes (Hitos 1 y 2)

- **Referencias Aisladas**: Repositorios de referencia `.ref_cleanlauncher` y `.ref_tlauncher` clonados en carpetas de solo lectura en `C:\Users\Jeisson\Documents\AGENCIA`.
- **Independización del Repositorio**: Inicialización de repositorio Git limpio (`main`) en `mazda-launcher` sin historial previo.
- **Identidad del Sistema**: `applicationId` configurado en `com.mazda2.launcher` y nombre visible como `"Mazda OS"`. Refactorización de paquetes Java a `com.mazda2.launcher`.
- **Purga de Bloatware FM**:
  - `RadioActivity` eliminada de `AndroidManifest.xml` y del árbol de código fuente.
  - `RadioClient` y `DialView` extirpados.
  - Polling periódico de frecuencia FM (`radioPoll`) que consultaba la MCU cada 1000ms desactivado en `MainActivity`.
  - Conexión Binder con `com.nwd.radio.service` eliminada tanto en `MainActivity` como en `GreeterService`.
  - Reemplazo del widget FM en pantalla principal por tarjeta de reloj/fecha/estado con escala vertical equilibrada.
  - Dock inferior actualizado: acceso directo a música local / reproductor en lugar del sintonizador FM analógico.
- **Prioridad Multimedia**:
  - `NowPlaying` configurado con prioridad absoluta para almacenamiento local (USB / memoria interna) y sesiones del sistema (`MediaSessionManager` / `NotificationListenerService` para CarPlay y servicios de streaming).
  - Curva de ecualización en `EqAuto` fijada permanentemente a curva Hi-Fi (`MUSIC`) optimizada para USB y Bluetooth.
