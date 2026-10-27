package il.yehuda.matzali;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends Activity {
    private final int bg=Color.rgb(12,19,38), panel=Color.rgb(25,35,61), mint=Color.rgb(80,225,190), white=Color.WHITE;
    private final List<ApplicationInfo> apps=new ArrayList<>();
    private Spinner picker;
    private TextView status;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(bg); getWindow().setNavigationBarColor(bg);
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(22),dp(24),dp(22),dp(18)); root.setBackgroundColor(bg);
        ScrollView scroll=new ScrollView(this); LinearLayout content=new LinearLayout(this); content.setOrientation(LinearLayout.VERTICAL);
        TextView title=text("⌁  הכפתור שלי",30,mint,true); title.setGravity(Gravity.RIGHT); content.addView(title);
        TextView sub=text("בחר מה ייפתח בלחיצה על כפתור המצלמה",16,Color.LTGRAY,false); sub.setGravity(Gravity.RIGHT); add(content,sub,8,22);
        LinearLayout card=new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL); card.setPadding(dp(18),dp(20),dp(18),dp(20)); card.setBackgroundColor(panel);
        TextView label=text("האפליקציה להפעלה",18,white,true); label.setGravity(Gravity.RIGHT); card.addView(label);
        picker=new Spinner(this); loadApps(); ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,appLabels()); adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); picker.setAdapter(adapter); add(card,picker,12,16);
        Button save=button("שמור בחירה"); add(card,save,0,10); save.setOnClickListener(v -> saveChoice());
        Button test=button("בדוק פתיחה"); add(card,test,0,0); test.setOnClickListener(v -> launchSelected());
        add(content,card,0,20);
        TextView section=text("הפעלה והגדרות",19,white,true); section.setGravity(Gravity.RIGHT); add(content,section,0,10);
        TextView explain=text("כדי לנסות לזהות לחיצת מצלמה, צריך להפעיל את שירות הנגישות של האפליקציה. אנדרואיד או יצרן הטלפון עדיין עשויים למנוע יירוט של הכפתור הייעודי.",14,Color.rgb(203,211,229),false); explain.setGravity(Gravity.RIGHT); explain.setLineSpacing(dp(4),1f); add(content,explain,0,12);
        Button accessibility=button("פתח הגדרות נגישות"); add(content,accessibility,0,10); accessibility.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        status=text("",14,mint,true); status.setGravity(Gravity.CENTER); add(content,status,10,8);
        TextView note=text("הערה: האפליקציה אינה משנה הגדרות מערכת מוסתרות ואינה מבטיחה פעולה בכל דגם. אם הכפתור מופעל ישירות על ידי הקושחה, ייתכן שיהיה צורך בהגדרת קיצור דרך של יצרן הטלפון.",13,Color.LTGRAY,false); note.setGravity(Gravity.RIGHT); note.setLineSpacing(dp(3),1f); add(content,note,18,10);
        scroll.addView(content); root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1)); setContentView(root); updateStatus();
    }
    private void loadApps() {
        PackageManager pm=getPackageManager();
        for(ApplicationInfo ai:pm.getInstalledApplications(PackageManager.GET_META_DATA)) {
            if(pm.getLaunchIntentForPackage(ai.packageName)!=null && !ai.packageName.equals(getPackageName())) apps.add(ai);
        }
        Collections.sort(apps,Comparator.comparing(a->pm.getApplicationLabel(a).toString().toLowerCase()));
    }
    private List<String> appLabels() {
        List<String> labels=new ArrayList<>(); PackageManager pm=getPackageManager(); labels.add("בחר אפליקציה…");
        for(ApplicationInfo ai:apps) labels.add(pm.getApplicationLabel(ai)+" ("+ai.packageName+")"); return labels;
    }
    private void saveChoice() {
        int i=picker.getSelectedItemPosition();
        if(i<=0 || i>apps.size()) { status.setText("בחר אפליקציה תחילה"); return; }
        getSharedPreferences("button_remap",MODE_PRIVATE).edit().putString("target_package",apps.get(i-1).packageName).apply();
        status.setText("הבחירה נשמרה ✓");
    }
    private void launchSelected() {
        String pkg=getSharedPreferences("button_remap",MODE_PRIVATE).getString("target_package",null);
        if(pkg==null) { saveChoice(); pkg=getSharedPreferences("button_remap",MODE_PRIVATE).getString("target_package",null); }
        if(pkg==null) return;
        Intent i=getPackageManager().getLaunchIntentForPackage(pkg);
        if(i!=null) { i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(i); } else status.setText("האפליקציה שנבחרה אינה זמינה");
    }
    private void updateStatus() {
        String pkg=getSharedPreferences("button_remap",MODE_PRIVATE).getString("target_package",null);
        status.setText(pkg==null?"עדיין לא נבחרה אפליקציה":"אפליקציה שנבחרה: "+pkg);
    }
    @Override protected void onResume(){super.onResume(); if(status!=null) updateStatus();}
    private TextView text(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
    private void add(LinearLayout p,View v,int top,int bottom){LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.topMargin=dp(top);lp.bottomMargin=dp(bottom);p.addView(v,lp);}
    private Button button(String s){Button b=new Button(this);b.setText(s);b.setTextSize(15);b.setAllCaps(false);b.setTextColor(bg);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(mint));return b;}
    private int dp(int x){return (int)(x*getResources().getDisplayMetrics().density+.5f);}
}
