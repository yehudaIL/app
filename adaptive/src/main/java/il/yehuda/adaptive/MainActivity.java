package il.yehuda.adaptive;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    private TextView status;
    private final int mint = Color.rgb(105, 235, 195);
    private static final int CAMERA_REQUEST = 410;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().getDecorView().setLayoutDirection(android.view.View.LAYOUT_DIRECTION_RTL);
        getWindow().setStatusBarColor(Color.rgb(14, 24, 44));
        getWindow().setNavigationBarColor(Color.rgb(14, 24, 44));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(28, 32, 28, 32);
        root.setBackgroundColor(Color.rgb(14, 24, 44));

        TextView title = new TextView(this);
        title.setText("בהירות חכמה");
        title.setTextColor(Color.WHITE);
        title.setTextSize(30);
        title.setGravity(Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView subtitle = new TextView(this);
        subtitle.setText("מעריכה את התאורה בעזרת מצלמת הסלפי\nומכוונת את בהירות המסך בהתאם");
        subtitle.setTextColor(Color.LTGRAY);
        subtitle.setTextSize(16);
        subtitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, -2);
        sp.setMargins(0, 18, 0, 26);
        root.addView(subtitle, sp);

        status = new TextView(this);
        status.setTextColor(mint);
        status.setTextSize(15);
        status.setGravity(Gravity.CENTER);
        root.addView(status, new LinearLayout.LayoutParams(-1, -2));

        Button permission = button("מתן הרשאת שינוי בהירות");
        permission.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= 23 && !Settings.System.canWrite(this)) {
                Intent i = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getPackageName()));
                startActivity(i);
            } else status.setText("הרשאת שינוי בהירות כבר פעילה");
        });
        root.addView(permission, buttonParams());

        Button start = button("הפעלת בהירות חכמה");
        start.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= 23 && !Settings.System.canWrite(this)) {
                status.setText("תחילה יש לאשר הרשאת שינוי בהירות");
                return;
            }
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST);
                return;
            }
            startCameraService();
        });
        root.addView(start, buttonParams());

        Button stop = button("כיבוי");
        stop.setOnClickListener(v -> {
            stopService(new Intent(this, BrightnessService.class));
            status.setText("הבהירות החכמה כבויה");
        });
        root.addView(stop, buttonParams());
        setContentView(root);
        status.setText("אשר הרשאת בהירות והרשאת מצלמה, ואז הפעל");
    }

    private void startCameraService() {
        Intent i = new Intent(this, BrightnessService.class);
        if (Build.VERSION.SDK_INT >= 26) startForegroundService(i); else startService(i);
        status.setText("פעילה — מצלמת הסלפי מעריכה את התאורה");
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) startCameraService();
            else status.setText("נדרשת הרשאת מצלמה כדי להעריך את התאורה");
        }
    }

    private Button button(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextColor(Color.rgb(14, 24, 44));
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(mint));
        return b;
    }

    private LinearLayout.LayoutParams buttonParams() {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.setMargins(0, 9, 0, 9);
        return p;
    }
}
