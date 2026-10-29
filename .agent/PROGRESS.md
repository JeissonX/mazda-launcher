# Mazda OS - Seguimiento de Progreso

Proyecto de launcher automotriz de alto rendimiento para **Mazda 2 DE (2013)** sobre pantalla Revo (4GB RAM / 64GB ROM).
Directivas maestras: Edición quirúrgica, trazabilidad, cero dependencias infladas, prioridad absoluta a rendimiento y 60 FPS estables.

## Hitos de Desarrollo

- [x] Hito 1: Descarga de referencias e independización de repositorio Git.
- [x] Hito 2: Purga de servicios FM y desacoplamiento de radio analógica.
- [x] Hito 3: Dock inferior persistente estilo Tesla y tarjeta de estado Mazda 2 DE.
- [ ] Hito 4: Módulo de mantenimiento post-overhaul (meta 2.000 km) y alertas TTS.
- [ ] Hito 5: Pipeline de compilación automática en GitHub Actions.

## Resumen de Cambios Recientes (Hito 3)

- **Dock Inferior Persistente (Estilo Tesla)**:
  - Barra de navegación fija inferior con altura táctil ergonómica de 64dp (`buildDock`) y fondo grafito oscuro (`#0D0D11`).
  - Mapeo exacto de los 5 accesos directos principales con touch targets de 56dp x 56dp y microinteracciones fluidas (`pressable`):
    1. **CarPlay / Zlink**: Disparo prioritario de paquetes de enlace telefónico (`com.zjinnova.zlink`, `com.tlink`, `com.zjinnova.zlink5`, AutoKit, fallback inteligente).
    2. **Música Local / USB**: Acceso directo al reproductor local de medios y almacenamiento USB con control de sesiones.
    3. **Cajón de Aplicaciones**: Lanzador limpio y libre de bloatware (`AppsActivity`).
    4. **Ajustes / EQ**: Acceso directo al ecualizador Hi-Fi paramétrico de 10 bandas y pad de balance/fader (`EqActivity`).
    5. **Pantalla Apagada**: Disparo nativo de display-off por hardware (`ScreenOffActivity`) conservando audio y mandos de timón.

- **Tarjeta de Vehículo Mazda 2 DE (Panel Lateral Izquierdo ~35%)**:
  - Maquetación ergonómica de tarjeta lateral izquierda (`buildVehicleCard`) con ponderación `weight(0.35f)`.
  - Silueta vectorial dedicada y precisa del Mazda 2 Hatchback chasis DE (`res/drawable/mazda2.xml` y `res/drawable/mazda2_lights.xml`).
  - Velocímetro digital centralizado con animación fluida a 60 FPS e interpolación continua (`ValueAnimator` con `DecelerateInterpolator`), eliminando fluctuaciones y saltos bruscos entre lecturas de GPS.
  - Indicador visual telltale de modo noche/luces activadas (`ic_lights.xml`) y crossfade suave de 600ms entre la silueta normal y la silueta con faros encendidos conectado al broadcast `com.nwd.ACTION_ILL_STATE_CHANGE`.
  - Pastilla de estado de odómetro/aceite vinculada a `OilActivity`.

- **Contenedor Multimedia USB y Acceso Rápido CarPlay (Panel Derecho ~65%)**:
  - Contenedor multimedia para USB y almacenamiento local (`buildMusicCard`) con visualización de carátula, metadatos ID3 y controles táctiles de reproducción (Prev, Play/Pause con acento ámbar, Next).
  - Tarjeta de enlace rápido a CarPlay / Android Auto (`buildCarPlayCard`) con botón táctil directo.
  - Tarjeta companion de reloj digital (`HH:mm`), fecha completa en español y estado de clima en tiempo real (`buildClockCard`).

- **Paleta de Color y Estilo Mazda 2**:
  - Creación de `res/values/colors.xml` con acentos ámbar/naranja (#FF6A00 / #E65100 / #FFA726) y gris grafito profundo (#121212 / #1E1E1E / #16161A), armonizando con el cuadro de instrumentos nocturno del Mazda 2.
  - Actualización de `Ui.java` con tokens de color, fondos de gradiente grafito con resplandor ámbar radial sutil y botones de acción primarios en ámbar Mazda.

## Resumen de Cambios Anteriores (Hitos 1 y 2)

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
