package il.yehuda.vault;

import android.app.*;
import android.os.Bundle;
import android.content.*;
import android.content.pm.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import java.security.MessageDigest;
import java.util.*;

public class MainActivity extends Activity {
    private final String PREF="vault_secure";
    private final String HIDDEN="hidden_packages";
    private LinearLayout root, list;
    private EditText pinInput;
    private boolean unlocked=false, showHidden=false;
    private String pinHash;
    private int navy=Color.rgb(9,15,34), panel=Color.rgb(20,30,57), gold=Color.rgb(225,184,105), muted=Color.rgb(157,172,198);

    @Override public void onCreate(Bundle b){ super.onCreate(b); getWindow().setStatusBarColor(navy); getWindow().setNavigationBarColor(navy); getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL); pinHash=getPreferences(0).getString("pin_hash",null); renderGate(); }

    private GradientDrawable bg(int color,int radius){ GradientDrawable d=new GradientDrawable(); d.setColor(color); d.setCornerRadius(radius); return d; }
    private TextView text(String s,int size,int color,boolean bold){ TextView t=new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color); t.setGravity(Gravity.CENTER_VERTICAL); t.setTypeface(Typeface.create("sans-serif",bold?Typeface.BOLD:Typeface.NORMAL)); return t; }
    private LinearLayout column(){ LinearLayout x=new LinearLayout(this); x.setOrientation(LinearLayout.VERTICAL); x.setPadding(22,20,22,20); return x; }
    private LinearLayout row(){ LinearLayout x=new LinearLayout(this); x.setGravity(Gravity.CENTER_VERTICAL); x.setOrientation(LinearLayout.HORIZONTAL); return x; }
    private void base(){ root=column(); root.setBackgroundColor(navy); setContentView(root); }
    private TextView button(String label){ TextView b=text(label,15,navy,true); b.setGravity(Gravity.CENTER); b.setPadding(18,14,18,14); b.setBackground(bg(gold,18)); return b; }

    private void renderGate(){
        base(); root.setGravity(Gravity.CENTER);
        TextView emblem=text("◈",58,gold,true); emblem.setGravity(Gravity.CENTER); root.addView(emblem,new LinearLayout.LayoutParams(-1,90));
        TextView title=text("STEALTH VAULT",25,Color.WHITE,true); title.setGravity(Gravity.CENTER); root.addView(title);
        TextView sub=text(pinHash==null?"הגדרת כספת פרטית":"הכספת שלך נעולה",15,muted,false); sub.setGravity(Gravity.CENTER); root.addView(sub);
        root.addView(space(26));
        pinInput=new EditText(this); pinInput.setSingleLine(true); pinInput.setGravity(Gravity.CENTER); pinInput.setTextColor(Color.WHITE); pinInput.setHintTextColor(muted); pinInput.setHint(pinHash==null?"בחר קוד בן 4 ספרות ומעלה":"הזן קוד כניסה"); pinInput.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_VARIATION_PASSWORD); pinInput.setBackground(bg(panel,18)); pinInput.setPadding(18,16,18,16); root.addView(pinInput,new LinearLayout.LayoutParams(-1,58));
        root.addView(space(14)); TextView go=button(pinHash==null?"יצירת כספת":"כניסה מאובטחת"); root.addView(go,new LinearLayout.LayoutParams(-1,54));
        go.setOnClickListener(v->{ String p=pinInput.getText().toString(); if(p.length()<4){ pinInput.setError("הקוד חייב להכיל לפחות 4 ספרות"); return; } if(pinHash==null){ pinHash=hash(p); getPreferences(0).edit().putString("pin_hash",pinHash).apply(); unlocked=true; renderHome(); } else if(hash(p).equals(pinHash)){ unlocked=true; renderHome(); } else { pinInput.setError("קוד שגוי"); } });
        TextView note=text("הנתונים נשמרים במכשיר בלבד",12,muted,false); note.setGravity(Gravity.CENTER); root.addView(space(18)); root.addView(note);
    }
    private View space(int h){ View v=new View(this); v.setLayoutParams(new LinearLayout.LayoutParams(1,h)); return v; }
    private String hash(String s){ try { byte[] d=MessageDigest.getInstance("SHA-256").digest(("StealthVault:"+s).getBytes("UTF-8")); StringBuilder b=new StringBuilder(); for(byte x:d)b.append(String.format("%02x",x)); return b.toString(); } catch(Exception e){ return s; } }
    private Set<String> hidden(){ return new HashSet<>(getPreferences(0).getStringSet(HIDDEN,new HashSet<>())); }
    private void saveHidden(Set<String> s){ getPreferences(0).edit().putStringSet(HIDDEN,new HashSet<>(s)).apply(); }

    private void renderHome(){
        base();
        TextView brand=text("◈  STEALTH VAULT",21,gold,true); root.addView(brand);
        TextView heading=text("המרחב הפרטי שלך",25,Color.WHITE,true); root.addView(heading);
        TextView desc=text("בחר אפליקציות שיוסתרו ממסך הבית של הכספת",13,muted,false); root.addView(desc);
        root.addView(space(18));
        LinearLayout actions=row(); TextView all=button(showHidden?"הצג אפליקציות רגילות":"האפליקציות שלי"); actions.addView(all,new LinearLayout.LayoutParams(0,48,1)); TextView hiddenBtn=button(showHidden?"הסתרה":"מוסתרות"); LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(0,48,1); hp.setMargins(10,0,0,0); actions.addView(hiddenBtn,hp); root.addView(actions);
        all.setOnClickListener(v->{showHidden=false;renderHome();}); hiddenBtn.setOnClickListener(v->{showHidden=!showHidden;renderHome();});
        root.addView(space(12));
        TextView status=text("מצב: "+(isDefaultHome()?"הכספת מוגדרת כמסך הבית":"יש להגדיר את הכספת כמסך הבית כדי להסתיר אייקונים"),12,isDefaultHome()?gold:Color.YELLOW,true); root.addView(status);
        ScrollView scroll=new ScrollView(this); list=column(); list.setPadding(0,10,0,10); scroll.addView(list); root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        loadApps();
        TextView settings=text("הגדרות אבטחה  ·  שינוי קוד  ·  נעילה",13,muted,true); settings.setGravity(Gravity.CENTER); settings.setPadding(8,18,8,12); root.addView(settings);
        settings.setOnClickListener(v->securityDialog());
    }
    private boolean isDefaultHome(){ try { Intent i=new Intent(Intent.ACTION_MAIN); i.addCategory(Intent.CATEGORY_HOME); ResolveInfo r=getPackageManager().resolveActivity(i,PackageManager.MATCH_DEFAULT_ONLY); return r!=null && r.activityInfo!=null && getPackageName().equals(r.activityInfo.packageName); } catch(Exception e){return false;} }
    private void loadApps(){
        PackageManager pm=getPackageManager(); Intent intent=new Intent(Intent.ACTION_MAIN,null); intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> apps=pm.queryIntentActivities(intent,0); Collections.sort(apps,(a,b)->String.CASE_INSENSITIVE_ORDER.compare(a.loadLabel(pm).toString(),b.loadLabel(pm).toString()));
        Set<String> hidden=hidden(); int count=0;
        for(ResolveInfo ri:apps){ String pkg=ri.activityInfo.packageName; if(pkg.equals(getPackageName())) continue; boolean isHidden=hidden.contains(pkg); if(showHidden!=isHidden) continue; count++;
            LinearLayout card=row(); card.setPadding(14,10,14,10); card.setBackground(bg(panel,17)); LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,68); cp.setMargins(0,0,0,9); list.addView(card,cp);
            ImageView icon=new ImageView(this); try{icon.setImageDrawable(ri.loadIcon(pm));}catch(Exception ignored){} card.addView(icon,new LinearLayout.LayoutParams(42,42));
            LinearLayout labels=new LinearLayout(this); labels.setOrientation(LinearLayout.VERTICAL); labels.setPadding(12,0,8,0); TextView name=text(ri.loadLabel(pm).toString(),15,Color.WHITE,true); labels.addView(name); TextView pkgText=text(isHidden?"מוסתרת בכספת":"מותקנת במכשיר",10,muted,false); labels.addView(pkgText); card.addView(labels,new LinearLayout.LayoutParams(0,-2,1));
            TextView action=button(isHidden?"החזר":"הסתר"); card.addView(action,new LinearLayout.LayoutParams(-2,42));
            action.setOnClickListener(v->{ Set<String> now=hidden(); if(isHidden)now.remove(pkg);else now.add(pkg);saveHidden(now);renderHome(); });
            card.setOnClickListener(v->{ if(!isHidden){ try{ Intent launch=pm.getLaunchIntentForPackage(pkg); if(launch!=null)startActivity(launch); }catch(Exception ignored){} } });
        }
        if(count==0){ TextView empty=text(showHidden?"עדיין לא הסתרת אפליקציות":"לא נמצאו אפליקציות להצגה",15,muted,false); empty.setGravity(Gravity.CENTER); list.addView(empty,new LinearLayout.LayoutParams(-1,100)); }
    }
    private void securityDialog(){
        String[] options={"שינוי קוד הכניסה","הגדרת Stealth Vault כמסך הבית","נעילה"};
        new AlertDialog.Builder(this).setTitle("אבטחה והגדרות").setItems(options,(d,w)->{
            if(w==0){ EditText e=new EditText(this); e.setInputType(2|16); e.setHint("קוד חדש (4 ספרות ומעלה)"); new AlertDialog.Builder(this).setTitle("שינוי קוד").setView(e).setPositiveButton("שמירה",(x,y)->{String p=e.getText().toString();if(p.length()>=4){pinHash=hash(p);getPreferences(0).edit().putString("pin_hash",pinHash).apply();Toast.makeText(this,"הקוד עודכן",0).show();}else Toast.makeText(this,"הקוד קצר מדי",0).show();}).setNegativeButton("ביטול",null).show(); }
            else if(w==1){ try{startActivity(new Intent("android.settings.HOME_SETTINGS"));}catch(Exception e){startActivity(new Intent("android.settings.SETTINGS"));} }
            else {unlocked=false;renderGate();}
        }).show();
    }
    @Override public void onBackPressed(){ if(unlocked){ new AlertDialog.Builder(this).setMessage("לנעול את הכספת?").setPositiveButton("נעילה",(d,w)->{unlocked=false;renderGate();}).setNegativeButton("להישאר",null).show(); } else super.onBackPressed(); }
}
