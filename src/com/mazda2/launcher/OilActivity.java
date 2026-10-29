package com.mazda2.launcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

/**
 * Módulo de mantenimiento y cambio de aceite para Mazda 2 DE.
 * Intervalo estándar parametrizable (5.000, 7.500, 10.000 km; 5.000 km por defecto).
 */
public class OilActivity extends Activity {

    private static final int[] INTERVALS = {5000, 7500, 10000};

    private TextView remainingHeader, percentBadge, remainingValue, drivenValue, nextValue, currentValue, lastValue;
    private ProgressBar bar;
    private GradientDrawable statusBg;
    private final TextView[] intervalChips = new TextView[INTERVALS.length];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(12), dp(16), dp(16));
        root.setBackground(Ui.background(this));

        // Barra superior de navegación
        LinearLayout topBar = new LinearLayout(this);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.addView(Ui.iconButton(this, R.drawable.ic_back, 48, false, this::finish),
                new LinearLayout.LayoutParams(dp(48), dp(48)));
        TextView title = Ui.text(this, "Mantenimiento de Aceite · Mazda 2", 24, Color.WHITE);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        title.setPadding(dp(16), 0, 0, 0);
        topBar.addView(title, new LinearLayout.LayoutParams(0, -2, 1f));
        root.addView(topBar, new LinearLayout.LayoutParams(-1, dp(56)));

        // Cuerpo: Tarjeta de estado (izq) y Acciones (der)
        LinearLayout body = new LinearLayout(this);
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(-1, 0, 1f);
        blp.topMargin = dp(12);
        root.addView(body, blp);

        body.addView(buildStatus(), new LinearLayout.LayoutParams(0, -1, 1.15f));

        LinearLayout.LayoutParams alp = new LinearLayout.LayoutParams(0, -1, 1f);
        alp.leftMargin = dp(12);
        body.addView(buildActions(), alp);

        setContentView(root);
    }

    private View buildStatus() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(24), dp(20), dp(24), dp(20));
        statusBg = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{0xFF1E1E26, 0xFF121218});
        statusBg.setCornerRadius(dp(18));
        statusBg.setStroke(dp(1), 0x33FF6A00);
        card.setBackground(statusBg);

        LinearLayout topRow = new LinearLayout(this);
        topRow.setGravity(Gravity.CENTER_VERTICAL);
        remainingHeader = Ui.text(this, "VIDA DEL ACEITE", 12, Ui.COLOR_AMBER);
        remainingHeader.setTypeface(Typeface.DEFAULT_BOLD);
        remainingHeader.setLetterSpacing(0.14f);
        topRow.addView(remainingHeader, new LinearLayout.LayoutParams(0, -2, 1f));

        percentBadge = Ui.text(this, "100%", 13, Color.WHITE);
        percentBadge.setTypeface(Typeface.DEFAULT_BOLD);
        percentBadge.setPadding(dp(10), dp(4), dp(10), dp(4));
        GradientDrawable pBg = new GradientDrawable();
        pBg.setColor(0x26FF6A00);
        pBg.setCornerRadius(dp(12));
        percentBadge.setBackground(pBg);
        topRow.addView(percentBadge);
        card.addView(topRow);

        remainingValue = Ui.text(this, "-- km", 44, Color.WHITE);
        remainingValue.setTypeface(Typeface.DEFAULT_BOLD);
        remainingValue.setGravity(Gravity.START);
        remainingValue.setPadding(0, dp(10), 0, dp(4));
        card.addView(remainingValue);

        // Barra gráfica horizontal de porcentaje
        bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        bar.setMax(100);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(12));
        lp.setMargins(0, dp(4), 0, dp(18));
        card.addView(bar, lp);

        drivenValue = Ui.text(this, "", 15, Color.WHITE);
        drivenValue.setGravity(Gravity.START);
        card.addView(drivenValue);

        nextValue = Ui.text(this, "", 14, 0xCCFFFFFF);
        nextValue.setGravity(Gravity.START);
        nextValue.setPadding(0, dp(6), 0, 0);
        card.addView(nextValue);

        currentValue = Ui.text(this, "", 13, 0x99FFFFFF);
        currentValue.setGravity(Gravity.START);
        currentValue.setPadding(0, dp(4), 0, 0);
        card.addView(currentValue);

        lastValue = Ui.text(this, "", 13, 0x99FFFFFF);
        lastValue.setGravity(Gravity.START);
        lastValue.setPadding(0, dp(2), 0, 0);
        card.addView(lastValue);

        return card;
    }

    private View buildActions() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(20), dp(18), dp(20), dp(18));
        card.setBackground(Ui.card(this));

        // Botón de acción rápida
        card.addView(action("Registrar cambio de aceite hoy", 0xFFE65100, () -> {
            Odometer.resetToNow(this);
            Toast.makeText(this, "¡Listo! Ciclo reiniciado a 5.000 km", Toast.LENGTH_SHORT).show();
            refresh();
        }));

        card.addView(action("Ajustar kilometraje actual", 0x26FFFFFF,
                () -> askKm("Kilometraje actual del tablero", Odometer.currentKm(this), km -> {
                    boolean first = !Odometer.isConfigured(this);
                    Odometer.setCurrentKm(this, km);
                    if (first) Odometer.setLastChangeKm(this, km);
                    refresh();
                })));

        card.addView(action("Ajustar km del último cambio", 0x26FFFFFF,
                () -> askKm("Kilometraje del último cambio", Odometer.lastChangeKm(this), km -> {
                    Odometer.setLastChangeKm(this, km);
                    refresh();
                })));

        TextView every = Ui.text(this, "Intervalo de cambio", 14, 0xCCFFFFFF);
        every.setGravity(Gravity.START);
        every.setPadding(dp(2), dp(10), 0, dp(8));
        card.addView(every);

        LinearLayout chips = new LinearLayout(this);
        for (int i = 0; i < INTERVALS.length; i++) {
            int km = INTERVALS[i];
            TextView c = Ui.text(this, fmt(km) + " km", 14, Color.WHITE);
            c.setPadding(0, dp(10), 0, dp(10));
            Ui.pressable(c, 0.92f);
            c.setOnClickListener(v -> {
                Odometer.setIntervalKm(this, km);
                refresh();
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -2, 1f);
            lp.setMargins(dp(3), 0, dp(3), 0);
            chips.addView(c, lp);
            intervalChips[i] = c;
        }
        card.addView(chips);

        TextView note = Ui.text(this, "Ciclo estándar post-overhaul: 5.000 km. Conteo asistido por GPS.", 12, 0x80FFFFFF);
        note.setSingleLine(false);
        note.setGravity(Gravity.START);
        note.setPadding(dp(2), dp(12), dp(2), 0);
        card.addView(note);

        return card;
    }

    private TextView action(String label, int color, Runnable r) {
        TextView b = Ui.text(this, label, 15, Color.WHITE);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setPadding(dp(16), dp(12), dp(16), dp(12));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(color);
        bg.setCornerRadius(dp(14));
        if (color == 0xFFE65100) {
            bg.setStroke(dp(1), Ui.COLOR_AMBER);
        }
        b.setBackground(bg);
        Ui.pressable(b, 0.96f);
        b.setOnClickListener(v -> r.run());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.bottomMargin = dp(8);
        b.setLayoutParams(lp);
        return b;
    }

    interface KmListener {
        void onKm(int km);
    }

    private void askKm(String title, int initial, KmListener l) {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(initial > 0 ? String.valueOf(initial) : "");
        input.setSelectAllOnFocus(true);
        input.setTextSize(24);
        new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
                .setTitle(title)
                .setView(input)
                .setPositiveButton("Guardar", (d, w) -> {
                    try {
                        l.onKm(Integer.parseInt(input.getText().toString().trim()));
                    } catch (NumberFormatException ignored) { }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void refresh() {
        if (!Odometer.isConfigured(this)) {
            remainingValue.setText("—");
            percentBadge.setText("0%");
            drivenValue.setText("Configura el kilometraje del tablero");
            nextValue.setText("");
            currentValue.setText("");
            lastValue.setText("");
            updateProgressBar(0, 0xFF757575);
            return;
        }

        int rem = Odometer.remainingKm(this);
        int interval = Odometer.intervalKm(this);
        int driven = Odometer.drivenKm(this);
        int life = Odometer.lifePercent(this);
        int current = Odometer.currentKm(this);
        int lastChange = Odometer.lastChangeKm(this);

        percentBadge.setText(life + "% de vida útil");

        if (rem < 0) {
            remainingValue.setText("¡Vencido (" + fmt(-rem) + " km)!");
            remainingValue.setTextColor(0xFFD32F2F);
            statusBg.setColors(new int[]{0xCCD32F2F, 0xFF301010});
            updateProgressBar(0, 0xFFD32F2F);
        } else if (rem <= Odometer.WARN_KM) {
            remainingValue.setText("Restantes: " + fmt(rem) + " km");
            remainingValue.setTextColor(0xFFFFA726);
            statusBg.setColors(new int[]{0xCCFF9800, 0xFF302010});
            updateProgressBar(life, 0xFFFFA726);
        } else {
            remainingValue.setText("Restantes: " + fmt(rem) + " km");
            remainingValue.setTextColor(Color.WHITE);
            statusBg.setColors(new int[]{0xFF1E1E26, 0xFF121218});
            updateProgressBar(life, 0xFF00E676);
        }

        drivenValue.setText("Recorridos con este aceite: " + fmt(driven) + " km");
        nextValue.setText("Próximo cambio programado: " + fmt(lastChange + interval) + " km");
        currentValue.setText("Odómetro actual (GPS): " + fmt(current) + " km");
        lastValue.setText("Último cambio registrado: " + fmt(lastChange) + " km");

        for (int i = 0; i < INTERVALS.length; i++) {
            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(dp(12));
            boolean active = (INTERVALS[i] == interval);
            bg.setColor(active ? Ui.COLOR_AMBER : 0x1FFFFFFF);
            if (active) bg.setStroke(dp(1), Ui.COLOR_AMBER_LIGHT);
            intervalChips[i].setBackground(bg);
            intervalChips[i].setTextColor(active ? 0xFF121212 : Color.WHITE);
        }
    }

    private void updateProgressBar(int percent, int color) {
        bar.setProgress(percent);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(0x26FFFFFF);
        bg.setCornerRadius(dp(6));

        GradientDrawable prog = new GradientDrawable();
        prog.setColor(color);
        prog.setCornerRadius(dp(6));
        ClipDrawable clip = new ClipDrawable(prog, Gravity.START, ClipDrawable.HORIZONTAL);

        LayerDrawable ld = new LayerDrawable(new Drawable[]{bg, clip});
        ld.setId(0, android.R.id.background);
        ld.setId(1, android.R.id.progress);
        bar.setProgressDrawable(ld);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    static String fmt(int km) {
        return String.format(new Locale("es", "CO"), "%,d", km).replace(',', '.');
    }

    private int dp(int v) {
        return Ui.dp(this, v);
    }
}
