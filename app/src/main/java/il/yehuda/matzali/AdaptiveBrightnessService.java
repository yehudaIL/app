package il.yehuda.matzali;
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

public class AdaptiveBrightnessService extends Service implements SensorEventListener {
    private static final String CHANNEL = "adaptive_brightness";
    private SensorManager sensorManager;
    private Sensor lightSensor;
    private long lastChange;
    private float smoothedLux = -1f;
    private int lastBrightness = -1;

    @Override public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(CHANNEL, "בהירות דינמית", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("התאמת בהירות המסך לפי תאורת הסביבה");
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
        Notification.Builder builder = Build.VERSION.SDK_INT >= 26 ? new Notification.Builder(this, CHANNEL) : new Notification.Builder(this);
        builder.setSmallIcon(android.R.drawable.ic_menu_view).setContentTitle("בהירות דינמית פעילה")
                .setContentText("התאמת בהירות לפי חיישן האור").setOngoing(true);
        startForeground(7, builder.build());
        if (Build.VERSION.SDK_INT >= 23 && Settings.System.canWrite(this)) {\n            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);\n        }\n        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        lightSensor = sensorManager == null ? null : sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if (lightSensor != null) sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        else stopSelf();
    }

    @Override public void onSensorChanged(SensorEvent event) {
        if (event == null || event.values == null || event.values.length == 0) return;
        float lux = Math.max(0f, event.values[0]);
        smoothedLux = smoothedLux < 0 ? lux : smoothedLux * 0.72f + lux * 0.28f;
        long now = System.currentTimeMillis();
        if (now - lastChange < 1200) return;
        int target;
        if (smoothedLux < 5) target = 18;
        else if (smoothedLux < 25) target = 35;
        else if (smoothedLux < 100) target = 65;
        else if (smoothedLux < 400) target = 105;
        else if (smoothedLux < 1500) target = 160;
        else target = 255;
        if (lastBrightness >= 0 && Math.abs(target - lastBrightness) < 12) return;
        if (Build.VERSION.SDK_INT >= 23 && !Settings.System.canWrite(this)) return;
        try {
            if (Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, target)) {
                lastBrightness = target;
                lastChange = now;
            }
        } catch (Exception ignored) { }
    }
    @Override public void onAccuracyChanged(Sensor sensor, int accuracy) { }
    @Override public int onStartCommand(Intent intent, int flags, int startId) { return START_STICKY; }
    @Override public void onDestroy() {
        if (sensorManager != null) sensorManager.unregisterListener(this);
        super.onDestroy();
    }
    @Override public IBinder onBind(Intent intent) { return null; }
}