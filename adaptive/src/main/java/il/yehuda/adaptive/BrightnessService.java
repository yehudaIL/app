package il.yehuda.adaptive;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;

public class BrightnessService extends Service implements SensorEventListener {
    private static final String CHANNEL = "smart_brightness";
    private SensorManager manager;
    private Sensor light;
    private float filteredLux = -1f;
    private int lastTarget = -1;
    private long lastChangeAt = 0;

    @Override public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(CHANNEL, "בהירות חכמה", NotificationManager.IMPORTANCE_LOW);
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
        Notification.Builder nb = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(this, CHANNEL) : new Notification.Builder(this);
        nb.setSmallIcon(android.R.drawable.ic_menu_view)
                .setContentTitle("בהירות חכמה פעילה")
                .setContentText("מתאימה את המסך לתאורת הסביבה")
                .setOngoing(true);
        startForeground(31, nb.build());

        manager = (SensorManager) getSystemService(SENSOR_SERVICE);
        light = manager == null ? null : manager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if (light == null) {
            stopSelf();
            return;
        }
        manager.registerListener(this, light, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override public void onSensorChanged(SensorEvent event) {
        if (event == null || event.values == null || event.values.length == 0) return;
        float lux = Math.max(0f, event.values[0]);
        filteredLux = filteredLux < 0 ? lux : filteredLux * 0.75f + lux * 0.25f;
        long now = System.currentTimeMillis();
        if (now - lastChangeAt < 1500) return;

        int target;
        if (filteredLux < 5) target = 18;
        else if (filteredLux < 25) target = 35;
        else if (filteredLux < 100) target = 65;
        else if (filteredLux < 400) target = 110;
        else if (filteredLux < 1500) target = 165;
        else target = 255;

        if (lastTarget >= 0 && Math.abs(target - lastTarget) < 12) return;
        if (Build.VERSION.SDK_INT >= 23 && !Settings.System.canWrite(this)) return;
        try {
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            if (Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, target)) {
                lastTarget = target;
                lastChangeAt = now;
            }
        } catch (Exception ignored) { }
    }

    @Override public void onAccuracyChanged(Sensor sensor, int accuracy) { }
    @Override public int onStartCommand(Intent intent, int flags, int startId) { return START_STICKY; }
    @Override public void onDestroy() {
        if (manager != null) manager.unregisterListener(this);
        super.onDestroy();
    }
    @Override public IBinder onBind(Intent intent) { return null; }
}
