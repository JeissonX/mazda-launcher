package com.mazda2.launcher;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

/**
 * Service managing hardware audio routing across ACC off/on and auto-equalization.
 * TextToSpeech vocal synthesis and media playback have been purged to eliminate CPU/RAM
 * overhead and ensure 60 FPS performance without blocking audio threads.
 */
public class GreeterService extends Service {

    private static final String TAG = "GreeterService";

    // MCU source ids (ApplicationList.xml "Source") of the last audio source before ACC off.
    private static final int SRC_BT = -125, SRC_USB = -128;

    private final Handler handler = new Handler();
    private final SoundClient sound = new SoundClient();
    private boolean waitingForPhone;
    private boolean btPlaying;

    @Override
    public void onCreate() {
        super.onCreate();
        sound.bind(this, () -> handler.post(() -> EqAuto.apply(this, sound)));
        IntentFilter f = new IntentFilter();
        f.addAction("com.nwd.action.ACTION_CHANGE_SOURCE");       // auto EQ: source curve
        f.addAction("com.nwd.action.ACTION_VOLUME_STATE_CHANGE");  // auto EQ: loudness
        f.addAction("com.nwd.action.ACTION_MCU_STATE_CHANGE");    // ACC off (3) / on (1)
        f.addAction("com.bt.ACTION_A2DP_ESTABLISHED");
        f.addAction("com.bt.ACTION_AVRCP_ESTABLISHED");
        f.addAction("com.bt.ACTION_BT_MUSIC_PLAY");
        f.addAction("com.bt.ACTION_BT_MUSIC_PAUSE");
        f.addAction("com.mazda2.launcher.TEST_CAR_ON");           // adb test hook
        registerReceiver(events, f);

        // Right after boot, bring the last source back without speech interruptions.
        if (android.os.SystemClock.elapsedRealtime() < 3 * 60_000) {
            handler.postDelayed(this::restoreAudio, 4000);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private final BroadcastReceiver events = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent i) {
            String a = i.getAction();
            if ("com.nwd.action.ACTION_CHANGE_SOURCE".equals(a)) {
                handler.postDelayed(() -> EqAuto.apply(GreeterService.this, sound), 1500);
                return;
            }
            if ("com.nwd.action.ACTION_VOLUME_STATE_CHANGE".equals(a)) {
                handler.postDelayed(() -> EqAuto.applyLoudness(GreeterService.this, sound), 300);
                return;
            }
            if ("com.nwd.action.ACTION_MCU_STATE_CHANGE".equals(a)) {
                int state = i.getByteExtra("extra_mcu_state", (byte) -1);
                Log.d(TAG, "mcu_state=" + state);
                if (state == 3) onCarOff();
                else if (state == 1) onCarOn();
            } else if ("com.mazda2.launcher.TEST_CAR_ON".equals(a)) {
                onCarOn();
            } else if ("com.bt.ACTION_BT_MUSIC_PAUSE".equals(a)) {
                btPlaying = false;
            } else if ("com.bt.ACTION_BT_MUSIC_PLAY".equals(a)) {
                waitingForPhone = false;
                boolean started = !btPlaying;
                btPlaying = true;
                if (started) {
                    Log.d(TAG, "BT music started, switching audio to Bluetooth");
                    Ui.selectBluetooth(GreeterService.this);
                }
            } else if (waitingForPhone) {
                // Phone connected after ACC on: resume Bluetooth music.
                waitingForPhone = false;
                handler.postDelayed(() -> startBluetoothMusic(), 2500);
            }
        }
    };

    // ---------- Keep the audio source across ACC off/on ----------

    private void onCarOff() {
        int src = android.provider.Settings.System.getInt(getContentResolver(), "mcu_current_source", 0);
        NowPlaying np = NowPlaying.get(this);
        boolean btPlaying = np.track(NowPlaying.Source.BT).playing;
        if (btPlaying) src = SRC_BT;
        prefs().edit().putInt("last_source", src).apply();
        Log.d(TAG, "car off, last source=" + src);
    }

    private void onCarOn() {
        handler.postDelayed(this::restoreAudio, 3000);
    }

    private void restoreAudio() {
        if (!Profile.isNwd(this)) return; // NWD source handling only
        int src = prefs().getInt("last_source",
                android.provider.Settings.System.getInt(getContentResolver(), "mcu_current_source", 0));
        Log.d(TAG, "restore audio source=" + src);
        if (NowPlaying.get(this).track(NowPlaying.Source.BT).playing) {
            Ui.selectBluetooth(this); // phone already playing: keep Bluetooth
        } else if (src == SRC_BT) {
            if (NowPlaying.get(this).btClient.isPhoneConnected()) startBluetoothMusic();
            else waitingForPhone = true; // wait for the phone to connect
        } else {
            // Default to USB / Local storage music (Never analog radio)
            Ui.requestSource(this, Ui.SOURCE_MUSIC);
        }
    }

    private void startBluetoothMusic() {
        Ui.selectBluetooth(this);
        handler.postDelayed(() -> NowPlaying.get(this).btClient.playControl(BtClient.PLAY), 1500);
    }

    private SharedPreferences prefs() {
        return getSharedPreferences("greeter", MODE_PRIVATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(events);
        sound.unbind(this);
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /** Starts the service at boot even if the launcher is not shown yet. */
    public static class BootReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context c, Intent i) {
            c.startService(new Intent(c, GreeterService.class));
            c.startService(new Intent(c, VolumeOverlay.class));
            c.startService(new Intent(c, Odometer.class));
        }
    }
}
