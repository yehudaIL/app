package il.yv.ltd;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.wifi.*;
import android.bluetooth.*;
import android.bluetooth.le.*;
import android.location.LocationManager;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
  private final int bg=0xFF08111F, panel=0xFF132238, cyan=0xFF43D9E8, muted=0xFF9CB0C8, white=Color.WHITE;
  private LinearLayout root, content;
  private WifiManager wifi; private BluetoothAdapter bt; private BluetoothLeScanner ble;
  private final Handler handler=new Handler(Looper.getMainLooper());
  private final Map<String,String> wifiRows=new LinkedHashMap<>(), bleRows=new LinkedHashMap<>();
  private TextView wifiStatus, bleStatus; private boolean receiverRegistered=false, scanningBle=false;
  private final int REQ=77;
  private final BroadcastReceiver wifiReceiver=new BroadcastReceiver(){public void onReceive(Context c,Intent i){
    boolean updated=i.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED,false); List<ScanResult> rs=wifi.getScanResults();wifiRows.clear();
    for(ScanResult r:rs){String n=(r.SSID==null||r.SSID.isEmpty())?"רשת ללא שם":r.SSID;wifiRows.put(r.BSSID,n+"\n"+r.BSSID+" • "+r.frequency+" MHz • "+r.level+" dBm • "+signal(r.level));}
    renderWifi();if(wifiStatus!=null)wifiStatus.setText((updated?"הסריקה עודכנה":"תוצאות אחרונות")+" • "+rs.size()+" רשתות");
  }};
  private final ScanCallback scanCallback=new ScanCallback(){
    @Override public void onScanResult(int t,ScanResult r){addBle(r);}
    @Override public void onBatchScanResults(List<ScanResult> rs){for(ScanResult r:rs)addBle(r);}
    @Override public void onScanFailed(int c){scanningBle=false;if(bleStatus!=null)bleStatus.setText("הסריקה נכשלה (קוד "+c+")");}
  };
  private void addBle(ScanResult r){BluetoothDevice d=r.getDevice();String n=d.getName();if(n==null||n.trim().isEmpty())n="התקן Bluetooth ללא שם";String a=d.getAddress();bleRows.put(a,n+"\n"+a+" • "+r.getRssi()+" dBm • "+signal(r.getRssi()));renderBle();}
  private String signal(int r){if(r>=-55)return "אות חזק";if(r>=-70)return "אות בינוני";if(r>=-85)return "אות חלש";return "אות חלש מאוד";}
  @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(bg);getWindow().setNavigationBarColor(bg);getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);wifi=(WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);BluetoothManager bm=(BluetoothManager)getSystemService(BLUETOOTH_SERVICE);if(bm!=null)bt=bm.getAdapter();show("home");}
  private GradientDrawable shape(int c,int r){GradientDrawable d=new GradientDrawable();d.setColor(c);d.setCornerRadius(r);d.setStroke(1,0xFF29415B);return d;}
  private TextView txt(String s,int z,int c,boolean b){TextView t=new TextView(this);t.setText(s);t.setTextSize(z);t.setTextColor(c);t.setGravity(Gravity.RIGHT);t.setTypeface(Typeface.create("sans-serif",b?1:0));return t;}
  private void line(LinearLayout p,String s,int z,int c,boolean b,int m){TextView t=txt(s,z,c,b);LinearLayout.LayoutParams q=new LinearLayout.LayoutParams(-1,-2);q.bottomMargin=m;p.addView(t,q);}
  private LinearLayout card(){LinearLayout c=new LinearLayout(this);c.setOrientation(1);c.setPadding(17,15,17,15);c.setBackground(shape(panel,22));LinearLayout.LayoutParams q=new LinearLayout.LayoutParams(-1,-2);q.bottomMargin=12;content.addView(c,q);return c;}
  private void btn(LinearLayout p,String s,Runnable a){Button b=new Button(this);b.setText(s);b.setTextColor(white);b.setAllCaps(false);b.setTextSize(14);b.setBackground(shape(0xFF17415A,18));b.setOnClickListener(v->a.run());LinearLayout.LayoutParams q=new LinearLayout.LayoutParams(-1,-2);q.topMargin=8;p.addView(b,q);}
  private void show(String page){root=new LinearLayout(this);root.setOrientation(1);root.setPadding(16,14,16,8);root.setBackgroundColor(bg);setContentView(root);line(root,"Y.V  L.T.D.",24,cyan,true,1);line(root,"מאתר התקנים ורשתות בסביבה",12,muted,false,12);
    ScrollView sc=new ScrollView(this);content=new LinearLayout(this);content.setOrientation(1);sc.addView(content);root.addView(sc,new LinearLayout.LayoutParams(-1,0,1));
    if(page.equals("home"))home();else if(page.equals("wifi"))wifiPage();else if(page.equals("ble"))blePage();else if(page.equals("sensors"))sensorPage();else settings();
    LinearLayout nav=new LinearLayout(this);nav.setGravity(Gravity.CENTER);String[][] ns={{"ראשי","home"},{"Wi‑Fi","wifi"},{"Bluetooth","ble"},{"חיישנים","sensors"},{"פרטיות","settings"}};for(String[] n:ns){TextView t=txt(n[0],11,white,true);t.setGravity(Gravity.CENTER);t.setPadding(3,13,3,12);t.setOnClickListener(v->show(n[1]));nav.addView(t,new LinearLayout.LayoutParams(0,-2,1));}root.addView(nav);
  }
  private void home(){LinearLayout h=card();line(h,"גילוי סביבתי",21,white,true,5);line(h,"סרוק רשתות Wi‑Fi והתקני Bluetooth שמשדרים בקרבתך.",14,muted,false,12);btn(h,"התחל סריקת Wi‑Fi",()->show("wifi"));btn(h,"התחל סריקת Bluetooth",()->show("ble"));LinearLayout i=card();line(i,"מה תראה בתוצאות?",17,white,true,8);line(i,"• שם רשת או התקן, אם משודר\n• כתובת מזהה\n• עוצמת אות ב־dBm\n• דירוג אות משוער",13,muted,false,8);LinearLayout w=card();line(w,"חשוב לדעת",16,cyan,true,5);line(w,"לא כל טלפון משדר Bluetooth שניתן לגילוי. סריקת Wi‑Fi מציגה נקודות גישה, לא כל מכשיר שמחובר אליהן. אין זיהוי בעלים או מרחק מדויק.",12,muted,false,0);}
  private boolean hasLoc(){return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED;}
  private void ask(){if(!hasLoc())requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQ);}
  private boolean locOn(){LocationManager l=(LocationManager)getSystemService(LOCATION_SERVICE);try{return l.isProviderEnabled(LocationManager.GPS_PROVIDER)||l.isProviderEnabled(LocationManager.NETWORK_PROVIDER);}catch(Exception e){return false;}}
  private void wifiPage(){LinearLayout c=card();line(c,"סריקת רשתות Wi‑Fi",20,white,true,6);wifiStatus=txt("לחץ כדי לסרוק רשתות בסביבה",12,muted,false);c.addView(wifiStatus);btn(c,"סרוק עכשיו",()->startWifi());LinearLayout l=card();line(l,"רשתות שנמצאו",16,cyan,true,8);TextView h=txt("",13,white,false);h.setTag("wifi_holder");l.addView(h);renderWifi();}
  private void startWifi(){if(!hasLoc()){ask();return;}if(!locOn()){new android.app.AlertDialog.Builder(this).setTitle("נדרש שירות מיקום").setMessage("הפעל שירותי מיקום כדי לאפשר סריקת Wi‑Fi.").setPositiveButton("הגדרות",(d,w)->startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))).setNegativeButton("ביטול",null).show();return;}try{if(!receiverRegistered){registerReceiver(wifiReceiver,new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));receiverRegistered=true;}boolean ok=wifi.startScan();wifiStatus.setText(ok?"סורק רשתות…":"המערכת הגבילה סריקות; מציג תוצאות אחרונות");if(!ok){wifiRows.clear();for(ScanResult r:wifi.getScanResults())wifiRows.put(r.BSSID,(r.SSID.isEmpty()?"רשת ללא שם":r.SSID)+"\n"+r.BSSID+" • "+r.level+" dBm • "+signal(r.level));renderWifi();}}catch(Exception e){wifiStatus.setText("לא ניתן לסרוק: "+e.getMessage());}}
  private void renderWifi(){render("wifi_holder",wifiRows);}
  private void blePage(){LinearLayout c=card();line(c,"סריקת Bluetooth LE",20,white,true,6);bleStatus=txt("הסריקה תימשך 12 שניות",12,muted,false);c.addView(bleStatus);btn(c,"התחל סריקה",()->startBle());btn(c,"עצור סריקה",()->stopBle());LinearLayout l=card();line(l,"התקנים שהתגלו",16,cyan,true,8);TextView h=txt("",13,white,false);h.setTag("ble_holder");l.addView(h);renderBle();}
  private void startBle(){if(!hasLoc()){ask();return;}if(bt==null||!bt.isEnabled()){new android.app.AlertDialog.Builder(this).setTitle("Bluetooth כבוי").setMessage("הפעל Bluetooth כדי לסרוק התקנים.").setPositiveButton("הגדרות",(d,w)->startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS))).setNegativeButton("ביטול",null).show();return;}try{ble=bt.getBluetoothLeScanner();if(ble==null){bleStatus.setText("סריקת Bluetooth LE אינה זמינה");return;}bleRows.clear();ble.startScan(scanCallback);scanningBle=true;bleStatus.setText("סורק…");handler.postDelayed(()->{stopBle();if(bleStatus!=null)bleStatus.setText("הסריקה הסתיימה • "+bleRows.size()+" התקנים");},12000);}catch(Exception e){bleStatus.setText("שגיאה בהפעלת הסריקה: "+e.getMessage());}}
  private void stopBle(){try{if(ble!=null&&scanningBle)ble.stopScan(scanCallback);}catch(Exception ignored){}scanningBle=false;}
  private void renderBle(){render("ble_holder",bleRows);}
  private void render(String tag,Map<String,String> rows){if(content==null)return;for(int i=0;i<content.getChildCount();i++){View v=content.getChildAt(i);if(v instanceof LinearLayout){LinearLayout l=(LinearLayout)v;for(int j=0;j<l.getChildCount();j++){View x=l.getChildAt(j);if(x instanceof TextView&&tag.equals(x.getTag())){StringBuilder s=new StringBuilder();for(String r:rows.values())s.append("• ").append(r).append("\n\n");((TextView)x).setText(s.length()==0?"אין עדיין תוצאות. אשר הרשאה ולחץ סרוק.":s.toString());}}}}}
  private void sensorPage(){LinearLayout c=card();line(c,"חיישני המכשיר",19,white,true,8);android.hardware.SensorManager sm=(android.hardware.SensorManager)getSystemService(SENSOR_SERVICE);for(android.hardware.Sensor s:sm.getSensorList(android.hardware.Sensor.TYPE_ALL))line(c,s.getName()+"\n"+s.getVendor()+" • "+s.getStringType(),12,muted,false,10);}
  private void settings(){LinearLayout c=card();line(c,"פרטיות ומגבלות",19,white,true,8);line(c,"• סריקות רק בלחיצה שלך.\n• אין מעקב ברקע.\n• אין זיהוי בעלי מכשירים.\n• עוצמת אות אינה מרחק מדויק.\n• התוצאות תלויות בחומרה ובהגבלות Android.",13,muted,false,0);}
  @Override public void onRequestPermissionsResult(int r,String[] p,int[] g){super.onRequestPermissionsResult(r,p,g);if(r==REQ&&g.length>0&&g[0]==PackageManager.PERMISSION_GRANTED)Toast.makeText(this,"ההרשאה אושרה. לחץ שוב על סריקה.",Toast.LENGTH_LONG).show();}
  @Override protected void onDestroy(){stopBle();if(receiverRegistered)try{unregisterReceiver(wifiReceiver);}catch(Exception ignored){}super.onDestroy();}
}