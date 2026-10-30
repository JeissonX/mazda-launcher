package com.mazda2.launcher;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextClock;
import android.widget.TextView;

import java.util.Calendar;

public class MainActivity extends Activity implements NowPlaying.Listener, LocationListener {

    private static final String EQ = "com.nwd.audioset/.home_horizontalActivity";
    private static final String SETTINGS = "com.android.car.setting/.home_horizontalActivity";

    private static final long WEATHER_EVERY_MS = 30 * 60 * 1000;

    private final Handler handler = new Handler();
    private NowPlaying nowPlaying;
    private LocationManager lm;

    // Saludo visual y estado del vehículo (sin síntesis TTS)
    private TextView greetingText, vehicleStatusText;
    private GradientDrawable vehicleStatusBg;

    // Velocímetro digital por GPS con interpolación suave
    private TextView speedView, speedUnitView, gpsBadge;
    private float displayedSpeed = 0f;
    private ValueAnimator speedAnimator;
    private Location lastGpsLocation;
    private boolean hasGpsFix = false;

    // Tarjeta del vehículo Mazda 2 DE y luces
    private ImageView car, carLights, lightsTelltale;
    private boolean hasLightsImage;
    private TextView oilInfo;
    private GradientDrawable oilBg;

    // Reloj y Clima
    private TextView weatherTemp, weatherDesc;
    private ImageView weatherIcon;
    private long lastWeatherFetch;

    // Contenedor multimedia para USB / Local
    private ImageView art, playBtn;
    private TextView songSource, songTitle, songArtist;

    // Perfil y Tema
    private boolean createdDark;
    private String createdProfile;

    private String profileStamp() {
        return Profile.name(this) + "|" + Profile.carPhotoFile(this).lastModified();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(12), dp(10), dp(12), 0);
        root.setBackground(Ui.background(this));

        LinearLayout cards = new LinearLayout(this);
        cards.setOrientation(LinearLayout.HORIZONTAL);

        // 1. Panel lateral izquierdo (~35% del ancho): Tarjeta de Vehículo Mazda 2 DE
        cards.addView(buildVehicleCard(), weight(0.35f, 0));

        // 2. Panel derecho (~65% del ancho): Companion (CarPlay + Reloj) y Contenedor Multimedia USB
        LinearLayout rightPanel = new LinearLayout(this);
        rightPanel.setOrientation(LinearLayout.VERTICAL);

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.addView(buildClockCard(), weight(1f, 0));
        topRow.addView(buildCarPlayCard(), weight(1.15f, dp(10)));
        rightPanel.addView(topRow, new LinearLayout.LayoutParams(-1, 0, 1f));

        LinearLayout.LayoutParams mlp = new LinearLayout.LayoutParams(-1, 0, 1.25f);
        mlp.topMargin = dp(10);
        rightPanel.addView(buildMusicCard(), mlp);

        cards.addView(rightPanel, weight(0.65f, dp(12)));
        root.addView(cards, new LinearLayout.LayoutParams(-1, 0, 1f));

        // 3. Dock inferior persistente estilo Tesla (64dp)
        root.addView(buildDock(), new LinearLayout.LayoutParams(-1, dp(64)));
        setContentView(root);

        nowPlaying = NowPlaying.get(this);
        lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        createdDark = Ui.isDark(this);
        createdProfile = profileStamp();

        showWeather(Weather.cached(this));

        if (!Profile.isSetupDone(this) && Profile.bundledCar(this, false) == 0) {
            startActivity(new Intent(this, SetupActivity.class));
        }
        if (Profile.isNwd(this)) startService(new Intent(this, VolumeOverlay.class));
        startService(new Intent(this, GreeterService.class));
        startService(new Intent(this, Odometer.class));
    }

    // ---------- Tarjeta de Vehículo Mazda 2 DE (~35% ancho) ----------

    private View buildVehicleCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(14), dp(18), dp(12));
        card.setBackground(Ui.card(this));

        // 1. Header con modelo, indicador de luces nocturnas y estado GPS
        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);

        TextView modelBadge = Ui.text(this, "MAZDA 2", 15, Color.WHITE);
        modelBadge.setTypeface(Typeface.DEFAULT_BOLD);
        modelBadge.setLetterSpacing(0.14f);
        header.addView(modelBadge, new LinearLayout.LayoutParams(0, -2, 1f));

        lightsTelltale = new ImageView(this);
        lightsTelltale.setImageResource(R.drawable.ic_lights);
        lightsTelltale.setColorFilter(carLightsOn() ? 0xFF00E676 : 0x40FFFFFF);
        lightsTelltale.setAlpha(carLightsOn() ? 1f : 0.35f);
        header.addView(lightsTelltale, new LinearLayout.LayoutParams(dp(24), dp(24)));

        gpsBadge = Ui.text(this, "GPS", 11, hasGpsFix ? 0xFF00E676 : 0x66FFFFFF);
        gpsBadge.setTypeface(Typeface.DEFAULT_BOLD);
        gpsBadge.setPadding(dp(10), 0, 0, 0);
        header.addView(gpsBadge);
        card.addView(header);

        // 2. Velocímetro digital centralizado con animación fluida mediante interpolación
        LinearLayout speedBox = new LinearLayout(this);
        speedBox.setOrientation(LinearLayout.VERTICAL);
        speedBox.setGravity(Gravity.CENTER);
        speedBox.setPadding(0, dp(4), 0, dp(2));

        speedView = new TextView(this);
        speedView.setText(String.valueOf(Math.round(displayedSpeed)));
        speedView.setTextSize(54);
        speedView.setTextColor(Ui.COLOR_AMBER);
        speedView.setTypeface(Typeface.DEFAULT_BOLD);
        speedView.setIncludeFontPadding(false);
        speedView.setGravity(Gravity.CENTER);
        speedBox.addView(speedView);

        speedUnitView = Ui.text(this, "KM/H", 12, Ui.COLOR_AMBER_LIGHT);
        speedUnitView.setTypeface(Typeface.DEFAULT_BOLD);
        speedUnitView.setLetterSpacing(0.18f);
        speedBox.addView(speedUnitView);
        card.addView(speedBox);

        // 3. Silueta del Mazda 2 Hatchback (chasis DE)
        FrameLayout carBox = new FrameLayout(this);
        car = new ImageView(this);
        car.setScaleType(ImageView.ScaleType.FIT_CENTER);
        carLights = new ImageView(this);
        carLights.setScaleType(ImageView.ScaleType.FIT_CENTER);
        carLights.setAlpha(0f);

        android.graphics.Bitmap own = Profile.carPhoto(this);
        int bundled = Profile.bundledCar(this, false), bundledLights = Profile.bundledCar(this, true);
        if (own != null) {
            car.setImageBitmap(own);
        } else if (bundled != 0) {
            car.setImageResource(bundled);
            if (bundledLights != 0) {
                carLights.setImageResource(bundledLights);
                hasLightsImage = true;
            }
        } else {
            car.setImageResource(R.drawable.mazda2);
            carLights.setImageResource(R.drawable.mazda2_lights);
            hasLightsImage = true;
        }
        carBox.addView(car, new FrameLayout.LayoutParams(-1, -1));
        carBox.addView(carLights, new FrameLayout.LayoutParams(-1, -1));
        carBox.setOnClickListener(v -> startActivity(new Intent(this, SetupActivity.class)));
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(-1, 0, 1f);
        clp.setMargins(dp(8), dp(2), dp(8), dp(2));
        card.addView(carBox, clp);

        // 4. Indicador de mantenimiento / odómetro
        oilInfo = Ui.text(this, "", 12, Color.WHITE);
        oilInfo.setPadding(dp(14), dp(6), dp(14), dp(6));
        oilBg = new GradientDrawable();
        oilBg.setCornerRadius(dp(14));
        oilInfo.setBackground(oilBg);
        Ui.pressable(oilInfo, 0.92f);
        oilInfo.setOnClickListener(v -> startActivity(new Intent(this, OilActivity.class)));
        LinearLayout.LayoutParams olp = new LinearLayout.LayoutParams(-2, -2);
        olp.gravity = Gravity.CENTER_HORIZONTAL;
        olp.topMargin = dp(4);
        card.addView(oilInfo, olp);

        Ui.pressable(card, 0.98f);
        return card;
    }

    // ---------- Acceso Rápido a CarPlay (Zlink / Tlink) ----------

    private View buildCarPlayCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(14), dp(18), dp(14));

        float r = dp(20);
        GradientDrawable base = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{0xFF181F2C, 0xFF12151E, 0xFF0E1118});
        base.setCornerRadius(r);
        base.setStroke(dp(1), 0x264A6A94);

        GradientDrawable glow = new GradientDrawable();
        glow.setCornerRadius(r);
        glow.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        glow.setGradientRadius(dp(360));
        glow.setGradientCenter(0.9f, 0.1f);
        glow.setColors(new int[]{0x223B82F6, 0x003B82F6});

        card.setBackground(new LayerDrawable(new Drawable[]{base, glow}));

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        ImageView icon = new ImageView(this);
        icon.setImageResource(R.drawable.ic_carplay);
        icon.setColorFilter(0xFF60A5FA);
        top.addView(icon, new LinearLayout.LayoutParams(dp(26), dp(26)));

        TextView badge = Ui.text(this, "CARPLAY / ZLINK", 11, 0xFF93C5FD);
        badge.setTypeface(Typeface.DEFAULT_BOLD);
        badge.setLetterSpacing(0.12f);
        badge.setPadding(dp(8), 0, 0, 0);
        top.addView(badge);
        card.addView(top);

        TextView title = Ui.text(this, "Enlace Telefónico", 17, Color.WHITE);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setGravity(Gravity.START);
        title.setPadding(0, dp(6), 0, 0);
        card.addView(title);

        TextView sub = Ui.text(this, "Zlink · Tlink · CarPlay · Android Auto", 12, 0xB3FFFFFF);
        sub.setGravity(Gravity.START);
        sub.setPadding(0, dp(2), 0, dp(8));
        card.addView(sub);

        TextView btn = Ui.capsuleButton(this, "CONECTAR AHORA", true, this::openCarPlay);
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(-2, -2);
        blp.topMargin = dp(2);
        card.addView(btn, blp);

        Ui.pressable(card, 0.96f);
        card.setOnClickListener(v -> openCarPlay());
        return card;
    }

    // ---------- Reloj, Fecha y Clima (con Saludo Visual Contextual) ----------

    private View buildClockCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(18), dp(12), dp(18), dp(12));
        card.setBackground(Ui.card(this));

        // Cabecera informativa: Saludo contextual y estado breve del vehículo
        LinearLayout headerRow = new LinearLayout(this);
        headerRow.setGravity(Gravity.CENTER_VERTICAL);

        greetingText = Ui.text(this, "Bienvenido", 13, Ui.COLOR_AMBER);
        greetingText.setTypeface(Typeface.DEFAULT_BOLD);
        greetingText.setGravity(Gravity.START);
        headerRow.addView(greetingText, new LinearLayout.LayoutParams(0, -2, 1f));

        vehicleStatusText = Ui.text(this, "● Todo en orden", 11, 0xFF00E676);
        vehicleStatusText.setTypeface(Typeface.DEFAULT_BOLD);
        vehicleStatusText.setPadding(dp(8), dp(3), dp(8), dp(3));
        vehicleStatusBg = new GradientDrawable();
        vehicleStatusBg.setColor(0x2600E676);
        vehicleStatusBg.setCornerRadius(dp(10));
        vehicleStatusText.setBackground(vehicleStatusBg);
        headerRow.addView(vehicleStatusText);
        card.addView(headerRow);

        TextClock time = new TextClock(this);
        time.setFormat24Hour("HH:mm");
        time.setFormat12Hour("hh:mm");
        time.setTextSize(34);
        time.setTextColor(Color.WHITE);
        time.setTypeface(Typeface.DEFAULT_BOLD);
        time.setIncludeFontPadding(false);
        time.setGravity(Gravity.START);
        time.setPadding(0, dp(2), 0, 0);
        card.addView(time);

        TextClock date = new TextClock(this);
        date.setFormat24Hour("EEEE, d 'de' MMMM");
        date.setFormat12Hour("EEEE, d 'de' MMMM");
        date.setTextSize(13);
        date.setTextColor(0xB3FFFFFF);
        date.setPadding(0, dp(1), 0, dp(6));
        date.setGravity(Gravity.START);
        card.addView(date);

        LinearLayout wRow = new LinearLayout(this);
        wRow.setGravity(Gravity.CENTER_VERTICAL);
        weatherIcon = new ImageView(this);
        int curHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        boolean isDay = curHour >= 6 && curHour < 18;
        weatherIcon.setImageResource(isDay ? R.drawable.ic_w_sun : R.drawable.ic_w_moon);
        weatherIcon.setColorFilter(isDay ? 0xFFFFD25A : 0xFFDDE4FF);
        wRow.addView(weatherIcon, new LinearLayout.LayoutParams(dp(22), dp(22)));

        weatherTemp = Ui.text(this, "30°", 16, Color.WHITE);
        weatherTemp.setTypeface(Typeface.DEFAULT_BOLD);
        weatherTemp.setPadding(dp(6), 0, dp(6), 0);
        wRow.addView(weatherTemp);

        weatherDesc = Ui.text(this, isDay ? "Día despejado" : "Noche despejada", 12, 0xB3FFFFFF);
        wRow.addView(weatherDesc);
        card.addView(wRow);

        // Si Pico y Placa está activado opcionalmente, mostrar estado; si está desactivado, cero cálculo y cero contenedores
        if (Profile.picoPlacaEnabled(this)) {
            TextView picoView = Ui.text(this, "Pico y Placa activo", 11, Ui.COLOR_AMBER_LIGHT);
            picoView.setGravity(Gravity.START);
            picoView.setPadding(0, dp(4), 0, 0);
            card.addView(picoView);
        }

        Ui.pressable(card, 0.97f);
        card.setOnClickListener(v -> fetchWeather(true));
        return card;
    }

    // ---------- Contenedor Multimedia Equilibrado (USB y Almacenamiento Local) ----------

    private View buildMusicCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(14), dp(18), dp(12));
        card.setBackground(Ui.card(this));

        // Fila 1: Cabecera con Chip de Fuente y Badge de Calidad
        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);

        songSource = Ui.text(this, "USB / MÚSICA LOCAL", 11, Ui.COLOR_AMBER);
        songSource.setTypeface(Typeface.DEFAULT_BOLD);
        songSource.setLetterSpacing(0.12f);
        songSource.setPadding(dp(12), dp(4), dp(12), dp(4));
        GradientDrawable chip = new GradientDrawable();
        chip.setColor(0x28FF6A00);
        chip.setCornerRadius(dp(12));
        chip.setStroke(dp(1), 0x4DFF6A00);
        songSource.setBackground(chip);
        top.addView(songSource, new LinearLayout.LayoutParams(-2, -2));

        View spacer = new View(this);
        top.addView(spacer, new LinearLayout.LayoutParams(0, 1, 1f));

        TextView audioBadge = Ui.text(this, "HI-FI AUDIO", 10, 0x80FFFFFF);
        audioBadge.setTypeface(Typeface.DEFAULT_BOLD);
        audioBadge.setLetterSpacing(0.14f);
        top.addView(audioBadge);
        card.addView(top);

        // Fila 2: Carátula / Vinilo + Título, Subtítulo y Barra Decorativa Horizontal
        LinearLayout mid = new LinearLayout(this);
        mid.setOrientation(LinearLayout.HORIZONTAL);
        mid.setGravity(Gravity.CENTER_VERTICAL);
        mid.setPadding(0, dp(8), 0, dp(6));

        // Carátula elegante en reposo (disco de vinilo oscuro con centro ámbar)
        FrameLayout artBox = new FrameLayout(this);
        GradientDrawable artBg = new GradientDrawable();
        artBg.setColor(0x1A000000);
        artBg.setCornerRadius(dp(16));
        artBg.setStroke(dp(1), 0x22FFFFFF);
        artBox.setBackground(artBg);
        artBox.setClipToOutline(true);

        art = new ImageView(this);
        art.setScaleType(ImageView.ScaleType.FIT_CENTER);
        art.setImageResource(R.drawable.ic_vinyl);
        art.setAlpha(0.95f);
        artBox.addView(art, new FrameLayout.LayoutParams(-1, -1));
        mid.addView(artBox, new LinearLayout.LayoutParams(dp(86), dp(86)));

        LinearLayout trackInfo = new LinearLayout(this);
        trackInfo.setOrientation(LinearLayout.VERTICAL);
        trackInfo.setPadding(dp(16), 0, 0, 0);

        songTitle = Ui.text(this, "Música Local / USB", 18, Color.WHITE);
        songTitle.setTypeface(Typeface.DEFAULT_BOLD);
        songTitle.setGravity(Gravity.START);
        trackInfo.addView(songTitle);

        songArtist = Ui.text(this, "Lista para reproducir", 13, 0xB3FFFFFF);
        songArtist.setGravity(Gravity.START);
        songArtist.setPadding(0, dp(2), 0, dp(8));
        trackInfo.addView(songArtist);

        // Barra decorativa horizontal sutil que simula ecualizador / progreso
        FrameLayout progressBar = new FrameLayout(this);
        GradientDrawable trackD = new GradientDrawable();
        trackD.setColor(0x22FFFFFF);
        trackD.setCornerRadius(dp(2));
        progressBar.setBackground(trackD);

        View progressFill = new View(this);
        GradientDrawable fillD = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{Ui.COLOR_AMBER, Ui.COLOR_AMBER_LIGHT});
        fillD.setCornerRadius(dp(2));
        progressFill.setBackground(fillD);
        progressBar.addView(progressFill, new FrameLayout.LayoutParams(dp(120), -1));
        trackInfo.addView(progressBar, new LinearLayout.LayoutParams(-1, dp(4)));

        mid.addView(trackInfo, new LinearLayout.LayoutParams(0, -2, 1f));
        card.addView(mid, new LinearLayout.LayoutParams(-1, 0, 1f));

        // Fila 3: Controles multimedia centrados y ergonómicos (Prev, Play/Pause circular 56dp, Next)
        LinearLayout controls = new LinearLayout(this);
        controls.setGravity(Gravity.CENTER);
        controls.setPadding(0, dp(2), 0, dp(2));

        controls.addView(Ui.iconButton(this, R.drawable.ic_prev, 46, false, () -> nowPlaying.skip(false)),
                new LinearLayout.LayoutParams(dp(46), dp(46)));

        playBtn = Ui.iconButton(this, R.drawable.ic_play, 56, true, () -> nowPlaying.playPause());
        LinearLayout.LayoutParams plp = new LinearLayout.LayoutParams(dp(56), dp(56));
        plp.setMargins(dp(20), 0, dp(20), 0);
        controls.addView(playBtn, plp);

        controls.addView(Ui.iconButton(this, R.drawable.ic_next, 46, false, () -> nowPlaying.skip(true)),
                new LinearLayout.LayoutParams(dp(46), dp(46)));

        card.addView(controls);

        Ui.pressable(card, 0.98f);
        card.setOnClickListener(v -> nowPlaying.open(this));
        return card;
    }

    @Override
    public void onNowPlayingChanged() {
        NowPlaying.Source s = nowPlaying.current();
        NowPlaying.Track t = s == null ? null : nowPlaying.track(s);
        boolean empty = t == null || t.title.isEmpty();
        songSource.setText(s == null ? "MÚSICA LOCAL / USB" : nowPlaying.label(s).toUpperCase());
        songTitle.setText(empty ? "Música Local / USB" : t.title);
        songArtist.setText(empty ? "Lista para reproducir" : t.artist);
        playBtn.setImageResource(t != null && t.playing ? R.drawable.ic_pause : R.drawable.ic_play);

        if (t != null && t.art != null) {
            art.setPadding(0, 0, 0, 0);
            art.setScaleType(ImageView.ScaleType.CENTER_CROP);
            art.clearColorFilter();
            art.setAlpha(1f);
            art.setImageBitmap(t.art);
        } else {
            art.setPadding(0, 0, 0, 0);
            art.setScaleType(ImageView.ScaleType.FIT_CENTER);
            art.clearColorFilter();
            art.setAlpha(0.95f);
            art.setImageResource(R.drawable.ic_vinyl);
        }
    }

    // ---------- Dock Inferior Persistente (Estilo Tesla, 64dp) ----------

    private View buildDock() {
        LinearLayout dock = new LinearLayout(this);
        dock.setOrientation(LinearLayout.HORIZONTAL);
        dock.setGravity(Gravity.CENTER_VERTICAL);
        dock.setPadding(dp(16), 0, dp(16), 0);

        GradientDrawable dockBg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Ui.COLOR_GRAPHITE_DOCK, 0xFF08080A});
        dockBg.setStroke(dp(1), 0x1AFFFFFF);
        dock.setBackground(dockBg);

        // 1. [CarPlay / Zlink]
        dock.addView(dockItem(R.drawable.ic_carplay, this::openCarPlay), weight(1f, 0));
        // 2. [Música Local / USB]
        dock.addView(dockItem(R.drawable.ic_music, () -> nowPlaying.open(this)), weight(1f, 0));
        // 3. [Cajón de Aplicaciones]
        dock.addView(dockItem(R.drawable.ic_apps, () -> startActivity(new Intent(this, AppsActivity.class))), weight(1f, 0));
        // 4. [Ajustes / EQ]
        dock.addView(dockItem(R.drawable.ic_eq, this::openEqualizer), weight(1f, 0));
        // 5. [Pantalla Apagada]
        dock.addView(dockItem(R.drawable.ic_w_moon, () -> startActivity(new Intent(this, ScreenOffActivity.class))), weight(1f, 0));

        return dock;
    }

    private View dockItem(int icon, Runnable action) {
        FrameLayout slot = new FrameLayout(this);

        FrameLayout pill = new FrameLayout(this);
        GradientDrawable pillBg = new GradientDrawable();
        pillBg.setColor(0x0DFFFFFF);
        pillBg.setCornerRadius(dp(22));
        pillBg.setStroke(dp(1), 0x1AFFFFFF);
        pill.setBackground(pillBg);

        ImageView iv = new ImageView(this);
        iv.setImageResource(icon);
        iv.setColorFilter(0xF0FFFFFF);
        int pad = dp(11);
        iv.setPadding(pad, pad, pad, pad);
        pill.addView(iv, new FrameLayout.LayoutParams(-1, -1));

        FrameLayout.LayoutParams plp = new FrameLayout.LayoutParams(dp(54), dp(44), Gravity.CENTER);
        slot.addView(pill, plp);

        Ui.pressable(pill, 0.90f);
        pill.setOnClickListener(v -> action.run());
        return slot;
    }

    // ---------- Accesos directos e Intents ----------

    private void openCarPlay() {
        String[] carPlayPackages = {
            "com.zjinnova.zlink",
            "com.tlink",
            "com.zjinnova.zlink5",
            "cn.manstep.phonemirrorbox",
            "com.carletter.carletter"
        };
        for (String pkg : carPlayPackages) {
            Intent launch = getPackageManager().getLaunchIntentForPackage(pkg);
            if (launch != null && safeStart(launch)) return;
        }
        if (!launchByKeyword("zlink", "tlink", "carplay", "autokit", "phone link", "enlace")) {
            toast("CarPlay / Zlink no disponible");
        }
    }

    private void openEqualizer() {
        if (Profile.isNwd(this)) {
            startActivity(new Intent(this, EqActivity.class));
            return;
        }
        Intent eq = new Intent(android.media.audiofx.AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
        if (!safeStart(eq) && !launchByKeyword("ecualiz", "equaliz", "audio", "sonido", "dsp")) {
            startActivity(new Intent(this, EqActivity.class));
        }
    }

    private void openSettings() {
        if (Profile.isNwd(this)) Ui.launch(this, SETTINGS);
        else safeStart(new Intent(android.provider.Settings.ACTION_SETTINGS));
    }

    private boolean launchByKeyword(String... keywords) {
        PackageManager pm = getPackageManager();
        Intent main = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        for (android.content.pm.ResolveInfo ri : pm.queryIntentActivities(main, 0)) {
            if (ri.activityInfo.packageName.equals(getPackageName())) continue;
            String label = (ri.loadLabel(pm) + " " + ri.activityInfo.packageName).toLowerCase();
            for (String k : keywords) {
                if (label.contains(k)) {
                    Intent i = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
                            .setClassName(ri.activityInfo.packageName, ri.activityInfo.name);
                    return safeStart(i);
                }
            }
        }
        return false;
    }

    private boolean safeStart(Intent i) {
        try {
            startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void toast(String s) {
        android.widget.Toast.makeText(this, s, android.widget.Toast.LENGTH_SHORT).show();
    }

    // ---------- Velocímetro GPS e Interpolación ----------

    private void updateSpeed(float newSpeed) {
        if (speedAnimator != null && speedAnimator.isRunning()) {
            speedAnimator.cancel();
        }
        speedAnimator = ValueAnimator.ofFloat(displayedSpeed, newSpeed);
        speedAnimator.setDuration(700);
        speedAnimator.setInterpolator(new DecelerateInterpolator());
        speedAnimator.addUpdateListener(anim -> {
            displayedSpeed = (float) anim.getAnimatedValue();
            if (speedView != null) {
                speedView.setText(String.valueOf(Math.round(displayedSpeed)));
            }
        });
        speedAnimator.start();
    }

    @Override
    public void onLocationChanged(Location l) {
        if (l == null) return;
        hasGpsFix = true;
        if (gpsBadge != null) {
            gpsBadge.setTextColor(0xFF00E676);
            gpsBadge.setText("GPS FIX");
        }
        float speedKmh = 0f;
        if (l.hasSpeed()) {
            speedKmh = l.getSpeed() * 3.6f;
        } else if (lastGpsLocation != null && l.getTime() > lastGpsLocation.getTime()) {
            float dist = lastGpsLocation.distanceTo(l);
            float secs = (l.getTime() - lastGpsLocation.getTime()) / 1000f;
            if (secs > 0.2f) speedKmh = (dist / secs) * 3.6f;
        }
        lastGpsLocation = l;
        if (speedKmh < 2.5f) speedKmh = 0f;
        if (speedKmh > 220f) return;
        updateSpeed(speedKmh);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { }

    @Override
    public void onProviderEnabled(String provider) {
        hasGpsFix = false;
        if (gpsBadge != null) {
            gpsBadge.setTextColor(0x66FFFFFF);
            gpsBadge.setText("GPS");
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        hasGpsFix = false;
        if (gpsBadge != null) {
            gpsBadge.setTextColor(0x40FFFFFF);
            gpsBadge.setText("GPS OFF");
        }
        updateSpeed(0f);
    }

    // ---------- Luces del Carro e ILL Wire ----------

    private final android.content.BroadcastReceiver illReceiver = new android.content.BroadcastReceiver() {
        @Override
        public void onReceive(android.content.Context c, Intent i) {
            android.util.Log.d("CarLights", "ILL broadcast extras=" + i.getExtras());
            showCarLights(i.getByteExtra("extra_ill_state", (byte) 0) != 0);
            handler.postDelayed(() -> { if (Ui.isDark(MainActivity.this) != createdDark) recreate(); }, 1500);
        }
    };

    private boolean carLightsOn() {
        int state = android.provider.Settings.System.getInt(getContentResolver(), "mcu_no_source_device_state", 0);
        return (state & 2) != 0;
    }

    private void showCarLights(boolean on) {
        if (lightsTelltale != null) {
            lightsTelltale.animate().alpha(on ? 1f : 0.35f).setDuration(400).start();
            lightsTelltale.setColorFilter(on ? 0xFF00E676 : 0x40FFFFFF);
        }
        if (!hasLightsImage) return;
        if (carLights != null) carLights.animate().alpha(on ? 1f : 0f).setDuration(600).start();
        if (car != null) car.animate().alpha(on ? 0f : 1f).setDuration(600).start();
    }

    private void updateGreeting() {
        if (greetingText == null) return;
        int h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String salutation = (h < 12 ? "Buen día" : h < 19 ? "Buenas tardes" : "Buenas noches");
        String name = Profile.name(this);
        greetingText.setText(salutation + (name.isEmpty() ? "" : ", " + name));
    }

    private void updateVehicleStatus() {
        if (vehicleStatusText == null) return;
        if (!Odometer.isConfigured(this)) {
            vehicleStatusText.setText("● Odómetro listo");
            vehicleStatusText.setTextColor(0xB3FFFFFF);
            if (vehicleStatusBg != null) vehicleStatusBg.setColor(0x20FFFFFF);
            return;
        }
        int left = Odometer.remainingKm(this);
        if (left < 0) {
            vehicleStatusText.setText("⚠️ Aceite vencido");
            vehicleStatusText.setTextColor(0xFFD32F2F);
            if (vehicleStatusBg != null) vehicleStatusBg.setColor(0x33D32F2F);
        } else if (left <= Odometer.WARN_KM) {
            vehicleStatusText.setText("⚠️ Cambio en " + OilActivity.fmt(left) + " km");
            vehicleStatusText.setTextColor(0xFFFFA726);
            if (vehicleStatusBg != null) vehicleStatusBg.setColor(0x33FFA726);
        } else {
            vehicleStatusText.setText("● Todo en orden");
            vehicleStatusText.setTextColor(0xFF00E676);
            if (vehicleStatusBg != null) vehicleStatusBg.setColor(0x2600E676);
        }
    }

    private void updateOil() {
        if (oilInfo == null) return;
        if (!Odometer.isConfigured(this)) {
            oilInfo.setText("Aceite: configurar");
            oilBg.setColor(0x26FFFFFF);
            return;
        }
        int left = Odometer.remainingKm(this);
        int life = Odometer.lifePercent(this);
        if (left < 0) {
            oilInfo.setText("Aceite: ¡vencido (" + OilActivity.fmt(-left) + " km)!");
            oilBg.setColor(0xCCD32F2F);
        } else if (left <= Odometer.WARN_KM) {
            oilInfo.setText("Aceite: faltan " + OilActivity.fmt(left) + " km (" + life + "%)");
            oilBg.setColor(0xCCFF9800);
        } else {
            oilInfo.setText("Aceite: faltan " + OilActivity.fmt(left) + " km (" + life + "%)");
            oilBg.setColor(0x26FF6A00);
        }
    }

    // ---------- Clima ----------

    private void showWeather(Weather w) {
        if (w == null) return;
        if (weatherIcon != null) {
            weatherIcon.setImageResource(w.icon());
            weatherIcon.setColorFilter(w.icon() == R.drawable.ic_w_sun ? 0xFFFFD25A
                    : w.icon() == R.drawable.ic_w_moon ? 0xFFDDE4FF : Color.WHITE);
        }
        if (weatherTemp != null) weatherTemp.setText(w.temp + "°");
        if (weatherDesc != null) weatherDesc.setText(w.description());
    }

    private void fetchWeather(boolean force) {
        long now = SystemClock.elapsedRealtime();
        if (!force && lastWeatherFetch != 0 && now - lastWeatherFetch < WEATHER_EVERY_MS) return;
        lastWeatherFetch = now;
        Weather.fetch(this, this::showWeather);
    }

    private final Runnable periodic = new Runnable() {
        public void run() {
            if (Ui.isDark(MainActivity.this) != createdDark) recreate();
            updateGreeting();
            updateVehicleStatus();
            updateOil();
            fetchWeather(false);
            handler.postDelayed(this, 60 * 1000);
        }
    };

    // ---------- Ciclo de vida ----------

    @Override
    protected void onResume() {
        super.onResume();
        if (Ui.isDark(this) != createdDark || !profileStamp().equals(createdProfile)) {
            recreate();
            return;
        }
        updateGreeting();
        updateVehicleStatus();
        updateOil();
        showWeather(Weather.cached(this));
        registerReceiver(illReceiver, new android.content.IntentFilter("com.nwd.ACTION_ILL_STATE_CHANGE"));
        boolean lights = carLightsOn();
        if (lightsTelltale != null) {
            lightsTelltale.setAlpha(lights ? 1f : 0.35f);
            lightsTelltale.setColorFilter(lights ? 0xFF00E676 : 0x40FFFFFF);
        }
        if (hasLightsImage) {
            if (carLights != null) carLights.setAlpha(lights ? 1f : 0f);
            if (car != null) car.setAlpha(lights ? 0f : 1f);
        }
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                if (lm != null) lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
            } catch (Exception e) {
                android.util.Log.w("MainActivity", "GPS register failed", e);
            }
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        handler.post(periodic);
        nowPlaying.addListener(this);
        onNowPlayingChanged();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(illReceiver);
        handler.removeCallbacks(periodic);
        nowPlaying.removeListener(this);
        if (lm != null) {
            try {
                lm.removeUpdates(this);
            } catch (Exception ignored) { }
        }
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] perms, int[] results) {
        if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
            fetchWeather(true);
            try {
                if (lm != null) lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
            } catch (Exception ignored) { }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speedAnimator != null) {
            speedAnimator.cancel();
            speedAnimator = null;
        }
    }

    private LinearLayout.LayoutParams weight(float w, int left) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -1, w);
        lp.leftMargin = left;
        return lp;
    }

    private int dp(int v) {
        return Ui.dp(this, v);
    }

    static void pressable(View v) {
        Ui.pressable(v, 0.92f);
    }

    static void pressable(View v, float scale) {
        Ui.pressable(v, scale);
    }

    @Override
    public void onBackPressed() {
        // Launcher: nothing to go back to.
    }
}
