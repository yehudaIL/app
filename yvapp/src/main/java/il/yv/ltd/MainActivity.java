package il.yv.ltd;

import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    private final int bg=Color.rgb(8,13,24), panel=Color.rgb(17,27,43), cyan=Color.rgb(54,211,235), muted=Color.rgb(151,169,190);
    private LinearLayout root, content;
    private SensorManager sensors;
    private SharedPreferences prefs;
    private String page="home";
    private TextView status;
    @Override public void onCreate(Bundle b){super.onCreate(b); getWindow().setStatusBarColor(bg);getWindow().setNavigationBarColor(bg);getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);sensors=(SensorManager)getSystemService(SENSOR_SERVICE);prefs=getSharedPreferences("yv",0);show("home");}
    private GradientDrawable shape(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(radius);d.setStroke(1,Color.rgb(35,58,78));return d;}
    private TextView text(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setGravity(Gravity.RIGHT);t.setTypeface(Typeface.create("sans-serif",bold?Typeface.BOLD:Typeface.NORMAL));return t;}
    private void addText(LinearLayout box,String s,int size,int color,boolean bold,int bottom){TextView t=text(s,size,color,bold);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=bottom;box.addView(t,p);}
    private LinearLayout card(){LinearLayout c=new LinearLayout(this);c.setOrientation(1);c.setPadding(18,16,18,16);c.setBackground(shape(panel,24));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=12;content.addView(c,p);return c;}
    private void button(LinearLayout box,String label,Runnable action){Button b=new Button(this);b.setText(label);b.setTextColor(Color.WHITE);b.setAllCaps(false);b.setTextSize(14);b.setBackground(shape(Color.rgb(23,48,68),18));b.setOnClickListener(v->action.run());LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.topMargin=8;box.addView(b,p);}
    private void show(String target){page=target;root=new LinearLayout(this);root.setOrientation(1);root.setPadding(18,16,18,10);root.setBackgroundColor(bg);setContentView(root);
        LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);top.setOrientation(1);addText(top,"Y.V  L.T.D.",23,cyan,true,2);addText(top,"מרכז גילוי חכם  •  גרסה ראשונה",12,muted,false,16);root.addView(top);
        ScrollView scroll=new ScrollView(this);content=new LinearLayout(this);content.setOrientation(1);scroll.addView(content);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        if(target.equals("home"))home(); else if(target.equals("sensors"))sensorPage();else if(target.equals("history"))history();else if(target.equals("radar"))radar();else settings();
        LinearLayout nav=new LinearLayout(this);nav.setGravity(Gravity.CENTER);String[][] items={{"ראשי","home"},{"חיישנים","sensors"},{"מכ״ם","radar"},{"היסטוריה","history"},{"הגדרות","settings"}};for(String[] it:items){TextView n=text(it[0],11,it[1].equals(page)?cyan:muted,true);n.setGravity(Gravity.CENTER);n.setPadding(4,14,4,12);n.setOnClickListener(v->show(it[1]));nav.addView(n,new LinearLayout.LayoutParams(0,-2,1));}root.addView(nav);
    }
    private void home(){LinearLayout hero=card();addText(hero,"שלום, ברוך הבא",20,Color.WHITE,true,5);addText(hero,"הטלפון שלך. התמונה המלאה.",14,muted,false,14);addText(hero,"NEXUS INTELLIGENCE",11,cyan,true,3);addText(hero,"מרכז הבקרה האישי שלך מוכן לסריקה.",13,Color.WHITE,false,8);button(hero,"התחל בדיקת חיישנים  ←",()->show("sensors"));
        LinearLayout a=card();addText(a,"מה אפשר לגלות?",17,Color.WHITE,true,10);feature(a,"◉  חיישני המכשיר","בדיקת החיישנים שקיימים בפועל בטלפון");feature(a,"⌁  רשתות בסביבה","מידע Wi-Fi ו-Bluetooth בהתאם להרשאות Android");feature(a,"◷  ציר הזמן","שמירת בדיקות והשוואה בין מדידות");feature(a,"✦  תובנות","הסברים פשוטים על השינויים שנמדדו");
        LinearLayout b=card();addText(b,"התחלה פשוטה",16,Color.WHITE,true,6);addText(b,"לא צריך להיות מומחה. בחר אזור, לחץ על בדיקה וקבל הסבר ברור. כל הנתונים נשארים במכשיר.",13,muted,false,4);
    }
    private void feature(LinearLayout c,String title,String desc){addText(c,title,14,cyan,true,2);addText(c,desc,12,muted,false,10);}
    private void sensorPage(){LinearLayout intro=card();addText(intro,"בדיקת חיישנים",20,Color.WHITE,true,5);addText(intro,"נציג רק חיישנים שקיימים במכשיר שלך. לחץ על רענון כדי לבדוק שוב.",13,muted,false,5);button(intro,"רענן רשימת חיישנים",()->show("sensors"));
        List<Sensor> list=sensors.getSensorList(Sensor.TYPE_ALL);for(Sensor s:list){LinearLayout c=card();addText(c,s.getName(),15,Color.WHITE,true,4);addText(c,s.getVendor()+"  •  "+s.getStringType(),11,muted,false,3);addText(c,"צריכת חשמל משוערת: "+s.getPower()+" mA",12,cyan,false,0);}
        if(list.isEmpty()){LinearLayout c=card();addText(c,"לא נמצאו חיישנים זמינים.",14,muted,false,0);}
        button(content,"שמור בדיקה בהיסטוריה",()->{String now=new SimpleDateFormat("dd/MM/yyyy HH:mm",Locale.getDefault()).format(new Date());prefs.edit().putString("last",now).putInt("count",prefs.getInt("count",0)+1).apply();Toast.makeText(this,"הבדיקה נשמרה במכשיר",Toast.LENGTH_SHORT).show();});
    }
    private void radar(){LinearLayout c=card();addText(c,"מכ״ם סביבתי",20,Color.WHITE,true,6);addText(c,"סריקות רשת דורשות הרשאות מערכת, הפעלת שירותי מיקום ותמיכה של היצרן. בגרסה זו מוצג הסבר לפני הפעלת סריקות.",13,muted,false,10);button(c,"מידע על סריקת Wi-Fi",()->new android.app.AlertDialog.Builder(this).setTitle("סריקת Wi-Fi").setMessage("Android עשוי לדרוש הרשאת מיקום ושירותי מיקום פעילים. תוצאות הסריקה מוגבלות על ידי המכשיר ומדיניות המערכת.").setPositiveButton("הבנתי",null).show());button(c,"מידע על Bluetooth",()->new android.app.AlertDialog.Builder(this).setTitle("סריקת Bluetooth").setMessage("ניתן לגלות רק התקני Bluetooth שמשדרים וניתנים לגילוי. לא כל טלפון או התקן יופיעו בסריקה.").setPositiveButton("הבנתי",null).show());}
    private void history(){LinearLayout c=card();addText(c,"היסטוריית בדיקות",20,Color.WHITE,true,8);addText(c,"מספר בדיקות שנשמרו: "+prefs.getInt("count",0),14,cyan,true,6);String last=prefs.getString("last","עדיין לא בוצעה בדיקה");addText(c,"בדיקה אחרונה: "+last,13,muted,false,12);button(c,"מחק היסטוריה",()->new android.app.AlertDialog.Builder(this).setTitle("מחיקת היסטוריה").setMessage("למחוק את כל נתוני ההיסטוריה המקומיים?").setNegativeButton("ביטול",null).setPositiveButton("מחק",(d,w)->{prefs.edit().clear().apply();show("history");}).show());}
    private void settings(){LinearLayout c=card();addText(c,"הגדרות ופרטיות",20,Color.WHITE,true,8);feature(c,"פרטיות תחילה","בגרסה זו המידע נשמר מקומית במכשיר.");feature(c,"ללא Root","אין צורך בהרשאות Root.");feature(c,"עובד גם ללא אינטרנט","מסכי המידע, החיישנים וההיסטוריה זמינים אופליין.");feature(c,"שקיפות","האפליקציה לא מזהה בעלי מכשירים ולא מבטיחה לאתר כל משדר.");}
}
