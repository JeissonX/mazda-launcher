# Mazda OS - Seguimiento de Progreso

Proyecto de launcher automotriz de alto rendimiento para **Mazda 2 DE (2013)** sobre pantalla Revo (4GB RAM / 64GB ROM).
Directivas maestras: Edición quirúrgica, trazabilidad, cero dependencias infladas, prioridad absoluta a rendimiento y 60 FPS estables.

## Hitos de Desarrollo

- [x] Hito 1: Descarga de referencias e independización de repositorio Git.
- [x] Hito 2: Purga de servicios FM y desacoplamiento de radio analógica.
- [x] Hito 3: Dock inferior persistente estilo Tesla y tarjeta de estado Mazda 2 DE.
- [x] Hito 4: Módulo de mantenimiento configurable (ciclo 5.000 km) y tarjetería visual sin TTS.
- [x] Hito 5: Pipeline de compilación automática en GitHub Actions.
- [x] Hito 6: Rediseño visual moderno Mazda 2 DE, dock Tesla y tarjetería premium.
- [x] Hito 7: Adopción de foto real car_mazda2.png y colorimetría Tesla / CarPlay (Cyber Blue & Tesla Red).

## Adopción de Foto Real y Colorimetría Cyber Blue & Tesla Red (Hito 7)

- **Foto Real Mazda 2 DE (`car_mazda2.png`)**:
  - Sustitución de los vectores sintéticos obsoletos (`mazda2.xml` y `mazda2_lights.xml` eliminados de la base de código).
  - Carga predeterminada del activo de alta definición `car_mazda2.png` en `res/drawable-nodpi/` con proporción natural, `ScaleType.FIT_CENTER` y `setAdjustViewBounds(true)`.
  - Integración armónica de la sombra de piso original sobre la tarjetería grafito; desacoplamiento de animaciones de faros en la imagen estática preservando el telltale de tablero `ic_lights` conmutado por la línea ILL.
  - Adaptación de `Profile.bundledCar` y `build.sh` para soportar la imagen rasterizada.

- **Reestructuración de Colorimetría (Tesla OS & CarPlay)**:
  - **Paleta de Acento**: Transición integral de ámbar/naranja hacia **Cyber Blue** (`#0084FF`), **Ice Glow Neon** (`#00E5FF`) y **Tesla Red** (`#E82127`).
  - **Superficies y Fondos**: Fondos estructurados en Deep Carbon (`#0D0E12`), Slate Dark (`#15161C`) y tarjetas Steel Gray (`#181920`) con trazo translúcido azulado (`0x220084FF`) y micro-glow radial atenuado Ice Blue (`0x1200E5FF`).
  - **Tarjeta de Vehículo y Velocímetro**:
    - Dígito numérico del velocímetro digital en blanco nítido automotriz `#F5F5F7`.
    - Etiqueta "KM/H" en contraste discreto `0x99A0AEC0`.
    - Pastilla de aceite en azul CarPlay translúcido (`0x260084FF`) y texto `#00AAFF`, conmutando a Tesla Red (`#E82127`) con fondo sólido (`0xCCE82127`) únicamente ante condición de alerta o proximidad de cambio (< 200 km).
  - **Contenedor Multimedia y Reproductor**:
    - Disco de vinilo `ic_vinyl.xml` con galleta central y acentos en Cyber Blue e Ice Blue.
    - Chip de fuente USB / Local en tinte azul (`0x220084FF`) con texto Cyber Blue.
    - Barra de ecualizador / progreso con degradado horizontal de Cyber Blue (`#0084FF`) a Ice Glow (`#00E5FF`).
    - Botón central Play/Pause circular en Cyber Blue con icono blanco y micro-relieve táctil.
  - **Tarjeta CarPlay**:
    - Botón "CONECTAR AHORA" en cápsula táctil con degradado Cyber Blue y tipografía blanca nítida.
    - Tinte de icono y distintivo en azul CarPlay.
  - **Dock Inferior Tesla**:
    - Píldoras táctiles ergonómicas con fondo translúcido Cyber Blue `0x0F0084FF` y borde sutil `0x220084FF`.
  - **Módulo de Mantenimiento (`OilActivity.java`)**:
    - Reemplazo de tonos naranja por Cyber Blue e Ice Glow para cabeceras y botón "Registrar cambio de aceite hoy".
    - Chips de intervalo de kilometraje activos en Cyber Blue con contorno Ice Glow.
    - Barra de progreso y estado en Cyber Blue para condición óptima, conmutando a Tesla Red (`#E82127`) cuando el kilometraje restante es crítico o está vencido.

## Rediseño Visual Moderno y Refinamiento UI/UX Automotriz (Hito 6)

- **Silueta Vectorial Mazda 2 DE (`mazda2.xml` y `mazda2_lights.xml`)**:
  - Sustitución del vector plano y anguloso por la silueta aerodinámica reconocible del Mazda 2 Hatchback (chasis DE).
  - Trazo curvo deportivo con caída pronunciada de baúl y alerón/spoiler superior extendido.
  - Línea de cintura ascendente (kink característico del pilar C) y pasos de rueda abultados con llantas de aleación en estrella de 5 puntas.
  - En `mazda2_lights.xml`: proyectores rasgados iluminados con haz frontal en degradé cálido ámbar/xenón, pilotos traseros LED rojo rubí y repetidor ámbar en el espejo retrovisor.

- **Profundidad y Tarjetería Premium (`Ui.java`)**:
  - Fondos de tarjeta estructurados con degradado grafito de alta fidelidad (`#1C1C24` superior a `#121216` inferior) y esquinas redondeadas a 20dp.
  - Borde sutil semi-translúcido de 1dp en `0x1FFFFFFF` combinado con micro-resplandor radial ámbar en esquina de acento (`0x14FF6A00`).
  - Creación del helper `capsuleButton(Context, text, filled, action)` con acolchado generoso (12dp x 24dp), tipografía negrita y micro-relieve táctil a escala 0.94f.
  - Botones circulares con degradado sutil de ámbar Mazda y trazo de bisel para óptima ergonomía de conducción.

- **Contenedor Multimedia Equilibrado (`MainActivity.java`)**:
  - Eliminación total del vacío negro en reposo: nueva carátula por defecto de disco de vinilo estilizado (`ic_vinyl.xml`) con surcos concéntricos y galleta central en ámbar Mazda.
  - Reorganización espacial: contenedor de carátula de 86dp x 86dp con marco redondeado, título "Música Local / USB", subtítulo "Lista para reproducir" y barra decorativa horizontal simulando nivel de ecualizador / progreso.
  - Controles multimedia centrados y amplios: botón circular central Play/Pause en ámbar de 56dp flanqueado por botones Prev y Next de 46dp.

- **Tarjeta CarPlay / Enlace Telefónico (`MainActivity.java`)**:
  - Rediseño con fondo en degradado grafito con tinte azul profundo (`#181F2C` a `#0E1118`) y micro-glow radial cyan/azul.
  - Sustitución del botón de línea plana por una cápsula táctil sólida y prominente "CONECTAR AHORA" en ámbar Mazda.

- **Dock Inferior Estilo Tesla (`MainActivity.java`)**:
  - Encapsulación de cada uno de los 5 accesos directos dentro de una píldora táctil individual de 54dp x 44dp con radio de 22dp, fondo traslúcido `0x0DFFFFFF` y borde sutil `0x1AFFFFFF`.
  - Dispersión armónica sobre barra flotante grafito con respuesta háptica y micro-animación de escala 0.90f al tacto.

- **Condición Climática y Solar (`Weather.java` y `MainActivity.java`)**:
  - Ventana solar diurna de 06:00 a 18:00 horas sincronizada: icono solar (`ic_w_sun`) con tinte dorado cálido (`#FFFFD25A`) y estado textual "Día despejado".
  - Clima térmico base por defecto realista para Barrancabermeja (~30°C) con carga instantánea sin estados vacíos de `--°`.

- **Confirmación de Compilación en Verde en GitHub Actions (Run #41060170149)**:
  - Commit: `dde2cde` (`fix(ui): centralizar background de carPlayCard e importar drawables en MainActivity`).
  - Estado: `success` en 42s.
  - Artefacto generado: `MazdaOS-APK` (2.36 MB, `build/MazdaOS.apk`, retención 30 días).
  - URL de ejecución: `https://github.com/JeissonX/mazda-launcher/actions/runs/41060170149`.

## Cierre de Fase e Integración Continua (CI/CD)

- **Artefacto Compilado**: `build/MazdaOS.apk` (identificador en GitHub Actions: `MazdaOS-APK`).
- **Canal de Despliegue y Compilación**:
  - Repositorio remoto: GitHub (`origin/main`).
  - Pipeline automatizado mediante `.github/workflows/build.yml` ejecutado en runner `ubuntu-latest` con JDK 17 (`temurin`) y Android SDK `build-tools;33.0.2` + `platform-tools`.
  - Firma de depuración lista para instalación directa en hardware automotriz Revo (4GB/64GB, Android 7.1 NWD).
  - Trazabilidad total de la base de código libre de bloatware, con 24 clases limpias bajo `com.mazda2.launcher`.

## Resumen de Cambios Anteriores (Hitos 1 a 5)

- **Hito 5 (Pipeline CI/CD y Hotfix de Compilación)**:
  - Generación de `MazdaOS.apk` con firma debug y zipalign automático.
  - Corrección de llamadas táctiles estáticas hacia `Ui.pressable(...)` y actualización del runner de GitHub Actions a JDK 17 setup-java@v5.
- **Hito 4 (Mantenimiento 5.000 km, Pastillas Visuales y Toggle Pico y Placa)**:
  - Purga total de `TextToSpeech` y `MediaPlayer` en `GreeterService.java`, y eliminación de `Speech.java`.
  - Tarjetería visual informativa con saludo contextual dinámico según la hora y estado del vehículo en tiempo real ("● Todo en orden" / aviso de mantenimiento).
  - Módulo de mantenimiento parametrizable en `OilActivity.java` y `Odometer.java` con ciclo estándar de 5.000 km, barra gráfica de vida del aceite y botón de reseteo rápido.
  - Toggle booleano `pico_placa_enabled` en `Profile.java` desactivado por defecto (Barrancabermeja), suprimiendo cómputo y contenedores en pantalla principal.
- **Hito 3 (Dock Tesla & Tarjeta Mazda 2 DE)**:
  - Dock inferior persistente de 64dp de altura con 5 accesos directos: CarPlay/Zlink, Música local/USB, Cajón de aplicaciones, Ecualizador Hi-Fi y Pantalla apagada.
  - Tarjeta lateral izquierda (~35%) con silueta vectorial del Mazda 2 Hatchback DE, velocímetro digital centralizado con interpolación suave a 60 FPS y telltale de luces conmutado por señal ILL.
- **Hitos 1 y 2 (Base Limpia y Purga FM)**:
  - Desvinculación de repositorios de referencia y purga de radio analógica FM.
