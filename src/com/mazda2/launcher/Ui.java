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

    // Mazda 2 Instrument Cluster Colors
    static final int COLOR_AMBER = 0xFFFF6A00;
    static final int COLOR_AMBER_DEEP = 0xFFE65100;
    static final int COLOR_AMBER_LIGHT = 0xFFFFA726;
    static final int COLOR_GRAPHITE_BG = 0xFF121212;
    static final int COLOR_GRAPHITE_SURFACE = 0xFF1E1E1E;
    static final int COLOR_GRAPHITE_CARD = 0xFF16161A;
    static final int COLOR_GRAPHITE_DOCK = 0xFF0D0D11;

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
                    new int[]{COLOR_GRAPHITE_BG, 0xFF0A0A0E, 0xFF000000});
            GradientDrawable glow = new GradientDrawable();
            glow.setGradientType(GradientDrawable.RADIAL_GRADIENT);
            glow.setGradientRadius(dp(c, 560));
            glow.setGradientCenter(0.95f, 0.95f);
            glow.setColors(new int[]{0x1AFF6A00, 0x00FF6A00});
            return new LayerDrawable(new Drawable[]{base, glow});
        }
        GradientDrawable base = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{0xFF1E1E22, 0xFF16161A, 0xFF101014});
        GradientDrawable glow = new GradientDrawable();
        glow.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        glow.setGradientRadius(dp(c, 560));
        glow.setGradientCenter(0.15f, 0.1f);
        glow.setColors(new int[]{0x22FF6A00, 0x00FF6A00});
        return new LayerDrawable(new Drawable[]{base, glow});
    }

    static Drawable card(Context c) {
        float r = dp(c, 20);
        // Base gradient: refined dark graphite #1C1C24 to #121216
        GradientDrawable base = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0xFF1C1C24, 0xFF121216});
        base.setCornerRadius(r);
        base.setStroke(dp(c, 1), 0x1FFFFFFF);

        // Micro-glow: subtle radial amber glow at the upper accent corner
        GradientDrawable glow = new GradientDrawable();
        glow.setCornerRadius(r);
        glow.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        glow.setGradientRadius(dp(c, 360));
        glow.setGradientCenter(0.85f, 0.12f);
        glow.setColors(new int[]{0x14FF6A00, 0x00FF6A00});

        return new LayerDrawable(new Drawable[]{base, glow});
    }

    static Drawable carPlayCard(Context c) {
        float r = dp(c, 20);
        GradientDrawable base = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{0xFF181F2C, 0xFF12151E, 0xFF0E1118});
        base.setCornerRadius(r);
        base.setStroke(dp(c, 1), 0x264A6A94);

        GradientDrawable glow = new GradientDrawable();
        glow.setCornerRadius(r);
        glow.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        glow.setGradientRadius(dp(c, 360));
        glow.setGradientCenter(0.9f, 0.1f);
        glow.setColors(new int[]{0x223B82F6, 0x003B82F6});

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
                filled ? new int[]{COLOR_AMBER, COLOR_AMBER_DEEP}
                       : new int[]{0x28FFFFFF, 0x14FFFFFF});
        bg.setCornerRadius(dp(c, 24));
        bg.setStroke(dp(c, 1), filled ? 0x4DFFFFFF : 0x2EFFFFFF);
        b.setBackground(bg);
        b.setTextColor(filled ? COLOR_GRAPHITE_BG : Color.WHITE);
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

    /** Tactile round icon button. filled = amber disc with dark icon (primary action). */
    static ImageView iconButton(Context c, int icon, int sizeDp, boolean filled, Runnable action) {
        ImageView b = new ImageView(c);
        b.setImageResource(icon);
        int pad = Math.round(sizeDp * 0.24f);
        b.setPadding(dp(c, pad), dp(c, pad), dp(c, pad), dp(c, pad));
        if (filled) {
            GradientDrawable disc = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[]{COLOR_AMBER, COLOR_AMBER_DEEP});
            disc.setShape(GradientDrawable.OVAL);
            disc.setStroke(dp(c, 1), 0x4DFFFFFF);
            b.setBackground(disc);
            b.setColorFilter(COLOR_GRAPHITE_BG);
        } else {
            GradientDrawable disc = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[]{0x26FFFFFF, 0x12FFFFFF});
            disc.setShape(GradientDrawable.OVAL);
            disc.setStroke(dp(c, 1), 0x24FFFFFF);
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
