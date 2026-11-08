package il.yehuda.vault;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.util.*;

public class LauncherActivity extends Activity {
    private final int bg=Color.rgb(9,15,34), panel=Color.rgb(20,30,57), gold=Color.rgb(225,184,105), muted=Color.rgb(157,172,198);
    private LinearLayout root;
    @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(bg);getWindow().setNavigationBarColor(bg);render();}
    private TextView label(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setGravity(Gravity.CENTER);t.setTypeface(Typeface.create("sans-serif",bold?Typeface.BOLD:Typeface.NORMAL));return t;}
    private android.graphics.drawable.GradientDrawable shape(int color,int radius){android.graphics.drawable.GradientDrawable d=new android.graphics.drawable.GradientDrawable();d.setColor(color);d.setCornerRadius(radius);return d;}
    private void render(){
        root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(18,20,18,12);root.setBackgroundColor(bg);setContentView(root);
        LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);top.setOrientation(LinearLayout.HORIZONTAL);
        TextView title=label("STEALTH VAULT",20,gold,true);title.setGravity(Gravity.CENTER_VERTICAL|Gravity.LEFT);top.addView(title,new LinearLayout.LayoutParams(0,52,1));
        TextView vault=label("🔒  כספת",14,bg,true);vault.setPadding(16,12,16,12);vault.setBackground(shape(gold,16));top.addView(vault);vault.setOnClickListener(v->startActivity(new Intent(this,MainActivity.class)));root.addView(top);
        TextView hint=label("האפליקציות שלך · המוסתרות נשארות פעילות",13,muted,false);hint.setGravity(Gravity.CENTER_VERTICAL|Gravity.LEFT);hint.setPadding(2,4,2,16);root.addView(hint);
        ScrollView scroll=new ScrollView(this);LinearLayout grid=new LinearLayout(this);grid.setOrientation(LinearLayout.VERTICAL);scroll.addView(grid);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        Set<String> hidden=new HashSet<>(getSharedPreferences("vault_secure",MODE_PRIVATE).getStringSet("hidden_components",new HashSet<>()));
        Intent query=new Intent(Intent.ACTION_MAIN,null);query.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> apps=getPackageManager().queryIntentActivities(query,0);
        Collections.sort(apps,(x,y)->String.CASE_INSENSITIVE_ORDER.compare(x.loadLabel(getPackageManager()).toString(),y.loadLabel(getPackageManager()).toString()));
        LinearLayout row=null;int col=0,count=0;
        for(ResolveInfo ri:apps){
            if(ri.activityInfo==null)continue;
            String pkg=ri.activityInfo.packageName, cls=ri.activityInfo.name;
            if(pkg.equals(getPackageName())||hidden.contains(pkg+"|"+cls))continue;
            if(col==0){row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);row.setGravity(Gravity.TOP);grid.addView(row,new LinearLayout.LayoutParams(-1,-2));}
            LinearLayout item=new LinearLayout(this);item.setOrientation(LinearLayout.VERTICAL);item.setGravity(Gravity.CENTER);item.setPadding(4,12,4,12);
            ImageView icon=new ImageView(this);try{icon.setImageDrawable(ri.loadIcon(getPackageManager()));}catch(Exception ignored){}
            item.addView(icon,new LinearLayout.LayoutParams(54,54));
            TextView name=label(ri.loadLabel(getPackageManager()).toString(),11,Color.WHITE,false);name.setMaxLines(2);name.setEllipsize(android.text.TextUtils.TruncateAt.END);
            LinearLayout.LayoutParams np=new LinearLayout.LayoutParams(-1,38);np.topMargin=5;item.addView(name,np);
            row.addView(item,new LinearLayout.LayoutParams(0,112,1));
            final String packageName=pkg, className=cls;
            item.setOnClickListener(v->{try{Intent launch=new Intent(Intent.ACTION_MAIN);launch.addCategory(Intent.CATEGORY_LAUNCHER);launch.setClassName(packageName,className);launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);startActivity(launch);}catch(Exception e){Toast.makeText(this,"לא ניתן לפתוח את האפליקציה",Toast.LENGTH_SHORT).show();}});
            col=(col+1)%4;count++;
        }
        if(count==0){TextView empty=label("אין אפליקציות להצגה",15,muted,false);grid.addView(empty,new LinearLayout.LayoutParams(-1,100));}
        TextView manage=label("ניהול אפליקציות והסתרה",13,gold,true);manage.setPadding(8,14,8,10);root.addView(manage);manage.setOnClickListener(v->startActivity(new Intent(this,MainActivity.class)));
    }
}
