package il.yehuda.matzali;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {
    private static final int LOCATION_REQ = 41;
    private TextView status, coords, accuracy;
    private Button mapButton;
    private Location lastLocation;
    private int bg = Color.rgb(12, 19, 38), panel = Color.rgb(25, 35, 61), mint = Color.rgb(80, 225, 190), white = Color.WHITE;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(bg); getWindow().setNavigationBarColor(bg);
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(22), dp(22), dp(22), dp(18)); root.setBackgroundColor(bg);
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true);
        LinearLayout content = new LinearLayout(this); content.setOrientation(LinearLayout.VERTICAL);
        TextView logo = text("⌖  מצא לי", 31, mint, true); logo.setGravity(Gravity.RIGHT); content.addView(logo);
        TextView subtitle = text("איתור מיקום פשוט וברור", 16, Color.LTGRAY, false); subtitle.setGravity(Gravity.RIGHT); add(content, subtitle, 0, 14);
        LinearLayout card = new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL); card.setPadding(dp(20),dp(22),dp(20),dp(22)); card.setBackgroundColor(panel);
        TextView icon = text("◎", 48, mint, true); icon.setGravity(Gravity.CENTER); card.addView(icon);
        TextView heading = text("המיקום של המכשיר הזה", 21, white, true); heading.setGravity(Gravity.CENTER); add(card,heading,0,12);
        status = text("מוכן לאיתור", 16, mint, true); status.setGravity(Gravity.CENTER); add(card,status,0,16);
        coords = text("לחץ על הכפתור כדי לקבל מיקום", 15, white, false); coords.setGravity(Gravity.CENTER); add(card,coords,0,8);
        accuracy = text("נדרשת הרשאת מיקום במכשיר", 13, Color.LTGRAY, false); accuracy.setGravity(Gravity.CENTER); add(card,accuracy,0,18);
        Button locate = button("אתר את המכשיר הזה"); card.addView(locate); locate.setOnClickListener(v -> locate());
        mapButton = button("פתח מיקום במפות"); add(card,mapButton,12,0); mapButton.setEnabled(false); mapButton.setAlpha(.5f); mapButton.setOnClickListener(v -> openMap());
        add(content,card,0,22);
        TextView noteTitle = text("חשוב לדעת",18,white,true); noteTitle.setGravity(Gravity.RIGHT); add(content,noteTitle,0,10);
        TextView note = text("האפליקציה מציגה את המיקום של הטלפון שעליו היא מותקנת בלבד. כדי לאתר טלפון של בן משפחה, צריך להפעיל בו שיתוף מיקום בהסכמה או להשתמש בשירות איתור שהוגדר מראש. האפליקציה לא יכולה לזהות טלפונים זרים בסביבה או לאתר מכשיר כבוי.",14,Color.rgb(203,211,229),false); note.setGravity(Gravity.RIGHT); note.setLineSpacing(dp(4),1f); add(content,note,0,8);
        TextView privacy = text("🔒  המיקום נקרא במכשיר ואינו נשלח לשרת",12,mint,false); privacy.setGravity(Gravity.CENTER); add(content,privacy,0,20);
        scroll.addView(content); root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1)); setContentView(root);
    }
    private void locate() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQ); return;
        }
        try {
            LocationManager lm = (LocationManager)getSystemService(LOCATION_SERVICE);
            Location best = null;
            for (String provider : lm.getProviders(true)) { try { Location l = lm.getLastKnownLocation(provider); if (l != null && (best == null || l.getTime() > best.getTime())) best = l; } catch (SecurityException ignored) {} }
            if (best != null) showLocation(best);
            String provider = lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ? LocationManager.GPS_PROVIDER : LocationManager.NETWORK_PROVIDER;
            status.setText("מחפש מיקום עדכני…");
            lm.requestSingleUpdate(provider, new LocationListener() { @Override public void onLocationChanged(Location l) { runOnUiThread(() -> showLocation(l)); } @Override public void onProviderEnabled(String p) {} @Override public void onProviderDisabled(String p) {} @Override public void onStatusChanged(String p,int s,Bundle b) {} }, getMainLooper());
        } catch (Exception e) { status.setText("לא ניתן לקבל מיקום"); accuracy.setText("בדוק שהמיקום מופעל בהגדרות המכשיר"); }
    }
    private void showLocation(Location l) { lastLocation=l; status.setText("המיקום התקבל ✓"); coords.setText(String.format(java.util.Locale.US,"%.6f, %.6f",l.getLatitude(),l.getLongitude())); accuracy.setText("דיוק משוער: " + (int)l.getAccuracy() + " מטר · עדכון: " + new java.text.SimpleDateFormat("HH:mm:ss",java.util.Locale.getDefault()).format(new java.util.Date(l.getTime()))); mapButton.setEnabled(true); mapButton.setAlpha(1f); }
    private void openMap() { if(lastLocation!=null) { Uri uri=Uri.parse("geo:"+lastLocation.getLatitude()+","+lastLocation.getLongitude()+"?q="+lastLocation.getLatitude()+","+lastLocation.getLongitude()+"(המיקום שלי)"); startActivity(new Intent(Intent.ACTION_VIEW,uri)); } }
    @Override public void onRequestPermissionsResult(int r,String[] p,int[] g) { super.onRequestPermissionsResult(r,p,g); if(r==LOCATION_REQ) { boolean ok=false; for(int x:g) if(x==PackageManager.PERMISSION_GRANTED) ok=true; if(ok) locate(); else { status.setText("נדרשת הרשאת מיקום"); accuracy.setText("אפשר הרשאת מיקום כדי להמשיך"); } } }
    private TextView text(String s,int size,int color,boolean bold) { TextView t=new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color); if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD); return t; }
    private void add(LinearLayout p,View v,int top,int bottom) { LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.topMargin=dp(top); lp.bottomMargin=dp(bottom); p.addView(v,lp); }
    private Button button(String label) { Button b=new Button(this); b.setText(label); b.setTextSize(15); b.setAllCaps(false); b.setTextColor(bg); b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(mint)); return b; }
    private int dp(int x) { return (int)(x*getResources().getDisplayMetrics().density+.5f); }
}
