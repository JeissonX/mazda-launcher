# Mazda OS - Seguimiento de Progreso

Proyecto de launcher automotriz de alto rendimiento para **Mazda 2 DE (2013)** sobre pantalla Revo (4GB RAM / 64GB ROM).
Directivas maestras: Edición quirúrgica, trazabilidad, cero dependencias infladas, prioridad absoluta a rendimiento y 60 FPS estables.

## Hitos de Desarrollo

- [x] Hito 1: Descarga de referencias e independización de repositorio Git.
- [x] Hito 2: Purga de servicios FM y desacoplamiento de radio analógica.
- [x] Hito 3: Dock inferior persistente estilo Tesla y tarjeta de estado Mazda 2 DE.
- [x] Hito 4: Módulo de mantenimiento configurable (ciclo 5.000 km) y tarjetería visual sin TTS.
- [x] Hito 5: Pipeline de compilación automática en GitHub Actions.

## Cierre de Fase e Integración Continua (CI/CD)

- **Artefacto Compilado**: `build/MazdaOS.apk` (identificador en GitHub Actions: `MazdaOS-APK`).
- **Canal de Despliegue y Compilación**:
  - Repositorio remoto: GitHub (`origin/main`).
  - Pipeline automatizado mediante `.github/workflows/build.yml` ejecutado en runner `ubuntu-latest` con JDK 17 (`temurin`) y Android SDK `build-tools;33.0.2` + `platform-tools`.
  - Firma de depuración lista para instalación directa en hardware automotriz Revo (4GB/64GB, Android 7.1 NWD).
  - Trazabilidad total de la base de código libre de bloatware, con 24 clases limpias bajo `com.mazda2.launcher`.

## Resumen de Cambios Recientes (Hito 5)

- **Auditoría y Endurecimiento de `build.sh`**:
  - Salida de APK normalizada a `MazdaOS.apk` (y `MazdaOS-compartir.apk` para variante de distribución limpia).
  - Verificación de rutas de entrada y compilación limpia de las 24 clases activas bajo el paquete `com.mazda2.launcher` sin referencias a módulos obsoletos (FM, TextToSpeech).
  - Compatibilidad de build tools configurada por defecto a `build-tools;33.0.2` alineada con la plataforma `android-33` y retrocompatible con `minSdkVersion 25` (Android 7.1 Revo / NWD).
  - Firma automática de depuración garantizada mediante generación desatendida de `debug.keystore` (RSA 2048-bit, validez 10.000 días) y firma dual v1/v2/v3 con `zipalign` y `apksigner` para instalación directa sin restricciones en la unidad central.

- **Pipeline de CI en GitHub Actions (`.github/workflows/build.yml`)**:
  - Ejecución en runner `ubuntu-latest` ante eventos `push` a la rama `main` y ejecución manual desatendida mediante `workflow_dispatch`.
  - Configuración de JDK 17 headless (`actions/setup-java@v4`, distribución `temurin`).
  - Provisión de herramientas Android SDK oficiales (`android-actions/setup-android@v3`): `cmdline-tools;latest`, `platform-tools`, `platforms;android-33` y `build-tools;33.0.2`.
  - Ejecución del script de empaquetado: `chmod +x build.sh && ./build.sh` con variable de entorno `BUILD_TOOLS: "33.0.2"`.
  - Publicación del artefacto compilado (`actions/upload-artifact@v4`) con identificador `MazdaOS-APK`, ruta `build/MazdaOS.apk`, retención garantizada de 30 días y control de error estricto si el binario no es generado (`if-no-files-found: error`).

## Resumen de Cambios Anteriores (Hitos 1 a 4)

- **Hito 4 (Mantenimiento 5.000 km, Pastillas Visuales y Toggle Pico y Placa)**:
  - Purga total de `TextToSpeech` y `MediaPlayer` en `GreeterService.java`, y eliminación de `Speech.java`.
  - Tarjetería visual informativa con saludo contextual dinámico según la hora y estado del vehículo en tiempo real ("● Todo en orden" / aviso de mantenimiento).
  - Módulo de mantenimiento parametrizable en `OilActivity.java` y `Odometer.java` con ciclo estándar de 5.000 km, barra gráfica de vida del aceite y botón de reseteo rápido.
  - Toggle booleano `pico_placa_enabled` en `Profile.java` desactivado por defecto (Barrancabermeja), suprimiendo cómputo y contenedores en pantalla principal.
- **Hito 3 (Dock Tesla & Tarjeta Mazda 2 DE)**:
  - Dock inferior persistente de 64dp de altura con 5 accesos directos: CarPlay/Zlink, Música local/USB, Cajón de aplicaciones, Ecualizador Hi-Fi y Pantalla apagada.
  - Tarjeta lateral izquierda (~35%) con silueta vectorial del Mazda 2 Hatchback DE (`mazda2.xml` y `mazda2_lights.xml`), velocímetro digital centralizado con interpolación suave a 60 FPS y telltale de luces conmutado por señal ILL.
  - Contenedor multimedia dedicado para USB y tarjeta de conexión rápida para CarPlay.
  - Paleta ámbar (#FF6A00 / #E65100) y gris grafito profundo (#121212 / #1E1E1E) en `colors.xml` y `Ui.java`.
- **Hitos 1 y 2 (Base Limpia y Purga FM)**:
  - Desvinculación de repositorios de referencia `.ref_cleanlauncher` y `.ref_tlauncher`.
  - Independización de Git, paquete configurado en `com.mazda2.launcher` y nombre visible "Mazda OS".
  - Purga total de radio FM analógica (`RadioActivity`, `RadioClient`, `DialView`, polling MCU de 1000ms y conexión Binder a `com.nwd.radio.service`).
  - Prioridad de reproducción fijada a USB / medios locales y ecualización Hi-Fi predeterminada.
