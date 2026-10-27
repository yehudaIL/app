package il.yehuda.matzali;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

public class CameraKeyService extends AccessibilityService {
    @Override public void onServiceConnected() {
        AccessibilityServiceInfo info=new AccessibilityServiceInfo();
        info.eventTypes=AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType=AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.flags=AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS;
        info.notificationTimeout=100;
        setServiceInfo(info);
    }
    @Override protected boolean onKeyEvent(KeyEvent event) {
        if(event.getAction()!=KeyEvent.ACTION_DOWN || (event.getRepeatCount()>0)) return false;
        int code=event.getKeyCode();
        if(code!=KeyEvent.KEYCODE_CAMERA && code!=KeyEvent.KEYCODE_FOCUS) return false;
        SharedPreferences prefs=getSharedPreferences("button_remap",MODE_PRIVATE);
        String pkg=prefs.getString("target_package",null);
        if(pkg==null || pkg.equals(getPackageName())) return false;
        Intent launch=getPackageManager().getLaunchIntentForPackage(pkg);
        if(launch==null) return false;
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        try { startActivity(launch); return true; } catch(Exception e) { return false; }
    }
    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() {}
}
