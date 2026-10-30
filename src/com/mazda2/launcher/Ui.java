package com.mazda2.launcher;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/** Shared look (Mazda Amber & Deep Graphite) and small helpers for all screens. */
final class Ui {

    // Tesla / CarPlay Cyber Blue, Slate Dark & Tesla Red Colorimetry
    static final int COLOR_ACCENT_BLUE = 0xFF0084FF;       // Cyber Blue / CarPlay Blue
    static final int COLOR_ACCENT_BLUE_LIGHT = 0xFF00AAFF; // Cyber Blue Light
    static final int COLOR_GLOW_ICE = 0xFF00E5FF;          // Ice Blue Neon
    static final int COLOR_ALERT_RED = 0xFFE82127;         // Tesla Red
    static final int COLOR_ALERT_RED_DARK = 0xFFB71C1C;    // Dark Crimson Red

    static final int COLOR_BG_DARK = 0xFF0D0E12;          // Deep Carbon
    static final int COLOR_SURFACE = 0xFF15161C;          // Slate Dark
    static final int COLOR_CARD = 0xFF181920;             // Graphite / Steel Gray
    static final int COLOR_DOCK = 0xFF0A0B0E;             // Deep Slate Dock

    // Compatibility aliases for existing references
    static final int COLOR_AMBER = COLOR_ACCENT_BLUE;
    static final int COLOR_AMBER_DEEP = 0xFF0066CC;
    static final int COLOR_AMBER_LIGHT = COLOR_ACCENT_BLUE_LIGHT;
    static final int COLOR_GRAPHITE_BG = COLOR_BG_DARK;
    static final int COLOR_GRAPHITE_SURFACE = COLOR_SURFACE;
    static final int COLOR_GRAPHITE_CARD = COLOR_CARD;
    static final int COLOR_GRAPHITE_DOCK = COLOR_DOCK;

    // MCU source ids for ACTION_REQUEST_CHANGE_SOURCE: the "Source" column of
    // /nwdappconfig/app/ApplicationList.xml (not the AppID).
    static final int SOURCE_MUSIC = -128;
    static final int SOURCE_RADIO = 4;
    static final int SOURCE_BT_MUSIC = -125;

    private Ui() { }

    static int dp(Context c, float v) {
        return Math.round(v * c.getResources().getDisplayMetrics().density);
    }

    // ---------- Theme: light (blue/purple neon) or dark (black) ----------

    static final String THEME_AUTO = "auto", THEME_LIGHT = "light", THEME_DARK = "dark";

    static String themeMode(Context c) {
        return c.getSharedPreferences("ui", Context.MODE_PRIVATE).getString("theme", THEME_AUTO);
    }

    static void setThemeMode(Context c, String mode) {
        c.getSharedPreferences("ui", Context.MODE_PRIVATE).edit().putString("theme", mode).apply();
    }

    /** Auto = dark while the car's lights are on (ILL wire) or at night. */
    static boolean isDark(Context c) {
        String m = themeMode(c);
        if (THEME_DARK.equals(m)) return true;
        if (THEME_LIGHT.equals(m)) return false;
        int ill = android.provider.Settings.System.getInt(c.getContentResolver(), "mcu_no_source_device_state", 0);
        int h = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        return (ill & 2) != 0 || h >= 19 || h < 6;
    }

    /** Picks the light or dark variant of a gradient. */
    static int[] pick(Context c, int[] light, int[] dark) {
        return isDark(c) ? dark : light;
    }

    static Drawable background(Context c) {
        if (isDark(c)) {
            GradientDrawable base = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                    new int[]{COLOR_BG_DARK, COLOR_SURFACE, 0xFF08090C});
            GradientDrawable glow = new GradientDrawable();
            glow.setGradientType(GradientDrawable.RADIAL_GRADIENT);
            glow.setGradientRadius(dp(c, 580));
            glow.setGradientCenter(0.95f, 0.95f);
            glow.setColors(new int[]{0x1200E5FF, 0x0000E5FF}); // Resplandor Ice Blue radial atenuado
            return new LayerDrawable(new Drawable[]{base, glow});
        }
        GradientDrawable base = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{0xFF1C1E26, COLOR_SURFACE, COLOR_BG_DARK});
        GradientDrawable glow = new GradientDrawable();
        glow.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        glow.setGradientRadius(dp(c, 580));
        glow.setGradientCenter(0.15f, 0.1f);
        glow.setColors(new int[]{0x160084FF, 0x000084FF});
        return new LayerDrawable(new Drawable[]{base, glow});
    }

    static Drawable card(Context c) {
        float r = dp(c, 20);
        // Base gradient: Graphite / Steel Gray #181920 to #121318
        GradientDrawable base = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{COLOR_CARD, 0xFF121318});
        base.setCornerRadius(r);
        base.setStroke(dp(c, 1), 0x220084FF); // Stroke semi-translúcido azulado

        // Micro-glow: resplandor Ice Blue radial atenuado 0x1200E5FF
        GradientDrawable glow = new GradientDrawable();
        glow.setCornerRadius(r);
        glow.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        glow.setGradientRadius(dp(c, 360));
        glow.setGradientCenter(0.85f, 0.12f);
        glow.setColors(new int[]{0x1200E5FF, 0x0000E5FF});

        return new LayerDrawable(new Drawable[]{base, glow});
    }

    static Drawable carPlayCard(Context c) {
        float r = dp(c, 20);
        GradientDrawable base = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{0xFF141926, 0xFF10141F, 0xFF0B0E17});
        base.setCornerRadius(r);
        base.setStroke(dp(c, 1), 0x330084FF);

        GradientDrawable glow = new GradientDrawable();
        glow.setCornerRadius(r);
        glow.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        glow.setGradientRadius(dp(c, 360));
        glow.setGradientCenter(0.9f, 0.1f);
        glow.setColors(new int[]{0x2200E5FF, 0x0000E5FF});

        return new LayerDrawable(new Drawable[]{base, glow});
    }

    static GradientDrawable circle(int color) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.OVAL);
        d.setColor(color);
        return d;
    }

    /** Tactile capsule button with micro-relief padding (12dp x 24dp) and bold typography. */
    static TextView capsuleButton(Context c, CharSequence text, boolean filled, Runnable action) {
        TextView b = new TextView(c);
        b.setText(text);
        b.setTextSize(12);
        b.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        b.setLetterSpacing(0.08f);
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(c, 24), dp(c, 12), dp(c, 24), dp(c, 12));

        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                filled ? new int[]{COLOR_ACCENT_BLUE, 0xFF0066CC}
                       : new int[]{0x280084FF, 0x140084FF});
        bg.setCornerRadius(dp(c, 24));
        bg.setStroke(dp(c, 1), filled ? 0x6600E5FF : 0x4D0084FF);
        b.setBackground(bg);
        b.setTextColor(Color.WHITE);
        pressable(b, 0.94f);
        if (action != null) b.setOnClickListener(v -> action.run());
        return b;
    }

    static TextView text(Context c, CharSequence s, float sp, int color) {
        TextView tv = new TextView(c);
        tv.setText(s);
        tv.setTextSize(sp);
        tv.setTextColor(color);
        tv.setSingleLine(true);
        tv.setEllipsize(TextUtils.TruncateAt.END);
        tv.setGravity(Gravity.CENTER);
        return tv;
    }

    /** Tactile round icon button. filled = Cyber Blue disc with white icon (primary action). */
    static ImageView iconButton(Context c, int icon, int sizeDp, boolean filled, Runnable action) {
        ImageView b = new ImageView(c);
        b.setImageResource(icon);
        int pad = Math.round(sizeDp * 0.24f);
        b.setPadding(dp(c, pad), dp(c, pad), dp(c, pad), dp(c, pad));
        if (filled) {
            GradientDrawable disc = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[]{COLOR_ACCENT_BLUE, 0xFF0066CC});
            disc.setShape(GradientDrawable.OVAL);
            disc.setStroke(dp(c, 1), 0x6600E5FF);
            b.setBackground(disc);
            b.setColorFilter(Color.WHITE);
        } else {
            GradientDrawable disc = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[]{0x26FFFFFF, 0x10FFFFFF});
            disc.setShape(GradientDrawable.OVAL);
            disc.setStroke(dp(c, 1), 0x220084FF);
            b.setBackground(disc);
            b.setColorFilter(Color.WHITE);
        }
        pressable(b, 0.88f);
        b.setOnClickListener(v -> action.run());
        return b;
    }

    static void pressable(View v) {
        pressable(v, 0.92f);
    }

    static void pressable(View v, float scale) {
        v.setClickable(true);
        v.setOnTouchListener((view, e) -> {
            if (e.getAction() == MotionEvent.ACTION_DOWN) {
                view.animate().scaleX(scale).scaleY(scale).alpha(0.8f).setDuration(90).start();
            } else if (e.getAction() == MotionEvent.ACTION_UP || e.getAction() == MotionEvent.ACTION_CANCEL) {
                view.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(140).start();
            }
            return false;
        });
    }

    static boolean launchSafe(Context c, String component) {
        try {
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.addCategory(Intent.CATEGORY_LAUNCHER);
            i.setComponent(ComponentName.unflattenFromString(component));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            c.startActivity(i);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    static void launch(Context c, String component) {
        if (!launchSafe(c, component)) {
            Toast.makeText(c, "No disponible", Toast.LENGTH_SHORT).show();
        }
    }

    /** Analog radio is permanently disabled in Mazda OS. */
    static boolean radioIsSource(Context c) {
        return false;
    }

    /**
     * Makes Bluetooth music the source. Source id -125 is shared by the BT phone (AppID 10) and
     * BT music (AppID 15); a plain source request picks the phone entry, which mutes the
     * amplifier. Announcing the BT music app (KernelUtils.appStart: id 15, op 1, event 0) is what
     * the factory apps do and routes the music properly.
     */
    static void selectBluetooth(Context c) {
        Intent i = new Intent("com.nwd.action.ACTION_APP_IN_OUT");
        i.putExtra("extra_app_id", 15);
        i.putExtra("extra_app_operation", 1);
        i.putExtra("extra_app_event", 0);
        c.sendBroadcast(i);
        // Picking the radio mutes Bluetooth audio (A2DP mute = amplifier mute flag 32) and only
        // the factory BT screen lifts it. Lift it after the source change has gone through.
        new android.os.Handler().postDelayed(
                () -> c.sendBroadcast(new Intent("com.bt.ACTION_A2DP_UNMUTE")), 500);
    }

    /** Asks the NWD kernel to route this audio source to the speakers. */
    static void requestSource(Context c, int sourceId) {
        Intent i = new Intent("com.nwd.action.ACTION_REQUEST_CHANGE_SOURCE");
        i.putExtra("extra_source_id", (byte) sourceId);
        c.sendBroadcast(i);
    }
}
