package il.yehuda.hotspot;

import android.app.Activity;
import android.content.ComponentName;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.service.quicksettings.TileService;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private int dp(float v) { return (int)(v * getResources().getDisplayMetrics().density + .5f); }

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(8, 15, 31));
        getWindow().setNavigationBarColor(Color.rgb(8, 15, 31));
        getWindow().getDecorView().setSystemUiVisibility(0);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(24), dp(42), dp(24), dp(24));
        root.setBackgroundColor(Color.rgb(8, 15, 31));

        TextView symbol = new TextView(this);
        symbol.setText("◉");
        symbol.setTextSize(64);
        symbol.setTextColor(Color.rgb(37, 214, 255));
        symbol.setGravity(Gravity.CENTER);
        root.addView(symbol, new LinearLayout.LayoutParams(-1, dp(100)));

        TextView title = new TextView(this);
        title.setText("נקודה חמה מהירה");
        title.setTextSize(27);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setTextColor(Color.WHITE);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView desc = new TextView(this);
        desc.setText("הוסף כפתור נקודה חמה לחלונית ההגדרות המהירות שלך. מעכשיו תוכל להגיע למסך הנקודה החמה בנגיעה אחת.");
        desc.setTextSize(16);
        desc.setTextColor(Color.rgb(190, 204, 224));
        desc.setGravity(Gravity.CENTER);
        desc.setLineSpacing(dp(5), 1f);
        LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(-1, -2);
        descParams.setMargins(0, dp(18), 0, dp(28));
        root.addView(desc, descParams);

        Button add = new Button(this);
        add.setText("הוסף לחלונית המהירה");
        add.setTextSize(16);
        add.setAllCaps(false);
        root.addView(add, new LinearLayout.LayoutParams(-1, dp(58)));
        add.setOnClickListener(v -> addTile());

        TextView note = new TextView(this);
        note.setText("לאחר האישור, פתח את חלונית ההתראות וגרור את האריח למקום הרצוי. בלחיצה עליו ייפתח מסך הנקודה החמה של המכשיר.");
        note.setTextSize(14);
        note.setTextColor(Color.rgb(145, 163, 190));
        note.setGravity(Gravity.CENTER);
        note.setLineSpacing(dp(4), 1f);
        LinearLayout.LayoutParams noteParams = new LinearLayout.LayoutParams(-1, -2);
        noteParams.setMargins(0, dp(25), 0, 0);
        root.addView(note, noteParams);

        setContentView(root);
    }

    private void addTile() {
        if (Build.VERSION.SDK_INT >= 33) {
            TileService.requestAddTileService(
                this,
                new ComponentName(this, HotspotTileService.class),
                getString(R.string.tile_label),
                android.graphics.drawable.Icon.createWithResource(this, R.drawable.ic_hotspot),
                getMainExecutor(),
                result -> {
                    String message;
                    switch (result) {
                        case TileService.TILE_ADD_REQUEST_RESULT_TILE_ADDED:
                            message = "הכפתור נוסף לחלונית!";
                            break;
                        case TileService.TILE_ADD_REQUEST_RESULT_TILE_ALREADY_ADDED:
                            message = "הכפתור כבר נמצא בחלונית";
                            break;
                        default:
                            message = "לא נוסף. אפשר להוסיף ידנית דרך עריכת החלונית";
                    }
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                }
            );
        } else {
            Toast.makeText(this, "פתח את עריכת חלונית ההגדרות וחפש: נקודה חמה מהירה", Toast.LENGTH_LONG).show();
        }
    }
}
