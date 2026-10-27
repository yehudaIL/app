package il.yehuda.matzali;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.camera2.*;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import java.nio.ByteBuffer;

public class BrightnessActivity extends Activity {
    private TextView status, level;
    private CameraDevice camera;
    private CameraCaptureSession session;
    private ImageReader reader;
    private HandlerThread thread;
    private Handler handler;
    private boolean running=false;
    private float smoothed=120f;
    private long lastUpdate=0;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        getWindow().setStatusBarColor(0xff10172a);
        LinearLayout root=new LinearLayout(this); root.setOrientation(1); root.setPadding(32,40,32,28); root.setBackgroundColor(0xff10172a);
        TextView title=new TextView(this); title.setText("בהירות דינמית"); title.setTextSize(28); title.setTextColor(0xff62e6c2); title.setGravity(Gravity.RIGHT); root.addView(title);
        TextView desc=new TextView(this); desc.setText("האפליקציה דוגמת את התמונה מהמצלמה הקדמית ומנסה להתאים את בהירות המסך לתאורה. המצלמה פעילה רק בזמן שהמסך הזה פתוח."); desc.setTextSize(16); desc.setTextColor(0xffeeeeee); desc.setGravity(Gravity.RIGHT); desc.setPadding(0,20,0,20); root.addView(desc);
        level=new TextView(this); level.setText("רמת תאורה: ממתין להפעלה"); level.setTextSize(18); level.setTextColor(-1); root.addView(level);
        status=new TextView(this); status.setText("כדי לשנות בהירות מערכתית, יש לאשר הרשאת שינוי הגדרות."); status.setTextSize(14); status.setTextColor(0xffb8c5dc); status.setPadding(0,18,0,18); root.addView(status);
        Button start=new Button(this); start.setText("הפעל בהירות דינמית"); root.addView(start);
        start.setOnClickListener(v -> startAdaptive());
        Button stop=new Button(this); stop.setText("עצור"); root.addView(stop);
        stop.setOnClickListener(v -> stopCamera());
        Button permission=new Button(this); permission.setText("אישור שינוי בהירות מערכת"); root.addView(permission);
        permission.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT>=23 && !Settings.System.canWrite(this))
                startActivity(new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:"+getPackageName())));
            else status.setText("הרשאת שינוי הגדרות כבר מאושרת.");
        });
        setContentView(root);
    }

    private void startAdaptive() {
        if (Build.VERSION.SDK_INT>=23 && !Settings.System.canWrite(this)) {
            status.setText("תחילה לחץ על «אישור שינוי בהירות מערכת» ואשר במסך ההגדרות.");
            return;
        }
        if (checkSelfPermission(Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA},41); return;
        }
        try {
            CameraManager cm=(CameraManager)getSystemService(CAMERA_SERVICE);
            String front=null;
            for(String id:cm.getCameraIdList()) {
                CameraCharacteristics c=cm.getCameraCharacteristics(id);
                Integer facing=c.get(CameraCharacteristics.LENS_FACING);
                if(facing!=null && facing==CameraCharacteristics.LENS_FACING_FRONT){front=id;break;}
            }
            if(front==null){status.setText("לא נמצאה מצלמה קדמית במכשיר.");return;}
            CameraCharacteristics chars=cm.getCameraCharacteristics(front);
            android.util.Size size=new android.util.Size(320,240);
            android.util.Size[] outputs=chars.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.YUV_420_888);
            if(outputs!=null && outputs.length>0) size=outputs[outputs.length-1];
            reader=ImageReader.newInstance(size.getWidth(),size.getHeight(),ImageFormat.YUV_420_888,2);
            reader.setOnImageAvailableListener(r -> {
                Image im=null;
                try {
                    im=r.acquireLatestImage(); if(im==null)return;
                    ByteBuffer y=im.getPlanes()[0].getBuffer(); long sum=0; int n=0;
                    int step=Math.max(1,y.remaining()/2500);
                    for(int i=0;i<y.limit();i+=step){sum+=(y.get(i)&255);n++;}
                    float avg=n==0?128f:(float)sum/n;
                    smoothed=smoothed*0.78f+avg*0.22f;
                    long now=System.currentTimeMillis();
                    if(now-lastUpdate>1200){lastUpdate=now; final float light=smoothed; runOnUiThread(()->applyBrightness(light));}
                } catch(Exception ignored){} finally {if(im!=null) im.close();}
            }, getHandler());
            thread=new HandlerThread("adaptive-camera"); thread.start(); handler=new Handler(thread.getLooper());
            reader.setOnImageAvailableListener(r -> analyze(r),handler);
            if(Build.VERSION.SDK_INT>=23) cm.openCamera(front,new CameraDevice.StateCallback(){
                public void onOpened(CameraDevice c){camera=c; createSession();}
                public void onDisconnected(CameraDevice c){c.close();camera=null;}
                public void onError(CameraDevice c,int e){c.close();camera=null;runOnUiThread(()->status.setText("שגיאה בפתיחת המצלמה: "+e));}
            },handler);
        } catch(Exception e) {status.setText("לא ניתן להפעיל את המצלמה: "+e.getMessage());}
    }
    private void analyze(ImageReader r) {
        Image im=null;
        try {
            im=r.acquireLatestImage(); if(im==null)return;
            ByteBuffer y=im.getPlanes()[0].getBuffer(); long sum=0; int n=0,step=Math.max(1,y.remaining()/1800);
            for(int i=0;i<y.limit();i+=step){sum+=(y.get(i)&255);n++;}
            float avg=n==0?128f:(float)sum/n;
            smoothed=smoothed*0.8f+avg*0.2f;
            long now=System.currentTimeMillis();
            if(now-lastUpdate>1200){lastUpdate=now;final float light=smoothed;runOnUiThread(()->applyBrightness(light));}
        } catch(Exception ignored){} finally {if(im!=null)im.close();}
    }
    private void createSession() {
        try {
            camera.createCaptureSession(java.util.Collections.singletonList(reader.getSurface()),new CameraCaptureSession.StateCallback(){
                public void onConfigured(CameraCaptureSession s){
                    session=s;
                    try {CaptureRequest.Builder b=camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);b.addTarget(reader.getSurface());b.set(CaptureRequest.CONTROL_MODE,CameraMetadata.CONTROL_MODE_AUTO);s.setRepeatingRequest(b.build(),null,handler);runOnUiThread(()->status.setText("פעיל — התאמת בהירות לפי המצלמה הקדמית."));}
                    catch(Exception e){runOnUiThread(()->status.setText("לא ניתן להתחיל מדידה: "+e.getMessage()));}
                }
                public void onConfigureFailed(CameraCaptureSession s){runOnUiThread(()->status.setText("המצלמה לא אפשרה להפעיל מדידה."));}
            },handler);
        } catch(Exception e){status.setText("שגיאה בהפעלת המדידה.");}
    }
    private void applyBrightness(float light) {
        if(level==null)return;
        level.setText("רמת תאורה: "+Math.round(light)+"/255");
        // A conservative curve avoids abrupt brightness changes.
        int value=Math.max(18,Math.min(255,(int)(18f+light*1.55f)));
        try {Settings.System.putInt(getContentResolver(),Settings.System.SCREEN_BRIGHTNESS,value);status.setText("בהירות מערכתית: "+value+"/255");}
        catch(Exception e){status.setText("אין הרשאה לשינוי בהירות המערכת.");}
    }
    private void stopCamera() {
        try {if(session!=null){session.stopRepeating();session.close();session=null;}}catch(Exception ignored){}
        try {if(camera!=null){camera.close();camera=null;}}catch(Exception ignored){}
        try {if(reader!=null){reader.close();reader=null;}}catch(Exception ignored){}
        if(thread!=null){thread.quitSafely();thread=null;}
        status.setText("המדידה נעצרה.");
    }
    @Override public void onRequestPermissionsResult(int request,String[] permissions,int[] results) {
        super.onRequestPermissionsResult(request,permissions,results);
        if(request==41 && results.length>0 && results[0]==PackageManager.PERMISSION_GRANTED) startAdaptive();
        else status.setText("נדרשת הרשאת מצלמה כדי למדוד תאורה.");
    }
    @Override protected void onDestroy(){stopCamera();super.onDestroy();}
    @Override protected void onPause(){super.onPause();stopCamera();}
}
