# Mazda OS - Seguimiento de Progreso

Proyecto de launcher automotriz de alto rendimiento para **Mazda 2 DE (2013)** sobre pantalla Revo (4GB RAM / 64GB ROM).
Directivas maestras: Edición quirúrgica, trazabilidad, cero dependencias infladas, prioridad absoluta a rendimiento y 60 FPS estables.

## Hitos de Desarrollo

- [x] Hito 1: Descarga de referencias e independización de repositorio Git.
- [x] Hito 2: Purga de servicios FM y desacoplamiento de radio analógica.
- [x] Hito 3: Dock inferior persistente estilo Tesla y tarjeta de estado Mazda 2 DE.
- [x] Hito 4: Módulo de mantenimiento configurable (ciclo 5.000 km) y tarjetería visual sin TTS.
- [ ] Hito 5: Pipeline de compilación automática en GitHub Actions.

## Resumen de Cambios Recientes (Hito 4)

- **Purga Total de Síntesis de Voz (TTS) y Clips de Audio**:
  - `TextToSpeech` y `MediaPlayer` eliminados por completo de `GreeterService.java`.
  - Archivo `Speech.java` extirpado del repositorio (cero bloatware, cero consumo residual de RAM/CPU).
  - `GreeterService.java` refactorizado como servicio ultra liviano enfocado exclusivamente en enrutamiento de audio hardware y ecualización automática ante eventos de ACC y Bluetooth.
  - Cero interferencias en los hilos de audio y cero latencia al encender el vehículo.

- **Tarjetería Visual Contextual Sutil en `MainActivity.java`**:
  - Sustitución de anuncios de voz por pastilla/tarjeta visual informativa en la cabecera del panel derecho (`buildClockCard`).
  - Mensaje contextual visual dependiente de la hora ("Buen día", "Buenas tardes", "Buenas noches") con saludo personalizado según el perfil del conductor.
  - Indicador dinámico de estado del vehículo en tiempo real ("● Todo en orden" en verde `#00E676`, aviso de mantenimiento preventivo si aplica).

- **Módulo de Mantenimiento Ajustable (Ciclo Estándar 5.000 km)**:
  - Configuración post-overhaul (rodaje completado físicamente) con ciclo estándar de 5.000 km en `Odometer.java` y `OilActivity.java`.
  - Persistencia en `SharedPreferences` con claves normalizadas: `last_oil_change_km`, `oil_interval_km` (editable: 5.000, 7.500, 10.000 km; por defecto 5.000 km) y `current_odometer_km`.
  - Botón de acción rápida: "Registrar cambio de aceite hoy" (resetea el contador inmediatamente al kilometraje actual con feedback Toast).
  - Tarjeta ilustrativa en `OilActivity.java`:
    * Barra de progreso horizontal gráfica limpia con porcentaje dinámico de vida del aceite.
    * Métricas explícitas: "Recorridos con este aceite: X km" / "Restantes: Y km para próximo cambio".
    * Semáforo de estados de color: Verde/Ámbar durante uso normal; Naranja/Rojo (`#D32F2F`) si restan menos de 200 km o si el ciclo está vencido.
  - Sincronización dinámica de la pastilla de mantenimiento en la tarjeta de vehículo de `MainActivity.java`, abriendo `OilActivity` al tacto.

- **Toggle Opcional de Pico y Placa (Barrancabermeja)**:
  - Incorporación de clave `pico_placa_enabled` en `Profile.java` con valor por defecto `false` (vehículo opera en Barrancabermeja sin restricción vehicular).
  - Switch táctil en `SetupActivity.java` para activar/desactivar la alerta voluntariamente.
  - Si `pico_placa_enabled == false`, se suprimen completamente el cálculo, los textos y los contenedores de Pico y Placa en `MainActivity.java`, ahorrando cómputo de CPU y manteniendo la tarjeta de reloj/clima despejada y legible.

## Resumen de Cambios Anteriores (Hitos 1 a 3)

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
