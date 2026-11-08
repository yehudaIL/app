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
        pinHash=getPreferences(0).getString("pin_hash",null);
        Shizuku.addRequestPermissionResultListener(permissionListener);
        renderGate();
    }

    @Override protected void onDestroy(){
        Shizuku.removeRequestPermissionResultListener(permissionListener);
        super.onDestroy();
    }

    private GradientDrawable bg(int color,int radius){ GradientDrawable d=new GradientDrawable(); d.setColor(color); d.setCornerRadius(radius); return d; }
    private TextView text(String s,int size,int color,boolean bold){ TextView t=new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color); t.setGravity(Gravity.CENTER_VERTICAL); t.setTypeface(Typeface.create("sans-serif",bold?Typeface.BOLD:Typeface.NORMAL)); return t; }
    private LinearLayout column(){ LinearLayout x=new LinearLayout(this); x.setOrientation(LinearLayout.VERTICAL); x.setPadding(22,20,22,20); return x; }
    private LinearLayout row(){ LinearLayout x=new LinearLayout(this); x.setGravity(Gravity.CENTER_VERTICAL); x.setOrientation(LinearLayout.HORIZONTAL); return x; }
    private void base(){ root=column(); root.setBackgroundColor(navy); setContentView(root); }
    private TextView button(String label){ TextView b=text(label,14,navy,true); b.setGravity(Gravity.CENTER); b.setPadding(15,12,15,12); b.setBackground(bg(gold,18)); return b; }
    private View space(int h){ View v=new View(this); v.setLayoutParams(new LinearLayout.LayoutParams(1,h)); return v; }

    private void renderGate(){
        base(); root.setGravity(Gravity.CENTER);
        TextView emblem=text("◈",58,gold,true); emblem.setGravity(Gravity.CENTER); root.addView(emblem,new LinearLayout.LayoutParams(-1,90));
        TextView title=text("STEALTH VAULT",25,Color.WHITE,true); title.setGravity(Gravity.CENTER); root.addView(title);
        TextView sub=text(pinHash==null?"הגדרת כספת פרטית":"הכספת שלך נעולה",15,muted,false); sub.setGravity(Gravity.CENTER); root.addView(sub);
        root.addView(space(26));
        pinInput=new EditText(this); pinInput.setSingleLine(true); pinInput.setGravity(Gravity.CENTER); pinInput.setTextColor(Color.WHITE); pinInput.setHintTextColor(muted); pinInput.setHint(pinHash==null?"בחר קוד בן 4 ספרות ומעלה":"הזן קוד כניסה"); pinInput.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_VARIATION_PASSWORD); pinInput.setBackground(bg(panel,18)); pinInput.setPadding(18,16,18,16); root.addView(pinInput,new LinearLayout.LayoutParams(-1,58));
        root.addView(space(14)); TextView go=button(pinHash==null?"יצירת כספת":"כניסה מאובטחת"); root.addView(go,new LinearLayout.LayoutParams(-1,54));
        go.setOnClickListener(v->{
            String p=pinInput.getText().toString();
            if(p.length()<4){pinInput.setError("הקוד חייב להכיל לפחות 4 ספרות");return;}
            if(pinHash==null){pinHash=hash(p);getPreferences(0).edit().putString("pin_hash",pinHash).apply();unlocked=true;renderHome();}
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
        Set<String> saved=getPreferences(0).getStringSet(HIDDEN,new HashSet<>());
        return new HashSet<>(saved);
    }
    private void saveHidden(Set<String> s){getPreferences(0).edit().putStringSet(HIDDEN,new HashSet<>(s)).apply();}
    private String key(String pkg,String cls){return pkg+"|"+cls;}
    private String[] splitKey(String value){int p=value.indexOf('|');return p<1?new String[]{value,""}:new String[]{value.substring(0,p),value.substring(p+1)};}

    private boolean hasShizuku(){
        try{return Shizuku.pingBinder();}
        catch(Throwable e){return false;}
    }
    private boolean hasShizukuPermission(){
        try{return hasShizuku() && Shizuku.checkSelfPermission()==PackageManager.PERMISSION_GRANTED;}
        catch(Throwable e){return false;}
    }
    private void requestShizuku(){
        if(!hasShizuku()){
            new AlertDialog.Builder(this).setTitle("צריך להפעיל Shizuku")
                .setMessage("התקן את Shizuku, הפעל אותו דרך ניפוי באגים אלחוטי, ואז חזור לכאן. ההפעלה נדרשת שוב לאחר הפעלה מחדש של המכשיר.")
                .setPositiveButton("פתיחת דף Shizuku",(d,w)->startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://shizuku.rikka.app/download/"))))
                .setNegativeButton("ביטול",null).show();
            return;
        }
        try{
            if(Shizuku.isPreV11()){
                Toast.makeText(this,"נדרשת גרסת Shizuku עדכנית",Toast.LENGTH_LONG).show();return;
            }
            if(hasShizukuPermission()){
                Toast.makeText(this,"Shizuku כבר מחובר ומאושר",Toast.LENGTH_SHORT).show();return;
            }
            Shizuku.requestPermission(SHIZUKU_REQUEST);
        }catch(Throwable e){
            new AlertDialog.Builder(this).setMessage("לא ניתן לבקש הרשאת Shizuku: "+e.getMessage()).setPositiveButton("סגירה",null).show();
        }
    }

    private void renderHome(){
        base();
        TextView brand=text("◈  STEALTH VAULT",21,gold,true);root.addView(brand);
        TextView heading=text("המרחב הפרטי שלך",25,Color.WHITE,true);root.addView(heading);
        TextView desc=text("הלאונצ'ר המקורי נשאר כמו שהוא. הכספת מסתירה רק את קיצורי האפליקציות שבחרת.",13,muted,false);root.addView(desc);
        root.addView(space(15));
        LinearLayout actions=row();
        TextView visible=button("אפליקציות מותקנות");actions.addView(visible,new LinearLayout.LayoutParams(0,48,1));
        TextView hiddenBtn=button("מוסתרות");LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(0,48,1);hp.setMargins(9,0,0,0);actions.addView(hiddenBtn,hp);root.addView(actions);
        visible.setOnClickListener(v->{showHidden=false;renderHome();});
        hiddenBtn.setOnClickListener(v->{showHidden=true;renderHome();});
        root.addView(space(12));
        TextView status=text("Shizuku: "+(hasShizukuPermission()?"מחובר ומאושר":(hasShizuku()?"ממתין לאישור":"לא פעיל")),13,hasShizukuPermission()?gold:Color.YELLOW,true);root.addView(status);
        status.setOnClickListener(v->requestShizuku());
        TextView setup=button(hasShizukuPermission()?"בדיקת חיבור Shizuku":"התחברות / אישור Shizuku");
        LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,46);sp.setMargins(0,8,0,4);root.addView(setup,sp);setup.setOnClickListener(v->requestShizuku());
        ScrollView scroll=new ScrollView(this);list=column();list.setPadding(0,10,0,10);scroll.addView(list);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        loadApps();
        TextView settings=text("הגדרות אבטחה  ·  שינוי קוד  ·  נעילה",13,muted,true);settings.setGravity(Gravity.CENTER);settings.setPadding(8,18,8,12);root.addView(settings);settings.setOnClickListener(v->securityDialog());
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
            List<ResolveInfo> results=pm.queryIntentActivities(intent,PackageManager.MATCH_DISABLED_COMPONENTS);
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
        LinearLayout card=row();card.setPadding(13,9,13,9);card.setBackground(bg(panel,17));
        LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,70);cp.setMargins(0,0,0,9);list.addView(card,cp);
        ImageView image=new ImageView(this);image.setImageDrawable(icon);card.addView(image,new LinearLayout.LayoutParams(42,42));
        LinearLayout labels=new LinearLayout(this);labels.setOrientation(LinearLayout.VERTICAL);labels.setPadding(12,0,8,0);
        labels.addView(text(name,15,Color.WHITE,true));labels.addView(text(subtitle,10,muted,false));
        card.addView(labels,new LinearLayout.LayoutParams(0,-2,1));
        TextView act=button(actionLabel);card.addView(act,new LinearLayout.LayoutParams(-2,42));act.setOnClickListener(v->action.run());
    }

    private void hidePackage(String pkg,List<String> classes){
        if(!hasShizukuPermission()){requestShizuku();return;}
        new AlertDialog.Builder(this).setTitle("להסתיר את "+pkg+"?")
            .setMessage("קיצורי הדרך של האפליקציה יוסתרו ממסך הבית וממגירת האפליקציות. האפליקציה עצמה לא תוסר, והנתונים שלה לא יימחקו.")
            .setPositiveButton("הסתר",(d,w)->runComponentCommand(pkg,classes,false))
            .setNegativeButton("ביטול",null).show();
    }

    private void restorePackage(String pkg,List<String> classes){
        if(!hasShizukuPermission()){requestShizuku();return;}
        runComponentCommand(pkg,classes,true);
    }

    private String quote(String s){return "'"+s.replace("'","'\\''")+"'";}

    private void runComponentCommand(String pkg,List<String> classes,boolean enable){
        new Thread(()->{
            StringBuilder command=new StringBuilder();
            for(String cls:classes){
                String component=quote(pkg+"/"+cls);
                command.append(enable?"pm enable ":"pm disable-user --user 0 ").append(component).append("\n");
            }
            command.append("echo STEALTH_VAULT_DONE\n");
            String output="";
            try{
                Method method=Shizuku.class.getDeclaredMethod("newProcess",String[].class,String[].class,String.class);
                method.setAccessible(true);
                Process process=(Process)method.invoke(null,new String[]{"sh"},null,null);
                OutputStream os=process.getOutputStream();os.write(command.toString().getBytes("UTF-8"));os.flush();os.close();
                ByteArrayOutputStream buffer=new ByteArrayOutputStream();InputStream in=process.getInputStream();byte[] data=new byte[1024];int n;
                while((n=in.read(data))!=-1)buffer.write(data,0,n);
                int exit=process.waitFor();output=buffer.toString("UTF-8")+"\nexit="+exit;
            }catch(Throwable e){output="ERROR: "+e.getClass().getSimpleName()+": "+e.getMessage();}
            final String result=output;
            runOnUiThread(()->{
                boolean ok=result.contains("STEALTH_VAULT_DONE")&&!result.startsWith("ERROR");
                if(ok){
                    Set<String> saved=hidden();
                    for(String cls:classes){String k=key(pkg,cls);if(enable)saved.remove(k);else saved.add(k);}
                    saveHidden(saved);
                    Toast.makeText(this,enable?"האפליקציה הוחזרה":"האפליקציה הוסתרה",Toast.LENGTH_LONG).show();
                    renderHome();
                }else{
                    new AlertDialog.Builder(this).setTitle("הפעולה לא הושלמה").setMessage("Shizuku לא הצליח לשנות את קיצורי האפליקציה.\n"+result).setPositiveButton("סגירה",null).show();
                }
            });
        }).start();
    }

    private void securityDialog(){
        String[] options={"שינוי קוד הכניסה","נעילה"};
        new AlertDialog.Builder(this).setTitle("אבטחה והגדרות").setItems(options,(d,w)->{
            if(w==0){
                EditText e=new EditText(this);e.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_VARIATION_PASSWORD);e.setHint("קוד חדש (4 ספרות ומעלה)");
                new AlertDialog.Builder(this).setTitle("שינוי קוד").setView(e).setPositiveButton("שמירה",(x,y)->{
                    String p=e.getText().toString();if(p.length()>=4){pinHash=hash(p);getPreferences(0).edit().putString("pin_hash",pinHash).apply();Toast.makeText(this,"הקוד עודכן",Toast.LENGTH_SHORT).show();}
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
