# Mazda OS (Launcher Automotriz)

Launcher Android nativo de ultra alto rendimiento (60 FPS estables) a medida para **Mazda 2 DE (2013)** sobre pantalla Revo (4GB RAM / 64GB ROM).

Construido con enfoque quirúrgico sin bloatware ni dependencias infladas, reemplazando el launcher de fábrica con una interfaz ultra fluida, rápida y optimizada para uso vehicular.

## Características

- **Pantalla principal**: Reloj grande de alta legibilidad, fecha, clima en tiempo real y tarjeta de reproducción multimedia.
- **Reproducción Multimedia Priorizada**: Soporte y prioridad para almacenamiento local (USB / memoria interna), streaming y CarPlay vía MediaSession y NotificationListenerService.
- **Sin Bloatware FM**: Purga total del sintonizador de radio analógica, servicios de escaneo y polling Binder MCU en segundo plano para garantizar cero consumo fantasma de RAM y máxima fluidez.
- **Bluetooth nativo**: Teléfono (marcador, contactos) y audio Bluetooth A2DP/AVRCP.
- **Overlay de volumen propio**: HUD minimalista y reactivo.
- **Modo pantalla apagada**: Mantiene la operación del sistema respondiendo a controles de volante.
- **Módulo de mantenimiento**: Control de odómetro y seguimiento post-overhaul con alertas visuales y por voz.
- **Dock automotriz**: Acceso rápido y persistente a mapas, música, Bluetooth, aplicaciones, apagado de pantalla y ajustes.

## Compilación

No usa Gradle: [`build.sh`](build.sh) invoca directamente el toolchain nativo de Android (`aapt2`, `javac`, `d8`, `zipalign` y `apksigner`) garantizando compilaciones instantáneas y sin overhead.

```sh
sh build.sh            # Genera build/MazdaLauncher.apk
sh build.sh install    # Compila e instala vía ADB
```
