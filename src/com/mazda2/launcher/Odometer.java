package com.mazda2.launcher;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/**
 * GPS odometer and vehicle maintenance tracker for Mazda 2 DE.
 * Tracks distance driven and monitors oil-change cycles (default 5.000 km).
 */
public class Odometer extends Service implements LocationListener {

    private static final String TAG = "Odometer";
    public static final int DEFAULT_INTERVAL_KM = 5000;
    public static final int WARN_KM = 200; // Alerta naranja/roja cuando restan < 200 km o se vence

    public static final String PREF_LAST_CHANGE_KM = "last_oil_change_km";
    public static final String PREF_INTERVAL_KM = "oil_interval_km";
    public static final String PREF_CURRENT_KM = "current_odometer_km";
    public static final String PREF_GPS_KM = "gps_km";

    private LocationManager lm;
    private Location last;
    private float pendingMeters;

    @Override
    public void onCreate() {
        super.onCreate();
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, this);
            } catch (Exception e) {
                Log.w(TAG, "no GPS", e);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onLocationChanged(Location l) {
        if (!l.hasAccuracy() || l.getAccuracy() > 25) return;
        if (last != null) {
            float d = last.distanceTo(l);
            float secs = Math.max(1, (l.getTime() - last.getTime()) / 1000f);
            float kmh = d / secs * 3.6f;
            boolean moving = (l.hasSpeed() ? l.getSpeed() * 3.6f : kmh) > 4;
            // Count only real driving; drop parked drift and impossible jumps.
            if (moving && kmh < 200) pendingMeters += d;
        }
        last = l;
        if (pendingMeters >= 100) { // persist every 100 m
            SharedPreferences p = prefs(this);
            p.edit().putFloat(PREF_GPS_KM, p.getFloat(PREF_GPS_KM, 0) + pendingMeters / 1000f).apply();
            pendingMeters = 0;
        }
    }

    public void onStatusChanged(String p, int s, Bundle b) { }
    public void onProviderEnabled(String p) { }
    public void onProviderDisabled(String p) { }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (lm != null) lm.removeUpdates(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // ---------- Shared state & Maintenance API ----------

    static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences("odometer", Context.MODE_PRIVATE);
    }

    static boolean isConfigured(Context c) {
        SharedPreferences p = prefs(c);
        return p.contains(PREF_CURRENT_KM) || p.contains("base_km");
    }

    /** Estimated dashboard reading: the value the user entered plus GPS distance since. */
    static int currentKm(Context c) {
        SharedPreferences p = prefs(c);
        int base = p.getInt(PREF_CURRENT_KM, p.getInt("base_km", 0));
        return Math.round(base + p.getFloat(PREF_GPS_KM, 0));
    }

    static void setCurrentKm(Context c, int km) {
        prefs(c).edit()
                .putInt(PREF_CURRENT_KM, km)
                .putInt("base_km", km)
                .putFloat(PREF_GPS_KM, 0)
                .apply();
    }

    /** Odómetro al momento del último cambio de aceite registrado. */
    static int lastChangeKm(Context c) {
        SharedPreferences p = prefs(c);
        return p.getInt(PREF_LAST_CHANGE_KM, p.getInt("last_change_km", currentKm(c)));
    }

    static void setLastChangeKm(Context c, int km) {
        prefs(c).edit()
                .putInt(PREF_LAST_CHANGE_KM, km)
                .putInt("last_change_km", km)
                .apply();
    }

    /** Intervalo de cambio de aceite en km (editable: 5000, 7500, 10000 km; por defecto 5000 km). */
    static int intervalKm(Context c) {
        SharedPreferences p = prefs(c);
        return p.getInt(PREF_INTERVAL_KM, p.getInt("interval_km", DEFAULT_INTERVAL_KM));
    }

    static void setIntervalKm(Context c, int km) {
        prefs(c).edit()
                .putInt(PREF_INTERVAL_KM, km)
                .putInt("interval_km", km)
                .apply();
    }

    /** Km recorridos con el aceite actual. */
    static int drivenKm(Context c) {
        return Math.max(0, currentKm(c) - lastChangeKm(c));
    }

    /** Km restantes para el próximo cambio (negativo si está vencido). */
    static int remainingKm(Context c) {
        return (lastChangeKm(c) + intervalKm(c)) - currentKm(c);
    }

    /** Porcentaje de vida útil del aceite (0% a 100%). */
    static int lifePercent(Context c) {
        int interval = Math.max(1, intervalKm(c));
        int remaining = remainingKm(c);
        if (remaining <= 0) return 0;
        return Math.max(0, Math.min(100, Math.round((remaining * 100f) / interval)));
    }

    /** Resetea el ciclo de cambio al kilometraje actual. */
    static void resetToNow(Context c) {
        setLastChangeKm(c, currentKm(c));
    }
}
