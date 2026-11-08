package il.yehuda.vault;

import android.app.*;
import android.os.Bundle;
import android.content.*;
import android.content.pm.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.util.*;
import rikka.shizuku.Shizuku;

public class MainActivity extends Activity {
    private static final String PREF="vault_secure";
    private static final String HIDDEN="hidden_components";
    private static final int SHIZUKU_REQUEST=7412;
    private LinearLayout root, list;
    private EditText pinInput;
    private boolean unlocked=false, showHidden=false;
    private String pinHash;
    private int navy=Color.rgb(9,15,34), panel=Color.rgb(20,30,57), gold=Color.rgb(225,184,105), muted=Color.rgb(157,172,198);

    private final Shizuku.OnRequestPermissionResultListener permissionListener=(requestCode, grantResult)->{
        if(requestCode==SHIZUKU_REQUEST){
            runOnUiThread(()->{
                if(grantResult==PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,"החיבור אושר. אפשר להסתיר אפליקציות",Toast.LENGTH_LONG).show();
                    renderHome();
                }else{
                    new AlertDialog.Builder(this).setMessage("לא התקבל אישור Shizuku. בלי האישור אי אפשר להסתיר אייקונים בלאונצ'ר המקורי.")
                        .setPositiveButton("הבנתי",null).show();
                }
            });
        }
    };

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        getWindow().setStatusBarColor(navy);
        getWindow().setNavigationBarColor(navy);
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        pinHash=getSharedPreferences(PREF,MODE_PRIVATE).getString("pin_hash",null);
        renderGate();
    }

    @Override protected void onDestroy(){
        super.onDestroy();
    }

    private GradientDrawable bg(int color,int radius){ GradientDrawable d=new GradientDrawable(); d.setColor(color); d.setCornerRadius(radius); return d; }
    private TextView text(String s,int size,int color,boolean bold){ TextView t=new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color); t.setGravity(Gravity.CENTER_VERTICAL); t.setTypeface(Typeface.create("sans-serif",bold?Typeface.BOLD:Typeface.NORMAL)); return t; }
    private LinearLayout column(){ LinearLayout x=new LinearLayout(this); x.setOrientation(LinearLayout.VERTICAL); x.setPadding(24,26,24,24); return x; }
    private LinearLayout row(){ LinearLayout x=new LinearLayout(this); x.setGravity(Gravity.CENTER_VERTICAL); x.setOrientation(LinearLayout.HORIZONTAL); return x; }
    private void base(){ root=column(); root.setBackgroundColor(navy); setContentView(root); }
    private TextView button(String label){ TextView b=text(label,16,navy,true); b.setGravity(Gravity.CENTER); b.setPadding(18,14,18,14); b.setMinHeight(52); b.setBackground(bg(gold,18)); return b; }
    private View space(int h){ View v=new View(this); v.setLayoutParams(new LinearLayout.LayoutParams(1,h)); return v; }

    private void renderGate(){
        base(); root.setGravity(Gravity.CENTER);
        TextView emblem=text("◈",58,gold,true); emblem.setGravity(Gravity.CENTER); root.addView(emblem,new LinearLayout.LayoutParams(-1,90));
        TextView title=text("STEALTH VAULT",25,Color.WHITE,true); title.setGravity(Gravity.CENTER); root.addView(title);
        TextView sub=text(pinHash==null?"הגדרת כספת פרטית":"הכספת שלך נעולה",15,muted,false); sub.setGravity(Gravity.CENTER); root.addView(sub);
        root.addView(space(26));
        pinInput=new EditText(this); pinInput.setSingleLine(true); pinInput.setGravity(Gravity.CENTER); pinInput.setTextColor(Color.WHITE); pinInput.setHintTextColor(muted); pinInput.setHint(pinHash==null?"בחר קוד בן 4 ספרות ומעלה":"הזן קוד כניסה"); pinInput.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_VARIATION_PASSWORD); pinInput.setBackground(bg(panel,18)); pinInput.setPadding(20,18,20,18); pinInput.setTextSize(18); root.addView(pinInput,new LinearLayout.LayoutParams(-1,64));
        root.addView(space(14)); TextView go=button(pinHash==null?"יצירת כספת":"כניסה מאובטחת"); root.addView(go,new LinearLayout.LayoutParams(-1,60));
        go.setOnClickListener(v->{
            String p=pinInput.getText().toString();
            if(p.length()<4){pinInput.setError("הקוד חייב להכיל לפחות 4 ספרות");return;}
            if(pinHash==null){pinHash=hash(p);getSharedPreferences(PREF,MODE_PRIVATE).edit().putString("pin_hash",pinHash).apply();unlocked=true;renderHome();}
            else if(hash(p).equals(pinHash)){unlocked=true;renderHome();}
            else pinInput.setError("קוד שגוי");
        });
        TextView note=text("הנתונים נשמרים במכשיר בלבד",12,muted,false); note.setGravity(Gravity.CENTER); root.addView(space(18)); root.addView(note);
    }

    private String hash(String s){
        try{byte[] d=MessageDigest.getInstance("SHA-256").digest(("StealthVault:"+s).getBytes("UTF-8"));StringBuilder b=new StringBuilder();for(byte x:d)b.append(String.format("%02x",x));return b.toString();}
        catch(Exception e){return s;}
    }

    private Set<String> hidden(){
        Set<String> saved=getSharedPreferences(PREF,MODE_PRIVATE).getStringSet(HIDDEN,new HashSet<>());
        return new HashSet<>(saved);
    }
    private void saveHidden(Set<String> s){getSharedPreferences(PREF,MODE_PRIVATE).edit().putStringSet(HIDDEN,new HashSet<>(s)).apply();}
    private String key(String pkg,String cls){return pkg+"|"+cls;}
    private String[] splitKey(String value){int p=value.indexOf('|');return p<1?new String[]{value,""}:new String[]{value.substring(0,p),value.substring(p+1)};}

    private void renderHome(){
        base();
        TextView brand=text("◈  STEALTH VAULT",21,gold,true);root.addView(brand);
        root.addView(space(12));
        TextView heading=text("המרחב הפרטי שלך",27,Color.WHITE,true);root.addView(heading);
        TextView desc=text("הלאונצ'ר המקורי נשאר כמו שהוא. הכספת מסתירה רק את קיצורי האפליקציות שבחרת.",15,muted,false);desc.setLineSpacing(4,1f);root.addView(desc);
        root.addView(space(22));
        LinearLayout actions=row();
        TextView visible=button("אפליקציות מותקנות");actions.addView(visible,new LinearLayout.LayoutParams(0,56,1));
        TextView hiddenBtn=button("מוסתרות");LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(0,56,1);hp.setMargins(9,0,0,0);actions.addView(hiddenBtn,hp);root.addView(actions);
        visible.setOnClickListener(v->{showHidden=false;renderHome();});
        hiddenBtn.setOnClickListener(v->{showHidden=true;renderHome();});
        root.addView(space(20));
        TextView status=text("מצב הסתרה: משגר פרטי",13,gold,true);root.addView(status);
        TextView setup=button("הגדרת Stealth Vault כמסך הבית");
        LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,54);sp.setMargins(0,12,0,10);root.addView(setup,sp);
        setup.setOnClickListener(v->{try{startActivity(new Intent(android.provider.Settings.ACTION_HOME_SETTINGS));}catch(Exception e){startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));}});
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(false);scroll.setClipToPadding(false);list=column();list.setPadding(0,14,0,14);scroll.addView(list);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        loadApps();
        TextView settings=text("הגדרות אבטחה  ·  שינוי קוד  ·  נעילה",13,muted,true);settings.setGravity(Gravity.CENTER);settings.setPadding(12,22,12,18);root.addView(settings);settings.setOnClickListener(v->securityDialog());
    }

    private void loadApps(){
        PackageManager pm=getPackageManager();
        Set<String> hidden=hidden();
        if(showHidden){
            Map<String,List<String>> grouped=new TreeMap<>();
            for(String item:hidden){String[] p=splitKey(item);if(p[1].length()>0){List<String> a=grouped.get(p[0]);if(a==null){a=new ArrayList<>();grouped.put(p[0],a);}a.add(p[1]);}}
            int count=0;
            for(Map.Entry<String,List<String>> entry:grouped.entrySet()){
                String pkg=entry.getKey();if(pkg.equals(getPackageName()))continue;
                try{
                    ApplicationInfo ai=pm.getApplicationInfo(pkg,0);
                    addAppRow(pm,ai.loadLabel(pm).toString(),pm.getApplicationIcon(ai),pkg,"מוסתרת ממגירת האפליקציות", "החזר",()->restorePackage(pkg,entry.getValue()));
                    count++;
                }catch(PackageManager.NameNotFoundException e){for(String cls:entry.getValue())hidden.remove(key(pkg,cls));saveHidden(hidden);}
            }
            if(count==0)empty("עדיין לא הסתרת אפליקציות");
        }else{
            Intent intent=new Intent(Intent.ACTION_MAIN,null);intent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> results=pm.queryIntentActivities(intent,0);
            Map<String,List<String>> grouped=new TreeMap<>();
            Map<String,ResolveInfo> representative=new HashMap<>();
            for(ResolveInfo ri:results){
                if(ri.activityInfo==null)continue;
                String pkg=ri.activityInfo.packageName;
                if(pkg.equals(getPackageName()))continue;
                String cls=ri.activityInfo.name;
                if(hidden.contains(key(pkg,cls)))continue;
                if(!grouped.containsKey(pkg)){grouped.put(pkg,new ArrayList<>());representative.put(pkg,ri);}
                grouped.get(pkg).add(cls);
            }
            List<String> packages=new ArrayList<>(grouped.keySet());
            Collections.sort(packages,(a,b)->String.CASE_INSENSITIVE_ORDER.compare(representative.get(a).loadLabel(pm).toString(),representative.get(b).loadLabel(pm).toString()));
            int count=0;
            for(String pkg:packages){
                ResolveInfo ri=representative.get(pkg);
                addAppRow(pm,ri.loadLabel(pm).toString(),ri.loadIcon(pm),pkg,"מוכנה להסתרה", "הסתר",()->hidePackage(pkg,grouped.get(pkg)));
                count++;
            }
            if(count==0)empty("לא נמצאו אפליקציות להצגה");
        }
    }

    private void empty(String message){TextView e=text(message,15,muted,false);e.setGravity(Gravity.CENTER);list.addView(e,new LinearLayout.LayoutParams(-1,100));}

    private void addAppRow(PackageManager pm,String name,android.graphics.drawable.Drawable icon,String pkg,String subtitle,String actionLabel,Runnable action){
        LinearLayout card=row();card.setPadding(16,12,16,12);card.setBackground(bg(panel,17));
        LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,84);cp.setMargins(0,0,0,12);list.addView(card,cp);
        ImageView image=new ImageView(this);image.setImageDrawable(icon);card.addView(image,new LinearLayout.LayoutParams(50,50));
        LinearLayout labels=new LinearLayout(this);labels.setOrientation(LinearLayout.VERTICAL);labels.setPadding(14,0,10,0);
        labels.addView(text(name,17,Color.WHITE,true));labels.addView(text(subtitle,12,muted,false));
        card.addView(labels,new LinearLayout.LayoutParams(0,-2,1));
        TextView act=button(actionLabel);card.addView(act,new LinearLayout.LayoutParams(-2,50));act.setOnClickListener(v->action.run());
    }

    private void hidePackage(String pkg,List<String> classes){
        new AlertDialog.Builder(this).setTitle("להסתיר את האפליקציה?")
            .setMessage("האפליקציה תישאר מותקנת ופעילה. הסמל שלה יוסתר רק במשגר Stealth Vault. כדי שההסתרה תעבוד, יש להגדיר את Stealth Vault כמסך הבית.")
            .setPositiveButton("הסתר",(d,w)->{
                Set<String> saved=hidden();
                for(String cls:classes)saved.add(key(pkg,cls));
                saveHidden(saved);
                Toast.makeText(this,"האפליקציה הוסתרה מהמשגר הפרטי",Toast.LENGTH_LONG).show();
                renderHome();
            }).setNegativeButton("ביטול",null).show();
    }

    private void restorePackage(String pkg,List<String> classes){
        new AlertDialog.Builder(this).setTitle("האפליקציה מוסתרת")
            .setItems(new String[]{"פתח את האפליקציה","החזר אותה למגירה"},(d,w)->{
                if(w==0){
                    try{Intent launch=getPackageManager().getLaunchIntentForPackage(pkg);if(launch!=null)startActivity(launch);}
                    catch(Exception e){Toast.makeText(this,"לא ניתן לפתוח את האפליקציה",Toast.LENGTH_SHORT).show();}
                }else{
                    Set<String> saved=hidden();
                    for(String cls:classes)saved.remove(key(pkg,cls));
                    saveHidden(saved);
                    renderHome();
                }
            }).setNegativeButton("ביטול",null).show();
    }

    private void securityDialog(){
        String[] options={"שינוי קוד הכניסה","נעילה"};
        new AlertDialog.Builder(this).setTitle("אבטחה והגדרות").setItems(options,(d,w)->{
            if(w==0){
                EditText e=new EditText(this);e.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_VARIATION_PASSWORD);e.setHint("קוד חדש (4 ספרות ומעלה)");
                new AlertDialog.Builder(this).setTitle("שינוי קוד").setView(e).setPositiveButton("שמירה",(x,y)->{
                    String p=e.getText().toString();if(p.length()>=4){pinHash=hash(p);getSharedPreferences(PREF,MODE_PRIVATE).edit().putString("pin_hash",pinHash).apply();Toast.makeText(this,"הקוד עודכן",Toast.LENGTH_SHORT).show();}
                    else Toast.makeText(this,"הקוד קצר מדי",Toast.LENGTH_SHORT).show();
                }).setNegativeButton("ביטול",null).show();
            }else{unlocked=false;renderGate();}
        }).show();
    }

    @Override public void onBackPressed(){
        if(unlocked){new AlertDialog.Builder(this).setMessage("לנעול את הכספת?").setPositiveButton("נעילה",(d,w)->{unlocked=false;renderGate();}).setNegativeButton("להישאר",null).show();}
        else super.onBackPressed();
    }
}
