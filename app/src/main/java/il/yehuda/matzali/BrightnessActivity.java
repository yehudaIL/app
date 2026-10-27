package il.yehuda.matzali;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.hardware.Sensor;
import android.hardware.SensorManager;

public class BrightnessActivity extends Activity {
    private TextView status;
    private LinearLayout root;
    private final int bg = Color.rgb(12, 19, 38);
    private final int mint = Color.rgb(80, 225, 190);

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(bg);
        getWindow().setNavigationBarColor(bg);
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(24), dp(30), dp(24), dp(24));
        root.setBackgroundColor(bg);

        TextView title = new TextView(this);
        title.setText("בהירות דינמית");
        title.setTextSize(28);
        title.setTextColor(mint);
        title.setGravity(Gravity.RIGHT);
        root.addView(title);

        TextView desc = new TextView(this);
        desc.setText("הבהירות משתנה לפי חיישן האור של הטלפון. זה חסכוני יותר משימוש רציף במצלמה. השירות פועל ברקע ומציג התראה בזמן הפעילות.");
        desc.setTextSize(16);
        desc.setTextColor(Color.WHITE);
        desc.setGravity(Gravity.RIGHT);
        desc.setPadding(0, dp(18), 0, dp(18));
        root.addView(desc);

        status = new TextView(this);
        status.setTextSize(15);
        status.setTextColor(0xffb8c5dc);
        status.setGravity(Gravity.RIGHT);
        root.addView(status);

        Button permission = button("אישור שינוי בהירות מערכת");
        root.addView(permission);
        permission.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= 23 && !Settings.System.canWrite(this)) {
                startActivity(new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                        Uri.parse("package:" + getPackageName())));
            } else {
                status.setText("הרשאת שינוי הבהירות מאושרת.");
            }
        });

        Button start = button("הפעל בהירות דינמית");
        root.addView(start);
        start.setOnClickListener(v -> startAdaptive());

        Button stop = button("עצור בהירות דינמית");
        root.addView(stop);
        stop.setOnClickListener(v -> {
            stopService(new Intent(this, AdaptiveBrightnessService.class));
            status.setText("הבהירות הדינמית נעצרה.");
        });

        setContentView(root);
        updateStatus();
    }

    private void startAdaptive() {
        if (Build.VERSION.SDK_INT >= 23 && !Settings.System.canWrite(this)) {
            status.setText("תחילה אשר שינוי הגדרות במסך ההרשאות, ואז לחץ שוב על הפעלה.");
            startActivity(new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                    Uri.parse("package:" + getPackageName())));
            return;
        }
        SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sm == null || sm.getDefaultSensor(Sensor.TYPE_LIGHT) == null) {
            status.setText("לא נמצא חיישן אור במכשיר. המימוש הנוכחי דורש חיישן אור; מצלמת הסלפי אינה מופעלת ברקע.");
            return;
        }
        Intent service = new Intent(this, AdaptiveBrightnessService.class);
        if (Build.VERSION.SDK_INT >= 26) startForegroundService(service);
        else startService(service);
        status.setText("הבהירות הדינמית הופעלה. אפשר לצאת מהמסך; ההתראה תישאר בזמן הפעילות.");
    }

    private void updateStatus() {
        SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        boolean hasLight = sm != null && sm.getDefaultSensor(Sensor.TYPE_LIGHT) != null;
        status.setText(hasLight ? "חיישן אור זמין במכשיר." :
                "לא נמצא חיישן אור במכשיר. המימוש הנוכחי דורש חיישן אור.");
    }

    private Button button(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextSize(15);
        b.setAllCaps(false);
        b.setTextColor(bg);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(mint));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.topMargin = dp(14);
        b.setLayoutParams(lp);
        return b;
    }

    private int dp(int x) {
        return (int) (x * getResources().getDisplayMetrics().density + 0.5f);
    }
}
