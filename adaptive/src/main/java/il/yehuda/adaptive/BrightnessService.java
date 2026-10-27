package il.yehuda.adaptive;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Surface;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class BrightnessService extends Service {
    private static final String CHANNEL = "smart_brightness";
    private CameraDevice camera;
    private CameraCaptureSession session;
    private ImageReader reader;
    private HandlerThread thread;
    private Handler handler;
    private long lastChangeAt;
    private int lastTarget = -1;
    private float filteredLuma = -1f;
    private boolean opening;

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
                .setContentText("מעריכה תאורה באמצעות מצלמת הסלפי")
                .setOngoing(true);
        startForeground(31, nb.build());

        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            stopSelf();
            return;
        }
        thread = new HandlerThread("FrontCameraBrightness");
        thread.start();
        handler = new Handler(thread.getLooper());
        openFrontCamera();
    }

    private void openFrontCamera() {
        if (opening || camera != null) return;
        opening = true;
        try {
            CameraManager cm = (CameraManager) getSystemService(CAMERA_SERVICE);
            String selected = null;
            for (String id : cm.getCameraIdList()) {
                CameraCharacteristics c = cm.getCameraCharacteristics(id);
                Integer facing = c.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    selected = id;
                    break;
                }
            }
            if (selected == null) { stopSelf(); return; }
            reader = ImageReader.newInstance(320, 240, ImageFormat.YUV_420_888, 2);
            reader.setOnImageAvailableListener(r -> {
                Image image = null;
                try {
                    image = r.acquireLatestImage();
                    if (image == null) return;
                    Image.Plane plane = image.getPlanes()[0];
                    ByteBuffer buffer = plane.getBuffer();
                    int rowStride = plane.getRowStride();
                    int pixelStride = plane.getPixelStride();
                    long sum = 0;
                    int count = 0;
                    for (int y = 0; y < image.getHeight(); y += 4) {
                        for (int x = 0; x < image.getWidth(); x += 4) {
                            int index = y * rowStride + x * pixelStride;
                            if (index < buffer.limit()) {
                                sum += buffer.get(index) & 0xff;
                                count++;
                            }
                        }
                    }
                    if (count > 0) applyBrightness(sum / (float) count);
                } catch (Exception ignored) {
                } finally {
                    if (image != null) image.close();
                }
            }, handler);
            cm.openCamera(selected, new CameraDevice.StateCallback() {
                @Override public void onOpened(CameraDevice device) {
                    camera = device;
                    opening = false;
                    try {
                        Surface surface = reader.getSurface();
                        camera.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                            @Override public void onConfigured(CameraCaptureSession s) {
                                session = s;
                                try {
                                    CaptureRequest.Builder request = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                                    request.addTarget(surface);
                                    request.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
                                    request.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
                                    s.setRepeatingRequest(request.build(), null, handler);
                                } catch (CameraAccessException | IllegalStateException e) { stopSelf(); }
                            }
                            @Override public void onConfigureFailed(CameraCaptureSession s) { stopSelf(); }
                        }, handler);
                    } catch (CameraAccessException | IllegalStateException e) { stopSelf(); }
                }
                @Override public void onDisconnected(CameraDevice device) {
                    device.close(); camera = null; stopSelf();
                }
                @Override public void onError(CameraDevice device, int error) {
                    device.close(); camera = null; stopSelf();
                }
            }, handler);
        } catch (CameraAccessException | SecurityException | IllegalArgumentException e) {
            opening = false;
            stopSelf();
        }
    }

    private void applyBrightness(float luma) {
        // The camera auto-exposure and the subject in view affect this estimate; smooth it to avoid flicker.
        filteredLuma = filteredLuma < 0 ? luma : filteredLuma * 0.82f + luma * 0.18f;
        long now = System.currentTimeMillis();
        if (now - lastChangeAt < 1800) return;

        int target;
        if (filteredLuma < 24) target = 20;
        else if (filteredLuma < 48) target = 38;
        else if (filteredLuma < 78) target = 65;
        else if (filteredLuma < 112) target = 105;
        else if (filteredLuma < 155) target = 155;
        else target = 220;

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

    @Override public int onStartCommand(Intent intent, int flags, int startId) { return START_STICKY; }

    @Override public void onDestroy() {
        try { if (session != null) session.close(); } catch (Exception ignored) { }
        try { if (camera != null) camera.close(); } catch (Exception ignored) { }
        try { if (reader != null) reader.close(); } catch (Exception ignored) { }
        if (thread != null) {
            if (Build.VERSION.SDK_INT >= 18) thread.quitSafely(); else thread.quit();
        }
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}
